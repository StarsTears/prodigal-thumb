package com.prodigal.thumb.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prodigal.thumb.mapper.BlogMapper;
import com.prodigal.thumb.model.entity.Thumb;
import com.prodigal.thumb.model.enums.ThumbTypeEnum;
import com.prodigal.thumb.service.impl.ThumbServiceImpl;
import com.prodigal.thumb.utils.RedisKeyUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-thumb
 * @date 2025/4/19 9:07
 * @description: 定时同步 临时点赞记录到数据库
 * 定时从 Redis 中获取临时点赞 和 取消点赞的数据，将其（批量）写入 数据库中；然后在使用《虚拟线程 异步》删除 Redis 中的数据。
 */
@Slf4j
@Component
public class SyncThumb2DBJob {
    @Resource
    private ThumbServiceImpl thumbService;
    @Resource
    private BlogMapper blogMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Scheduled(initialDelay = 10000, fixedRate = 10000)
    public void run(){
        log.info("-------开始同步点赞记录到数据库-------");
        DateTime startDate = DateUtil.date();
        String date = DateUtil.format(startDate, "HH:mm:") + (DateUtil.second(startDate) / 10) * 10;
        log.info("开始同步点赞记录到数据库->{}", date);
        sysncThumb2DBByDate(date);
        log.info("-------临时数据同步完成->耗时：{}ms-------", DateUtil.date().getTime()-startDate.getTime());
    }

    public void sysncThumb2DBByDate(String date) {
        //获取临时点赞记录和取消点赞数据
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(date);
        Map<Object, Object> allTempThumbMap = redisTemplate.opsForHash().entries(tempThumbKey);
        boolean isEmpty = CollUtil.isEmpty(allTempThumbMap);
        //同步点赞到数据库===>构建插入列表并收集 blogID
        if (isEmpty) {
            return;
        }
        Map<Long, Long> blogThumbCountMap = new HashMap<>();
        ArrayList<Thumb> thumbList = new ArrayList<>();
        LambdaQueryWrapper<Thumb> queryWrapper = new LambdaQueryWrapper<>();
        boolean needRemove = false;//是否需要从redis中删除
        for (Object userIdBlogIdObj : allTempThumbMap.keySet()) {
                String userIdBlogId = (String) userIdBlogIdObj;
            String[] userIdAndBlogId = userIdBlogId.split(StrPool.COLON);
            Long userId = Long.valueOf(userIdAndBlogId[0]);
            Long blogId = Long.valueOf(userIdAndBlogId[1]);
            //-1 取消点赞 1 点赞
            int thumbType = Integer.parseInt(allTempThumbMap.get(userIdBlogId).toString());
            if (thumbType == ThumbTypeEnum.INCR.getValue()){
                Thumb thumb = new Thumb();
                thumb.setUserId(userId);
                thumb.setBlogId(blogId);
                thumbList.add(thumb);
            } else if (thumbType == ThumbTypeEnum.DECR.getValue()){
                //拼接查询条件、批量删除
                needRemove  =true;
                queryWrapper.or().eq(Thumb::getUserId, userId).eq(Thumb::getBlogId, blogId);
            }else{
                if (thumbType == ThumbTypeEnum.NON.getValue()){
                    log.error("点赞状态异常：{}", thumbType+"->"+userId+","+blogId);
                }
                continue;
            }
            //计算点赞增量
            blogThumbCountMap.put(blogId, blogThumbCountMap.getOrDefault(blogId, 0L) + thumbType);
        }
        //批量保存点赞记录
        boolean isOk = thumbService.saveBatch(thumbList);
        //批量删除点赞记录
        if (needRemove){
            boolean remove = thumbService.remove(queryWrapper);
            if (!remove){
                log.error("批量删除点赞记录失败");
            }
        }
        //批量更新点赞数
        if (!blogThumbCountMap.isEmpty()) {
            blogMapper.batchUpdateThumbCount(blogThumbCountMap);
        }

        //异步删除
        Thread.startVirtualThread(() -> {
            redisTemplate.delete(RedisKeyUtil.getTempThumbKey(date));
        });
    }

}

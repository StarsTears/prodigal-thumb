package com.prodigal.thumb.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import com.prodigal.thumb.constants.ThumbConstant;
import com.prodigal.thumb.service.ThumbService;
import com.prodigal.thumb.utils.RedisKeyUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-thumb
 * @date 2025/4/20 12:43
 * @description: 对定时同步数据进行补偿（临时（取消）点赞数据）
 */
@Slf4j
@Component
public class SyncThumb2DBCompensatoryJob {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private SyncThumb2DBJob syncThumb2DBJob;

    @Scheduled(cron = "0 0 2 * * * ") //凌晨两点执行
    public void run() {
        log.info("开始补偿数据");
        DateTime startDate = DateUtil.date();
        Set<String> thumbKeys = redisTemplate.keys(RedisKeyUtil.getTempThumbKey("") + "*");
//        Set<String> thumbKeys = redisTemplate.keys(RedisKeyUtil.getTempUnThumbKey("") + "*");
        Set<String> needHandleDataSet = new HashSet<>();
        thumbKeys.stream().filter(ObjUtil::isNotNull).forEach(thumbKey -> {
            needHandleDataSet.add(thumbKey.replace(ThumbConstant.TEMP_THUMB_KEY_PREFIX.formatted(""), ""));
        });

        if (ObjUtil.isEmpty(needHandleDataSet)){
            log.info("暂无需要补偿的数据");
            return;
        }
        for (String date : needHandleDataSet) {
            syncThumb2DBJob.sysncThumb2DBByDate(date);
        }

        log.info("临时数据补偿完成->耗时：{}", DateUtil.date().getTime() - startDate.getTime());
    }
}

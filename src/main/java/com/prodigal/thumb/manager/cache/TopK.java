package com.prodigal.thumb.manager.cache;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-thumb
 * @date 2025/4/21 16:04
 * @description: TODO
 */
public interface TopK {

    AddResult add(String key, int increment);

    List <Item> list();
    BlockingQueue<Item> expelled();
    void fading();
    long total();
}

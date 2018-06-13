package com.ey.seckill.service;

import com.ey.seckill.entities.Goods;
import org.springframework.transaction.annotation.Transactional;

public interface IGoodsService {

    Goods getById(int id);

    @Transactional
    int add(Goods goods);

    @Transactional
    int update(Goods goods);
}

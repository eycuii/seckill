package com.ey.seckill.service.impl;

import com.ey.seckill.dao.IGoodsDao;
import com.ey.seckill.entities.Goods;
import com.ey.seckill.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoodsService implements IGoodsService {

    @Autowired
    private IGoodsDao targetDao;

    @Override
    public Goods getById(int id) {
        return targetDao.getById(id);
    }

    @Override
    public int add(Goods goods) {
        return targetDao.add(goods);
    }

    @Override
    public int update(Goods goods) {
        return targetDao.update(goods);
    }
}

package com.ey.seckill.service.impl;

import com.ey.seckill.dao.IOrderDao;
import com.ey.seckill.entities.Order;
import com.ey.seckill.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService implements IOrderService {

    @Autowired
    private IOrderDao targetDao;

    @Override
    public Order getById(int id) {
        return targetDao.getById(id);
    }
}

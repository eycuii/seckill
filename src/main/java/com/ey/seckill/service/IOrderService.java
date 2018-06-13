package com.ey.seckill.service;

import com.ey.seckill.entities.Order;

public interface IOrderService {
    
    Order getById(int id);

    int order(int goodsId, int count);
}

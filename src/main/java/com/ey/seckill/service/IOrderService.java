package com.ey.seckill.service;

import com.ey.seckill.entities.Order;
import org.springframework.transaction.annotation.Transactional;

public interface IOrderService {
    
    Order getById(int id);

    @Transactional
    int order(int goodsId, int count);
}

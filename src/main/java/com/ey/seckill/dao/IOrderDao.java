package com.ey.seckill.dao;

import com.ey.seckill.entities.Order;
import org.apache.ibatis.annotations.Param;

public interface IOrderDao {

    int add(Order order);

    int update(Order order);

    Order getById(@Param("id") int id);
}

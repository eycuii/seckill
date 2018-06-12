package com.ey.seckill.dao;

import com.ey.seckill.entities.Goods;
import org.apache.ibatis.annotations.Param;

public interface IGoodsDao {

    int add(Goods goods);

    int update(Goods goods);

    Goods getById(@Param("id") int id);
}

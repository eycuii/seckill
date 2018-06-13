package com.ey.seckill.service.impl;

import com.ey.seckill.dao.IOrderDao;
import com.ey.seckill.entities.Goods;
import com.ey.seckill.entities.Order;
import com.ey.seckill.lock.DistributedLock;
import com.ey.seckill.service.IGoodsService;
import com.ey.seckill.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class OrderService implements IOrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IOrderDao targetDao;
    @Autowired
    private IGoodsService goodsService;

    @Override
    public Order getById(int id) {
        return targetDao.getById(id);
    }

    @Override
    public int order(int goodsId, int count) {
        int result = 0;
        String lockKey = "lock-goods-" + goodsId;
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        DistributedLock lock = new DistributedLock(connection, 5000);
        if (lock.lock(lockKey, Integer.MAX_VALUE, 200)) {
            result = handleOrder(goodsId, count);
            lock.unlock(lockKey);
        }
        return result;
    }

    private int handleOrder(int goodsId, int count) {
        if (count <= 0) {
            throw new RuntimeException("交易数量必须大于零！");
        }
        Goods goods = goodsService.getById(goodsId);
        int newCount = goods.getCount() - count;
        if (newCount < 0) {
            throw new RuntimeException("商品" + goods.getName()
                    + "库存不足！当前库存：" + goods.getCount());
        }
        goods.setCount(newCount);
        goodsService.update(goods);

        Order order = new Order();
        order.setGoodsId(goodsId);
        order.setCount(count);
        order.setCreateTime(new Date());
        return targetDao.add(order);
    }
}
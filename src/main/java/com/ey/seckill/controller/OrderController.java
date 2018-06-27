package com.ey.seckill.controller;

import com.ey.seckill.lock.GoodsRedisLock;
import com.ey.seckill.lock.base.RedisLock;
import com.ey.seckill.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IOrderService orderService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public int order(@RequestParam int goodsId, @RequestParam int count) {
        int result = 0;
        GoodsRedisLock lock = new GoodsRedisLock(redisTemplate, goodsId);
        try {
            if (lock.lock()) {
                result = orderService.order(goodsId, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return result;
    }
}

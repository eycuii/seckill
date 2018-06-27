package com.ey.seckill.controller;

import com.ey.seckill.lock.RedisLock;
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
        String lockKey = "lock-goods-" + goodsId;
        RedisLock lock = new RedisLock(redisTemplate, lockKey, 5000, 4000);
        try {
            // TODO: 使用注解实现（如@TransactionalWithLock）
            if (lock.lock(Integer.MAX_VALUE, 200)) {
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

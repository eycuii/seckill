package com.ey.seckill.controller;

import com.ey.seckill.lock.DistributedLock;
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
        DistributedLock lock = new DistributedLock(redisTemplate, 5000);
        boolean acquire = false;
        Thread daemonTread = lock.createDaemonThread(lockKey, 4000);
        try {
            acquire = lock.lock(lockKey, Integer.MAX_VALUE, 200);
            if (acquire) {
                daemonTread.start();
                result = orderService.order(goodsId, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            daemonTread.interrupt();
            if(acquire) {
                lock.unlock(lockKey);
            }
        }
        return result;
    }
}

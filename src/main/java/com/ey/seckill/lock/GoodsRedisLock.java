package com.ey.seckill.lock;

import com.ey.seckill.lock.base.RedisLock;
import com.ey.seckill.lock.base.TimingWheel;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

public class GoodsRedisLock {

    private static final String KEY_PREFIX = "lock-goods-";
    private static final int EXPIRE = 5000;
    private static final int SERVICE_DURATION = 4000;
    private static final int MAX_TRY_COUNT = Integer.MAX_VALUE;
    private static final int PER_SLEEP_MILLI_SECONDS = 200;

    private static final TimingWheel<RedisLock.LockExtender> lockExtenderTimingWheel =
            new TimingWheel<>(1000, SERVICE_DURATION / 1000, TimeUnit.MILLISECONDS);
    static {
        lockExtenderTimingWheel.addExpirationListener((lockExtender) -> {
            if (!lockExtender.isReleased()) {
                lockExtender.extend();
                lockExtenderTimingWheel.add(lockExtender);
            }
        });
        lockExtenderTimingWheel.start();
    }

    private final RedisLock lock;

    public GoodsRedisLock(RedisTemplate redisTemplate, int goodsId) {
        String lockKey = KEY_PREFIX + goodsId;
        lock = new RedisLock(redisTemplate, lockKey, EXPIRE, lockExtenderTimingWheel);
    }

    public boolean lock() {
        return lock.lock(MAX_TRY_COUNT, PER_SLEEP_MILLI_SECONDS);
    }

    public boolean unlock() {
        return lock.unlock();
    }
}

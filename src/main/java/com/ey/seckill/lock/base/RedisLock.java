package com.ey.seckill.lock.base;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisLock {

    private static final byte[] NX = "NX".getBytes();
    private static final byte[] PX = "PX".getBytes();
    private static final byte[] DEL_SCRIPT = (
            "if redis.call('get', KEYS[1]) == KEYS[2] then " +
            "return redis.call('del', KEYS[1]) " +
            "else return 0 end").getBytes();

    private final RedisConnection connection;
    private final byte[] lockKey;
    private final byte[] expire;
    private final byte[] threadId;
    private final LockExtender lockExtender;
    private boolean released = false;
    private final TimingWheel<LockExtender> lockExtenderTimingWheel;

    public RedisLock(RedisTemplate redisTemplate, String lockKey, int expire,
                     TimingWheel<LockExtender> lockExtenderTimingWheel) {
        this.connection = redisTemplate.getConnectionFactory().getConnection();
        this.lockKey = lockKey.getBytes();
        this.expire = (expire + "").getBytes();
        this.threadId = (Thread.currentThread().getId() + "").getBytes();
        this.lockExtender = new LockExtender(this);
        this.lockExtenderTimingWheel = lockExtenderTimingWheel;
    }

    public boolean lock(int maxTryCount, int perSleepMilliSeconds) {
        int tryCount = 0;
        while (tryCount++ <= maxTryCount) {
            if(tryLock()) {
                lockExtenderTimingWheel.add(lockExtender);
                return true;
            }
            try {
                Thread.sleep(perSleepMilliSeconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean unlock() {
        released = true;
        Long result = connection.eval(DEL_SCRIPT, ReturnType.INTEGER, 2,
                lockKey, threadId);
        return result == 1;
    }

    public boolean isReleased() {
        return released;
    }

    public static class LockExtender {
        private RedisLock lock;

        LockExtender(RedisLock lock) {
            this.lock = lock;
        }

        public void extend() {
            lock.extendExpireTime();
        }

        public boolean isReleased() {
            return lock.isReleased();
        }
    }

    private boolean tryLock() {
        return "OK".equals(
                connection.execute("SET", lockKey, threadId,
                        PX, expire, NX));
    }

    /**
     * 延长锁的过期时间
     */
    private void extendExpireTime() {
        connection.execute("SET", lockKey, threadId,
                PX, expire);
    }
}

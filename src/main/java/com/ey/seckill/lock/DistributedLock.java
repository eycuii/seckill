package com.ey.seckill.lock;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;

public class DistributedLock {

    private static final byte[] NX = "NX".getBytes();
    private static final byte[] PX = "PX".getBytes();
    private static final byte[] DEL_SCRIPT = (
            "if redis.call('get', KEYS[1]) == KEYS[2] then " +
            "return redis.call('del', KEYS[1]) " +
            "else return 0 end").getBytes();

    private final RedisConnection connection;
    private final byte[] expireMilliSeconds;
    private final byte[] threadId;

    public DistributedLock(RedisConnection connection, int expireMilliSeconds) {
        this.connection = connection;
        this.expireMilliSeconds = (expireMilliSeconds + "").getBytes();
        this.threadId = (Thread.currentThread().getId() + "").getBytes();
    }

    public boolean lock(String lockKey, int maxTryCount, int perWaitingMilliSeconds) {
        int tryCount = 0;
        while (tryCount++ <= maxTryCount) {
            if(tryLock(lockKey)) {
                return true;
            }
            try {
                Thread.sleep(perWaitingMilliSeconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean tryLock(String lockKey) {
        return "OK".equals(
                connection.execute("SET", lockKey.getBytes(), threadId,
                        PX, expireMilliSeconds, NX));
    }

    public void unlock(String lockKey) {
        connection.eval(DEL_SCRIPT, ReturnType.INTEGER, 2,
                lockKey.getBytes(), threadId);
    }
}

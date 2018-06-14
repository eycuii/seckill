package com.ey.seckill.lock;

import io.lettuce.core.RedisCommandInterruptedException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisTemplate;

public class DistributedLock {

    private static final byte[] NX = "NX".getBytes();
    private static final byte[] PX = "PX".getBytes();
    private static final byte[] DEL_SCRIPT = (
            "if redis.call('get', KEYS[1]) == KEYS[2] then " +
            "return redis.call('del', KEYS[1]) " +
            "else return 0 end").getBytes();

    private final RedisConnection connection;
    private final byte[] expire;
    private final byte[] threadId;

    public DistributedLock(RedisTemplate redisTemplate, int expire) {
        this.connection = redisTemplate.getConnectionFactory().getConnection();
        this.expire = (expire + "").getBytes();
        this.threadId = (Thread.currentThread().getId() + "").getBytes();
    }

    public boolean lock(String lockKey, int maxTryCount, int perSleepMilliSeconds) {
        int tryCount = 0;
        while (tryCount++ <= maxTryCount) {
            if(tryLock(lockKey)) {
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

    public boolean tryLock(String lockKey) {
        return "OK".equals(
                connection.execute("SET", lockKey.getBytes(), threadId,
                        PX, expire, NX));
    }

    public boolean unlock(String lockKey) {
        Long result = connection.eval(DEL_SCRIPT, ReturnType.INTEGER, 2,
                lockKey.getBytes(), threadId);
        return result == 1;
    }

    public Thread createDaemonThread(String lockKey, int start) {
        Thread daemonTread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    Thread.sleep(start);
                    connection.execute("SET", lockKey.getBytes(), threadId,
                            PX, expire);
                }
            } catch (InterruptedException | RedisCommandInterruptedException e) {
//                e.printStackTrace();
            }
        });
        daemonTread.setDaemon(true);
        return daemonTread;
    }
}

package com.ey.seckill.lock;

import io.lettuce.core.RedisCommandInterruptedException;
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
    private final Thread daemonTread;

    public RedisLock(RedisTemplate redisTemplate, String lockKey, int expire, int serviceDuration) {
        this.connection = redisTemplate.getConnectionFactory().getConnection();
        this.lockKey = lockKey.getBytes();
        this.expire = (expire + "").getBytes();
        this.threadId = (Thread.currentThread().getId() + "").getBytes();
        this.daemonTread = createDaemonThread(serviceDuration);
    }

    public boolean lock(int maxTryCount, int perSleepMilliSeconds) {
        int tryCount = 0;
        while (tryCount++ <= maxTryCount) {
            if(tryLock()) {
                daemonTread.start();
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

    public boolean tryLock() {
        return "OK".equals(
                connection.execute("SET", lockKey, threadId,
                        PX, expire, NX));
    }

    public boolean unlock() {
        daemonTread.interrupt();
        Long result = connection.eval(DEL_SCRIPT, ReturnType.INTEGER, 2,
                lockKey, threadId);
        return result == 1;
    }

    private Thread createDaemonThread(int serviceDuration) {
        Thread daemonTread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    Thread.sleep(serviceDuration);
                    connection.execute("SET", lockKey, threadId,
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

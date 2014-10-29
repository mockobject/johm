package redis.clients.johm;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.commons.lang.SystemUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import java.io.File;
import java.io.IOException;

import static redis.clients.johm.ResourceManager.extract;

public class JOhmTestBase extends Assert {
    protected JedisPool jedisPool;
    protected volatile static boolean benchmarkMode;

    private static Boolean redisIsRunning = false;

    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        if (!redisIsRunning) {
            final String redisServerPath;
            if (SystemUtils.IS_OS_LINUX) {
                redisServerPath = extract("/server/linux/redis-server");
            } else if (SystemUtils.IS_OS_WINDOWS) {
                redisServerPath = extract("/server/win64/redis-server.exe");
            } else if (SystemUtils.IS_OS_MAC_OSX) {
                redisServerPath = extract("/server/osx/redis-server");
            } else {
                throw new RuntimeException("Can't launch redis server for unit tests.");
            }

            final Process redisServerProcess = Runtime.getRuntime().exec(redisServerPath);

            final StreamGobbler errorGobbler = new
                    StreamGobbler(redisServerProcess.getErrorStream(), "ERROR");
            final StreamGobbler outputGobbler = new
                    StreamGobbler(redisServerProcess.getInputStream(), "OUTPUT");

            errorGobbler.start();
            outputGobbler.start();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    redisServerProcess.destroy();
                    try {
                        redisServerProcess.waitFor();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    try {
                        errorGobbler.join();
                        outputGobbler.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    File file = new File(redisServerPath);
                    file.delete();
                }
            });

            redisIsRunning = true;
        }
    }

    @Before
    public void startUp() {
        startJedisEngine();
    }

    protected void startJedisEngine() {
        if (benchmarkMode) {
            jedisPool = new JedisPool(new GenericObjectPoolConfig(), "localhost",
                    Protocol.DEFAULT_PORT, 2000);
        } else {
            jedisPool = new JedisPool(new GenericObjectPoolConfig(), "localhost");
        }
        JOhm.setPool(jedisPool);
        purgeRedis();
    }

    protected void purgeRedis() {
        Jedis jedis = jedisPool.getResource();
        jedis.flushAll();
        jedisPool.returnResource(jedis);
    }
}
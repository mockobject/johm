package redis.clients.johm;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.commons.lang.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import java.io.IOException;

import static redis.clients.johm.ResourceManager.extract;

public class JOhmTestBase extends Assert {
    protected JedisPool jedisPool;
    protected volatile static boolean benchmarkMode;

    private static String redisServerPath;
    private static Process redisServerProcess;
    private static StreamGobbler errorGobbler;
    private static StreamGobbler outputGobbler;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        if (SystemUtils.IS_OS_LINUX) {
            redisServerPath = extract("/server/linux/redis-server");
        } else if (SystemUtils.IS_OS_WINDOWS) {
            redisServerPath = extract("/server/win64/redis-server.exe");
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            redisServerPath = extract("/server/osx/redis-server");
        } else {
            throw new RuntimeException("Can't launch redis server for unit tests.");
        }

        redisServerProcess = Runtime.getRuntime().exec(redisServerPath);

        errorGobbler = new
                StreamGobbler(redisServerProcess.getErrorStream(), "ERROR");
        outputGobbler = new
                StreamGobbler(redisServerProcess.getInputStream(), "OUTPUT");

        errorGobbler.start();
        outputGobbler.start();

        // TODO: find out why some unit tests start too fast
        Thread.sleep(200);
    }

    @AfterClass
    public static void cleanup() throws InterruptedException {
        redisServerProcess.destroy();
        redisServerProcess.waitFor();

        //File file = new File(redisServerPath);
        //file.delete();
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
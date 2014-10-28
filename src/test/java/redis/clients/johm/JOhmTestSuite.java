package redis.clients.johm;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import redis.clients.johm.benchmark.SaveDeleteBenchmark;
import redis.clients.johm.benchmark.SaveGetBenchmark;
import redis.clients.johm.benchmark.SaveSearchBenchmark;

import java.io.File;
import java.io.IOException;

import static redis.clients.johm.ResourceManager.extract;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BasicPersistenceTest.class,
        CollectionsDataTypeTest.class,
        CollectionsTest.class,
        ConvertorTest.class,
        JOhmUtilsTest.class,
        NestTest.class,
        SearchTest.class,
        SaveDeleteBenchmark.class,
        SaveGetBenchmark.class,
        SaveSearchBenchmark.class
})
public class JOhmTestSuite {
    private static String redisServerPath;
    private static Process redisServerProcess;
    private static StreamGobbler errorGobbler;
    private static StreamGobbler outputGobbler;

    @BeforeClass
    public static void setUp() throws IOException {
        redisServerPath = extract("/server/win64/redis-server.exe");
        redisServerProcess = Runtime.getRuntime().exec(redisServerPath);

        errorGobbler = new
                StreamGobbler(redisServerProcess.getErrorStream(), "ERROR");
        outputGobbler = new
                StreamGobbler(redisServerProcess.getInputStream(), "OUTPUT");

        errorGobbler.start();
        outputGobbler.start();
    }

    @AfterClass
    public static void cleanup() throws InterruptedException {
        redisServerProcess.destroy();
        redisServerProcess.waitFor();

        File file = new File(redisServerPath);
        file.delete();
    }
}

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.sun.org.slf4j.internal.Logger;
import org.junit.Test;
import remote_server.RemoteServiceTestSimulator;

import static org.junit.Assert.assertEquals;

public class HystrixTest {


    Logger logger = new Logger("logger");

    @Test
    public void givenInputBobAndDefaultSettings_whenCommandExecuted_thenReturnHelloBob(){
        System.out.println(new CommandHelloWorld("Bob").execute());
        assertEquals(new CommandHelloWorld("Bob").execute(), "Hello Bob!");
    }

    @Test
    public void printAnimalName() throws InterruptedException {

        logger.warn("before ");
        Thread.sleep(5000);
        logger.warn("after ");
        assertEquals("Animal is Tiger", new PrintAnimalHystrixCommand(HystrixCommandGroupKey.Factory.asKey("print_animal"),"Tiger").execute());
    }

    @Test
    public void test2()
            throws InterruptedException {

        HystrixCommand.Setter config = HystrixCommand
                .Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroup2"));

        HystrixCommandProperties.Setter commandProperties = HystrixCommandProperties.Setter();
        commandProperties.withExecutionTimeoutInMilliseconds(2000);

        config.andCommandPropertiesDefaults(commandProperties);

        assertEquals(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(45000)).execute(),
                "Success");
    }

    @Test
    public void test3()
            throws InterruptedException {

        HystrixCommand.Setter config = HystrixCommand
                .Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroup2"));

        assertEquals(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(1080)).execute(),
                "Success");
    }


    @Test
    public void givenSvcTimeoutOf500AndExecTimeoutOf10000AndThreadPool_whenRemoteSvcExecuted_thenReturnSuccess()
            throws InterruptedException {

        HystrixCommand.Setter config = HystrixCommand
                .Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupThreadPool"));

        HystrixCommandProperties.Setter commandProperties = HystrixCommandProperties.Setter();
        commandProperties.withExecutionTimeoutInMilliseconds(10_000);
        config.andCommandPropertiesDefaults(commandProperties);
        config.andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                .withMaxQueueSize(10)
                .withCoreSize(3)
                .withQueueSizeRejectionThreshold(10));

        assertEquals(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(500)).execute(),
                "Success");
    }

    @Test
    public void givenCircuitBreakerSetup_whenRemoteSvcCmdExecuted_thenReturnSuccess()
            throws InterruptedException {

        HystrixCommand.Setter config = HystrixCommand
                .Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupCircuitBreaker"));

        HystrixCommandProperties.Setter properties = HystrixCommandProperties.Setter();
        properties.withExecutionTimeoutInMilliseconds(1000);
        properties.withCircuitBreakerSleepWindowInMilliseconds(4000);
        properties.withExecutionIsolationStrategy
                (HystrixCommandProperties.ExecutionIsolationStrategy.THREAD);
        properties.withCircuitBreakerEnabled(true);
        properties.withCircuitBreakerRequestVolumeThreshold(1); // itna shae sakta hai isse jada nahi

        config.andCommandPropertiesDefaults(properties);
        config.andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                .withMaxQueueSize(1)
                .withCoreSize(1)
                .withQueueSizeRejectionThreshold(1));

        assertEquals(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(5000)).execute(), "fallback method executed");
        assertEquals(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(5000)).execute(), "fallback method executed");
        assertEquals(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(500)).execute(), "fallback method executed");

        Thread.sleep(5000);

        System.out.println("checkpoint 1");
        assertEquals(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(500)).execute(),
                ("Success"));
        System.out.println("checkpoint 2");
        assertEquals(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(500)).execute(),
                ("Success"));
        System.out.println("checkpoint 3");
        assertEquals(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(500)).execute(),
                ("Success"));
    }
}

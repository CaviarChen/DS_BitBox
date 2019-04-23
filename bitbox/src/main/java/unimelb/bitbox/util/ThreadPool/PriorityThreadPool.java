package unimelb.bitbox.util.ThreadPool;


import java.util.concurrent.*;
import java.util.logging.Logger;


public class PriorityThreadPool {
    private static Logger log = Logger.getLogger(PriorityThreadPool.class.getName());

    private static ThreadPoolExecutor pool;
    private static int corePoolSize;
    private static int maximumPoolSize;
    private static int queueSize;
    private static long keepAliveTime;

    private static PriorityThreadPool instance = new PriorityThreadPool();;

    public static PriorityThreadPool getInstance() {
        return instance;
    }

    private PriorityThreadPool() {
        corePoolSize = Runtime.getRuntime().availableProcessors()+1;
        maximumPoolSize = 5000;
        queueSize = 5000;
        keepAliveTime = 2000L;

        pool = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<>(queueSize),
                new ThreadPoolExecutor.AbortPolicy()
                );
        log.info("Thread pool is ready to go");
    }

    public void submitTask(PriorityTask priorityTask) {
        synchronized (this) {
            pool.execute(priorityTask);
        }
    }
}

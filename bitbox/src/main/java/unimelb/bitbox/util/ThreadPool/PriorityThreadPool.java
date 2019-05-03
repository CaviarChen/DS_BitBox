package unimelb.bitbox.util.ThreadPool;


import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 *
 *
 * @author Wenqing Xue (813044)
 * @author Weizhi Xu (752454)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class PriorityThreadPool {
    private static Logger log = Logger.getLogger(PriorityThreadPool.class.getName());

    private static ThreadPoolExecutor pool;

    private static PriorityThreadPool instance = new PriorityThreadPool();


    public static PriorityThreadPool getInstance() {
        return instance;
    }


    private PriorityThreadPool() {

        pool = new ThreadPoolExecutor(
                Constants.THREAD_POOL_CORE_POOL_SIZE,
                Constants.THREAD_POOL_MAX_POOL_SZIE,
                Constants.THREAD_POOL_KEEP_ALIVE_TIME,
                TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<>(Constants.THREAD_POOL_QUEUE_SIZE),
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

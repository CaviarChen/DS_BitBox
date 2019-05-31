package unimelb.bitbox.util.ThreadPool;


import unimelb.bitbox.Constants;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 * PriorityThreadPool is a singleton class. It mainly handles all requests
 * and responses, sync events and clean up {@link unimelb.bitbox.util.FileSystem.FileLoaderWrapper}.
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class PriorityThreadPool {
    private static Logger log = Logger.getLogger(PriorityThreadPool.class.getName());

    private static ThreadPoolExecutor pool;

    private static PriorityThreadPool instance = new PriorityThreadPool();


    /**
     * Get the instance of PriorityThreadPool
     *
     * @return the instance of PriorityThreadPool
     */
    public static PriorityThreadPool getInstance() {
        return instance;
    }


    /**
     * Constructor of PriorityThreadPool.
     * <p>
     * if the number of tasks is less than core pool size, a new thread will be allocated for each of them.
     * if the number of tasks exceeds the core pool size but less than maximum pool size,
     * they will be added to a PriorityBlockingQueue.
     * if the number of tasks exceeds the maximum pool size, {@link RejectedExecutionException} will be thrown.
     */
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


    /**
     * Submit task with priority
     *
     * @param priorityTask {@link PriorityTask} the priority task to be submitted
     */
    public void submitTask(PriorityTask priorityTask) {
        synchronized (this) {
            pool.execute(priorityTask);
        }
    }
}

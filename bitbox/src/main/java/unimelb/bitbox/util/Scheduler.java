package unimelb.bitbox.util;

import unimelb.bitbox.util.ThreadPool.PriorityTask;
import unimelb.bitbox.util.ThreadPool.PriorityThreadPool;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Scheduler for managing periodical tasks
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class Scheduler {
    private static Logger log = Logger.getLogger(Scheduler.class.getName());

    private static Scheduler instance = new Scheduler();

    public static Scheduler getInstance() {
        return instance;
    }

    private final ScheduledExecutorService service;

    private Scheduler() {
        service = Executors.newSingleThreadScheduledExecutor();
    }

    // schedule a task and execute using the main priorityThreadPool
    public synchronized void addTask(int timeInterval, TimeUnit timeUnit, PriorityTask priorityTask) {
        log.info("New task added: " + priorityTask.getName() +
                " Interval: " + timeInterval + timeUnit.toString());

        service.scheduleAtFixedRate(() -> PriorityThreadPool.getInstance().submitTask(priorityTask),
                timeInterval, timeInterval, timeUnit);

    }
}

package unimelb.bitbox.util;

import unimelb.bitbox.util.ThreadPool.PriorityTask;
import unimelb.bitbox.util.ThreadPool.PriorityThreadPool;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

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
    public synchronized void addTask(int timeIntervalInSec, PriorityTask priorityTask) {
        log.info("New task added: " + priorityTask.getName() + " Interval: " + timeIntervalInSec);

        service.schedule(() -> PriorityThreadPool.getInstance().submitTask(priorityTask),
                timeIntervalInSec, TimeUnit.SECONDS);

    }
}

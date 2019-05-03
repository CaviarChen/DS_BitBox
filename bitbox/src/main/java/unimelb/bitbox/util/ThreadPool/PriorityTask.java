package unimelb.bitbox.util.ThreadPool;


import java.util.logging.Logger;


/**
 *
 * @author Wenqing Xue (813044)
 * @author Weizhi Xu (752454)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class PriorityTask implements Runnable, Comparable<PriorityTask> {

    private String name;
    private Priority priority;
    private Runnable task;
    private static Logger log = Logger.getLogger(PriorityTask.class.getName());


    public PriorityTask(String name, Priority priority, Runnable task) {
        this.name = name;
        this.priority = priority;
        this.task = task;
    }


    public Priority getPriority() {
        return this.priority;
    }


    public void setPriority(Priority priority) {
        this.priority = priority;
    }


    @Override
    public void run() {
        try {
            task.run();
        } catch (Exception e) {
            log.severe("Task run failed" + e.toString());
        }
    }


    @Override
    public int compareTo(PriorityTask o) {
        return this.priority.compareTo(o.priority);
    }
}

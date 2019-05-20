package unimelb.bitbox.util.ThreadPool;


import java.util.logging.Logger;


/**
 * This is a wrapper for tasks({@link Runnable}) with priority.
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class PriorityTask implements Runnable, Comparable<PriorityTask> {

    private String name;
    private Priority priority;
    private Runnable task;
    private static Logger log = Logger.getLogger(PriorityTask.class.getName());


    /**
     * Constructor for a task with priority
     *
     * @param name The name of the task
     * @param priority The priority of the task
     * @param task The actual task need to run
     */
    public PriorityTask(String name, Priority priority, Runnable task) {
        this.name = name;
        this.priority = priority;
        this.task = task;
    }


    /**
     * Get the priority of the task
     * @return the priority of the task
     */
    public Priority getPriority() {
        return this.priority;
    }


    public String getName() {
        return name;
    }

    /**
     * Set the priority of the task
     * @param priority the priority of the task wanted to be set
     */
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
        // use the Enum Priority's default value to compare
        // the one listed first has the highest priority
        return this.priority.compareTo(o.priority);
    }
}

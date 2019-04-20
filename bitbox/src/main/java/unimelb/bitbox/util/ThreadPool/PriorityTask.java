package unimelb.bitbox.util.ThreadPool;


public class PriorityTask implements Runnable, Comparable<PriorityTask>{

    private String name;
    private Priority priority;

    public PriorityTask(String name, Priority priority) {
        this.name = name;
        this.priority = priority;
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
            System.out.println(this.name + " is running");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int compareTo(PriorityTask o) {
        return this.priority.compareTo(o.priority);
    }
}

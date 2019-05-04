package unimelb.bitbox.util.ThreadPool;


/**
 * Priorities for threads in the thread pool. See {@link PriorityThreadPool}
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public enum Priority {
    // order is important as it is used to compare
    HIGH,
    NORMAL,
    LOW
}

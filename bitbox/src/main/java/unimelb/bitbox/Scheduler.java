package unimelb.bitbox;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.ThreadPool.Priority;
import unimelb.bitbox.util.ThreadPool.PriorityTask;
import unimelb.bitbox.util.ThreadPool.PriorityThreadPool;

import java.util.logging.Logger;

public class Scheduler {

    private static Logger log = Logger.getLogger(Scheduler.class.getName());

    private static final int MILLIS_PER_SEC = 1000;

    private final long syncInterval;
    private Thread thread;

    public Scheduler() {
        syncInterval = Long.parseLong(Configuration.getConfigurationValue("syncInterval")) * MILLIS_PER_SEC;
    }


    public void start() {
        if (thread != null) {
            throw new RuntimeException("Already running");
        }


        thread = new Thread(()->{
            while (!thread.isInterrupted()) {
                try {
                    Thread.sleep(syncInterval);

                    // generate sync event
                    SyncManager.getInstance().syncWithAllAsync();

                    // clean up FileLoaderWrapper, use thread with low priority
                    PriorityThreadPool.getInstance().submitTask(new PriorityTask(
                            "clean up FileLoaderWrapper",
                            Priority.LOW,
                            MessageHandler::cleanUpFileLoaderWrapper
                    ));

                } catch (Exception e) {
                    log.warning(e.toString());
                }
            }
        });
        thread.start();
    }
}

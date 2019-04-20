package unimelb.bitbox;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.ThreadPool.Priority;
import unimelb.bitbox.util.ThreadPool.PriorityTask;
import unimelb.bitbox.util.ThreadPool.PriorityThreadPool;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class Peer {
    private static Logger log = Logger.getLogger(Peer.class.getName());
    private static IncomingConnectionHelper incomingConnectionManager;

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");


        /* How to add a task to thread pool
        PriorityThreadPool.getInstance().submitTask(new PriorityTask("Task Name", Priority.NORMAL, () -> {
            // custom runnable here
        }));
        */

        int port = Integer.parseInt(Configuration.getConfigurationValue("port"));
        incomingConnectionManager = new IncomingConnectionHelper(port);

    }
}

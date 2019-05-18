package unimelb.bitbox.util.ConnectionUtils;

import java.util.logging.Logger;

public abstract class IncomingConnectionHelper {
    private static Logger log = Logger.getLogger(IncomingConnectionHelper.class.getName());
    private Thread thread = null;

    /**
     * start working thread
     */
    public void start() {
        if (thread != null) throw new RuntimeException("Already started");

        thread = new Thread(() -> {
            try {
                execute();
            } catch (Exception e) {
                log.severe(e.toString());
            }

        });
        thread.start();
    }

    protected abstract void execute() throws Exception;
}

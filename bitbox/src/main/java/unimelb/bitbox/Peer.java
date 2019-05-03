package unimelb.bitbox;


import unimelb.bitbox.ConnectionPkg.IncomingConnectionHelper;
import unimelb.bitbox.ConnectionPkg.OutgoingConnectionHelper;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystem.FileSystemManager;
import unimelb.bitbox.util.MessageHandler;
import unimelb.bitbox.util.Scheduler;
import unimelb.bitbox.util.SyncManager;

import java.util.logging.Logger;


public class Peer {
    private static Logger log = Logger.getLogger(Peer.class.getName());
    private static IncomingConnectionHelper incomingConnectionManager;
    private static OutgoingConnectionHelper outgoingConnectionHelper;
    private static Scheduler scheduler;


    public static void main(String[] args) throws Exception {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");

        FileSystemManager fileSystemManager =
                new FileSystemManager(Configuration.getConfigurationValue(Constants.CONFIG_FIELD_PATH),
                        (SyncManager.getInstance()::sendEventToAllAsync));

        SyncManager.getInstance().init(fileSystemManager);

        MessageHandler.setFileSystemManager(fileSystemManager);

        scheduler = new Scheduler();
        scheduler.start();

        int port = Integer.parseInt(Configuration.getConfigurationValue(Constants.CONFIG_FIELD_PORT));
        String advertisedName = Configuration.getConfigurationValue(Constants.CONFIG_FIELD_AD_NAME);
        incomingConnectionManager = new IncomingConnectionHelper(advertisedName, port);
        outgoingConnectionHelper = new OutgoingConnectionHelper(advertisedName, port);
        outgoingConnectionHelper.execute();
    }
}

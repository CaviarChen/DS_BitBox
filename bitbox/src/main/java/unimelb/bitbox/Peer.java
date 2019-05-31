package unimelb.bitbox;


import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.ConnectionUtils.ClientServer.ServerConnectionHelper;
import unimelb.bitbox.util.ConnectionUtils.Peer.*;
import unimelb.bitbox.util.FileSystem.FileSystemManager;
import unimelb.bitbox.util.MessageHandler;
import unimelb.bitbox.util.SecManager;
import unimelb.bitbox.util.SyncManager;

import java.util.logging.Logger;


/**
 * Main entrypoint of the bitbox
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class Peer {
    private static Logger log = Logger.getLogger(Peer.class.getName());
    private static IncomingConnectionHelper incomingConnectionManager;
    private static OutgoingConnectionHelper outgoingConnectionHelper;
    private static ServerConnectionHelper serverConnectionHelper;

    /**
     * Entry point
     *
     * @param args arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");

        FileSystemManager fileSystemManager =
                new FileSystemManager(Configuration.getConfigurationValue(Constants.CONFIG_FIELD_PATH),
                        (SyncManager.getInstance()::sendEventToAllAsync));

        SyncManager.getInstance().init(fileSystemManager);

        MessageHandler.init(fileSystemManager);
        SecManager.getInstance().init(SecManager.Mode.ServerMode);

        String advertisedName = Configuration.getConfigurationValue(Constants.CONFIG_FIELD_AD_NAME);

        if (Configuration.getConfigurationValue("mode").toLowerCase().equals("tcp")) {
            log.info("Using TCP mode.");

            int port = Integer.parseInt(Configuration.getConfigurationValue(Constants.CONFIG_FIELD_PORT));
            incomingConnectionManager = new TCPIncomingConnectionHelper(advertisedName, port);
            outgoingConnectionHelper = new TCPOutgoingConnectionHelper(advertisedName, port);

        } else {
            log.info("Using UDP mode.");

            int port = Integer.parseInt(Configuration.getConfigurationValue("udpPort"));
            incomingConnectionManager = new UDPIncomingConnectionHelper(advertisedName, port);
            outgoingConnectionHelper = new UDPOutgoingConnectionHelper(advertisedName, port,
                    ((UDPIncomingConnectionHelper) incomingConnectionManager).getServerSocket());

        }
        serverConnectionHelper = new ServerConnectionHelper(outgoingConnectionHelper);
        serverConnectionHelper.start();
        incomingConnectionManager.start();
        outgoingConnectionHelper.execute();

    }
}

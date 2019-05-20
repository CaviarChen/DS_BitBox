package unimelb.bitbox;


import unimelb.bitbox.util.*;
import unimelb.bitbox.util.ConnectionUtils.IncomingConnectionHelper;
import unimelb.bitbox.util.ConnectionUtils.OutgoingConnectionHelper;
import unimelb.bitbox.util.ConnectionUtils.TCPIncomingConnectionHelper;
import unimelb.bitbox.util.ConnectionUtils.TCPOutgoingConnectionHelper;
import unimelb.bitbox.util.FileSystem.FileSystemManager;

import java.util.LinkedHashMap;
import java.util.Map;
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
    private static TCPOutgoingConnectionHelper outgoingConnectionHelper;

    /**
     * Entry point
     * @param args arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

//        int port = Integer.parseInt(Configuration.getConfigurationValue("udpPort"));
//        String advertisedName = Configuration.getConfigurationValue(Constants.CONFIG_FIELD_AD_NAME);
//        incomingConnectionManager = new UDPIncomingConnectionHelper(advertisedName, port);
//        incomingConnectionManager.start();

        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");

        FileSystemManager fileSystemManager =
                new FileSystemManager(Configuration.getConfigurationValue(Constants.CONFIG_FIELD_PATH),
                        (SyncManager.getInstance()::sendEventToAllAsync));

        SyncManager.getInstance().init(fileSystemManager);

        MessageHandler.init(fileSystemManager);
        SecManager.getInstance().init(SecManager.Mode.ClientMode);


        int port = Integer.parseInt(Configuration.getConfigurationValue(Constants.CONFIG_FIELD_PORT));
        String advertisedName = Configuration.getConfigurationValue(Constants.CONFIG_FIELD_AD_NAME);
        incomingConnectionManager = new TCPIncomingConnectionHelper(advertisedName, port);
        incomingConnectionManager.start();
        outgoingConnectionHelper = new TCPOutgoingConnectionHelper(advertisedName, port);
        outgoingConnectionHelper.execute();
    }
}

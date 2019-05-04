package unimelb.bitbox.util.ConnectionUtils;


import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.HostPort;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


/**
 * ConnectionManager manages all connections and contains helper functions for them
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class ConnectionManager {
    private static ConnectionManager instance = new ConnectionManager();

    private static final int MAX_INCOMING_CONNECTIONS =
            Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"));


    public static ConnectionManager getInstance() {
        return instance;
    }


    private int incomingConnCounter = 0;
    private ConcurrentHashMap<HostPort, Connection> connectionMap;


    private ConnectionManager() {
        connectionMap = new ConcurrentHashMap<>();
    }


    /**
     * Add a new connection
     * @param conn connection
     * @param hostPort host&port of this connection
     * @return  0: ok, -1: exceed connection limit, -2: connection already exists
     */
    public int addConnection(Connection conn, HostPort hostPort) {
        synchronized (this) {
            boolean isIncoming = conn.type == Connection.ConnectionType.INCOMING;
            if (isIncoming) {
                if (incomingConnCounter >= MAX_INCOMING_CONNECTIONS) {
                    return -1;
                }
            }

            if (connectionMap.containsKey(hostPort)) {
                return -2;
            }

            connectionMap.put(hostPort, conn);

            if (isIncoming) {
                incomingConnCounter += 1;
            }

            return 0;
        }
    }

    /**
     * Broadcast message to all active connections
     * Async method
     * @param msg json message string
     */
    public void broadcastMsgAsync(String msg) {
        // no need to lock
        for (Connection conn : connectionMap.values()) {
            conn.sendAsync(msg);
        }
    }

    /**
     * @return true if incoming connection counter reaches the limit
     */
    public boolean isIncommingConnectionFull() {
        synchronized (this) {
            return incomingConnCounter >= MAX_INCOMING_CONNECTIONS;
        }
    }


    /**
     * Remove a connection from the active list
     * @param conn connection
     * @return true if success
     */
    public boolean removeConnection(Connection conn) {
        HostPort hostPort = conn.getHostPort();
        boolean res = connectionMap.remove(hostPort, conn);
        if (res && conn.type == Connection.ConnectionType.INCOMING) {
            synchronized (this) {
                incomingConnCounter -= 1;
            }
        }
        return res;
    }

    /**
     * @param hostPort
     * @return true if if we already connected with the given host&port
     */
    public boolean checkExist(HostPort hostPort) {
        return connectionMap.containsKey(hostPort);
    }

    /**
     * @return a list of host&port that we connected with
     */
    public ArrayList<HostPort> getConnectedPeers() {
        ArrayList<HostPort> hostPorts = new ArrayList<>();
        // no need to lock
        for (Connection conn : connectionMap.values()) {
            hostPorts.add(conn.getHostPort());
        }
        return hostPorts;
    }

}

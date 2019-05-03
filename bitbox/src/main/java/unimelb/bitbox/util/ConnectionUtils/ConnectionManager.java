package unimelb.bitbox.util.ConnectionUtils;


import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.HostPort;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 *
 * @author Wenqing Xue (813044)
 * @author Weizhi Xu (752454)
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


    // 0: ok, -1: exceed connection limit, -2: connection already exists
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


    public void broadcastMsgAsync(String msg) {
        // no need to lock
        for (Connection conn : connectionMap.values()) {
            conn.sendAsync(msg);
        }
    }


    public boolean isIncommingConnectionFull() {
        synchronized (this) {
            return incomingConnCounter >= MAX_INCOMING_CONNECTIONS;
        }
    }


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


    public boolean checkExist(HostPort hostPort) {
        return connectionMap.containsKey(hostPort);
    }


    public ArrayList<HostPort> getConnectedPeers() {
        ArrayList<HostPort> hostPorts = new ArrayList<>();
        // no need to lock
        for (Connection conn : connectionMap.values()) {
            hostPorts.add(conn.getHostPort());
        }

        return hostPorts;
    }

}

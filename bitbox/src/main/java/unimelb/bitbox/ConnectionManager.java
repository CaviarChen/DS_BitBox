package unimelb.bitbox;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.HostPort;

import java.util.HashMap;
import java.util.Map;

public class ConnectionManager {
    private static ConnectionManager instance = new ConnectionManager();

    private static final int MAX_INCOMING_CONNECTIONS =
            Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"));

    private static int incomingConnCounter = 0;
    private static Map<HostPort, Connection> connectionMap;

    public static ConnectionManager getInstance() {
        return instance;
    }

    private ConnectionManager() {
        connectionMap = new HashMap<>();
    }

    // TODO: maybe change to exception
    // 0: ok, -1: exceed connection limit, -2: connection already exists
    public int addConnection(Connection conn, HostPort hostPort) {
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
        conn.init(hostPort);

        if (isIncoming) {
            incomingConnCounter += 1;
        }

        return 0;
    }


}

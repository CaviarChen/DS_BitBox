package unimelb.bitbox.util.ConnectionUtils.Connection;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.HostPort;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class UDPConnection extends Connection {

    private static final int listenPort = Integer.parseInt(Configuration.getConfigurationValue("udpPort"));

    private final HostPort hostPort;
    private final HostPort actualHostPort;

    public UDPConnection(HostPort hostPort) throws UnknownHostException {
        this.hostPort = hostPort;
        String ip = InetAddress.getByName(hostPort.host).getHostAddress();
        this.actualHostPort = new HostPort(ip, hostPort.port);
    }


}

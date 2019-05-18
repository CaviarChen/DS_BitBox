package unimelb.bitbox.util.ConnectionUtils;

import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.util.HostPort;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class UDPConnection extends Connection {

//    private static final int listenPort = Integer.parseInt(Configuration.getConfigurationValue("udpPort"));

    private final DatagramSocket serverSocket;
    private final HostPort hostPort;
    private final InetAddress hostAddree;

    public UDPConnection(DatagramSocket serverSocket, HostPort hostPort) throws UnknownHostException {
        this.serverSocket = serverSocket;
        this.hostPort = hostPort;
        this.hostAddree = InetAddress.getByName(hostPort.host);
    }


    @Override
    public void sendAsync(Protocol protocol) {
        // TODO: add retry mechanism
        String msg = ProtocolFactory.marshalProtocol(protocol);
        byte[] buffer = msg.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, hostAddree, hostPort.port);
//        serverSocket.send(packet);
    }
}

package unimelb.bitbox;


import unimelb.bitbox.util.CmdParser;
import unimelb.bitbox.util.ConnectionUtils.ClientServer.ClientConnectionHelper;
import unimelb.bitbox.util.ConnectionUtils.ClientServer.ClientConnection;
import unimelb.bitbox.util.HostPort;
import unimelb.bitbox.util.SecManager;

import java.net.Socket;


public class Client {

    public static void main(String[] args) throws Exception {
        // parse command line args
        CmdParser cmdParser = new CmdParser(args);
        cmdParser.parse();

        SecManager.getInstance().init(SecManager.Mode.ClientMode);
        SecManager.setPrivateIdentity(cmdParser.getIdentity());

        // establish connection with the peer using TCP
        HostPort serverHostPort = new HostPort(cmdParser.getServer());

        Socket clientSocket = new Socket(serverHostPort.host, serverHostPort.port);
        ClientConnection clientConn = new ClientConnection(clientSocket);

        ClientConnectionHelper clientConnectionHelper = new ClientConnectionHelper(clientConn);

        clientConnectionHelper.handleAuth();

        switch (cmdParser.getCmd()) {
            case "list_peers":
                clientConnectionHelper.handleListPeer();
                break;
            case "connect_peer":
                clientConnectionHelper.handleConnectPeer(cmdParser.getPeer());
                break;
            case "disconnect_peer":
                clientConnectionHelper.handleDisConnectPeer(cmdParser.getPeer());
                break;
        }
    }
}

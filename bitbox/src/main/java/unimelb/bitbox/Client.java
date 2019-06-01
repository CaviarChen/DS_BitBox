package unimelb.bitbox;


import unimelb.bitbox.util.CmdParser;
import unimelb.bitbox.util.ConnectionUtils.ClientServer.ClientConnection;
import unimelb.bitbox.util.ConnectionUtils.ClientServer.ClientConnectionHelper;
import unimelb.bitbox.util.HostPort;
import unimelb.bitbox.util.SecManager;

import java.net.Socket;


/**
 * Class is used for client connection
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class Client {

    public static void main(String[] args) throws Exception {
        // parse command line args
        CmdParser cmdParser = new CmdParser(args);
        cmdParser.parse();

        SecManager.getInstance().init(SecManager.Mode.ClientMode);
        SecManager.getInstance().setPrivateIdentity(cmdParser.getIdentity());

        // establish connection with the peer using TCP
        HostPort serverHostPort = new HostPort(cmdParser.getServer());

        Socket clientSocket = new Socket(serverHostPort.host, serverHostPort.port);
        ClientConnection clientConn = new ClientConnection(clientSocket);

        ClientConnectionHelper clientConnectionHelper = new ClientConnectionHelper(clientConn);

        clientConnectionHelper.handleAuth();

        switch (cmdParser.getCmd()) {
            case Constants.CLIENT_CMD_LIST_PEERS:
                clientConnectionHelper.handleListPeer();
                break;
            case Constants.CLIENT_CMD_CONNECT_PEER:
                clientConnectionHelper.handleConnectPeer(cmdParser.getPeer());
                break;
            case Constants.CLIENT_CMD_DISCONNECT_PEER:
                clientConnectionHelper.handleDisConnectPeer(cmdParser.getPeer());
                break;
            default:
                throw new Exception("The command " + cmdParser.getCmd() + "is not supported: ");
        }

        clientConn.close();
    }
}

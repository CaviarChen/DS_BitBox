package unimelb.bitbox;


import unimelb.bitbox.protocol.ClientProtocol;
import unimelb.bitbox.protocol.ClientProtocolFactory;
import unimelb.bitbox.protocol.ClientProtocolType;
import unimelb.bitbox.util.CmdParser;
import unimelb.bitbox.util.ConnectionUtils.ClientServer.ClientServerConnection;
import unimelb.bitbox.util.HostPort;
import unimelb.bitbox.util.SecManager;

import java.net.Socket;


public class Client {

    public static void main(String[] args) throws Exception {
        // parse command line args
        CmdParser cmdParser = new CmdParser(args);
        cmdParser.parse();

        SecManager.getInstance().init(SecManager.Mode.ClientMode);

        // establish connection with the peer using TCP
        HostPort serverHostPort = new HostPort(cmdParser.getServer());

        Socket clientSocket = new Socket(serverHostPort.host, serverHostPort.port);
        ClientServerConnection clientConn = new ClientServerConnection(clientSocket);

        // send auth request
        String identity = SecManager.getInstance().getPrivateIdentity();
        ClientProtocol.AuthRequest authReq = new ClientProtocol.AuthRequest();
        authReq.authIdentity.identity = identity;
        clientConn.send(authReq, false);

        // get auth response
        String res = clientConn.receive();
        ClientProtocol protocol = ClientProtocolFactory.parseProtocol(res);
        ClientProtocolType protocolType = ClientProtocolType.typeOfProtocol(protocol);

        // send cmd

        // get cmd response
    }
}

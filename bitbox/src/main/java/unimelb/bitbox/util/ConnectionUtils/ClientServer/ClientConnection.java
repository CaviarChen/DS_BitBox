package unimelb.bitbox.util.ConnectionUtils.ClientServer;


import unimelb.bitbox.protocol.ClientProtocol;
import unimelb.bitbox.protocol.ClientProtocolFactory;
import unimelb.bitbox.protocol.InvalidProtocolException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;


public class ClientConnection {
    private static Logger log = Logger.getLogger(ClientConnection.class.getName());

    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;

    public ClientConnection(Socket socket) throws IOException {
        this.socket = socket;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                StandardCharsets.UTF_8));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),
                StandardCharsets.UTF_8));
    }

    public void send(ClientProtocol protocol) throws Exception {
        String msg = ClientProtocolFactory.marshalProtocol(protocol);

        try {
            bufferedWriter.write(msg + '\n');
            bufferedWriter.flush();
            log.info(socket.toString() + ":: Message sent: " + msg);
        } catch (IOException e) {
            close();
            log.severe(socket.toString() + ":: Unable to send message " + e.toString());
        }
    }

    private String receive() {
        // TODO: timeout
        String msg = "";
        try {
            msg = bufferedReader.readLine();
            log.info(socket.toString() + ":: Message received: " + msg);
        } catch (IOException e) {
        }

        return msg;
    }

    public ClientProtocol receviceProtocol() throws InvalidProtocolException {
        String msg = receive();
        return ClientProtocolFactory.parseProtocol(msg);
    }

    public void close() {
        try {
            bufferedWriter.close();
            bufferedReader.close();
            socket.close();
        } catch (IOException e) {
            log.severe(e.toString());
        }
    }
}

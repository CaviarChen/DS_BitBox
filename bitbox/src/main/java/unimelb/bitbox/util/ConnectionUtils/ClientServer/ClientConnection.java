package unimelb.bitbox.util.ConnectionUtils.ClientServer;


import unimelb.bitbox.protocol.ClientProtocol;
import unimelb.bitbox.protocol.ClientProtocolFactory;
import unimelb.bitbox.protocol.InvalidProtocolException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;


/**
 * Connection for client and server
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class ClientConnection {
    private static Logger log = Logger.getLogger(ClientConnection.class.getName());

    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;


    /**
     * ClientConnection constructor which initializes buffers
     *
     * @param socket the socket used to communicate
     * @throws IOException socket having trouble get Input or Output Stream
     */
    public ClientConnection(Socket socket) throws IOException {
        this.socket = socket;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                StandardCharsets.UTF_8));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),
                StandardCharsets.UTF_8));
    }


    /**
     * Marshall and send given protocol message
     *
     * @param protocol the protocol message wanted to be sent
     * @throws Exception having error sending the message
     */
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


    /**
     * Read from the socket buffer to receive a message
     *
     * @return the message
     */
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


    /**
     * Receive message and parse to protocol
     *
     * @return the received protocol
     * @throws InvalidProtocolException having error parse the received message
     */
    public ClientProtocol receiveProtocol() throws InvalidProtocolException {
        String msg = receive();
        return ClientProtocolFactory.parseProtocol(msg);
    }


    /**
     * close the connection and related buffers
     */
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

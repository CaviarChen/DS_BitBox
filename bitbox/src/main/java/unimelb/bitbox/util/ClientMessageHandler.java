package unimelb.bitbox.util;

import unimelb.bitbox.Constants;
import unimelb.bitbox.protocol.*;
import unimelb.bitbox.util.ConnectionUtils.ClientServer.ClientConnection;
import unimelb.bitbox.util.ConnectionUtils.Peer.Connection;

public class ClientMessageHandler {

    public static void handleMessage(String message, ClientConnection conn) {

        try {
            Document doc = Document.parse(message);
            String cmd = Document.parse(message).getString(Constants.PROTOCOL_FIELD_CMD);
            // maybe there is a better way to identify if this is a auth request or not
            if (cmd.equals(Constants.PROTOCOL_TYPE_AUTH_REQUEST)) {
                ClientProtocol protocol = ClientProtocolFactory.parseProtocol(message);
                handleSpecificProtocol((ClientProtocol.AuthRequest)protocol, conn);
            } else {
                // if we can find a payload field
                // note that payload: [identity,{json object}]
                String decryptedMessage = SecManager.getInstance().decryptPayload(
                        Document.parse(message).getString(Constants.PROTOCOL_FIELD_PAYLOAD));
                String[] messageArray = decryptedMessage.split(",");
                String jsonProtocol = messageArray[1].substring(0, messageArray[1].length()-1);
                ClientProtocol protocol = ClientProtocolFactory.parseProtocol(
                        Document.parse(jsonProtocol).getString(Constants.PROTOCOL_FIELD_PAYLOAD)
                );
                ClientProtocolType protocolType = ClientProtocolType.typeOfProtocol(protocol);
                switch (protocolType) {
                    case LIST_PEERS_REQUEST:
                        handleSpecificProtocol((ClientProtocol.ListPeersRequest) protocol, conn);
                        break;
                    case CONNECT_PEER_REQUEST:
                        handleSpecificProtocol((ClientProtocol.ConnectPeerRequest) protocol, conn);
                        break;
                    case DISCONNECT_PEER_REQUEST:
                        handleSpecificProtocol((ClientProtocol.DisconnectPeerRequest) protocol, conn);
                        break;
                    case LIST_PEERS_RESPONSE:
                    case CONNECT_PEER_RESPONSE:
                    case DISCONNECT_PEER_RESPONSE:
                        break;

                    default:
                        throw new InvalidProtocolException("Unexpected command: " + protocol.getClass().getName(), null);

                }
            }
        } catch (InvalidProtocolException e) {
            //TODO: Handle
        } catch (Exception e) {
            //TODO: Handle
        }


    }

    private static void handleSpecificProtocol(ClientProtocol.AuthRequest protocol, ClientConnection conn) {
        ClientProtocol.AuthResponse response = new ClientProtocol.AuthResponse();
        String identity = protocol.authIdentity.identity;

        try {
            response.authKey.key = SecManager.getInstance().encryptAESWithRSA(identity);
            response.response.status = true;
            response.response.msg = Constants.PROTOCOL_RESPONSE_MESSAGE_PUBLIC_KEY_FOUND;
            //conn.send(ClientProtocolFactory.marshalProtocol(response));

        } catch (Exception e) {
            response.response.status = false;
            response.response.msg = Constants.PROTOCOL_RESPONSE_MESSAGE_PUBLIC_KEY_NOT_FOUND;
            //conn.send(ClientProtocolFactory.marshalProtocol(response));
        }
    }

    private static void handleSpecificProtocol(ClientProtocol.DisconnectPeerRequest protocol, ClientConnection conn) {

        try {
            ClientProtocol.DisconnectPeerResponse response = new ClientProtocol.DisconnectPeerResponse();
            Document doc = new Document();
            response.hostPort.host = protocol.hostPort.host;
            response.hostPort.port = protocol.hostPort.port;
            response.response.status = true;
            response.response.msg = Constants.PROTOCOL_RESPONSE_MESSAGE_DISCONNECT_PEER;
            doc.append(Constants.PROTOCOL_FIELD_CMD,
                    SecManager.getInstance().encryptJSON(ClientProtocolFactory.marshalProtocol(response)));
            //conn.send(doc.toString());
        } catch (Exception e) {
            //TODO: Handle
        }
    }

    private static void handleSpecificProtocol(ClientProtocol.ConnectPeerRequest protocol, ClientConnection conn) {

        try {
            ClientProtocol.ConnectPeerResponse response = new ClientProtocol.ConnectPeerResponse();
            Document doc = new Document();
            response.hostPort.host = protocol.hostPort.host;
            response.hostPort.port = protocol.hostPort.port;
            response.response.status = true;
            response.response.msg = Constants.PROTOCOL_RESPONSE_MESSAGE_CONNECT_PEER;
            doc.append(Constants.PROTOCOL_FIELD_CMD,
                    SecManager.getInstance().encryptJSON(ClientProtocolFactory.marshalProtocol(response)));
            //conn.send(doc.toString());
        } catch (Exception e) {
            //TODO: Handle
        }
    }

    private static void handleSpecificProtocol(ClientProtocol.ListPeersRequest protocol, ClientConnection conn) {
        ClientProtocol.ListPeersResponse response = new ClientProtocol.ListPeersResponse();
        Document doc = new Document();
        //Todo: list the currently connected/known peers
    }
}

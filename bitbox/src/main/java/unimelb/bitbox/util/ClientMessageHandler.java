package unimelb.bitbox.util;

import unimelb.bitbox.Constants;
import unimelb.bitbox.protocol.InvalidProtocolException;
import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.protocol.ProtocolType;
import unimelb.bitbox.util.ConnectionUtils.Connection;

public class ClientMessageHandler {

    // this might not be needed
    private static  SecManager secManager = null;

    // this might not be needed
    public static void init(SecManager sm) {
        secManager = sm;
    }


    public static void handleMessage(String message, Connection conn) {

        try {
            // unchecked
            if (Document.parse(message).getString(Constants.PROTOCOL_FIELD_PAYLOAD).isEmpty()) {
                Protocol protocol = ProtocolFactory.parseProtocol(message);
                ProtocolType protocolType = ProtocolType.typeOfProtocol(protocol);
                switch (protocolType) {
                    case AUTH_REQUEST:
                        handleSpecificProtocol((Protocol.AuthRequest) protocol, conn);
                        break;

                    case AUTH_RESPONSE:
                        break;

                    default:
                        throw new InvalidProtocolException("Unexpected command: " + protocol.getClass().getName(), null);

                }
            } else {
                Protocol protocol = ProtocolFactory.parseProtocol(
                        SecManager.decryptPayload(Document.parse(message).getString(Constants.PROTOCOL_FIELD_PAYLOAD))
                );
                ProtocolType protocolType = ProtocolType.typeOfProtocol(protocol);
                switch (protocolType) {
                    case LIST_PEERS_REQUEST:
                        handleSpecificProtocol((Protocol.ListPeersRequest) protocol, conn);
                        break;
                    case CONNECT_PEER_REQUEST:
                        handleSpecificProtocol((Protocol.ConnectPeerRequest) protocol, conn);
                        break;
                    case DISCONNECT_PEER_REQUEST:
                        handleSpecificProtocol((Protocol.DisconnectPeerRequest) protocol, conn);
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
            conn.abortWithInvalidProtocol(e.getMessage());
        } catch (Exception e) {
            conn.abortWithInvalidProtocol(e.getMessage());
        }


    }

    private static void handleSpecificProtocol(Protocol.AuthRequest protocol, Connection conn) {
        Protocol.AuthResponse response = new Protocol.AuthResponse();
        String identity = protocol.authIdentity.identity;

        try {
            response.authKey.key = secManager.encryptAESWithRSA(identity);
            response.response.status = true;
            response.response.msg = Constants.PROTOCOL_RESPONSE_MESSAGE_PUBLIC_KEY_FOUND;
            conn.send(ProtocolFactory.marshalProtocol(response));

        } catch (Exception e) {
            response.response.status = false;
            response.response.msg = Constants.PROTOCOL_RESPONSE_MESSAGE_PUBLIC_KEY_NOT_FOUND;
            conn.send(ProtocolFactory.marshalProtocol(response));
        }
    }

    private static void handleSpecificProtocol(Protocol.DisconnectPeerRequest protocol, Connection conn) {
        Protocol.DisconnectPeerResponse response = new Protocol.DisconnectPeerResponse();
        response.hostPort.host = protocol.hostPort.host;
        response.hostPort.port = protocol.hostPort.port;
        response.response.status = true;
        response.response.msg = Constants.PROTOCOL_RESPONSE_MESSAGE_DISCONNECT_PEER;
        conn.send(ProtocolFactory.marshalProtocol(response));
    }

    private static void handleSpecificProtocol(Protocol.ConnectPeerRequest protocol, Connection conn) {
        Protocol.ConnectPeerResponse response = new Protocol.ConnectPeerResponse();
        response.hostPort.host = protocol.hostPort.host;
        response.hostPort.port = protocol.hostPort.port;
        response.response.status = true;
        response.response.msg = Constants.PROTOCOL_RESPONSE_MESSAGE_CONNECT_PEER;
        conn.send(ProtocolFactory.marshalProtocol(response));
    }

    private static void handleSpecificProtocol(Protocol.ListPeersRequest protocol, Connection conn) {
        Protocol.ListPeersResponse response = new Protocol.ListPeersResponse();

    }
}

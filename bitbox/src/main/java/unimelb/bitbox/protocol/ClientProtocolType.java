package unimelb.bitbox.protocol;

import unimelb.bitbox.Constants;


/**
 * ClientProtocolType contains all the client protocols class type
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public enum ClientProtocolType {
    AUTH_REQUEST(Constants.PROTOCOL_TYPE_AUTH_REQUEST, ClientProtocol.AuthRequest.class, false),
    AUTH_RESPONSE(Constants.PROTOCOL_TYPE_AUTH_RESPONSE, ClientProtocol.AuthResponse.class, false),
    LIST_PEERS_REQUEST(Constants.PROTOCOL_TYPE_LIST_PEERS_REQUEST, ClientProtocol.ListPeersRequest.class, true),
    LIST_PEERS_RESPONSE(Constants.PROTOCOL_TYPE_LIST_PEERS_RESPONSE, ClientProtocol.ListPeersResponse.class, true),
    CONNECT_PEER_REQUEST(Constants.PROTOCOL_TYPE_CONNECT_PEER_REQUEST, ClientProtocol.ConnectPeerRequest.class, true),
    CONNECT_PEER_RESPONSE(Constants.PROTOCOL_TYPE_CONNECT_PEER_RESPONSE, ClientProtocol.ConnectPeerResponse.class, true),
    DISCONNECT_PEER_REQUEST(Constants.PROTOCOL_TYPE_DISCONNECT_PEER_REQUEST, ClientProtocol.DisconnectPeerRequest.class, true),
    DISCONNECT_PEER_RESPONSE(Constants.PROTOCOL_TYPE_DISCONNECT_PEER_RESPONSE, ClientProtocol.DisconnectPeerResponse.class, true);


    private final String key;
    private final Class value;
    private final boolean needEncryption;


    ClientProtocolType(String key, Class value, boolean needEncryption) {
        this.key = key;
        this.value = value;
        this.needEncryption = needEncryption;
    }


    public String getKey() {
        return key;
    }


    public Class getValue() {
        return value;
    }

    public boolean isNeedEncryption() {
        return needEncryption;
    }

    /**
     * Get the protocol type of the given command
     *
     * @param command the command string to match
     * @return a corresponding protocol type
     * @throws InvalidProtocolException the command does not match any of the protocol in the system
     */
    public static ClientProtocolType typeOfCommand(String command) throws InvalidProtocolException {
        for (ClientProtocolType e : values()) {
            if (e.key.equals(command)) {
                return e;
            }
        }
        throw new InvalidProtocolException("Unknown command: " + command, null);
    }


    /**
     * Get the protocol type from a protocol
     *
     * @param protocol a protocol
     * @return protocol type
     */
    public static ClientProtocolType typeOfProtocol(ClientProtocol protocol) {
        for (ClientProtocolType e : values()) {
            if (e.value == protocol.getClass()) {
                return e;
            }
        }
        // shouldn't happen
        throw new RuntimeException("Unknown protocol class");
    }
}


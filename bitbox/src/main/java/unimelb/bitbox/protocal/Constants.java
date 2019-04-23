package unimelb.bitbox.protocal;

class Constants {

    static final String CMD = "command";
    static final String MSG = "message";
    static final String PEER = "peers";
    static final String HOST = "host";
    static final String PORT = "port";
    static final String HOST_PORT = "hostPort";


    enum ProtocolType{
        INVALID_PROTOCOL("INVALID_PROTOCOL", Protocol.InvalidProtocol.class),
        CONNECTION_REFUSED("CONNECTION_REFUSED", Protocol.ConnectionRefused.class),
        HANDSHAKE_REQUEST("HANDSHAKE_REQUEST", Protocol.HandshakeRequest.class),
        HANDSHAKE_RESPONSE("HANDSHAKE_RESPONSE", Protocol.HandshakeResponse.class);

        private final String key;
        private final Class value;

        ProtocolType(String key, Class value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }
        public Class getValue() {
            return value;
        }

        public static ProtocolType typeOfCommand(String command) {
            for (ProtocolType e : values()) {
                if (e.key.equals(command)) {
                    return e;
                }
            }
            return null;
        }
    }
}

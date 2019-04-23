package unimelb.bitbox.protocol;

public class ProtocolFactoryDemo {
    public static void main(String[] args) {
//        ProtocolFactory factory = new ProtocolFactory();
        String test1 = "{\n" +
                "    \"command\": \"HANDSHAKE_RESPONSE\",\n" +
                "    \"hostPort\" : {\n" +
                "        \"host\" : \"bigdata.cis.unimelb.edu.au\",\n" +
                "        \"port\" : 8500\n" +
                "    }\n" +
                "}";

        Protocol protocol = ProtocolFactory.parseProtocol(test1);

        // assume we expect HandshakeResponse
        if (ProtocolType.typeOfProtocol(protocol) == ProtocolType.HANDSHAKE_RESPONSE) {
            Protocol.HandshakeResponse response = (Protocol.HandshakeResponse) protocol;
            System.out.println(response.peer.host);

        } else {
            // null or wrong protocol
        }

        // switch
        ProtocolType protocolType = ProtocolType.typeOfProtocol(protocol);
        if (protocolType != null) {
            switch (protocolType) {
                case HANDSHAKE_RESPONSE:
                    break;
                case INVALID_PROTOCOL:
                    break;
                // ......
            }
        }

        System.out.println("--------");

        test1 = "{\"command\": \"FILE_CREATE_RESPONSE\",\"fileDescriptor\": {\"md5\": \"074195d72c47315efae797b69393e5e5\",\"lastModified\": 1553417607000,\"fileSize\": 45787},\"pathName\": \"test.jpg\",\"message\": \"file loader ready\",\"status\": true}";
        protocol = ProtocolFactory.parseProtocol(test1);
        System.out.println(ProtocolFactory.marshalProtocol(protocol));


//        String test2 = "{\n" +
//                "    \"command\": \"CONNECTION_REFUSED\",\n" +
//                "    \"message\": \"connection limit reached\"\n" +
//                "    \"peers\": [\n" +
//                "        {\n" +
//                "            \"host\" : \"sunrise.cis.unimelb.edu.au\",\n" +
//                "            \"port\" : 8111\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"host\" : \"bigdata.cis.unimelb.edu.au\",\n" +
//                "            \"port\" : 8500\n" +
//                "        }\n" +
//                "    ]\n" +
//                "}";
//        String test3 = "{\n" +
//                "    \"command\": \"INVALID_PROTOCOL\",\n" +
//                "    \"message\": \"message must contain a command field as string\"\n" +
//                "}";
//         Document doc = Document.parse(test3);
//         String command = doc.getString(CMD);
//         ProtocolFactory.InvalidProtocol p1;
//
//         if(command.equals(INVALID_PROTOCOL)){
//             p1 = factory.parseInvalidProtocol(test3);
//             System.out.println(p1.toJson());
//         }else if(command.equals(CONNECTION_REFUSED)){
//             factory.parseConnectionRefused(test3);
//         }else if(command.equals(HANDSHAKE_REQUEST) ||
//                 command.equals(HANDSHAKE_RESPONSE)){
//             factory.parseHandshakeReqRes(test3);
//         }
    }
}

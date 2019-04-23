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
        System.out.println("FILE_CREATE_RESPONSE: " + ProtocolFactory.marshalProtocol(protocol));

        System.out.println("--------");
        test1 = "{\n" +
                "    \"command\": \"FILE_CREATE_REQUEST\",\n" +
                "    \"fileDescriptor\" : {\n" +
                "        \"md5\" : \"074195d72c47315efae797b69393e5e5\",\n" +
                "        \"lastModified\" : 1553417607000,\n" +
                "        \"fileSize\" : 45787\n" +
                "    },\n" +
                "    \"pathName\" : \"test.jpg\"\n" +
                "}";
        protocol = ProtocolFactory.parseProtocol(test1);
        System.out.println("FILE_CREATE_REQUEST: " + ProtocolFactory.marshalProtocol(protocol));

        System.out.println("--------");
        test1 = "{\n" +
                "    \"command\": \"FILE_BYTES_REQUEST\",\n" +
                "    \"fileDescriptor\" : {\n" +
                "        \"md5\" : \"b1946ac92492d2347c6235b4d2611184\",\n" +
                "        \"lastModified\" : 1553417607000,\n" +
                "        \"fileSize\" : 6\n" +
                "    },\n" +
                "    \"pathName\" : \"hello.txt\",\n" +
                "    \"position\" : 0,\n" +
                "    \"length\" : 6\n" +
                "}";
        protocol = ProtocolFactory.parseProtocol(test1);
        System.out.println("FILE_BYTES_REQUEST: " + ProtocolFactory.marshalProtocol(protocol));

        System.out.println("--------");
        test1 = "{\n" +
                "    \"command\": \"FILE_BYTES_RESPONSE\",\n" +
                "    \"fileDescriptor\" : {\n" +
                "        \"md5\" : \"b1946ac92492d2347c6235b4d2611184\",\n" +
                "        \"lastModified\" : 1553417607000,\n" +
                "        \"fileSize\" : 6\n" +
                "    },\n" +
                "    \"pathName\" : \"hello.txt\",\n" +
                "    \"position\" : 0,\n" +
                "    \"length\" : 6,\n" +
                "    \"content\" : \"aGVsbG8K\"\n" +
                "    \"message\" : \"successful read\",\n" +
                "    \"status\" : true\n" +
                "\n" +
                "}";
        protocol = ProtocolFactory.parseProtocol(test1);
        System.out.println("FILE_BYTES_RESPONSE: " + ProtocolFactory.marshalProtocol(protocol));

        System.out.println("--------");
        test1 = "{\n" +
                "    \"command\": \"FILE_DELETE_REQUEST\",\n" +
                "    \"fileDescriptor\" : {\n" +
                "        \"md5\" : \"074195d72c47315efae797b69393e5e5\",\n" +
                "        \"lastModified\" : 1553417607000,\n" +
                "        \"fileSize\" : 45787\n" +
                "    },\n" +
                "    \"pathName\" : \"test.jpg\"\n" +
                "}";
        protocol = ProtocolFactory.parseProtocol(test1);
        System.out.println("FILE_DELETE_REQUEST: " + ProtocolFactory.marshalProtocol(protocol));

        System.out.println("--------");
        test1 = "{\n" +
                "    \"command\": \"FILE_DELETE_RESPONSE\",\n" +
                "    \"fileDescriptor\" : {\n" +
                "        \"md5\" : \"074195d72c47315efae797b69393e5e5\",\n" +
                "        \"lastModified\" : 1553417607000,\n" +
                "        \"fileSize\" : 45787\n" +
                "    },\n" +
                "    \"pathName\" : \"test.jpg\",\n" +
                "    \"message\" : \"pathname does not exist\",\n" +
                "    \"status\" : false\n" +
                "}";
        protocol = ProtocolFactory.parseProtocol(test1);
        System.out.println("FILE_DELETE_RESPONSE: " + ProtocolFactory.marshalProtocol(protocol));

        System.out.println("--------");
        test1 = "{\n" +
                "    \"command\": \"FILE_MODIFY_REQUEST\",\n" +
                "    \"fileDescriptor\" : {\n" +
                "        \"md5\" : \"d35eab5dd9cb8b0d467c7e742c9b8c4c\",\n" +
                "        \"lastModified\" : 1553417617000,\n" +
                "        \"fileSize\" : 46787\n" +
                "    },\n" +
                "    \"pathName\" : \"test.jpg\"\n" +
                "}";
        protocol = ProtocolFactory.parseProtocol(test1);
        System.out.println("FILE_MODIFY_REQUEST: " + ProtocolFactory.marshalProtocol(protocol));

        System.out.println("--------");
        test1 = "{\n" +
                "    \"command\": \"FILE_MODIFY_RESPONSE\",\n" +
                "    \"fileDescriptor\" : {\n" +
                "        \"md5\" : \"074195d72c47315efae797b69393e5e5\",\n" +
                "        \"lastModified\" : 1553417607000,\n" +
                "        \"fileSize\" : 45787\n" +
                "    },\n" +
                "    \"pathName\" : \"test.jpg\",\n" +
                "    \"message\" : \"file loader ready\",\n" +
                "    \"status\" : true\n" +
                "}";
        protocol = ProtocolFactory.parseProtocol(test1);
        System.out.println("FILE_MODIFY_RESPONSE: " + ProtocolFactory.marshalProtocol(protocol));

        System.out.println("--------");
        test1 = "{\n" +
                "    \"command\": \"DIRECTORY_CREATE_REQUEST\",\n" +
                "    \"pathName\" : \"dir/subdir/etc\"\n" +
                "}";
        protocol = ProtocolFactory.parseProtocol(test1);
        System.out.println("DIRECTORY_CREATE_REQUEST: " + ProtocolFactory.marshalProtocol(protocol));
        System.out.println("--------");

        test1 = "{\n" +
                "    \"command\": \"DIRECTORY_CREATE_RESPONSE\",\n" +
                "    \"pathName\" : \"dir/subdir/etc\",\n" +
                "    \"message\" : \"pathname already exists\",\n" +
                "    \"status\" : false\n" +
                "}";
        protocol = ProtocolFactory.parseProtocol(test1);
        System.out.println("DIRECTORY_CREATE_RESPONSE: " + ProtocolFactory.marshalProtocol(protocol));
        System.out.println("--------");

        test1 = "{\n" +
                "    \"command\": \"DIRECTORY_DELETE_REQUEST\",\n" +
                "    \"pathName\" : \"dir/subdir/etc\"\n" +
                "}";
        protocol = ProtocolFactory.parseProtocol(test1);
        System.out.println("DIRECTORY_DELETE_REQUEST: " + ProtocolFactory.marshalProtocol(protocol));
        System.out.println("--------");

        test1 = "{\n" +
                "    \"command\": \"DIRECTORY_DELETE_RESPONSE\",\n" +
                "    \"pathName\" : \"dir/subdir/etc\",\n" +
                "    \"message\" : \"directory deleted\",\n" +
                "    \"status\" : true\n" +
                "}";
        protocol = ProtocolFactory.parseProtocol(test1);
        System.out.println("DIRECTORY_DELETE_RESPONSE: " + ProtocolFactory.marshalProtocol(protocol));
        System.out.println("--------");


    }
}

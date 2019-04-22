package unimelb.bitbox;

import unimelb.bitbox.util.Document;

import static unimelb.bitbox.util.Constants.*;

public class ProtocalFactoryDemo {
    public static void main(String[] args) {
        ProtocolFactory factory = new ProtocolFactory();
        String test1 = "{\n" +
                "    \"command\": \"HANDSHAKE_RESPONSE\",\n" +
                "    \"hostPort\" : {\n" +
                "        \"host\" : \"bigdata.cis.unimelb.edu.au\",\n" +
                "        \"port\" : 8500\n" +
                "    }\n" +
                "}";
        String test2 = "{\n" +
                "    \"command\": \"CONNECTION_REFUSED\",\n" +
                "    \"message\": \"connection limit reached\"\n" +
                "    \"peers\": [\n" +
                "        {\n" +
                "            \"host\" : \"sunrise.cis.unimelb.edu.au\",\n" +
                "            \"port\" : 8111\n" +
                "        },\n" +
                "        {\n" +
                "            \"host\" : \"bigdata.cis.unimelb.edu.au\",\n" +
                "            \"port\" : 8500\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        String test3 = "{\n" +
                "    \"command\": \"INVALID_PROTOCOL\",\n" +
                "    \"message\": \"message must contain a command field as string\"\n" +
                "}";
         Document doc = Document.parse(test3);
         String command = doc.getString(CMD);
         ProtocolFactory.InvalidProtocol p1;

         if(command.equals(INVALID_PROTOCOL)){
             p1 = factory.parseInvalidProtocol(test3);
             System.out.println(p1.toJson());
         }else if(command.equals(CONNECTION_REFUSED)){
             factory.parseConnectionRefused(test3);
         }else if(command.equals(HANDSHAKE_REQUEST) ||
                 command.equals(HANDSHAKE_RESPONSE)){
             factory.parseHandshakeReqRes(test3);
         }
    }
}

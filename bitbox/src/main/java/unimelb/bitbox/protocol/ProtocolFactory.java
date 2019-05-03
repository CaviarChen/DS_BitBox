package unimelb.bitbox.protocol;


import unimelb.bitbox.util.Document;

import static unimelb.bitbox.Constants.PROTOCOL_FIELD_CMD;


public class ProtocolFactory {

    /**
     * connection related json to each protocol method
     */

    public static Protocol parseProtocol(String json) throws InvalidProtocolException {
        try {

            Document doc = Document.parse(json);
            String command = doc.getString(PROTOCOL_FIELD_CMD);


            ProtocolType protocolType = ProtocolType.typeOfCommand(command);
            Protocol protocol = (Protocol) protocolType.getValue().newInstance();

            protocol.unmarshalFromJson(doc);

            return protocol;
        } catch (Exception e) {
            throw (e instanceof InvalidProtocolException) ? (InvalidProtocolException) e :
                    new InvalidProtocolException("Parse error", e);
        }

    }


    public static String marshalProtocol(Protocol protocol) {
        Document doc = new Document();
        doc.append(PROTOCOL_FIELD_CMD, ProtocolType.typeOfProtocol(protocol).getKey());
        protocol.marshalToJson(doc);
        return doc.toJson();
    }
}





package unimelb.bitbox.protocol;

import unimelb.bitbox.util.Document;

import static unimelb.bitbox.Constants.PROTOCOL_FIELD_CMD;


/**
 * A factory of client protocols.
 * Used to convert from JSON string to corresponding protocol class
 * and from protocol class to JSON string
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class ClientProtocolFactory {

    /**
     * un-marshal parse a JSON string to the corresponding protocol class
     */
    public static ClientProtocol parseProtocol(String json) throws InvalidProtocolException {
        try {

            Document doc = Document.parse(json);
            String command = doc.getString(PROTOCOL_FIELD_CMD);


            ClientProtocolType protocolType = ClientProtocolType.typeOfCommand(command);
            ClientProtocol protocol = (ClientProtocol) protocolType.getValue().newInstance();

            protocol.unmarshalFromJson(doc);

            return protocol;
        } catch (Exception e) {
            throw (e instanceof InvalidProtocolException) ? (InvalidProtocolException) e :
                    new InvalidProtocolException("Parse error", e);
        }

    }


    /**
     * marshal protocol class to JSON string
     * @param protocol
     * @return
     */
    public static String marshalProtocol(ClientProtocol protocol) {
        Document doc = new Document();
        doc.append(PROTOCOL_FIELD_CMD, ClientProtocolType.typeOfProtocol(protocol).getKey());
        protocol.marshalToJson(doc);
        return doc.toJson();
    }
}






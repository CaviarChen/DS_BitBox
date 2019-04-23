package unimelb.bitbox.protocol;

import unimelb.bitbox.util.Document;

import static unimelb.bitbox.protocol.Constants.*;

/**
 *   // How to use factory json to objects
 *
 *   Document doc = Document.parse(json);     // json string you want to parse
 *   String command = doc.getString(CMD);
 *   ProtocolFactory.InvalidProtocol p1;
 *   ProtocolFactory factory = new ProtocolFactory();
 *   if(command.equals(INVALID_PROTOCOL)){
 *       p1 = factory.parseInvalidProtocol(json);
 *   }else if(command.equals(CONNECTION_REFUSED)){
 *       factory.parseConnectionRefused(json); // you can assign this to a ConnectionRefused
 *   }else if(command.equals(HANDSHAKE_REQUEST) ||
 *           command.equals(HANDSHAKE_RESPONSE)){
 *       factory.parseHandshakeReqRes(json);   // you can assign this to a parseHandshakeReqRes
 *   }
 *
 *   //How to convert object to json
 *   System.out.println(p1.toJson());
 *
 *   note: 我不是很清楚如果在factory外面不判断一遍protocol是哪个type怎么才能
 *   知道return的object里面有哪些值
 *   例如我在下面comment掉的代码，假如这是一个Invalid Protocol， factory就直接
 *   parse json string，然后 return 一个Protocol 的object，这样虽然把逻辑放进
 *   了factory，但是你不能access Invalid Protocol里的message的值
 *
 * */
public class ProtocolFactory {

    /**
     *  connection related json to each protocol method
     *  */

    public static Protocol parseProtocol(String json){
        try {
            Document doc = Document.parse(json);
            String command = doc.getString(CMD);

            ProtocolType protocolType = ProtocolType.typeOfCommand(command);
            Protocol protocol =  (Protocol) protocolType.getValue().newInstance();

            protocol.unmarshalFromJson(doc);

            return protocol;
        } catch (Exception e) {
            // TODO: log
            return null;
        }
    }

    public static String marshalProtocol(Protocol protocol) {
        Document doc = new Document();
        doc.append(CMD, ProtocolType.typeOfProtocol(protocol).getKey());
        protocol.marshalToJson(doc);
        return doc.toJson();
    }
}





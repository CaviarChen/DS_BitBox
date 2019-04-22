package unimelb.bitbox;

import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

import java.util.ArrayList;
import java.util.Iterator;

import static unimelb.bitbox.util.Constants.*;

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

//    public Protocol parseProtocol(String json){
//        Document doc = Document.parse(json);
//        String command = doc.getString(CMD);
//        System.out.println("cmd: " + doc.getString(CMD));
//        if(command.equals(INVALID_PROTOCOL)){
//            return new InvalidProtocol(json);
//        }else if(command.equals(CONNECTION_REFUSED)){
//            return new ConnectionRefused(json);
//        }else if(command.equals(HANDSHAKE_REQUEST) ||
//                command.equals(HANDSHAKE_RESPONSE)){
//            return new HandshakeReqRes(json);
//        }else{
//            return null;
//        }
//    }

    public ConnectionRefused parseConnectionRefused(String json){
        return new ConnectionRefused(json);
    }

    public InvalidProtocol parseInvalidProtocol(String json){
        return new InvalidProtocol(json);
    }
    public HandshakeReqRes parseHandshakeReqRes(String json){
        return new HandshakeReqRes(json);
    }

    /**
     *  connection related protocols classes
     *  */

    public class Protocol{
        public Document doc;
        public Protocol(String json){
            this.doc = Document.parse(json);
        }
        public String toJson(){
            return doc.toJson();
        }
    }

    public class InvalidProtocol extends Protocol{
        public String msge; //message
        public InvalidProtocol(String json){
            super(json);
            this.msge = this.doc.getString(MES);
        }

        @Override
        public String toString() {
            return "InvalidProtocol{" +
                    "msge='" + msge + '\'' +
                    '}';
        }
    }

    public class ConnectionRefused extends Protocol{
        public String msge;               // message
        public ArrayList<HostPort> peers; // list of peers
        public ConnectionRefused(String json){
            super(json);
            ArrayList<HostPort> peersList = new ArrayList<HostPort>();
            ArrayList<Document> peers = (ArrayList<Document>) this.doc.get(PEER);
            Iterator<Document> iterator = peers.iterator();
            while (iterator.hasNext()) {
                peersList.add(new HostPort(
                        iterator.next().getString(HOST),
                        (int)iterator.next().getLong(PORT))
                );
            }
            this.msge = this.doc.getString(MES);
            this.peers = peersList;
        }

        @Override
        public String toString() {
            String results = "+";
            for(HostPort d : peers) {
                results += d.toString();
            }
            return "ConnectionRefused{" +
                    "msge='" + msge + '\'' +
                    ", peers=" + results +
                    '}';
        }
    }

    /**
     * HandshakeRequest/HandshakeResponse
     * */
    public class HandshakeReqRes extends Protocol{
        public HostPort peer;
        public HandshakeReqRes(String json){
            super(json);
            Document tmp = (Document)this.doc.get(HOST_PORT);
            this.peer = new HostPort(
                    tmp.getString(HOST),
                    (int)tmp.getLong(PORT));
        }

        @Override
        public String toString() {
            return "HandshakeReqRes{" +
                    "peer=" + peer.toString() +
                    '}';
        }
    }

    /**
     *  file related protocols
     *  */
    public class FileDes{
        String md5;
        long lastModified;
        long fileSize;
        String path;         // pathName

        public FileDes(String md5, long lastModified, long fileSize, String path ){
            this.md5 = md5;
            this.lastModified = lastModified;
            this.fileSize = fileSize;
            this.path = path;
        }
    }

    public class FileRange{
        long pos;            // position
        long len;            // length

        public FileRange(long pos, long len){
            this.pos = pos;
            this.len = len;
        }
    }

    public class Response{
        String msge;         // message
        Boolean status;      // status

        public Response(String msge, Boolean status){
            this.msge = msge;
            this.status = status;
        }
    }


    public class FileBytesResponse{
        FileDes fileDes;
        FileRange fileRange;
        Response res;
        String content;
        public FileBytesResponse(String path,
                                 String md5,
                                 long lastModified,
                                 long fileSize,
                                 String msge,
                                 Boolean status,
                                 long pos,
                                 long len,
                                 String content){
            this.fileDes = new FileDes(md5,lastModified,fileSize,path);
            this.fileRange = new FileRange(pos,len);
            this.res = new Response(msge,status);
            this.content = content;
        }
    }


}





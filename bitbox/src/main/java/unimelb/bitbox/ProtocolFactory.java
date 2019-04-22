package unimelb.bitbox;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class ProtocolFactory {

    public static String MES = "message";
    public static String PEER = "peers";
    public static String HOST = "host";
    public static String PORT = "port";
    public static String HOST_PORT = "hostPort";

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
    /**
     *  connection related json to protocol classes
     *  */

    private JSONObject str2json(String str){
        JSONParser parser = new JSONParser();
        try{
            JSONObject jsonObj = (JSONObject) parser.parse(str);
            return jsonObj;
        } catch (Exception e){
            return null;
        }
    }

    public InvalidProtocal parseInvalidProtocal(String json){
        JSONObject jsonObj = str2json(json);
        if(jsonObj.isEmpty() || !jsonObj.containsKey(MES) ) {
            // TODO: handle failed parsing, give a response say invalid request
            return null;
        }else{
            return new InvalidProtocal(jsonObj.get(MES).toString());
        }
    }

    public ConnectionRefused parseConnectionRefuesd(String json){
        JSONObject jsonObj = str2json(json);
        if(jsonObj.isEmpty() || !jsonObj.containsKey(MES) || !jsonObj.containsKey(PEER)){
            // TODO: handle failed parsing, give a response say invalid request
            return null;
        }else{
            ArrayList<Peer> peersList = new ArrayList<Peer>();
            JSONArray peers = (JSONArray)jsonObj.get(PEER);
            Iterator<JSONObject> iterator = peers.iterator();
            while (iterator.hasNext()) {
                peersList.add(new Peer(
                        iterator.next().get(HOST).toString(),
                        Integer.parseInt(iterator.next().get(PORT).toString())
                        )
                );
            }
            return new ConnectionRefused(jsonObj.get(MES).toString(),peersList);
        }
    }

    public HandshakeReqRes parseHandshakeReqRes(String json){
        JSONObject jsonObj = str2json(json);
        if(jsonObj.isEmpty() || !jsonObj.containsKey(HOST_PORT) ) {
            // TODO: handle failed parsing, give a response say invalid request
            return null;
        }else{
            JSONObject tmp = (JSONObject)jsonObj.get(HOST_PORT);
            return new HandshakeReqRes(
                    new Peer(
                            tmp.get(HOST).toString(),
                            Integer.parseInt(tmp.get(PORT).toString())
                    )
            );
        }
    }

    /**
     *  connection related protocols classes
     *  */

    public class Peer{
        String host;  // host
        Integer port; // port
        public Peer(String host, Integer port){
            this.host = host;
            this.port = port;
        }
    }

    public class InvalidProtocal{
        String msge; //message
        public InvalidProtocal(String msge){
            this.msge = msge;
        }
    }

    public class ConnectionRefused{
        String msge;           // message
        ArrayList<Peer> peers; // list of peers
        public ConnectionRefused(String msge, ArrayList<Peer> peers){
            this.msge = msge;
            this.peers = peers;
        }
    }

    /**
     * HandshakeRequest/HandshakeResponse
     * */
    public class HandshakeReqRes{
        Peer peer;
        public HandshakeReqRes(Peer peer){
            this.peer = peer;
        }
    }

}





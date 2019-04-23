package unimelb.bitbox.protocal;

import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

import java.util.ArrayList;

import static unimelb.bitbox.protocal.Constants.*;

public abstract class Protocol implements IProtocol {

    @Override
    public void unmarshalFromJson(Document doc) {
        // use reflection to fill ProtocolField
    }

    @Override
    public void marshalToJson(Document doc) {
        // use reflection to fill ProtocolField
    }


    public static class InvalidProtocol extends Protocol {
        public String msg; //message

        public InvalidProtocol() {}

        @Override
        public void unmarshalFromJson(Document doc) {
            super.unmarshalFromJson(doc);
            this.msg = doc.getString(MSG);
        }

        @Override
        public void marshalToJson(Document doc) {
            super.marshalToJson(doc);
            doc.append(MSG, msg);
        }

    }


    public static class ConnectionRefused extends Protocol {
        public String msg;                // message
        public ArrayList<HostPort> peers = new ArrayList<>(); // list of peers

        public ConnectionRefused() {}

        @Override
        public void unmarshalFromJson(Document doc) {
            super.unmarshalFromJson(doc);
            for (Document subdoc: (ArrayList<Document>) doc.get(PEER)) {
                peers.add(new HostPort(subdoc));
            }
            this.msg = doc.getString(MSG);
        }

        @Override
        public void marshalToJson(Document doc) {
            super.marshalToJson(doc);

            ArrayList<Document> peerDocs = new ArrayList<>();
            for(HostPort hostPort: peers) {
                peerDocs.add(hostPort.toDoc());
            }
            doc.append(PEER, peerDocs);
            doc.append(MSG, msg);
        }
    }


    public static class HandshakeRequest extends Protocol {
        public HostPort peer;

        public HandshakeRequest() {}

        @Override
        public void unmarshalFromJson(Document doc) {
            super.unmarshalFromJson(doc);

            this.peer = new HostPort((Document) doc.get(HOST_PORT));
        }

        @Override
        public void marshalToJson(Document doc) {
            super.marshalToJson(doc);
            doc.append(HOST_PORT, this.peer.toDoc());
        }
    }

    public static class HandshakeResponse extends HandshakeRequest {
        public HandshakeResponse() {}
    }
}

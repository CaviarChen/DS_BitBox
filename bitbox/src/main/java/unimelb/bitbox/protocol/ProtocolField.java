package unimelb.bitbox.protocol;

import unimelb.bitbox.util.Document;

public abstract class ProtocolField implements IProtocol{

//  subclass  ------------------------------------

    public static class FileDes extends ProtocolField{
        String md5;
        long lastModified;
        long fileSize;
        String path;         // pathName

        @Override
        public void unmarshalFromJson(Document doc) {
            Document subDoc = (Document) doc.get("fileDescriptor");
            this.md5 = subDoc.getString("md5");
            this.lastModified = subDoc.getLong("lastModified");
            this.fileSize = subDoc.getLong("fileSize");
            this.path = doc.getString("pathName");
        }

        @Override
        public void marshalToJson(Document doc) {
            Document subDoc = new Document();
            subDoc.append("md5", this.md5);
            subDoc.append("lastModified", this.lastModified);
            subDoc.append("fileSize", this.fileSize);
            doc.append("fileDescriptor", subDoc);
            doc.append("pathName", this.path);
        }
    }

    public static class Response extends ProtocolField{
        String msg;         // message
        Boolean status;      // status

        @Override
        public void unmarshalFromJson(Document doc) {
            this.msg = doc.getString("message");
            this.status = doc.getBoolean("status");
        }

        @Override
        public void marshalToJson(Document doc) {
            doc.append("message", this.msg);
            doc.append("status", this.status);
        }
    }

}

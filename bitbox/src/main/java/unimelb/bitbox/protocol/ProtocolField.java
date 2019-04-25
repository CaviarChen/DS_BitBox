package unimelb.bitbox.protocol;

import unimelb.bitbox.util.Document;
import static unimelb.bitbox.protocol.Constants.*;

public abstract class ProtocolField implements IProtocol{

//  subclass  ------------------------------------

    public static class Path extends ProtocolField {
        public String path;         // pathName

        @Override
        public void unmarshalFromJson(Document doc) {
            this.path = doc.getString(PATH_NAME);
        }

        @Override
        public void marshalToJson(Document doc) {
            doc.append(PATH_NAME, this.path);
        }
    }


    public static class FileDes extends Path{
        public String md5;
        public long lastModified;
        public long fileSize;

        @Override
        public void unmarshalFromJson(Document doc) {
            super.unmarshalFromJson(doc);
            Document subDoc = (Document) doc.get(FILE_DES);
            this.md5 = subDoc.getString(MD5);
            this.lastModified = subDoc.getLong(LAST_MODIFIED);
            this.fileSize = subDoc.getLong(FILE_SIZE);
            this.path = doc.getString(PATH_NAME);
        }

        @Override
        public void marshalToJson(Document doc) {
            super.marshalToJson(doc);
            Document subDoc = new Document();
            subDoc.append(MD5, this.md5);
            subDoc.append(LAST_MODIFIED, this.lastModified);
            subDoc.append(FILE_SIZE, this.fileSize);
            doc.append(FILE_DES, subDoc);
        }
    }

    public static class Response extends ProtocolField{
        public String msg;         // message
        public Boolean status;      // status

        @Override
        public void unmarshalFromJson(Document doc) {
            this.msg = doc.getString(MSG);
            this.status = doc.getBoolean(STATUS);
        }

        @Override
        public void marshalToJson(Document doc) {
            doc.append(MSG, this.msg);
            doc.append(STATUS, this.status);
        }
    }

    public static class FilePosition extends ProtocolField{
        public long pos;         // position
        public long len;         // length

        @Override
        public void unmarshalFromJson(Document doc) {
            this.pos = doc.getLong(POSITION);
            this.len = doc.getLong(LEN);
        }

        @Override
        public void marshalToJson(Document doc) {
            doc.append(POSITION, this.pos);
            doc.append(LEN, this.len);
        }
    }

    public static class FileContent extends FilePosition{
        public String content;      // content

        @Override
        public void unmarshalFromJson(Document doc) {
            super.unmarshalFromJson(doc);
            this.content = doc.getString(CONTENT);
        }

        @Override
        public void marshalToJson(Document doc) {
            super.marshalToJson(doc);
            doc.append(CONTENT, this.content);
        }
    }

}

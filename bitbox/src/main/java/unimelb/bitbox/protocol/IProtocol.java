package unimelb.bitbox.protocol;


import unimelb.bitbox.util.Document;


public interface IProtocol {
    void unmarshalFromJson(Document doc);


    void marshalToJson(Document doc);
}

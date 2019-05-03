package unimelb.bitbox.protocol;


import unimelb.bitbox.util.Document;


/**
 * Top-level interface for protocols.
 *
 * @author Wenqing Xue (813044)
 * @author Weizhi Xu (752454)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public interface IProtocol {
    /**
     * Un-marshal the given {@link Document} contains information from JSON to class representation
     *
     * @param doc the document wanted to be un-marshalled
     */
    void unmarshalFromJson(Document doc);


    /**
     * Marshal a Protocol class to a {@link Document}
     *
     * @param doc the document wanted to be marshalled
     */
    void marshalToJson(Document doc);
}

package unimelb.bitbox.protocol;


/**
 * Exception Wrapper for Invalid Protocol
 * so that the error messages are meaningful
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class InvalidProtocolException extends Exception {
    public InvalidProtocolException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}

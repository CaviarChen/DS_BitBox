package unimelb.bitbox.protocol;


public class InvalidProtocolException extends Exception {
    public InvalidProtocolException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}

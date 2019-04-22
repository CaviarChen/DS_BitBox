package unimelb.bitbox;

public class PeerInfo {

    private int port;
    private String host;
    private String status;

    public PeerInfo(String host, int port) {
        this.host = host;
        this.port = port;
        this.status = "";
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
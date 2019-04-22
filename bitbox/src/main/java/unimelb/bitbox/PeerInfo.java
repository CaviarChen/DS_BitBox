package unimelb.bitbox;

public class PeerInfo {

    private int port;
    private long time;
    private String host;

    public PeerInfo(String host, int port) {
        this.host = host;
        this.port = port;
        this.time = System.currentTimeMillis();
    }

    public int getPort() {
        return port;
    }

    public long getTime() { return time; }

    public void setTime() {
        this.time = System.currentTimeMillis();
    }

    public String getHost() {
        return host;
    }

}
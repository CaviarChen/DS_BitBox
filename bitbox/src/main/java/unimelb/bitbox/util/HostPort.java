package unimelb.bitbox.util;

import java.util.Objects;

import com.sun.tools.javac.code.Attribute;
import unimelb.bitbox.Constants;


/**
 * Simple class to manage a host string and port number. Provides conversion to and from a {@link Document}
 * which further provides conversion to a JSON string.
 *
 * @author aaron
 *
 * Modified.
 */
public class HostPort {
    public String host;
    public int port;

    public HostPort() {}

    public HostPort(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public HostPort(String hostPort) {
        this.host = hostPort.split(Constants.CONFIG_HOSTNAME_PORT_SEPARATOR)[0];
        this.port = Integer.parseInt(hostPort.split(Constants.CONFIG_HOSTNAME_PORT_SEPARATOR)[1]);
    }

    public HostPort(Document hostPort) {
        this.host = hostPort.getString(Constants.PROTOCOL_FIELD_HOST);
        this.port = (int) hostPort.getLong(Constants.PROTOCOL_FIELD_PORT);
    }

    public Document toDoc() {
        Document hp = new Document();
        hp.append(Constants.PROTOCOL_FIELD_HOST, host);
        hp.append(Constants.PROTOCOL_FIELD_PORT, port);
        return hp;
    }

    public String toString() {
        return host + Constants.CONFIG_HOSTNAME_PORT_SEPARATOR + port;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof HostPort)) {
            return false;
        }
        HostPort c = (HostPort) o;
        return host.equals(c.host) && port == c.port;
    }

    // for hashmap
    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }
}

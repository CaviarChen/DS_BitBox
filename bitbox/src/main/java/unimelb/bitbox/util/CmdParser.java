package unimelb.bitbox.util;


import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import unimelb.bitbox.Constants;

import java.io.IOException;
import java.util.logging.Logger;


/**
 * Class for command line parser
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class CmdParser {
    private static Logger log = Logger.getLogger(CmdParser.class.getName());

    private String[] args;

    @Option(required = true, name = "-i", usage = "The identity of the client")
    private String identity;

    @Option(required = true, name = "-c", usage = "Set the cmd [list_peers, connect_peer, disconnect_peer]")
    private String cmd;

    @Option(required = true, name = "-s", usage = "Set the server's Host and IP")
    private String server;

    @Option(name = "-p", usage = "Set the peer's Host and IP")
    private String peer;


    public CmdParser(String[] args) {
        this.args = args;
    }


    public void parse() throws IOException {
        CmdLineParser cmdLineParser = new CmdLineParser(this);
        try {
            cmdLineParser.parseArgument(args);

            switch (cmd) {
                case Constants.CLIENT_CMD_LIST_PEERS:
                    break;
                case Constants.CLIENT_CMD_CONNECT_PEER:
                case Constants.CLIENT_CMD_DISCONNECT_PEER:
                    if (peer != null && server != null) {
                        if (peer.split(Constants.CONFIG_HOSTNAME_PORT_SEPARATOR).length == 2 &&
                            server.split(Constants.CONFIG_HOSTNAME_PORT_SEPARATOR).length == 2) {
                            break;
                        }
                    }
                    throw new CmdLineException(cmdLineParser, "Missing peer host post", null);
                default:
                    throw new CmdLineException(cmdLineParser, "Command does not exist", null);
            }
        } catch (CmdLineException e) {
            log.severe(e.toString());
            cmdLineParser.printUsage(System.err);
            System.exit(1);
        }
    }


    public String getCmd() {
        return cmd;
    }


    public String getServer() {
        return server;
    }


    public String getPeer() {
        return peer;
    }


    public String getIdentity() {
        return identity;
    }


    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("CmdParser:");
        stringBuffer.append("\n\tcmd: " + cmd);
        stringBuffer.append("\n\tserver: " + server);
        stringBuffer.append("\n\tpeer: " + peer);
        stringBuffer.append("\n");
        return stringBuffer.toString();
    }
}

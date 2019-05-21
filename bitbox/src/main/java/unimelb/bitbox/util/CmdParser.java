package unimelb.bitbox.util;


import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.logging.Logger;


public class CmdParser {
    private static Logger log = Logger.getLogger(CmdParser.class.getName());

    private String[] args;

    @Option(required = true, name = "-c", usage = "Set the cmd [list_peers, connect_peer, disconnect_peer]")
    private String cmd;

    @Option(name = "-s", usage = "Set the server's Host and IP")
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
        } catch (CmdLineException e) {
            log.severe(e.toString());
            cmdLineParser.printUsage(System.err);
            throw new IOException(e);
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

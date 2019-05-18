package unimelb.bitbox;


import unimelb.bitbox.util.CmdParser;
import unimelb.bitbox.util.SecManager;


public class Client {

    public static void main(String[] args) throws Exception {
        // parse command line args
        CmdParser cmdParser = new CmdParser(args);
        cmdParser.parse();

        SecManager.getInstance().init(SecManager.Mode.ClientMode);
        // establish connection with the peer using TCP

    }
}

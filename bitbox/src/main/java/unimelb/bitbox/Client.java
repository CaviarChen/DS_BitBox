package unimelb.bitbox;


import unimelb.bitbox.util.CmdParser;

import java.io.IOException;
import java.util.logging.Logger;


public class Client {

    public static void main(String[] args) {
        // parse command line args
        CmdParser cmdParser = new CmdParser(args);
        try {
            cmdParser.parse();
        } catch (IOException e) {
        }

        // establish connection with the peer using TCP
    }
}

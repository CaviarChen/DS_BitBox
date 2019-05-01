package unimelb.bitbox.protocol;


import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


public class InvalidProtocolExceptionTest {

    @Test
    public void testInvalidProtocolException() {
        try {
            ProtocolFactory.parseProtocol("invalid");
            fail("Expected an InvalidProtocolException to be thrown");

        } catch (InvalidProtocolException e) {
//            System.out.println(e.getMessage());
            assertThat(e.getMessage(), is("Unknown command: null"));
        }
    }

    @Test
    public void testInvalidProtocolException2() {
        try {
            ProtocolFactory.parseProtocol("{\"command\":\"HANDSHAKE_RESPONSE\"}");
            fail("Expected an InvalidProtocolException to be thrown");

        } catch (InvalidProtocolException e) {
//            System.out.println(e.getMessage());
            assertThat(e.getMessage(), is("Parse error"));
        }
    }
}


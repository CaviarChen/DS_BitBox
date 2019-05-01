package unimelb.bitbox.protocol;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.skyscreamer.jsonassert.JSONAssert;


@RunWith(PowerMockRunner.class)
@PrepareForTest(ProtocolFactory.class)
public class ProtocolFactoryTest {

    ProtocolFactory protocolFactory = new ProtocolFactory();


    @Test
    public void testMarshalProtocol() {
        String expected = "{" +
                "command:\"HANDSHAKE_RESPONSE\"," +
                "hostPort:{" +
                "host:\"bigdata.cis.unimelb.edu.au\"," +
                "port:8500" +
                "}" +
                "}";

        Protocol.HandshakeResponse handshakeResponse = new Protocol.HandshakeResponse();
        handshakeResponse.peer.host = "bigdata.cis.unimelb.edu.au";
        handshakeResponse.peer.port = 8500;

        String actual = ProtocolFactory.marshalProtocol(handshakeResponse);

        try {
            JSONAssert.assertEquals(expected, actual, false);
        } catch (JSONException e) {
            Assert.fail("Failed to parse JSON");
        }
    }

    @Test
    public void testParseProtocol() throws InvalidProtocolException {

//        // mock all the static methods in a class
//        PowerMockito.mockStatic(ProtocolFactory.class);
//
//        // use Mockito to set up your expectation
//        Mockito.when(ProtocolFactory.parseProtocol(json)).thenReturn(protocol);
//
//        Assert.assertEquals(ProtocolFactory.parseProtocol(json),protocol);
    }

}


package unimelb.bitbox.protocol;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ProtocolFactory.class)
public class ProtocolFactoryTest {

    public final String json = "{\n" +
            "    \"command\": \"HANDSHAKE_RESPONSE\",\n" +
            "    \"hostPort\" : {\n" +
            "        \"host\" : \"bigdata.cis.unimelb.edu.au\",\n" +
            "        \"port\" : 8500\n" +
            "    }\n" +
            "}";
    public Protocol.HandshakeResponse protocol = new Protocol.HandshakeResponse();

    @Test
    public void testMarshalProtocol() {

        // mock all the static methods in a class
        PowerMockito.mockStatic(ProtocolFactory.class);

        // use Mockito to set up your expectation
        Mockito.when(ProtocolFactory.marshalProtocol(protocol)).thenReturn(json);

        Assert.assertEquals(ProtocolFactory.marshalProtocol(protocol),json);
    }

    @Test
    public void testParseProtocol() throws InvalidProtocolException {

        // mock all the static methods in a class
        PowerMockito.mockStatic(ProtocolFactory.class);

        // use Mockito to set up your expectation
        Mockito.when(ProtocolFactory.parseProtocol(json)).thenReturn(protocol);

        Assert.assertEquals(ProtocolFactory.parseProtocol(json),protocol);
    }

}


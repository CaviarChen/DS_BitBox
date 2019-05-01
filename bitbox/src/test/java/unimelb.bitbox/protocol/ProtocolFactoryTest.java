package unimelb.bitbox.protocol;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import unimelb.bitbox.util.Document;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
@PrepareForTest(ProtocolFactory.class)
public class ProtocolFactoryTest {

    String expected;
    Protocol.HandshakeResponse handshakeResponse;

    @Before
    public void initialize() {
        this.expected = "{" +
                "\"command\":\"HANDSHAKE_RESPONSE\"," +
                "\"hostPort\":{" +
                "\"host\":\"bigdata.cis.unimelb.edu.au\"," +
                "\"port\":8500" +
                "}" +
                "}";
        this.handshakeResponse = new Protocol.HandshakeResponse();
        this.handshakeResponse.unmarshalFromJson(Document.parse(this.expected));
    }

    @Test
    public void testMarshalProtocol() {

        String actual = ProtocolFactory.marshalProtocol(this.handshakeResponse);

        try {
            JSONAssert.assertEquals(this.expected, actual, false);
        } catch (JSONException e) {
            Assert.fail("Failed to parse JSON");
        }
    }


    @Test
    public void testParseProtocol() throws InvalidProtocolException {

        Protocol actual = ProtocolFactory.parseProtocol(this.expected);
        Protocol.HandshakeResponse actualProtocol = (Protocol.HandshakeResponse)actual;

        try {
            assertEquals(this.handshakeResponse.peer, actualProtocol.peer);
        } catch (Exception e) {
            Assert.fail("Failed to parse Protocol");
        }
    }

}


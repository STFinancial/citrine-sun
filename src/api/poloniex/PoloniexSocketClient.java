package api.poloniex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Timothy on 8/11/17.
 */
public class PoloniexSocketClient extends WebSocketClient {
    private static final URI SOCKET_URI = constructURI("wss://api2.poloniex.com:443");
    private final PoloniexData data;
    private JsonNode subPayload;
    private final ObjectMapper mapper = new ObjectMapper();

    // TODO(stfinancial): Remove throws here.
    PoloniexSocketClient(JsonNode subPayload, PoloniexData data) throws IOException {
        super(SOCKET_URI);
        setSocket(SSLSocketFactory.getDefault().createSocket(SOCKET_URI.getHost(), 443));
        connect();
        System.out.println(subPayload.toString());
        this.subPayload = subPayload;
        this.data = data;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Open");
        System.out.println(subPayload.toString());
        send(subPayload.toString());
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Message: " + message);
        JsonNode json;
        try {
            json = mapper.readTree(message);
            System.out.println(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return;
        }
        int messageType = json.get(0).asInt();
        // TODO(stfinancial): Throw out first message of a channel.
        switch (messageType) {
            case 1002:
                // Ticker
                break;
            default:
                // Trollbox or heartbeat
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Closed for reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("Error");
    }

    private static URI constructURI(String uri) {
        try {
            return new URI("wss://api2.poloniex.com:443");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return null;
        }
    }
}
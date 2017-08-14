package api.bitfinex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;

/**
 * Created by Timothy on 8/10/17.
 */
public class BitfinexSocketClient extends WebSocketClient {
    private JsonNode subPayload;
    private final ObjectMapper mapper = new ObjectMapper();

    public BitfinexSocketClient(URI serverUri, JsonNode subPayload) throws IOException {
        super(serverUri);
        setSocket(SSLSocketFactory.getDefault().createSocket(serverUri.getHost(), 443));
        connect();
        System.out.println(subPayload.toString());
        this.subPayload = subPayload;
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
        try {
            System.out.println(mapper.readTree(message));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
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
}

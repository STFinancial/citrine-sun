package api.poloniex;

import api.*;
import api.QueueStrategy;
import api.request.AssetPairRequest;
import api.request.AssetPairResponse;
import api.request.MarketResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;

// TODO(stfinancial): Possible alternative way of defining this. Tradable, Lendable, etc. interfaces.

// TODO(stfinancial): Consider factory for this. Depedency injection etc. etc.

/**
 * Class representing the Poloniex {@code Market}.
 */
public final class Poloniex extends Market {
    // TODO(stfinancial): Look into cancelTriggerOrder and variants for getting those.
    // TODO(stfinancial): THREAD LOCAL FOR THREAD SPECIFIC OBJECTS.

    private static final String NAME = "Poloniex";
    private static final String ENCODING = "UTF-8";
    private static final HmacAlgorithm ALGORITHM = HmacAlgorithm.HMACSHA512;

//     TODO(stfinancial): Initialize in static block?
//     TODO(stfinancial): We should only need one of these per Market. Make this static?
//    private WampClientWrapper wampClientWrapper;
//    private WampSubscription<Ticker> tickerSubscription;
    private final PoloniexData data;

    private static HashMap<String, PoloniexQueue> accountQueues = new HashMap<>();

    private final PoloniexQueue queue;

    // TODO(stfinancial): Switch to static factory method to avoid multiple instances with the same API keys.
    // Need to avoid IP bans by ensuring that a single IP can have a single market instance.
    public Poloniex(Credentials credentials) {
        super(credentials);
        if (!credentials.isPublicOnly()) {
            this.signer = new HmacSigner(ALGORITHM, credentials, false);
        }
        this.requestRewriter = new PoloniexRequestRewriter();
        this.responseParser = new PoloniexResponseParser();
//        this.trader = new PoloniexTrader(this);
        if (!accountQueues.containsKey(apiKey)) {
            queue = new PoloniexQueue(this, QueueStrategy.STRICT, 5);
            accountQueues.put(apiKey, queue);
        } else {
            queue = accountQueues.get(apiKey);
        }
//        try {
//            WampClientBuilder builder = new WampClientBuilder();
//            IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
//            builder.withConnectorProvider(connectorProvider)
//                    .withUri(WAMP_ENDPOINT)
//                    .withRealm("realm1")
//                    .withInfiniteReconnects()
//                    .withReconnectInterval(1, TimeUnit.SECONDS);
//            wampClientWrapper = new WampClientWrapper(builder.build());
//            wampClientWrapper = null;
//        } catch (ApplicationError e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        AssetPairRequest apr = new AssetPairRequest();
        MarketResponse r = processMarketRequest(apr);
        int retryCount = 5;
        while (!r.isSuccess() && retryCount-- > 0) {
            System.out.println("Could not get market data for " + NAME + ": " + r.getJsonResponse());
            r = processMarketRequest(apr);
        }
        if (!r.isSuccess()) {
            data = new PoloniexData(Collections.emptyMap(), Collections.emptyMap());
        } else {
            System.out.println(r.getJsonResponse());
            data = new PoloniexData(((AssetPairResponse) r).getAssetIds(), ((AssetPairResponse) r).getIdAssets());
        }
//        ObjectNode root = JsonNodeFactory.instance.objectNode();
//        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
//        String payload = timestamp + "GET" + "/users/self";
//        root.put("command", "subscribe");
//        root.set("channel", JsonNodeFactory.instance.arrayNode().add("1002").add("marketChannel"));
////        root.put("channel", "1002");
////        root.put()
//        try {
//            PoloniexSocketClient socket = new PoloniexSocketClient(root, data);
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println(e.getMessage());
//        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    // TODO(stfinancial): Why this method instead of a request?
    @Override
    public MarketConstants getConstants() {
        return PoloniexConstants.getInstance();
    }


//    // TODO(stfinancial): Also take in a currency pair if this becomes an interface?
//    public boolean registerTickerSubscription(Action1<Ticker> callback) {
//        if (tickerSubscription == null) {
//            tickerSubscription = new WampSubscription<>(wampClientWrapper, "ticker", (data) -> {
//                // TODO(stfinancial): Is this too slow?
//                JsonNode args = data.arguments();
//                Ticker.Builder b = new Ticker.Builder(PoloniexUtils.parseCurrencyPair(args.get(0).asText()), args.get(1).asDouble(), args.get(2).asDouble(), args.get(3).asDouble());
//                b.percentChange(args.get(4).asDouble());
//                b.baseVolume(args.get(5).asDouble());
//                b.quoteVolume(args.get(6).asDouble());
//                return b.build();
//            });
//        }
//        return tickerSubscription.registerCallback(callback);
//    }

    @Override
    protected HttpUriRequest constructHttpRequest(RequestArgs args) {
        HttpUriRequest httpRequest;
        String responseString;
        JsonNode jsonResponse;
        long timestamp = System.currentTimeMillis();
        // TODO(stfinancial): Does it make sense to check the http type anyway to be defensive?
        if (args.isPrivate()) {
            // TODO(stfinancial): Decide if there are cases where we want to refresh nonce. OR just make the nonce here.
//            args.refreshNonce();
            // TODO(stfinancial): Not sure this is threadsafe.
            String sign = signer.getHexDigest(args.getQueryString().getBytes());
            httpRequest = new HttpPost(args.getUri());
            httpRequest.addHeader("Key", apiKey);
            httpRequest.addHeader("Sign", sign);
            try {
                // TODO(stfinancial): What does this line actually do? Seems it sets the body of the http request. See if doing what gdax does works as well?
                ((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(args.asNameValuePairs(), ENCODING));
            } catch (UnsupportedEncodingException e) {
                System.out.println("Unsupported encoding: " + ENCODING);
                e.printStackTrace();
                return null;
                // TODO(stfinancial): Throw an exception instead.
//                return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_ENCODING));
            }
        } else {
            httpRequest = new HttpGet(args.asUrl(true));
        }
        return httpRequest;
    }
}

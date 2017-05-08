package api.poloniex;

import api.*;
import api.request.*;
import api.wamp.WampClientWrapper;
import api.wamp.WampSubscription;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import rx.functions.Action1;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

// TODO(stfinancial): Possible alternative way of defining this. Tradable, Lendable, etc. interfaces.

// TODO(stfinancial): Consider factory for this. Depedency injection etc. etc.

/**
 * Class representing the Poloniex market.
 */
public final class Poloniex extends Market { //implements Tradable {
    // TODO(stfinancial): THREAD LOCAL FOR THREAD SPECIFIC OBJECTS.

    private static final String MARKET_NAME = "Poloniex";
    private static final String ENCODING = "UTF-8";
    private static final String PUBLIC_URI = "https://poloniex.com/public";
    private static final String PRIVATE_URI = "https://poloniex.com/tradingApi";
    private static final String WAMP_ENDPOINT = "wss://api.poloniex.com";
    // TODO(stfinancial): What about stuff to https://poloniex.com/private?

    private static final HmacAlgorithm ALGORITHM = HmacAlgorithm.HMACSHA512;
    // TODO(stfinancial): Review the thread safety of this object.
    private static final ObjectMapper mapper = new ObjectMapper();


    // TODO(stfinancial): Initialize in static block?
    // TODO(stfinancial): We should only need one of these per Market. Make this static?
    private WampClientWrapper wampClientWrapper;
    private WampSubscription<Ticker> tickerSubscription;

    private static HashMap<String, PoloniexQueue> accountQueues = new HashMap<>();

    private PriorityBlockingQueue<MarketRequest> requestQueue;
    //    private final PoloniexTrader trader;
    private final PoloniexQueue queue;


    // TODO(stfinancial): Switch to static factory method to avoid multiple instances with the same API keys.
    // Need to avoid IP bans by ensuring that a single IP can have a single market instance.
    public Poloniex(Credentials credentials) {
        this.apiKey = credentials.getApiKey();
        this.signer = new HmacSigner(ALGORITHM, credentials.getSecretKey(), false);
        this.httpClient = HttpClients.createDefault();
//        this.trader = new PoloniexTrader(this);
        if (!accountQueues.containsKey(apiKey)) {
            queue = new PoloniexQueue(QueueStrategy.CONSTANT, 10000);
            accountQueues.put(apiKey, queue);
        } else {
            queue = accountQueues.get(apiKey);
        }
        try {
            WampClientBuilder builder = new WampClientBuilder();
            IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
            builder.withConnectorProvider(connectorProvider)
                    .withUri(WAMP_ENDPOINT)
                    .withRealm("realm1")
                    .withInfiniteReconnects()
                    .withReconnectInterval(1, TimeUnit.SECONDS);
            wampClientWrapper = new WampClientWrapper(builder.build());
        } catch (ApplicationError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO(stfinancial): Need to at least specify that we will return MarketResponse on failure instead of the expected type.
    @Override
    public MarketResponse processMarketRequest(MarketRequest request) {
        // TODO(stfinancial): How do we ensure that we return the correct result for this?
        return sendRequest(request);
//        queue.offer(action);
//        if (action instanceof TradeRequest) {
//            return trader.placeOrder((TradeRequest) action);
//        }
    }

    @Override
    public String getName() {
        return MARKET_NAME;
    }

    // TODO(stfinancial): Why this method instead of a request?
    @Override
    public MarketConstants getConstants() {
        return PoloniexConstants.getInstance();
    }


    // TODO(stfinancial): Also take in a currency pair if this becomes an interface?
    public boolean registerTickerSubscription(Action1<Ticker> callback) {
        if (tickerSubscription == null) {
            tickerSubscription = new WampSubscription<>(wampClientWrapper, "ticker", (data) -> {
                // TODO(stfinancial): Is this too slow?
                JsonNode args = data.arguments();
                Ticker.Builder b = new Ticker.Builder(PoloniexUtils.parseCurrencyPair(args.get(0).asText()), args.get(1).asDouble(), args.get(2).asDouble(), args.get(3).asDouble());
                b.percentChange(args.get(4).asDouble());
                b.baseVolume(args.get(5).asDouble());
                b.quoteVolume(args.get(6).asDouble());
                return b.build();
            });
        }
        return tickerSubscription.registerCallback(callback);
    }

    private MarketResponse sendRequest(MarketRequest request) {
        HttpUriRequest httpRequest;
        String responseString;
        JsonNode jsonResponse;
        long timestamp = System.currentTimeMillis();

        RequestArgs args = PoloniexRequestRewriter.rewriteRequest(request);
        if (args.isUnsupported()) {
            return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST, "This request type is not supported or the request cannot be translated to a command."));
        }

        if (!args.isPrivate()) {
            // TODO(stfinancial): Does it make sense to check the http type anyway to be defensive?
            httpRequest = new HttpGet(args.getUrl());
        } else {
            // TODO(stfinancial): Decide if there are cases where we want to refresh nonce.
//            args.refreshNonce();
            String sign = signer.getHexDigest(args.getQueryString().getBytes());
//            System.out.println(args.getUrl());
            // TODO(stfinancial): Does it make sense to check the http type anyway to be defensive?
            httpRequest = new HttpPost(PRIVATE_URI);
            httpRequest.addHeader("Key", apiKey);
            httpRequest.addHeader("Sign", sign);
            try {
                // TODO(stfinancial): What does this line actually do? Seems it sets the body of the http request. See if doing what gdax does works as well?
                ((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(args.asNameValuePairs(), ENCODING));
            } catch (UnsupportedEncodingException e) {
                System.out.println("Unsupported encoding: " + ENCODING);
                e.printStackTrace();
                return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_ENCODING));
            }
        }
//        System.out.println("About to try");
        try {
            CloseableHttpResponse response = httpClient.execute(httpRequest);
            timestamp = System.currentTimeMillis();
            HttpEntity entity = response.getEntity();
            responseString = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
        } catch (IOException e) {
            System.out.println("IOException occurred while executing HTTP request.");
            e.printStackTrace();
            return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.CONNECTION_ERROR, e, "Request failed"));
        }

        try {
            try {
                jsonResponse = mapper.readTree(responseString);
            } catch (JsonMappingException e) {
                // TODO(stfinancial): Put some return statements here instead of initializing to null node.
                if (responseString == null) {
                    System.out.println("JsonMappingException, null string.");
                    return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNPARSABLE_RESPONSE, e, "JsonMappingException while trying to parse null response string."));
                } else {
                    System.out.println("JsonMappingException: " + responseString);
                    return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNPARSABLE_RESPONSE, e, "JsonMappingException while trying to parse response string: " + responseString));
                }
            } catch (JsonParseException e) {
                System.out.println("JsonMappingException: " + responseString);
                return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNPARSABLE_RESPONSE, e, "JsonMappingException while trying to parse response string: " + responseString));
            }

        } catch (IOException e) {
            System.out.println("Error occurred while parsing responseString to JsonNode: " + responseString);
            e.printStackTrace();
            return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNPARSABLE_RESPONSE, e, "Unable to parse response as JSON: " + responseString));
        }
        // TODO(stfinancial): Post-processing and add/convert timestamp.

        return PoloniexResponseParser.constructMarketResponse(jsonResponse, request, timestamp);
    }
}

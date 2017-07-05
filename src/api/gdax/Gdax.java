package api.gdax;

import api.*;
import api.request.MarketRequest;
import api.request.MarketResponse;
import api.request.RequestStatus;
import api.request.StatusType;
import api.wamp.WampClientWrapper;
import api.wamp.WampSubscription;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import rx.functions.Action1;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Timothy on 3/7/17.
 */
public class Gdax extends Market {
    // TODO(stfinancial): The WAMP for gdax is a bit strange. Need to figure out how to deal with it before proceeding.


    private static final String NAME = "Gdax";
    private static final String ENCODING = "UTF-8"; // TODO(stfinancial): Going to assume that this is fine for now.
    private static final String WAMP_ENDPOINT = "wss://ws-feed.gdax.com";

    // TODO(stfinancial): Make this into a superclass field?
    private static final HmacAlgorithm ALGORITHM = HmacAlgorithm.HMACSHA256;
    private final String passphrase;

    // TODO(stfinancial): Make this static and initialize in static block?
//    private WampClientWrapper wampClientWrapper;
//    private WampSubscription<Ticker> tickerSubscription;

    // TODO(stfinancial): Review the thread safety of this object.
    // TODO(stfinancial): Potentially move this into the superclass if we are going to do this for every market.
    private static final ObjectMapper mapper = new ObjectMapper();

    public Gdax(Credentials credentials) {
        this.apiKey = credentials.getApiKey();
        this.passphrase = credentials.getPassphrase();
        this.signer = new HmacSigner(ALGORITHM, credentials.getSecretKey(), true);
        this.httpClient = HttpClients.createDefault();
//        try {
//            WampClientBuilder builder = new WampClientBuilder();
//            IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
//            builder.withConnectorProvider(connectorProvider)
//                    .withUri(WAMP_ENDPOINT)
//                    .withRealm("realm1")
//                    .withInfiniteReconnects()
//                    .withReconnectInterval(1, TimeUnit.SECONDS);
//            wampClientWrapper = new WampClientWrapper(builder.build());
//        } catch (ApplicationError e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public MarketResponse processMarketRequest(MarketRequest request) {
        return sendRequest(request);
    }

    @Override
    protected MarketResponse sendRequest(MarketRequest request) {
        HttpUriRequest httpRequest;
        String responseString;
        JsonNode jsonResponse;
        long timestamp = System.currentTimeMillis();

        RequestArgs args = GdaxRequestRewriter.rewriteRequest(request);
        if (args.isUnsupported()) {
            return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST, "This request type is not supported or the request cannot be translated to a command."));
        }

        int statusCode = -1;
        String url = args.asUrl(true);
        if (!args.isPrivate()) {
            // TODO(stfinancial): Should probably check the httprequesttype.
            httpRequest = new HttpGet(url);
        } else {
            if (args.getHttpRequestType() == RequestArgs.HttpRequestType.GET) {
                httpRequest = new HttpGet(url);
            } else if (args.getHttpRequestType() == RequestArgs.HttpRequestType.POST) {
//                System.out.println(args.getUrl());
                httpRequest = new HttpPost(url);
                ((HttpPost) httpRequest).setEntity(new StringEntity(args.asJson().toString(), ContentType.APPLICATION_JSON));
            } else if (args.getHttpRequestType() == RequestArgs.HttpRequestType.DELETE) {
//                System.out.println(args.getUrl());
                httpRequest = new HttpDelete(url);
            } else {
                System.out.println("(" + NAME + ")" + "Invalid HttpRequestType: " + args.getHttpRequestType());
                return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.MALFORMED_REQUEST,"Invalid HttpRequestType: " + args.getHttpRequestType()));
            }
            // TODO(stfinancial): Will change these names after getting a working implementation.
            String CB_ACCESS_KEY = apiKey;
//            System.out.println("API: " + CB_ACCESS_KEY);
            String CB_ACCESS_TIMESTAMP = String.valueOf(System.currentTimeMillis() / 1000);
//            System.out.println("Timestamp: " + CB_ACCESS_TIMESTAMP);
            String CB_ACCESS_PASSPHRASE = passphrase;
//            System.out.println("Passphrase: " + CB_ACCESS_PASSPHRASE);
            JsonNode json = args.asJson();
            System.out.println(json);
            String what;
            if (json.isNull()) {
                what = String.valueOf(CB_ACCESS_TIMESTAMP) + args.getHttpRequestType().toString().toUpperCase() + args.getResourcePath();
            } else {
                what = String.valueOf(CB_ACCESS_TIMESTAMP) + args.getHttpRequestType().toString().toUpperCase() + args.getResourcePath() + args.asJson().toString();
            }

//            System.out.println("what: " + what);
            httpRequest.addHeader("CB-ACCESS-KEY", CB_ACCESS_KEY);
            httpRequest.addHeader("CB-ACCESS-SIGN", signer.getBase64Digest(what.getBytes()));
            httpRequest.addHeader("CB-ACCESS-TIMESTAMP", CB_ACCESS_TIMESTAMP);
            httpRequest.addHeader("CB-ACCESS-PASSPHRASE", CB_ACCESS_PASSPHRASE);
//            httpRequest.addHeader("Content-Type", "Application/JSON");
//            ((HttpPost) httpRequest).setEntity(new StringEntity(args.asJson().toString(), ENCODING));
//            try {
//                // TODO(stfinancial): What does this line actually do? Seems it sets the body of the http request. See if doing what gdax does works as well?
//                ((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(args.asNameValuePairs(), ENCODING));
//            } catch (UnsupportedEncodingException e) {
//                System.out.println("Unsupported encoding: " + ENCODING);
//                e.printStackTrace();
//                return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_ENCODING));
//            }
        }
        try {
            CloseableHttpResponse response = httpClient.execute(httpRequest);
            statusCode = response.getStatusLine().getStatusCode();
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
        // TODO(stfinancial): More sophisticated handling of errors codes...
        boolean isError = statusCode != HttpStatus.SC_OK;
//        System.out.println(statusCode);
        return GdaxResponseParser.constructMarketResponse(jsonResponse, request, timestamp, isError);
    }

//    public boolean registerTickerSubscription(Action1<Ticker> callback) {
//        // TODO(stfinancial): Handle public vs. private authentication stuff.
//        if (tickerSubscription == null) {
//            tickerSubscription = new WampSubscription<>(wampClientWrapper, "subscribe", (data) -> {
//                return null;
//            });
//        }
//        return tickerSubscription.registerCallback(callback);
//    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public MarketConstants getConstants() {
        return null;
    }
}

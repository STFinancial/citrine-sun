package api.gdax;

import api.*;
import api.request.MarketRequest;
import api.request.MarketResponse;
import api.request.RequestStatus;
import api.request.StatusType;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Class representing the Gdax {@code Market}.
 */
public class Gdax extends Market {
    private static final String NAME = "Gdax";
    private static final String WAMP_ENDPOINT = "wss://ws-feed.gdax.com";

    // TODO(stfinancial): Make this into a superclass field?
    private static final HmacAlgorithm ALGORITHM = HmacAlgorithm.HMACSHA256;
    private final String passphrase;

    public Gdax(Credentials credentials) {
        super(credentials);
        this.passphrase = credentials.getPassphrase();
        this.signer = new HmacSigner(ALGORITHM, credentials, true);
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
            if (args.getHttpRequestType() != RequestArgs.HttpRequestType.GET) {
                return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.MARKET_ERROR, "Public requests must use HttpGet. Type was: " + args.getHttpRequestType().toString().toUpperCase()));
            }
            httpRequest = new HttpGet(url);
        } else {
            switch (args.getHttpRequestType()) {
                case GET:
                    httpRequest = new HttpGet(url);
                    break;
                case POST:
                    httpRequest = new HttpPost(url);
                    // TODO(stfinancial): Check that this is actually needed.
                    ((HttpPost) httpRequest).setEntity(new StringEntity(args.asJson().toString(), ContentType.APPLICATION_JSON));
                    break;
                case DELETE:
                    httpRequest = new HttpDelete(url);
                    break;
                default:
                    System.out.println("(" + NAME + ")" + "Invalid HttpRequestType: " + args.getHttpRequestType());
                    return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.MALFORMED_REQUEST,"Invalid HttpRequestType: " + args.getHttpRequestType()));
            }
            // TODO(stfinancial): Will change these names after getting a working implementation.
            String CB_ACCESS_TIMESTAMP = String.valueOf(System.currentTimeMillis() / 1000);
//            System.out.println("Timestamp: " + CB_ACCESS_TIMESTAMP);
            JsonNode json = args.asJson();
            System.out.println(json);
            String what;
            if (args.getHttpRequestType() != RequestArgs.HttpRequestType.POST || json.isNull()) {
                if (args.getQueryString() != null || !args.getQueryString().isEmpty()) {
                    what = String.valueOf(CB_ACCESS_TIMESTAMP) + args.getHttpRequestType().toString().toUpperCase() + args.getResourcePath() + "?" + args.getQueryString();
                } else {
                    what = String.valueOf(CB_ACCESS_TIMESTAMP) + args.getHttpRequestType().toString().toUpperCase() + args.getResourcePath();
                }
            } else {
                what = String.valueOf(CB_ACCESS_TIMESTAMP) + args.getHttpRequestType().toString().toUpperCase() + args.getResourcePath() + args.asJson().toString();
            }
            System.out.println("what: " + what);
            httpRequest.addHeader("CB-ACCESS-KEY", apiKey);
            httpRequest.addHeader("CB-ACCESS-SIGN", signer.getBase64Digest(what.getBytes()));
            httpRequest.addHeader("CB-ACCESS-TIMESTAMP", CB_ACCESS_TIMESTAMP);
            httpRequest.addHeader("CB-ACCESS-PASSPHRASE", passphrase);
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

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public MarketConstants getConstants() {
        return null;
    }
}

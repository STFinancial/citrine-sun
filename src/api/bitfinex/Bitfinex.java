package api.bitfinex;

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
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

// TODO(stfinancial): Apparently you can't place a trade through the rest API... so you need to get the websocket shit working.
/**
 * Class representing the Bitfinex {@code Market}.
 */
public final class Bitfinex extends Market {
    // TODO(stfinancial): Handle decimal precision here (5 sigfigs) and other places (8 decimal places on Polo).
    private static final String MARKET_NAME = "Bitfinex";
    private static final HmacAlgorithm algorithm = HmacAlgorithm.HMACSHA384;

    private final BitfinexRequestRewriter requestRewriter;
    private final BitfinexResponseParser responseParser;

    public Bitfinex(Credentials credentials) {
        super(credentials);
        if (!credentials.isPublicOnly()) {
            this.signer = new HmacSigner(algorithm, credentials, false);
        }
        this.requestRewriter = new BitfinexRequestRewriter();
        this.responseParser = new BitfinexResponseParser();
    }

    @Override
    public MarketResponse processMarketRequest(MarketRequest request) {
        return sendRequest(request);
    }

    @Override
    public String getName() {
        return MARKET_NAME;
    }

    @Override
    public MarketConstants getConstants() {
        return null;
    }

    @Override
    protected MarketResponse sendRequest(MarketRequest request) {
        HttpUriRequest httpRequest;
        String responseString;
        JsonNode jsonResponse;
        long timestamp = System.currentTimeMillis();
        int statusCode = -1;

        RequestArgs args = requestRewriter.rewriteRequest(request);
        if (args.isUnsupported()) {
            return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST, "This request type is not supported or the request cannot be translated to a command."));
        }
        if (!args.isPrivate()) {
            if (args.getHttpRequestType() != RequestArgs.HttpRequestType.GET) {
                return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.MARKET_ERROR, "Public requests must use HttpGet. Type was: " + args.getHttpRequestType().toString().toUpperCase()));
            }
            httpRequest = new HttpGet(args.asUrl(true));
        } else {
            String url = args.asUrl(true);
            System.out.println("Url: " + url);
            httpRequest = new HttpPost(url);
            httpRequest.addHeader("bfx-nonce", String.valueOf(timestamp));
            System.out.println("Nonce: " + String.valueOf(timestamp));
            httpRequest.addHeader("bfx-apikey", apiKey);
            System.out.println("ApiKey: " + apiKey);
            String stuff = "/api" + args.getResourcePath() + String.valueOf(timestamp) + args.getQueryString();
//            String stuff = "/api/" + url + String.valueOf(timestamp) + args.getQueryString();
            System.out.println("Body: " + stuff);
            // TODO(stfinancial): Need to check that credentials aren't public only.
//            String sign = signer.getHexDigest(stuff.getBytes());
            String sign = signer.getHexDigest(stuff.getBytes(Charset.forName("UTF-8")));
            httpRequest.addHeader("bfx-signature", sign);
//            ((HttpPost) httpRequest).setEntity(new StringEntity("{}", ContentType.APPLICATION_JSON));
//            try { ((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(Arrays.asList(new NameValuePair[]{new BasicNameValuePair("json", "{}")}), "UTF-8")); } catch (Exception e) {};
//            ((HttpPost) httpRequest).setEntity(new StringEntity(args.asJson().toString(), ContentType.APPLICATION_JSON));
            ((HttpPost) httpRequest).setEntity(new StringEntity(args.asJson().toString(), ContentType.APPLICATION_FORM_URLENCODED));
        }

        // TODO(stfinancial): This logic is duplicated everywhere, maybe move this to market?
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
        System.out.println(jsonResponse);
        return responseParser.constructMarketResponse(jsonResponse, request, timestamp, isError);
    }
}

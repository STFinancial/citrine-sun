package api.kraken;

import api.*;
import api.request.MarketRequest;
import api.request.MarketResponse;
import api.request.RequestStatus;
import api.request.StatusType;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Timothy on 3/7/17.
 */
public class Kraken extends Market {
    private static final String NAME = "Kraken";
    private static final String ENCODING = "UTF-8";

    // TODO(stfinancial): Make this into a superclass field?
    private static final HmacAlgorithm ALGORITHM = HmacAlgorithm.HMACSHA512;
    private MessageDigest digest;


    // TODO(stfinancial): Review the thread safety of this object.
    // TODO(stfinancial): Potentially move this into the superclass if we are going to do this for every market.
    private static final ObjectMapper mapper = new ObjectMapper();

    public Kraken(Credentials credentials) {
        this.apiKey = credentials.getApiKey();
        this.signer = new HmacSigner(ALGORITHM, credentials.getSecretKey(), true);
        this.httpClient = HttpClients.createDefault();
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MarketResponse processMarketRequest(MarketRequest request) {
        return sendRequest(request);
    }

    private MarketResponse sendRequest(MarketRequest request) {
        HttpUriRequest httpRequest;
        long timestamp = System.currentTimeMillis();
        String responseString;
        JsonNode jsonResponse;
        int statusCode = -1;

        final RequestArgs args = KrakenRequestRewriter.rewriteRequest(request);
        if (args.isUnsupported()) {
            return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST, "This request type is not supported or the request cannot be translated to a command."));
        }
        switch (args.getHttpRequestType()) {
            case GET:
                httpRequest = new HttpGet(args.getUrl());
                break;
            case POST:
                httpRequest = new HttpPost(args.getUrl());
                break;
            default:
                System.out.println("(" + NAME + ")" + "Invalid HttpRequestType: " + args.getHttpRequestType());
                return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.MALFORMED_REQUEST, "Invalid HttpRequestType: " + args.getHttpRequestType()));
        }
        if (args.isPrivate()) {
            // TODO(stfinancial): Implement this.
            // TODO(stfinancial): Change names once we have confirmed it is working.
            String API_KEY = apiKey;
            // TODO(stfinancial): We know it's httppost, so the above code is a bit awkward.
            byte[] sha256Hash = digest.digest((timestamp + "nonce=" + timestamp + args.getQueryString()).getBytes()); //.getBytes("UTF-8")
            byte[] pathBytes = args.getResourcePath().getBytes();
            byte[] bytes = new byte[sha256Hash.length + pathBytes.length];
            String sign = signer.getBase64Digest(bytes);
            httpRequest.addHeader("API-KEY", apiKey);
            httpRequest.addHeader("API-Sign", sign);
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
        return KrakenResponseParser.constructMarketResponse(jsonResponse, request, timestamp, isError);
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

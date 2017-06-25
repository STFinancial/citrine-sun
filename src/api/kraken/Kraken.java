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
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by Timothy on 3/7/17.
 */
public class Kraken extends Market {
    // TODO(stfinancial): Kraken has a nonce window depending on the api key.

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
        System.out.println(args.getUrl());
        if (args.isPrivate()) {
            // TODO(stfinancial): Implement this.
            // TODO(stfinancial): Change names once we have confirmed it is working.
            String API_KEY = apiKey;
            // TODO(stfinancial): We know it's httppost, so the above code is a bit awkward.
            System.out.println("QueryString: " + args.getQueryString());
            System.out.println("Resource Path: " + args.getResourcePath());
            String encoded;
            // TODO(stfinancial): Use getBytes(Charset.forName(ENCODING)) instead. Or... getBytes(CHARSET).
            try {
                encoded = URLEncoder.encode(timestamp + "nonce=" + timestamp + args.getQueryString(), ENCODING);
            } catch (UnsupportedEncodingException e) {
                System.out.println("Unsupported encoding: " + ENCODING);
                e.printStackTrace();
                return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_ENCODING));
            }
            System.out.println("Encoded: " + encoded);
            // TODO(stfinancial): Move this into the try block with the encoding?
            // TODO(stfinancial): Fix all of this.
            byte[] sha256Hash = digest.digest(encoded.getBytes()); //.getBytes(ENCODING);
            System.out.println(sha256Hash);
            System.out.println(digest.digest((timestamp + "nonce=" + timestamp + args.getQueryString()).getBytes(Charset.forName("UTF-8"))));
            sha256Hash = digest.digest((timestamp + "nonce=" + timestamp + args.getQueryString()).getBytes(Charset.forName("UTF-8")));
//            byte[] sha256Hash = digest.digest((timestamp + "nonce=" + timestamp + args.getQueryString()).getBytes()); //.getBytes("UTF-8")
//            byte[] sha256Hash = digest.digest((timestamp + "nonce=" + timestamp).getBytes()); //.getBytes("UTF-8")
            // TODO(stfinancial): Move this into the try block with the encoding?
            byte[] pathBytes = args.getResourcePath().getBytes(Charset.forName("UTF-8"));
//            byte[] pathBytes = args.getResourcePath().getBytes(Charset.forName(ENCODING));
            byte[] bytes = new byte[pathBytes.length + sha256Hash.length];
            System.arraycopy(pathBytes, 0, bytes, 0, pathBytes.length);
            System.arraycopy(sha256Hash, 0, bytes, pathBytes.length, sha256Hash.length);
            String sign = signer.getBase64Digest(bytes);
            System.out.println("Sign: "  + sign);
            httpRequest.addHeader("API-KEY", apiKey);
            httpRequest.addHeader("API-Sign", sign);
            try {
                NameValuePair p = new BasicNameValuePair("nonce", String.valueOf(timestamp));
                ((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(Arrays.asList(p), ENCODING));
            } catch (UnsupportedEncodingException e) {
                System.out.println("Unsupported encoding: " + ENCODING);
                e.printStackTrace();
                return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_ENCODING));
            }
//            httpRequest.addHeader("ContentType", "application/x-www-form-urlencoded");
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

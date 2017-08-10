package api.kraken;

import api.*;
import api.request.AssetPairRequest;
import api.request.AssetPairResponse;
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
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Class representing the Kraken {@code Market}.
 */
public class Kraken extends Market {
    // TODO(stfinancial): Kraken has a nonce window depending on the api key.

    private static final String NAME = "Kraken";
    private static final String ENCODING = "UTF-8";

    // TODO(stfinancial): Make this into a superclass field?
    private static final HmacAlgorithm ALGORITHM = HmacAlgorithm.HMACSHA512;
    private MessageDigest digest;

    // TODO(stfinancial): This needs to be unified across all markets.
    // TODO(stfinancial): Does this really make sense? Do these objects ever hold state?
    private final KrakenResponseParser responseParser;
    private final KrakenRequestRewriter requestRewriter;
    private final KrakenData data;

    public Kraken(Credentials credentials) {
        super(credentials);
        this.signer = new HmacSigner(ALGORITHM, credentials, true);
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        requestRewriter = new KrakenRequestRewriter(this);
        responseParser = new KrakenResponseParser(this);
        AssetPairRequest apr = new AssetPairRequest();
        MarketResponse r = processMarketRequest(apr);
        int retryCount = 5;
        while (!r.isSuccess() && retryCount-- > 0) {
            System.out.println("Could not get market data for " + NAME + ": " + r.getJsonResponse());
            r = processMarketRequest(apr);
        }
        if (!r.isSuccess()) {
            data = new KrakenData(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());
        } else {
            System.out.println(r.getJsonResponse());
            data = new KrakenData(((AssetPairResponse) r).getAssetPairs(), ((AssetPairResponse) r).getAssetPairNames(), ((AssetPairResponse) r).getAssetPairKeys());
        }
    }

    @Override
    public MarketResponse processMarketRequest(MarketRequest request) {
        return sendRequest(request);
    }

    @Override
    protected MarketResponse sendRequest(MarketRequest request) {
        // TODO(stfinancial): Cleanup
        HttpUriRequest httpRequest;
        long timestamp = System.currentTimeMillis();
        String responseString;
        JsonNode jsonResponse;
        int statusCode = -1; // TODO(stfinancial): Think about how to properly use this error code.

        final RequestArgs args = requestRewriter.rewriteRequest(request);
        System.out.println("Json: " + args.asJson().toString());
        if (args.isUnsupported()) {
            return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST, "This request type is not supported or the request cannot be translated to a command."));
        }
        String url;
        switch (args.getHttpRequestType()) {
            case GET:
                url = args.asUrl(true);
                httpRequest = new HttpGet(url);
                break;
            case POST:
                if (args.isPrivate()) {
                    url = args.asUrl(false);
                } else {
                    // TODO(stfinancial): First of all, can this case ever happen? Second is this always true? May need another refactor of RequestArgs...
                    url = args.asUrl(true);
                }
                httpRequest = new HttpPost(url);
                break;
            default:
                System.out.println("(" + NAME + ")" + "Invalid HttpRequestType: " + args.getHttpRequestType());
                return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.MALFORMED_REQUEST, "Invalid HttpRequestType: " + args.getHttpRequestType()));
        }
        System.out.println("URL: " + url);
        if (!args.isPrivate()) {

        } else if (credentials.isPublicOnly()) {
            // TODO(stfinancial): Something here is not being encoded correctly, issues with commas and spaces.
            String baseQueryString = args.getQueryString();
            System.out.println("Base QueryString: " + baseQueryString);
//            try { baseQueryString = URLEncoder.encode(baseQueryString, ENCODING); } catch (Exception e) {}
//            System.out.println("Base QueryString (UrlEncoder): " + baseQueryString);
            String path = args.getResourcePath();
            System.out.println("Resource Path: " + path);

            String postData = "nonce=" + timestamp;
            if (baseQueryString != null && !baseQueryString.isEmpty()) {
                postData += "&" + baseQueryString;
            }
            System.out.println("Postdata: " + postData);

            byte[] encoded = (timestamp + postData).getBytes(Charset.forName("UTF-8"));
            System.out.println("Encoded: " + new String(encoded));

            byte[] noncePostdata = digest.digest(encoded);
            System.out.println("SHA256 of Encoded: " + new String(noncePostdata));

            byte[] pathBytes = path.getBytes(Charset.forName("UTF-8"));
            System.out.println("PathBytes: " + new String(pathBytes));

            byte[] message = new byte[pathBytes.length + noncePostdata.length];
            System.arraycopy(pathBytes, 0, message, 0, pathBytes.length);
            System.arraycopy(noncePostdata, 0, message, pathBytes.length, noncePostdata.length);
            System.out.println("Message: " + new String(message));

            String signature = signer.getBase64Digest(message);
            System.out.println("Signature: " + signature);

            httpRequest.addHeader("API-Key", apiKey);
            httpRequest.addHeader("API-Sign", signature);
//            httpRequest.addHeader("ContentType", "application/x-www-form-urlencoded");
            try {
                NameValuePair p = new BasicNameValuePair("nonce", String.valueOf(timestamp));
                ArrayList<NameValuePair> nvps = new ArrayList<>();
                nvps.add(p);
                nvps.addAll(args.asNameValuePairs());
                nvps.forEach((nvp)->System.out.println(nvp));
                ((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(nvps, ENCODING));
            } catch (UnsupportedEncodingException e) {
                System.out.println("Unsupported encoding: " + ENCODING);
                e.printStackTrace();
                return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_ENCODING));
            }
        } else {
            return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST, "This request is not available for public only access."));
        }
        System.out.println(httpRequest.toString());
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
        if (isError) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.MARKET_ERROR, jsonResponse.asText()));
        }
//        System.out.println(statusCode);
//        System.out.println(jsonResponse);
        return responseParser.constructMarketResponse(jsonResponse, request, timestamp);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public MarketConstants getConstants() {
        return null;
    }

    protected KrakenData getData() { return data; }
}

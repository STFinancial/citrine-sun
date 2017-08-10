package api;

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
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Abstract class for
 */
public abstract class Market {
    // TODO(stfinancial): Need to make these non-protected, these are too visible. How can we abstract this stuff away?

    // TODO(stfinancial): Will there be one of these per thread? Do we need a different thing for multiple threads?
    protected CloseableHttpClient httpClient;


    protected final ObjectMapper mapper;
    // TODO(stfinancial): Is this the correct visibility on this?
    protected final String apiKey;
    protected final Credentials credentials;

    // TODO(stfinancial): How does the subclass know to initialize these?
    protected HmacSigner signer;
    // TODO(stfinancial): Currently cannot enforce that these are set in the constructor due to them taking in the Market object in their constructor
    protected RequestRewriter requestRewriter;
    protected ResponseParser responseParser;

    public Market(Credentials credentials) {
        this.credentials = credentials;
        this.apiKey = credentials.getApiKey();
        this.mapper = new ObjectMapper();
        this.httpClient = HttpClients.createDefault();
    }

    protected abstract HttpUriRequest constructHttpRequest(RequestArgs args);

    public abstract String getName();

    // TODO(stfinancial): Handle this a little better.
    public abstract MarketConstants getConstants();

    // TODO(stfinancial): Does this make sense?
    public String getApiKey() { return apiKey; }

    public MarketResponse processMarketRequest(MarketRequest request) {
        // TODO(stfinancial): Cleanup.
        long timestamp = System.currentTimeMillis();
        String responseString;
        JsonNode jsonResponse;
        int statusCode = -1; // TODO(stfinancial): Think about how to properly use this error code.

        final RequestArgs args = requestRewriter.rewriteRequest(request);
        if (credentials.isPublicOnly() && args.isPrivate()) {
            return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST, "This request is not available for public only access."));
        }

        final HttpUriRequest httpRequest = constructHttpRequest(args);
        // TODO(stfinancial): Switch to throws instead, maybe. What do we throw for invalid http type... a general exception?
        // TODO(stfinancial): On second thought, perhaps we should just let the request fail naturally if the type is wrong.
        if (httpRequest == null) {
            return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.MALFORMED_REQUEST, "Invalid HttpRequestType: " + args.getHttpRequestType()));
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

    // TODO(stfinancial): Do we want a separate method for creating a price alert? Does this at all break anything else we're doing?
    // TODO(stfinancial): It does perhaps mean we need a separate thread listening to this channel. I'm not sure though.
}

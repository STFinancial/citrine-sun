package api.bittrex;

import api.*;
import api.request.MarketRequest;
import api.request.MarketResponse;
import api.request.RequestStatus;
import api.request.StatusType;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;

/**
 * Created by Timothy on 8/3/17.
 */
public class Bittrex extends Market {
    private static final String NAME = "Bittrex";
    private static final HmacAlgorithm ALGORITHM = HmacAlgorithm.HMACSHA512;
    private final BittrexRequestRewriter requestRewriter;
    private final BittrexResponseParser responseParser;

    public Bittrex(Credentials credentials) {
        super(credentials);
        this.requestRewriter = new BittrexRequestRewriter();
        this.responseParser = new BittrexResponseParser();
        this.signer = new HmacSigner(ALGORITHM, credentials.getSecretKey(), false);
    }

    @Override
    public MarketResponse processMarketRequest(MarketRequest request) {
        return sendRequest(request);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public MarketConstants getConstants() {
        return null;
    }

    @Override
    protected MarketResponse sendRequest(MarketRequest request) {
        HttpUriRequest httpRequest;
        long timestamp = System.currentTimeMillis();
        String responseString = "";
        JsonNode jsonResponse;
        int statusCode = -1; // TODO(stfinancial): Think about how to properly use this error code.
        final RequestArgs args = requestRewriter.rewriteRequest(request);
        System.out.println("Json: " + args.asJson().toString());
        if (args.isUnsupported()) {
            return new MarketResponse(NullNode.getInstance(), request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST, "This request type is not supported or the request cannot be translated to a command."));
        }
        String url;
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
        return responseParser.constructMarketResponse(jsonResponse, request, timestamp);
    }
}

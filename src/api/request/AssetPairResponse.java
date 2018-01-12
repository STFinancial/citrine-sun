package api.request;

import api.AssetPair;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;

/**
 * Contains a list of available {@link AssetPair AssetPairs} for the {@link api.Market Market}.
 */
public final class AssetPairResponse extends MarketResponse {
    private final List<AssetPair> assetPairs;

    public AssetPairResponse(List<AssetPair> assetPairs, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.assetPairs = Collections.unmodifiableList(assetPairs);
    }

    public List<AssetPair> getAssetPairs() {
        return assetPairs;
    }
}

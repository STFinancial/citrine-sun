package api.kraken.request;

import api.request.MarketRequest;

/**
 * Created by Timothy on 7/8/17.
 */
// TODO(stfinancial): This may be fine as a general request.
public class AssetPairRequest extends MarketRequest {
    public AssetPairRequest(int priority, long timestamp) {
        super(priority, timestamp);
    }
}

package api.request;

/**
 * Created by Timothy on 3/25/17.
 */
public class VolumeRequest extends MarketRequest {
    // TODO(stfinancial): Eventually add support for non daily volume.
    // TODO(stfinancial): Potentially provide another constructor to take in Currency (aggregate)/CurrencyPair

    public VolumeRequest(int priority, long timestamp) {
        super(priority, timestamp);
    }
}

package api.request;

/**
 * Abstract class from which all requests to a {@link api.Market Market} should derive.
 */
public abstract class MarketRequest implements Comparable<MarketRequest> {
    private int priority;
    private long timestamp;
    private boolean useCachedValues = true; // If available, uses the cached result instead of making API call.

    public MarketRequest(int priority, long timestamp) {
        this.priority = priority;
        this.timestamp = timestamp;
    }

    public final void setUseCachedValues(boolean useCachedValues) {
        this.useCachedValues = useCachedValues;
    }

    public final int getPriority() { return priority; }
    public final boolean getUseCachedValues() { return useCachedValues; }
    public final long getTimestamp() { return timestamp; }

    @Override
    public final int compareTo(MarketRequest req) {
        if (priority > req.priority) {
            return 1;
        } else if (priority < req.priority) {
            return -1;
        } else if (timestamp > req.timestamp) {
            return -1;
        } else {
            return 1;
        }
    }

    // TODO(stfinancial): This seems a bit excessive to avoid an unchecked cast in CachedResource.
    // There are other good reasons to do this. Probably implement it in the future.
//    public abstract Class<? extends MarketResponse> getResponseClass();
}

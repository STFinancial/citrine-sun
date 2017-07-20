package api.request;

// TODO(stfinancial): Convert this into a JSON?
/**
 * Abstract class from which all requests to a {@link api.Market Market} should derive.
 */
public abstract class MarketRequest implements Comparable<MarketRequest> {
    // TODO(stfinancial): Add support for registering callback to be completed upon the request being done. This may require having a wrapper around the MarketResponse Future

    private int priority;
    private long timestamp;
    private boolean useCachedValues = true; // If available, uses the cached result instead of making API call.

    private long maxWaitTime;
    private boolean priorityOverride = false;

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
    public final long getMaxWaitTime() { return maxWaitTime; }
    public final boolean hasPriorityOverride() { return priorityOverride; }

    // TODO(stfinancial): I'm not sure about having to rely on timestamp being set properly. Maybe we can set a timestamp when it hits the queue?
    public final void maxPriorityAfter(long millis) {
        maxWaitTime = millis;
        priorityOverride = true;
    }

    @Override
    public final int compareTo(MarketRequest req) {
        // TODO(stfinancial): I'm not sure about having to rely on timestamp being set properly. Maybe we can set a timestamp when it hits the queue?
        if (priorityOverride && System.currentTimeMillis() > timestamp + maxWaitTime) {
            return 1;
        }
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

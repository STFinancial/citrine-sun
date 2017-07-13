package api;

/**
 * Created by Timothy on 7/2/17.
 */
public enum QueueStrategy {
    /** Do not use a queue to resolve API rate limits. Allow the clients to resolve this issue themselves. */
    DISABLED,
    /** Submit work as soon as it is received until API rate limit is reached. At this point, there will be delays in
     * returning futures as per the API rate limit. Strict always obeys priority. */
    STRICT,

//    // Submit work as soon as it is received. Due to API rate limits, will be submitted with a constant delay as
//    // necessary. Currently does not respect priority (unless the request queue manages to have multiple items), but can
//    // in the future when we can swap futures in the response.
//    IMMEDIATE;














    /**
     * Submits work and returns a {@link java.util.concurrent.Future<api.request.MarketResponse> Future<MarketResponse>} immediately (pending API limitations).
     * This means that high priority requests may not get served immediately if there is a significant queue backlog.
     */
//    IMMEDIATE; // This will be default for now.

//    /** Submits work items at a constant rate in accordance with API rate limits. */
//    CONSTANT_NO_FUTURE,

    // TODO(stfinancial): We may want this to occur on the request level (e.g. some clients using the same set of api keys may want different strategies.. if this is possible)

//    IMMEDIATE, // TODO(stfinancial): Not clear how this differs from a BEST_EFFORT
//    CONSTANT;
}

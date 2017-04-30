package api.poloniex;

/**
 * Created by Timothy on 3/4/17.
 */
public enum QueueStrategy {
    // TODO(stfinancial): We may want this to occur on the request level (e.g. some clients using the same set of api keys may want different strategies.. if this is possible)

    IMMEDIATE,
    CONSTANT;
}

package api.request;

/**
 * Designates a specific status encountered by the {@link api.Market Market} and used in {@link RequestStatus}.
 */
public enum StatusType {
    SUCCESS,
    CONNECTION_ERROR, // TODO(stfinancial): Find a better name for this.
    MALFORMED_REQUEST,
    MARKET_ERROR,
    UNPARSABLE_RESPONSE,
    UNSUPPORTED_ENCODING,
    UNSUPPORTED_REQUEST;
}

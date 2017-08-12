package api.book;

/**
 * Type of operation to perform on the {@link OrderBook} via an {@link OrderBookUpdate}.
 */
public enum UpdateType {
    INSERT,
    REMOVE,
    UPDATE;
    // TODO(stfinancial): Maybe MOVE
}

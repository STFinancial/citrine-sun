package api.request;

/**
 * Created by Timothy on 5/5/17.
 */
public final class OrderStatusRequest extends MarketRequest {
    private final String id;

    public OrderStatusRequest(String id, int priority, long timestamp) {
        super(priority, timestamp);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}

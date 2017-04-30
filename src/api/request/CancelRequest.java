package api.request;

import api.tmp_loan.PrivateLoanOrder;
import api.tmp_trade.TradeOrder;

/**
 * A request to a market to cancel a given order, whether that be a {@link PrivateLoanOrder} or {@link TradeOrder}.
 */
public final class CancelRequest extends MarketRequest {

    // TODO(stfinancial): Where do we want to handle move requests?


    // TODO(stfinancial): If I am doing this here, then why do I have TradeType in a separate class. Think carefully about the benefits and drawbacks of each.
    public enum CancelType {
        // TODO(stfinancial): What should this enum be called? CancelType or just Type?

        // TODO(stfinancial): Potentially add an invalid field here for the default case.

        // TODO(stfinancial): Might not even need this
        TRADE, LOAN;
    }

    // TODO(stfinancial): Rename to orderNumber?
    private final String id;
    private final CancelType type;

    public CancelRequest(String id, CancelType type, int priority, long timestamp) {
        super(priority, timestamp);
        this.id = id;
        this.type = type;
    }

    public String getId() { return id; }
    public CancelType getType() { return type; }

//    @Override
//    public Class<? extends MarketResponse> getResponseClass() {
//        return MarketResponse.class;
//    }
}

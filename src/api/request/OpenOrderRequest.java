package api.request;

import api.Currency;
import api.CurrencyPair;
import com.sun.istack.internal.Nullable;

/**
 * Created by Timothy on 12/27/16.
 */
public final class OpenOrderRequest extends MarketRequest {
    private CurrencyPair currencyPair;

    public OpenOrderRequest(int priority, long timestamp) {
        super(priority, timestamp);
    }

    /**
     * Returns all open {@link api.tmp_trade.TradeOrder}.
     * @param priority The priority of this request. The higher the priority, the faster the request will get served.
     * @param timestamp The timestamp at which this request was created.
     * @param currencyPair The {@code CurrencyPair} for which to get the market data. If null, return all open orders.
     */
    public OpenOrderRequest(int priority, long timestamp, CurrencyPair currencyPair) {
        this(priority, timestamp);
        this.currencyPair = currencyPair;
    }

    // TODO(stfinancial): Convert this to optional.
    @Nullable
    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }
}

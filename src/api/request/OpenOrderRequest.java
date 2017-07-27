package api.request;

import api.CurrencyPair;
import com.sun.istack.internal.Nullable;

/**
 * Obtain the set of open {@link api.tmp_trade.TradeOrder TradeOrders}, potentially restricted to a {@link CurrencyPair}
 * depending on what the specific {@link api.Market Market} allows.
 */
public final class OpenOrderRequest extends MarketRequest {
    private CurrencyPair currencyPair;

    public OpenOrderRequest() {}

    // TODO(stfinancial): How do we want to handle null values of currency pair?
    /**
     * Returns all open {@link api.tmp_trade.TradeOrder TradeOrders}.
     * @param currencyPair The {@code CurrencyPair} for which to get the market data. If null, return all open orders.
     */
    public OpenOrderRequest(CurrencyPair currencyPair) {
        this.currencyPair = currencyPair;
    }

    // TODO(stfinancial): Convert this to optional. Maybe...
    @Nullable
    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }
}

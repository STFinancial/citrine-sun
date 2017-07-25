package api.request;

import api.CurrencyPair;
import com.sun.istack.internal.Nullable;

/**
 * Request to get {@link api.tmp_trade.CompletedTrade Completed Trades} from the given {@link api.Market Market}.
 * {@link CurrencyPair} can be specified to restrict the results, and is required for some {@code Markets}.
 */
public final class TradeHistoryRequest extends MarketRequest {
    private final long start;
    private final long end;
    private CurrencyPair pair;

    // TODO(stfinancial): These parameters are not required by all markets (any markets?), but if we really wanted to circumvent these, we could just pass in 0, currentTimeMillis anyway
    public TradeHistoryRequest(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public TradeHistoryRequest(long start, long end, CurrencyPair pair) {
        this.start = start;
        this.end = end;
        this.pair = pair;
    }

    @Nullable
    public CurrencyPair getPair() { return pair; }
    public long getStart() { return start; }
    public long getEnd() { return end; }
}

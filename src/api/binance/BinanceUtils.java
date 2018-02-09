package api.binance;

import api.Currency;
import api.CurrencyPair;
import api.tmp_trade.TradeType;
import com.sun.istack.internal.Nullable;

/**
 * Utility class for the {@link Binance} {@link api.Market Market}.
 */
final class BinanceUtils {
    private static final String SELL_STRING = "SELL";
    private static final String BUY_STRING = "BUY";

    static String getCommandForTradeType(TradeType type) {
        switch(type) {
            case BUY:
                return BUY_STRING;
            case SELL:
                return SELL_STRING;
            default:
                return "INVALID";
        }
    }

    @Nullable
    static CurrencyPair parseCurrencyPair(String pair) {

        // TODO(stfinancial): Convert this to optional.
        String[] currencies = pair.split("_", 2);
        if (currencies.length != 2) {
            return null;
        }
        // Poloniex API does not conform to the CurrencyPair representation standard.
        System.out.println(pair);
        return CurrencyPair.of(Currency.getCanonicalName(currencies[1]), Currency.getCanonicalName(currencies[0]));
    }

    static String formatCurrencyPair(CurrencyPair pair) {
        // TODO(stfinancial): Is there ever actually a case where we need to use the underscore?
        return getCurrencyString(pair.getBase()) /* + "_" */ + getCurrencyString(pair.getQuote());
    }

    static String getCurrencyString(Currency currency) {
        // TODO(stfinancial): There has to be a more elegant solution than a giant switch statement. Maybe an enummap or something.
        switch (currency) {
            case USD_ARB:
                return "USDT";
            default:
                return currency.toString();
        }
    }

//    @Nullable
//    static CurrencyPair parseCurrencyPair(String pair) {
//
//    }
}

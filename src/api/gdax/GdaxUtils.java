package api.gdax;

import api.Currency;
import api.CurrencyPair;
import api.tmp_trade.TradeType;
import com.sun.istack.internal.Nullable;

/**
 * Created by Timothy on 4/23/17.
 */
final class GdaxUtils {
    private static final String BUY_STRING = "buy";
    private static final String SELL_STRING = "sell";

    // TODO(stfinancial): Need a method to parse timestamps from GDAX... form of YYYY-mm-DDTHH:MM:SS.MSMSMSZ

    static double getTakerFeeFromVolumeFraction(double fraction) {
        if (fraction > 0.2) {
            return 0.001;
        } else if (fraction > 0.1) {
            return 0.0015;
        } else if (fraction > 0.05) {
            return 0.0019;
        } else if (fraction > 0.025) {
            return 0.0022;
        } else if (fraction > 0.01) {
            return 0.0024;
        } else {
            return 0.003;
        }
    }

    static String getCommandForTradeType(TradeType type) {
        // TODO(stfinancial): Enum map may be faster, but not sure.
        String result;
        switch (type) {
            case BUY:
                result = BUY_STRING;
                break;
            case SELL:
                result = SELL_STRING;
                break;
            default:
                result = "INVALID";
                break;
        }
        return result;
    }

    // TODO(stfinancial): Perhaps optimize by using TradeType.valueOf ... or making TradeType.BUY the else...?
    @Nullable
    static TradeType getTradeTypeFromString(String type) {
        if (type.equals(SELL_STRING)) {
            return TradeType.SELL;
        } else if (type.equals(BUY_STRING)) {
            return TradeType.BUY;
        } else {
            return null;
//            return TradeType.INVALID;
        }
    }

    static long getTimestampFromGdaxTimestamp(String gdaxTimestamp) {
        // TODO(stfinancial): Implement... 2014-11-07T22:19:28.578544Z
        return 0;
    }


    // TODO(stfinancial): ****** NOTE ****** GDAX DOES CURRENCY PAIRS BACKWARDS FROM POLO... See if this is true in all cases on GDAX and POLO.
    // See how this affects trades. See what is the canonical way on other exchanges.

    @Nullable
    static CurrencyPair parseCurrencyPair(String pair) {
        // TODO(stfinancial): Convert this to optional?
//        System.out.println(pair);
        String[] currencies = pair.split("-", 2);
        if (currencies.length != 2) {
            return null;
        }
        // TODO(stfinancial): Make sure that this is right.
        return CurrencyPair.of(Currency.getCanonicalRepresentation(currencies[0]), Currency.getCanonicalRepresentation(currencies[1]));
    }

    /**
     * Converts a canonical {@code CurrencyPair} into a {@code String} representation for use by the Gdax API.
     * @param pair {@code CurrencyPair} to convert to {@code String} format.
     * @return Poloniex's {@code String} representation of a {@code CurrencyPair}. Returns the Gdax-specific
     * representation of the {@code CurrencyPair}'s base and quote {@link Currency}, respectively, with a hyphen
     * between the two (i.e. XBT and XMR -> "BTC-XMR").
     */
    static String formatCurrencyPair(CurrencyPair pair) {
        return String.format("%s-%s", pair.getBase(), pair.getQuote());
    }
}

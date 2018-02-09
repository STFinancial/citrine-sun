package api.bittrex;

import api.Currency;
import api.CurrencyPair;
import api.tmp_trade.TradeType;
import com.sun.istack.internal.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Created by Timothy on 5/31/17.
 */
final class BittrexUtils {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String LIMIT_BUY_STRING = "LIMIT_BUY";
    private static final String LIMIT_SELL_STRING = "LIMIT_SELL";

    @Nullable
    static TradeType getTradeTypeFromString(String tradeString) {
        if (tradeString.equals(LIMIT_BUY_STRING)) {
            return TradeType.BUY;
        } else if (tradeString.equals(LIMIT_SELL_STRING)) {
            return TradeType.SELL;
        } else {
            return null;
        }
    }

    static String formatCurrencyPair(CurrencyPair pair) {
        // Bittrex does this backwards.
        return getCurrencyString(pair.getQuote()) + "-" + getCurrencyString(pair.getBase());
    }

    static CurrencyPair parseCurrencyPair(String pair) {
        String[] currencies = pair.split("-", 2);
        if (currencies.length != 2) {
            return null;
        }
        // Bittrex API does not conform to the CurrencyPair representation standard.
        return CurrencyPair.of(Currency.getCanonicalName(currencies[1]), Currency.getCanonicalName(currencies[0]));
    }

    /**
     * Converts a timestamp {@code String} as returned by {@link Bittrex} into a UNIX timestamp.
     * @param bittrexTimestamp The timestamp {@code String} as returned by {@code Bittrex}.
     * @return The equivalent UNIX timestamp.
     */
    static long convertTimestamp(String bittrexTimestamp) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(ZoneId.of("UTC"))).appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, true).toFormatter();
        LocalDateTime d = LocalDateTime.from(formatter.parse(bittrexTimestamp));
        return d.atZone(ZoneOffset.UTC).toEpochSecond();
    }

    static String getCurrencyString(Currency c) {
        switch (c) {
            case USD_ARB:
                return "USDT";
            default:
                return c.toString();
        }
    }
}

package api.poloniex;

import api.AccountType;
import api.Currency;
import api.CurrencyPair;
import api.MarginType;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.internal.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Useful helpful methods for {@link Poloniex}.
 */
final class PoloniexUtils {
    // TODO(stfinancial): THREAD LOCAL FOR THREAD SPECIFIC OBJECTS.
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String BUY_STRING = "buy";
    private static final String CAPITALIZED_BUY_STRING = "Buy";
    private static final String SELL_STRING = "sell";
    private static final String CAPITALIZED_SELL_STRING = "Sell";

    private static final String SHORT_STRING = "short";
    private static final String LONG_STRING = "long";

    static Trade getTradeFromJson(JsonNode json, CurrencyPair pair) {
        // TODO(stfinancial): Unmarshall this directly to the object.
        // TODO(stfinancial): See if we can convert to using decimalValue in the future by searching for the proper decimal format.
        TradeType type = getTradeTypeFromString(json.get("type").asText());
        return new Trade(json.get("amount").asDouble(), json.get("rate").asDouble(), pair, type);
    }

    // TODO(stfinancial): This is a bit confusing... maybe just simplify this?
    static String getCommandForTradeType(TradeType type, boolean capitalize) {
        // TODO(stfinancial): Enum map may be faster, but not sure.
        String result;
        switch (type) {
            case BUY:
                result = capitalize ? CAPITALIZED_BUY_STRING : BUY_STRING;
                break;
            case SELL:
                result = capitalize ? CAPITALIZED_SELL_STRING : SELL_STRING;
                break;
            default:
                result = "INVALID";
                break;
        }
        return result;
    }

    static String getNameForAccountType(AccountType type) {
        // TODO(stfinancial): Enum map perhaps?
        String result;
        switch (type) {
            case ALL:
                result = "INVALID";
                break;
            case LOAN:
                result = "lending";
                break;
            case EXCHANGE:
                result =  "exchange";
                break;
            case MARGIN:
                result = "margin";
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


    // TODO(stfinancial): Perhaps optimize by using MarginType.valueOf ... or making MarginType.LONG the else...?
    @Nullable
    static MarginType getMarginTypeFromString(String type) {
        if (type.equals(SHORT_STRING)) {
            return MarginType.SHORT;
        } else if (type.equals(LONG_STRING)) {
            return MarginType.LONG;
        } else {
            return null;
        }
    }

    static long getTimestampFromPoloTimestamp(String poloTimestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(ZoneId.of("UTC"));
//        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        LocalDateTime d = LocalDateTime.from(formatter.parse(poloTimestamp));
        return d.atZone(ZoneOffset.UTC).toEpochSecond();
//        System.out.println("Printing this ridiculous thing: " + d.toEpochSecond(ZoneOffset.UTC));
//        System.out.println("Current time millis: \t\t\t" + System.currentTimeMillis());

        // TODO(stfinancial): Go to DateTimeFormat if this becomes to cumbersome.
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
//        DateTime
//        LocalDateTime date = LocalDateTime.parse(poloTimestamp, formatter);
//        System.out.println(date.toString());
//        return 0;

//        long timestamp = DateFormat.getDateTimeInstance();
    }

    @Nullable
    static CurrencyPair parseCurrencyPair(String pair) {
        // TODO(stfinancial): Convert this to optional.
        String[] currencies = pair.split("_", 2);
        if (currencies.length != 2) {
            return null;
        }
        // Poloniex API does not conform to the CurrencyPair representation standard.
        return CurrencyPair.of(Currency.getCanonicalRepresentation(currencies[1]), Currency.getCanonicalRepresentation(currencies[0]));
    }

    /**
     * Converts a canonical {@code CurrencyPair} into a {@code String} representation for use by the Poloniex API.
     * The Poloniex API does not conform to the standard, so this function will swap the base and quote.
     * @param pair {@code CurrencyPair} to convert to {@code String} format.
     * @return Poloniex's {@code String} representation of a {@code CurrencyPair}. Returns the Poloniex-specific
     * representation of the {@code CurrencyPair}'s base and quote {@link Currency}, respectively, with an
     * underscore between the two (i.e. XBT and XMR -> "XMR_BTC").
     */
    static String formatCurrencyPair(CurrencyPair pair) {
        return String.format("%s_%s", getCurrencyString(pair.getQuote()), getCurrencyString(pair.getBase()));
    }

    // TODO(stfinancial): Could make this an abstract method for Market.
    static String getCurrencyString(Currency currency) {
        // TODO(stfinancial): There has to be a more elegant solution than a giant switch statement. Maybe an enummap or something.
        switch (currency) {
            default:
                return currency.toString();
        }
    }
}

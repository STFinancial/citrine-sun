package api.tmp_trade;

import api.CurrencyPair;

import java.math.BigDecimal;

/**
 * Represents the "kernel" of a trade. Any type of trade, whether completed or order, will contain this.
 */
public final class Trade {
    // TODO(stfinancial): Do we have another constructor/static factory method for double?

    // TODO(stfinancial): Do we need bigdecimal if the smallest granularity is satoshi?
    private final double amount;
    private final double rate;
    private final CurrencyPair pair;
    private final TradeType type;

    // TODO(stfinancial): Consider using isMargin here instead of tradeorder.

    public Trade(double amount, double rate, CurrencyPair pair, TradeType type) {
        this.amount = amount;
        this.rate = rate;
        this.type = type;
        this.pair = pair;
    }

    /** @return The quantity of the quoted {@link api.Currency Currency}. This is the quote {@code Currency} in the {@link CurrencyPair}. */
    public double getAmount() { return amount; }
    /** @return The rate or price of the trade. The units are units of quote {@link api.Currency} per unit of base {@code Currency}. */
    public double getRate() { return rate; }
    /** @return The {@code CurrencyPair} for this {@code Trade}. The {@link Trade#rate} is quoted in the quote {@link api.Currency}. */
    public CurrencyPair getPair() { return pair; }
    /** @return The orientation of this {@code Trade}, either a {@code BUY} or {@code SELL}. */
    public TradeType getType() { return type; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("\tamount: " + amount).append("\n");
        sb.append("\trate: " + rate).append("\n");
        sb.append("\tpair: " + pair.toString()).append("\n");
        sb.append("\ttype: " + type.toString()).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Trade) {
            Trade t = (Trade) o;
            return amount == t.amount && rate == t.rate && t.pair.equals(pair) && type == t.type;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + Double.valueOf(amount).hashCode();
        hash = 31 * hash + Double.valueOf(rate).hashCode();
        hash = 31 * hash + pair.hashCode();
        hash = 31 * hash + type.ordinal();
        return hash;
    }
}
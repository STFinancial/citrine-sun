package api;

/**
 * Created by Timothy on 2/13/17.
 */
public class Ticker {
    private final CurrencyPair pair;
    private final double last;
    private final double lowestAsk;
    private final double highestBid;
    private final double percentChange;
    private final double baseVolume;
    private final double quoteVolume;

    // TODO(stfinancial)
    // isFrozen
    // high24hr
    // low24hr

    private Ticker(Builder builder) {
        this.pair = builder.pair;
        this.last = builder.last;
        this.lowestAsk = builder.lowestAsk;
        this.highestBid = builder.highestBid;
        this.percentChange = builder.percentChange;
        this.baseVolume = builder.baseVolume;
        this.quoteVolume = builder.quoteVolume;
    }

    public CurrencyPair getPair() { return pair; }
    public double getLast() { return last; }
    public double getLowestAsk() { return lowestAsk; }
    public double getHighestBid() { return highestBid; }
    // TODO(stfinancial): HIGH PRIORITY - Should this be 6% = 6 or 6% = 0.06?
    public double getPercentChange() { return percentChange; }
    public double getBaseVolume() { return baseVolume; }
    // TODO(stfinancial): HIGH PRIORITY - Differentiate between price weighted quote volume and quote volume.
    public double getQuoteVolume() { return quoteVolume; }


    public static class Builder {
        private final CurrencyPair pair;
        private final double last;
        private final double lowestAsk;
        private final double highestBid;
        private double percentChange;
        private double baseVolume;
        private double quoteVolume;

        public Builder(CurrencyPair pair, double last, double lowestAsk, double highestBid) {
            this.pair = pair;
            this.last = last;
            this.lowestAsk = lowestAsk;
            this.highestBid = highestBid;
        }

        public Builder percentChange(double percentChange) { this.percentChange = percentChange; return this; }

        public Builder baseVolume(double baseVolume) { this.baseVolume = baseVolume; return this; }

        public Builder quoteVolume(double quoteVolume) { this.quoteVolume = quoteVolume; return this; }

        public Ticker build() {
            return new Ticker(this);
        }

    }

}

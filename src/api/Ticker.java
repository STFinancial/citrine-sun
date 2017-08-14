package api;

/**
 * Contains Ticker information for a {@link CurrencyPair}.
 */
public class Ticker {
    private final CurrencyPair pair;
    private final double last;
    private final double lowestAsk;
    private final double highestBid;
    private final double percentChange;
    private final double baseVolume;
    private final double quoteVolume;
    private final double high24hr;
    private final double low24hr;

    // TODO(stfinancial)
    // isFrozen

    private Ticker(Builder builder) {
        this.pair = builder.pair;
        this.last = builder.last;
        this.lowestAsk = builder.lowestAsk;
        this.highestBid = builder.highestBid;
        this.percentChange = builder.percentChange;
        this.baseVolume = builder.baseVolume;
        this.quoteVolume = builder.quoteVolume;
        this.high24hr = builder.high24hr;
        this.low24hr = builder.low24hr;
    }

    // TODO(stfinancial): How do these getters work if they may not be provided by the market?
    public CurrencyPair getPair() { return pair; }
    public double getLast() { return last; }
    public double getLowestAsk() { return lowestAsk; }
    public double getHighestBid() { return highestBid; }
    // TODO(stfinancial): HIGH PRIORITY - Should this be 6% = 6 or 6% = 0.06?
    public double getPercentChange() { return percentChange; }
    public double getBaseVolume() { return baseVolume; }
    // TODO(stfinancial): HIGH PRIORITY - Differentiate between price weighted quote volume and quote volume.
    public double getQuoteVolume() { return quoteVolume; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("\tpair: ").append(pair.toString()).append("\n");
        sb.append("\tlast: ").append(last).append("\n");
        sb.append("\tlowestAsk: ").append(lowestAsk).append("\n");
        sb.append("\thighestBid: ").append(highestBid).append("\n");
        sb.append("\tpercentChange: ").append(percentChange).append("\n");
        sb.append("\tbaseVolume: ").append(baseVolume).append("\n");
        sb.append("\tquoteVolume: ").append(quoteVolume).append("\n");
        sb.append("\thigh24hr: ").append(high24hr).append("\n");
        sb.append("\tlow24hr: ").append(low24hr).append("\n");
        sb.append("}\n");
        return sb.toString();
    }


    public static class Builder {
        private final CurrencyPair pair;
        private final double last;
        private final double lowestAsk;
        private final double highestBid;
        private double percentChange;
        private double baseVolume;
        private double quoteVolume;
        private double high24hr;
        private double low24hr;

        public Builder(CurrencyPair pair, double last, double lowestAsk, double highestBid) {
            this.pair = pair;
            this.last = last;
            this.lowestAsk = lowestAsk;
            this.highestBid = highestBid;
        }

        public Builder percentChange(double percentChange) { this.percentChange = percentChange; return this; }

        public Builder baseVolume(double baseVolume) { this.baseVolume = baseVolume; return this; }

        public Builder quoteVolume(double quoteVolume) { this.quoteVolume = quoteVolume; return this; }

        public Builder high24hr(double high24hr) { this.high24hr = high24hr; return this; }

        public Builder low24hr(double low24hr) { this.low24hr = low24hr; return this; }

        public Ticker build() {
            return new Ticker(this);
        }
    }
}

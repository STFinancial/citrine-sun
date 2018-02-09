package api;

/**
 * This class is an abstraction for a single market pair in a {@link Market}. It contains a {@link CurrencyPair} as well
 * as {@code Market} specific metadata.
 */
public final class AssetPair {
    // TODO(stfinancial): Do we include the Market in this?
    // TODO(stfinancial): Max price, min price, min tick, decimal precision, frozen.

    private final CurrencyPair pair;
    private final String name;
    private final int id;
    private final double baseMinSize;
    private final double baseMaxSize;
    private final double quoteMinSize;
    private final double quoteMaxSize;

    private AssetPair(Builder b) {
        this.pair = b.pair;
        this.name = b.name;
        this.id = b.id;
        this.baseMinSize = b.baseMinSize;
        this.baseMaxSize = b.baseMaxSize;
        this.quoteMinSize = b.quoteMinSize;
        this.quoteMaxSize = b.quoteMaxSize;
    }

    public CurrencyPair getPair() { return pair; }
    public String getName() { return name; }
    public int getId() { return id; }
    public double baseMinSize() { return baseMinSize; }
    public double baseMaxSize() { return baseMaxSize; }
    public double quoteMinSize() { return quoteMinSize; }
    public double quoteMaxSize() { return quoteMaxSize; }

    public static class Builder {
        private final CurrencyPair pair;
        private final String name;
        private int id;
        private double baseMinSize = 0d;
        private double baseMaxSize = 0d;
        private double quoteMinSize = 0d;
        private double quoteMaxSize = 0d;

        public Builder(CurrencyPair pair, String name) {
            this.pair = pair;
            this.name = name;
        }

        public Builder id(int id) { this.id = id; return this; }
        public Builder baseMinSize(double baseMinSize) { this.baseMinSize = baseMinSize; return this; }
        public Builder baseMaxSize(double baseMaxSize) { this.baseMaxSize = baseMaxSize; return this; }
        public Builder quoteMinSize(double quoteMinSize) { this.quoteMinSize = quoteMinSize; return this; }
        public Builder quoteMaxSize(double quoteMaxSize) { this.quoteMaxSize = quoteMaxSize; return this; }

        public AssetPair build() { return new AssetPair(this); }
    }
}

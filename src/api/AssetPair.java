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

    private AssetPair(Builder b) {
        this.pair = b.pair;
        this.name = b.name;
        this.id = b.id;
    }

    public CurrencyPair getPair() { return pair; }
    public String getName() { return name; }
    public int getId() { return id; }

    public static class Builder {
        private final CurrencyPair pair;
        private final String name;
        private int id;

        public Builder(CurrencyPair pair, String name) {
            this.pair = pair;
            this.name = name;
        }

        public Builder id(int id) { this.id = id; return this; }

        public AssetPair build() { return new AssetPair(this); }
    }
}

package keys;

import api.Credentials;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A class for personal use that can easily obtain files for different markets.
 */
public final class KeyManager {
    // TODO(stfinancial): Why doesn't this return a Credentials instead of the file string?
    public enum Machine {
        LAPTOP("l"),
        DESKTOP("d");

        private final String suffix;

        Machine(String suffix) {
            this.suffix = suffix;
        }
    }

    private static final Map<String, String> KEYS = Collections.unmodifiableMap(new HashMap<String, String>(){{
        put("Poloniex_d", "F:\\\\Users\\\\Zarathustra\\\\Documents\\\\main_key.txt");
        put("Poloniex_l", "/Users/Timothy/Documents/Keys/main_key.txt");
        put("Gdax_d", "F:\\\\Users\\\\Zarathustra\\\\Documents\\\\gdax_key.txt");
        put("Gdax_l", "/Users/Timothy/Documents/Keys/gdax_key.txt");
        put("Kraken_l", "/Users/Timothy/Documents/Keys/kraken_key.txt");
        put("Bitfinex_l", "/Users/Timothy/Documents/Keys/bitfinex_key.txt");
        put("Bittrex_l", "/Users/Timothy/Documents/Keys/bittrex_key.txt");
    }});

    // TODO(stfinancial): Does it make sense to take in the market as a string or create an enum for it?
    public static String getKeyForMarket(String marketName, Machine machine) {
        return KEYS.getOrDefault(marketName + "_" + machine.suffix, "");
    }

    public static Credentials getCredentialsForMarket(String marketName, Machine machine) {
        return Credentials.fromFileString(getKeyForMarket(marketName, machine));
    }
}

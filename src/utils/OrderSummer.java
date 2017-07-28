package utils;

import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.poloniex.Poloniex;
import api.request.OpenOrderRequest;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;

/**
 * Created by Timothy on 12/27/16.
 */
public class OrderSummer {
//    private static final String API_KEYS = "/Users/Timothy/Documents/Keys/main_key.txt";
    private static final String API_KEYS = "F:\\Users\\Zarathustra\\Documents\\main_key.txt";
    private static final CurrencyPair PAIR = CurrencyPair.of(Currency.LTC, Currency.BTC);

    public static void main(String[] args) {
        OrderSummer o = new OrderSummer();
        o.sum();
    }

    private void sum() {
        Credentials c = Credentials.fromFileString(API_KEYS);
        Poloniex polo = new Poloniex(c);
//        parseAndPrintOrders(polo.processMarketRequest(new OpenOrderRequest(1, 1)).getJsonResponse());
        parseAndPrintOrders(polo.processMarketRequest(new OpenOrderRequest(PAIR)).getJsonResponse());
    }

    private void parseAndPrintOrders(JsonNode orders) {
//        System.out.println(orders.toString());
        if (orders.get("errors") != null) {
            System.out.println("There are errors");
            System.out.println(orders.toString());
            return;
        }
        Iterator<JsonNode> os = orders.iterator();
        JsonNode current;
        double sellSum = 0;
        double buySum = 0;
        while (os.hasNext()) {
            current = os.next();
            if (current.get("type").asText().equals("sell")) {
                sellSum += current.get("amount").asDouble();
            } else if (current.get("type").asText().equals("buy")) {
                buySum += current.get("amount").asDouble();
            } else {
                System.out.println("No buy or sell.");
                System.out.println(current.toString());
                return;
            }
        }
        System.out.println("Buy: " + buySum);
        System.out.println("Sell: " + sellSum);
    }

}

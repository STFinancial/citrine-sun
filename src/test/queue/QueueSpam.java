package test.queue;

import api.*;
import api.Currency;
import api.poloniex.Poloniex;
import api.request.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Timothy on 7/11/17.
 */
public class QueueSpam {
    private static final String API_KEYS = "/Users/Timothy/Documents/Keys/main_key.txt";

    private static final Map<Integer, MarketRequest> requests = Collections.unmodifiableMap(new HashMap<Integer, MarketRequest>(){{
        put(0, new TickerRequest(3, 5));
        put(1, new OrderBookRequest(CurrencyPair.of(Currency.LTC, Currency.BTC), 5, 6, 4));
        put(2, new AccountBalanceRequest(AccountType.EXCHANGE, 2, 2));
    }});

    public static void main(String[] args) {
        QueueSpam spam = new QueueSpam();
        spam.spam();
    }

    private void spam() {
        Market m = new Poloniex(Credentials.fromFileString(API_KEYS));
        WorkQueue q = new WorkQueue(m);

        Random r = new Random();
        MarketRequest req;
        Future<MarketResponse> response;
        for (int i = 0; i < 100; ++i) {
            req = requests.get(r.nextInt(3));
            System.out.println("Submitting request");
            response = q.submitWork(req);
            (new FutureCallback(response, ()->{})).run();
        }
    }

    private class FutureCallback implements Runnable {
        private final Future<MarketResponse> response;
        private final Runnable callback;

        FutureCallback(Future<MarketResponse> response, Runnable callback) {
            this.response = response;
            this.callback = callback;
        }

        @Override
        public void run() {
            MarketResponse r = null;
            try {
                r = response.get();
            } catch (Exception e) {
                System.out.println("Exception!: " + e.getMessage());
                e.printStackTrace();
            }
//            try {
//                r = response.get();
//            } catch (ExecutionException e) {
//                System.out.println("Execution Exception!: " + e.getMessage());
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                System.out.println("Interrupted Exception!: " + e.getMessage());
//                e.printStackTrace();
//            }
//            callback.run();
            System.out.println(r.getJsonResponse().toString());
        }
    }

}

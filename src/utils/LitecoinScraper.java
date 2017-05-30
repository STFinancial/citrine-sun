package utils;

import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.Ticker;
import api.poloniex.Poloniex;
import api.request.MarketResponse;
import api.request.TickerRequest;
import api.request.TickerResponse;
import api.request.TradeRequest;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zarathustra on 4/1/2017.
 */
public final class LitecoinScraper {
    //    private static final String API_KEYS = "/Users/Timothy/Documents/Keys/main_key.txt";
    private static final String API_KEYS = "F:\\Users\\Zarathustra\\Documents\\main_key.txt";
    private static final CurrencyPair PAIR = CurrencyPair.of(Currency.LTC, Currency.BTC);
    private static final int SCRAPE_INTERVAL = 6000;
    private static final String SCRAPE_URL = "http://www.litecoinpool.org/pools";
    private static final String CHECK = "✓";

    private final CloseableHttpClient httpClient;

    // TODO(stfinancial): Short if we see F2Pool stop signaling??

    private Map<String, Boolean> signals;
    private final Poloniex polo;

    public static void main(String[] args) {
        LitecoinScraper s = new LitecoinScraper();
        s.run();
    }

    public LitecoinScraper() {
        httpClient = HttpClients.createDefault();
        signals = new HashMap<>();
        Credentials c = Credentials.fromFileString(API_KEYS);
        polo = new Poloniex(c);
    }

    private void run() {
        HttpUriRequest httpRequest = new HttpGet(SCRAPE_URL);
        CloseableHttpResponse response = null;
        Document doc;
        String pool;
        String segwit;
        String segwitBackup;


        while (true) {
            try {
                Thread.sleep(SCRAPE_INTERVAL);
            } catch (InterruptedException e) {
                continue;
            }
//            if (!signals.containsKey("F2Pool") || !signals.get("F2Pool")) {
//                System.out.println("F2Pool not seen signaling.");
//            } else {
//                System.out.println("F2Pool seen signaling");
//            }

//            signals.forEach((k, v) -> {
//                System.out.println(k + ": " + (v ? "True" : "False"));
//            });

            try {
                response = httpClient.execute(httpRequest);
                doc = Jsoup.parse(EntityUtils.toString(response.getEntity()));
                Element entry = doc.select(".wide").select(".alt").first();
                if (entry == null || entry.children() == null || entry.children().get(2) == null) {
                    System.out.println("Null");
                    continue;
                }
                pool = entry.children().get(2).text();
                segwit = entry.select(".good").text();
                segwitBackup = entry.children().get(5).text();
                System.out.println(pool + " - " + segwit + " - " + segwitBackup);
                if (pool == null || pool.isEmpty()) {
                    System.out.println("Null/Empty Pool Name!!!!!!!");
                    continue;
                }
                if (segwit == null) {
                    System.out.println("Null segwit!!!!!!");
                    continue;
                }
                if (segwitBackup == null) {
                    System.out.println("Null segwit backup!!!!!!");
                    continue;
                }
                if (segwit.isEmpty()) {
                    if (segwit.equals(segwitBackup)) {
                        signals.put(pool, false);
                        continue;
                    } else {
                        System.out.println("DISCREPANCY BETWEEN SEGWIT AND BACKUP!!!!");
                        continue;
                    }
                }
                if (segwit.equals(CHECK)) {
                    if (segwit.equals(segwitBackup)) {
                        signals.put(pool, true);
                        if (pool.equals("LTC1BTC") || pool.equals("Antpool") || pool.equals("LTC.top") || pool.equals("BW.com")) {
                            // TODO(stfinancial): Do we check that F2Pool is still signaling?
                            if (!signals.containsKey("F2Pool") || !signals.get("F2Pool")) {
                                System.out.println("F2Pool is no longer signaling. Not purchasing.");
                            }
                            System.out.println("POOL IS SIGNALING SEGWIT. BUYING LITECOIN");
                            MarketResponse resp = polo.processMarketRequest(new TickerRequest(2,2));
                            double price;
                            if (!resp.isSuccess()) {
                                price = 0.012;
                            } else {
                                Ticker t = ((TickerResponse) resp).getTickers().getOrDefault(PAIR, null);
                                if (t == null) {
                                    price = 0.012;
                                } else {
                                    price = t.getLowestAsk();
                                }
                            }
                            TradeRequest req = new TradeRequest(new Trade(1 / price, price + 0.001, PAIR, TradeType.BUY), 2, 2);
                            req.setIsMargin(true);
                            req.setTimeInForce(TradeRequest.TimeInForce.IMMEDIATE_OR_CANCEL);
                            int success_count = 0;
                            int tries_remaining = 20;
                            while (success_count < 6 && tries_remaining-- > 0) {
                                // In case of changes in collateral available, we place 5 small trades instead of 1 large that may fail.
                                resp = polo.processMarketRequest(req);
                                if (!resp.isSuccess()) {
                                    try {
                                        System.out.println(resp.getJsonResponse());
                                        Thread.sleep(250);
                                        continue;
                                    } catch (InterruptedException e) {
                                        continue;
                                    }
                                } else {
                                    ++success_count;
                                }
                            }
                            // Now where do we place the sell orders?
                            req = new TradeRequest(new Trade(success_count / price, price * 1.39, PAIR, TradeType.SELL), 2, 2);
                            req.setIsMargin(true);
                            do {
                                try {
                                    Thread.sleep(250);
                                } catch (InterruptedException e) {}
                            } while (!polo.processMarketRequest(req).isSuccess());
                            return;
                        }
                    } else {
                        System.out.println("DISCREPANCY BETWEEN SEGWIT (CHECK) AND BACKUP!!!!");
                        continue;
                    }
                    continue;
                }
                System.out.println("Segwit is illegal character: " + segwit);
                continue;
            } catch (IOException e) {
                try {
                    Thread.sleep(SCRAPE_INTERVAL);
                } catch (InterruptedException i) {
                    continue;
                }
            }
        }


    }
}

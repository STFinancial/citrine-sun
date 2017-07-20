package utils;

import api.Credentials;
import api.CurrencyPair;
import api.poloniex.Poloniex;
import api.request.MarketResponse;
import api.request.OpenOrderRequest;
import api.request.OpenOrderResponse;
import api.tmp_trade.TradeOrder;
import keys.KeyManager;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Plays an alert when one of our trades has been hit.
 */
public class TradeAlerter {
    // TODO(stfinancial): Consider setting up a key manager for this.
    private static final String API_KEYS = KeyManager.getKeyForMarket("Poloniex", KeyManager.Machine.LAPTOP);

    private Map<CurrencyPair, List<TradeOrder>> prevOrders = new HashMap<>();

    public static void main(String[] args) {
        TradeAlerter s = new TradeAlerter();
        s.run();
    }

    private void run() {
        File audioFile = null;
        Clip sound = null;
        if (audioFile == null) {
            audioFile = new File("./data/sounds/alerts/caching.wav");
        }

        Credentials c = Credentials.fromFileString(API_KEYS);
        Poloniex polo = new Poloniex(c);
        MarketResponse resp = polo.processMarketRequest(new OpenOrderRequest());
        if (resp.isSuccess()) {
            OpenOrderResponse ooResp = (OpenOrderResponse) resp;
            prevOrders = ooResp.getOpenOrders();
        } else {
            System.out.println(resp.getJsonResponse().toString());
            return;
        }

        HashMap<CurrencyPair, Double> totals = new HashMap<>();
        //TODO(stfinancial): This is going off even when a trade is partially filled. See if we can fix this issue.
        while (true) {
            try {
                System.out.println("Sleeping");
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                return;
            }
            totals.clear();
            resp = polo.processMarketRequest(new OpenOrderRequest());
            if (resp.isSuccess()) {
                OpenOrderResponse ooResp = (OpenOrderResponse) resp;
//                System.out.println(ooResp.getJsonResponse());
                for (Map.Entry<CurrencyPair, List<TradeOrder>> prevOrderList : prevOrders.entrySet()) {
                    if (ooResp.getOpenOrders().containsKey(prevOrderList.getKey())) {
                        for (TradeOrder o : prevOrderList.getValue()) {
                            if (!ooResp.getOpenOrders().get(prevOrderList.getKey()).contains(o)) {
                                totals.put(o.getTrade().getPair(), totals.getOrDefault(o.getTrade().getPair(), 0d) + o.getTrade().getAmount());
                                System.out.println(o);
                                loadClip(sound, audioFile);
                            }
                        }
                    } else {
                        loadClip(sound, audioFile);
                    }
                }
                prevOrders = ooResp.getOpenOrders();
            } else {
                System.out.println("Failure: " + resp.getJsonResponse().toString());
                continue;
            }
            for (Map.Entry e : totals.entrySet()) {
                System.out.println(e.getKey().toString() + ": " + e.getValue());
            }
        }
    }

    private void loadClip(Clip sound, File file) {
        try {
            sound = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
            sound.open(AudioSystem.getAudioInputStream(file));
            sound.start();
            sound.drain();
        } catch (LineUnavailableException e) {
            // TODO(stfinancial): May want to throw these to the client instead.
            e.printStackTrace();
            System.out.println("Audio line unavailable, could not play alert.");
            return;
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
            System.out.println("Audio file format unsupported: " + e.getMessage());
            return;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Exception while attempting to play clip.");
            return;
        }
    }
}

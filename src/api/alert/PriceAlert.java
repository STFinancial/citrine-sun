package api.alert;

import api.CurrencyPair;
import api.Market;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by Timothy on 1/16/17.
 */
public class PriceAlert {
    // TODO(stfinancial): Maybe move this to a package for the actual trading platform application, rather than the market api package.


    // TODO(stfinancial): Not sure how exactly should this be done. We may want to send this alert to the market and have it take care of playing the clip as needed
    // The other option is subscribing to the WAMP data (not all markets may have this). And responding accordingly.

    String fileString;
    double price;
    CurrencyPair pair;
    Market market;

    private File audioFile;
    private Clip sound;

    public PriceAlert(String fileString, double price, CurrencyPair pair, Market market) {
        this.fileString = fileString;
        this.price = price;
        this.pair = pair;
        this.market = market;
    }

    public void playAlert() {
        if (audioFile == null) {
            audioFile = new File(fileString);
        }
        try {
            sound = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
            sound.open(AudioSystem.getAudioInputStream(audioFile));
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

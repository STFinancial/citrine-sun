package api;

import com.sun.istack.internal.Nullable;

import java.math.BigDecimal;

/**
 * Created by Timothy on 1/15/17.
 */
public final class FeeInfo {
    // TODO(stfinancial): This class needs a ton of cleanup.

    private final double makerFee;
    private final double takerFee;
//    private CurrencyPair currencyPair;
    // TODO(stfinancial): We need to assign a currencyPair value to this volume?
    // e.g. for kraken, what is the currency for this, can it be algorithmically determined?
    // TODO(stfinancial): What is the unit for this number?
    private double currentVolume;
//    private BigDecimal nextTierVolume;
//    private BigDecimal nextTierFee;

    // TODO(stfinancial): Not really sure about this class.

    // TODO(stfinancial): This class will likely change as we discover more fee structures.
    public FeeInfo(double makerFee, double takerFee, double currentVolume) {
        this.makerFee = makerFee;
        this.takerFee = takerFee;
        this.currentVolume = currentVolume;
    }

    // TODO(stfinancial): This is a pretty bulky constructor.
//    public FeeInfo(BigDecimal makerFee, BigDecimal takerFee, CurrencyPair currencyPair, BigDecimal currentVolume, BigDecimal nextTierVolume, BigDecimal nextTierFee) {
//        this(makerFee, takerFee);
////        this.currencyPair = currencyPair;
//        this.currentVolume = currentVolume;
////        this.nextTierVolume = nextTierVolume;
////        this.nextTierFee = nextTierFee;
//    }

    public double getMakerFee() { return makerFee; }
    public double getTakerFee() { return takerFee; }

//    /**
//     * @return The {@code CurrencyPair} corresponding to this particular {@code FeeInfo}. Returns null if this is a {@code Market}-wide fee.
//     */
//    @Nullable public CurrencyPair getCurrencyPair() { return currencyPair; }
    // TODO(stfinancial): Rename this 30 day trailing volume.
    public double getCurrentVolume() { return currentVolume; }
//    public BigDecimal getNextTierVolume() { return nextTierVolume; }
//    public BigDecimal getNextTierFee() { return nextTierFee; }
}

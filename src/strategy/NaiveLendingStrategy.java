package strategy;

import api.AccountType;
import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.poloniex.Poloniex;
import api.poloniex.PoloniexConstants;
import api.request.*;
import api.request.tmp_loan.*;
import api.tmp_loan.Loan;
import api.tmp_loan.LoanType;
import api.tmp_loan.PrivateLoanOrder;
import api.tmp_loan.PublicLoanOrder;

import java.math.BigDecimal;
import java.util.*;


public final class NaiveLendingStrategy extends Strategy {
    // TODO(stfinancial): Move 25% of gains to margin account, 25% to exchange to move to wallets.

    private static final String API_KEYS = "/Users/Timothy/Documents/Keys/main_key.txt";
//    private static final String API_KEYS = "F:\\Users\\Zarathustra\\Documents\\main_key.txt";

    // Instead of undercutting, we place the loan at this fraction of total daily trade volume deep into the order book.
    private static final double VOLUME_FRACTION = 0.00076;
    // The number of seconds to wait before changing the order. This should be related to the VOLUME_FRACTION.
    private static final double ORDER_DURATION = 300;
    private static final double TOLERABLE_LENDING_MULTIPLIER = 50;

    private static final double MIN_TRANSFER_AMOUNT = 0.00001;
    private static final double EXCHANGE_TRANSFER_FRACTION = 0.35;
    private static final double MARGIN_TRANSFER_FRACTION = 0.05;

    private static final Set<Currency> BLACKLIST = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            Currency.LTC
    )));

    private Poloniex polo;
    // TODO(stfinancial): Figure a way to stop using these defined constants.
    private PoloniexConstants constants;

    public NaiveLendingStrategy(Poloniex polo) {
        this.polo = polo;
    }

    // TODO(stfinancial): Clean up this code.

    public static void main(String[] args) {
        Credentials c = Credentials.fromFileString(API_KEYS);
        NaiveLendingStrategy strat = new NaiveLendingStrategy(new Poloniex(c));
        strat.run();
    }

    @Override
    public void run() {
        if (EXCHANGE_TRANSFER_FRACTION + MARGIN_TRANSFER_FRACTION > 1) {
            System.out.println("Transfer fractions cannot add to be greater than 1.");
            return;
        }

        // Get the constants
        constants = (PoloniexConstants) polo.getConstants();
        long currentTime = System.currentTimeMillis();
        long prevTime;

        while (true) {
            prevTime = currentTime;
            currentTime = System.currentTimeMillis();
            MarketResponse resp;

            do {
                sleep(300);
                // TODO(stfinancial): Figure out why this doesn't work. The timeframe may be too short for Poloniex.
            } while (!(resp = polo.processMarketRequest(new GetLendingHistoryRequest(prevTime, currentTime, 1, 1))).isSuccess());
            // Transfer gains.
            ((GetLendingHistoryResponse) resp).getLoans().forEach((loan) -> {
                double earned = loan.getEarned();
                Currency currency = loan.getLoan().getCurrency();
                System.out.println("Completed Loan " + earned + currency.toString());
//                System.exit(0);
                if (earned < MIN_TRANSFER_AMOUNT) {
                    System.out.println("Loan below minimum amount. No transfer.");
                    return;
                }

                do {
                    sleep(300);
                    System.out.println("Transferring " + (earned * EXCHANGE_TRANSFER_FRACTION) + currency.toString() + " to EXCHANGE.");
                } while (!polo.processMarketRequest(new TransferBalanceRequest(currency, earned * EXCHANGE_TRANSFER_FRACTION, AccountType.LOAN, AccountType.EXCHANGE, 1, 1)).isSuccess());
                do {
                    sleep(300);
                    System.out.println("Transferring " + (earned * MARGIN_TRANSFER_FRACTION) + currency.toString() + " to MARGIN.");
                } while (!polo.processMarketRequest(new TransferBalanceRequest(currency, earned * MARGIN_TRANSFER_FRACTION, AccountType.LOAN, AccountType.MARGIN, 1, 1)).isSuccess());
            });


            // Obtain all private open loan offers and cancel them.
            do {
                sleep(300);
            } while (!(resp = polo.processMarketRequest(new GetPrivateLoanOffersRequest(1, 1))).isSuccess());

            // TODO(stfinancial): this check is redundant, fix this.
            if (resp.isSuccess()) {
                GetPrivateLoanOffersResponse r = (GetPrivateLoanOffersResponse) resp;
                r.getOffers().values().forEach((offers)->{
                    offers.forEach((offer)->{
                        if ((offer.getTimestamp() + ORDER_DURATION) * 1000 <= System.currentTimeMillis()) {
                            System.out.println("Canceling stale order.");
//                            System.out.println("Current: " + System.currentTimeMillis());
//                            System.out.println("Stale time: " + (offer.getTimestamp() + ORDER_DURATION) * 1000);
                            do {
                                sleep(300);
                            } while(!polo.processMarketRequest(new CancelRequest(offer.getOrderId(), CancelRequest.CancelType.LOAN, 1, 1)).isSuccess());
                        }
                    });
                });
            }
//            System.out.println(resp.getJsonResponse());

            sleep(300);

            // Get account balances.
            resp = polo.processMarketRequest(new AccountBalanceRequest(AccountType.LOAN, 1, 1));

            Map<Currency, Double> balances;
            if (resp.isSuccess()) {
                // TODO(stfinancial): Poloniex saying that there is nothing in lending account when there actually is. Need to repro the issue and figure out what is up with that.
                balances = ((AccountBalanceResponse) resp).getBalances().get(AccountType.LOAN);
//                System.out.println(resp.getJsonResponse().toString());
            } else {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    continue;
                }
                continue;
            }

            resp = polo.processMarketRequest(new VolumeRequest(1, 1));
            Map<Currency, Double> volumes;
            if (resp.isSuccess()) {
                volumes = ((VolumeResponse) resp).getCurrencyVolumes();
//                System.out.println("BTC Volume Test: " + volumes.get(Currency.ETH));
            } else {
                System.out.println("Could not get volumes.");
                sleep(5000);
                continue;
            }

            // Get all the tmp_loan currencies.
            for (Currency c : constants.getLendableCurrencies()) {
                if (BLACKLIST.contains(c)) {
                    continue;
                }
                if (!balances.containsKey(c)) {
//                    System.out.println("No balance for: " + c.toString());
                    continue;
                }
                if (balances.get(c).compareTo(constants.getMinLendingAmount()) < 1) {
                    System.out.println("Balance is too small for: " + c.toString() + " : " + balances.get(c));
                    continue;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    continue;
                }
                resp = polo.processMarketRequest(new GetPublicLoanOrdersRequest(c, 1, 1));
                if (!resp.isSuccess()) {
                    System.out.println("Failure processing request: " + resp.getJsonResponse().toString());
                    continue;
                }
                List<PublicLoanOrder> offers = ((GetPublicLoanOrdersResponse) resp).getOffers();
                Loan loan;
                PrivateLoanOrder order;
                if (offers.size() == 0) {
                    loan = new Loan(balances.get(c), constants.getMaxLendingRate(), c, LoanType.OFFER);
                    order = new PrivateLoanOrder(loan, "1", System.currentTimeMillis(), 60, false);
                } else {
                    double rate = offers.get(0).getRate() - constants.getLendingRateIncrement();
                    double thresholdVolume = VOLUME_FRACTION * volumes.getOrDefault(c, 0.0);

                    double volumeTotal = 0.0;
                    for (PublicLoanOrder offer : offers) {
                        volumeTotal += offer.getAmount();
                        if (volumeTotal > thresholdVolume) {
                            rate = offer.getRate() - constants.getLendingRateIncrement();
                            break;
                        }
                    }
                    if (volumeTotal <= thresholdVolume) {
                        rate = constants.getMaxLendingRate();
                    }

                    int duration = constants.getMinLendingDuration();
                    if (rate < constants.getMinLendingRate() * TOLERABLE_LENDING_MULTIPLIER) {
                        rate = constants.getMinLendingRate() * TOLERABLE_LENDING_MULTIPLIER;
                    } else if (rate > 0.00049) {
                        duration = constants.getMaxLendingDuration();
                    }
                    loan = new Loan(balances.get(c), rate, c, LoanType.OFFER);
                    order = new PrivateLoanOrder(loan, "1", System.currentTimeMillis(), duration, false);
                }
                resp = polo.processMarketRequest(new CreateLoanOfferRequest(order, 1, 1));
                System.out.println("Placing loan offer: { amount: " + order.getAmount() + ", rate: " + order.getRate() + ", currency: " + order.getCurrency().toString() + ", duration: " + order.getDuration() + " }");
                if (!resp.isSuccess()) {
                    System.out.println("Failure placing request: " + resp.getJsonResponse().toString());
                }
            }

            sleep(30000);
        }
    }

    private void sleep(long millis) {
        try {
//            System.out.println("Sleeping " + millis + "...");
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            return;
        }
    }
}
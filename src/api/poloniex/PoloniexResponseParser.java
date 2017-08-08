package api.poloniex;

// TODO(stfinancial): Establish naming convention for json vs java object.

// TODO(stfinancial): Consider refactoring lambda expressions to helper functions.
// TODO(stfinancial): Instead of parsing JSON, do we want to use the object mapper to map to Poloniex internal classes, which are then mapped to the generics?



import api.*;
import api.Currency;
import api.request.*;
import api.request.tmp_loan.*;
import api.request.tmp_trade.MoveOrderRequest;
import api.tmp_loan.*;
import api.tmp_trade.CompletedTrade;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeOrder;
import api.tmp_trade.TradeType;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.util.converter.BigDecimalStringConverter;

import java.util.*;

/**
 * Converts a {@link com.fasterxml.jackson.databind.JsonNode JsonNode} response from {@link Poloniex} into a
 * {@link api.Market} agnostic {@link api.request.MarketResponse}.
 */
final class PoloniexResponseParser {
    // TODO(stfinancial): We are currently not consistent about whether these methods return a MarketResponse or a specific type of response. Fix this.


    // TODO(stfinancial): Write a function that takes in a JSON node, and for each element in the node, maps to a list of resulting classes
    // e.g. List<T> mapJSon(JsonNode j, Mapper<JsonNode, T>)



    // TODO(stfinancial): Helper function to create trade from JsonNode.
    // TODO(stfinancial): If we're going to check for a jsonResponse error in every method, do it earlier. Maybe check for success instead?

    // TODO(stfinancial): Change the returnXResponse functions to return the specific type of MarketResponse?

    // TODO(stfinancial): Check this doesn't have the same issues as decimalValue.
    private static final BigDecimalStringConverter converter = new BigDecimalStringConverter();

    // TODO(stfinancial): We need to check to make sure we sent the right kind of command and arguments and make sure to fail on our side if that happens.
    static MarketResponse constructMarketResponse(JsonNode jsonResponse, MarketRequest request, long timestamp) {
        // TODO(stfinancial): Is there a better way to do this, instead of checking the types repeatedly?
        // TODO(stfinancial): Potentially separate public and private methods.
        if (jsonResponse.isNull()) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNPARSABLE_RESPONSE));
        }
        if (jsonResponse.has("error")) {
            // TODO(stfinancial): Think about if we need specific response types for this later.
            // TODO(stfinancial): Think about whether it makes sense to do more robust checking of Json responses and returning more detailed error responses.
            // TODO(stfinancial): Think whether it makes sense to also check for the presence of a success flag (success:0).
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.MARKET_ERROR, jsonResponse.get("error").asText()));
        }
        // TODO(stfinancial): Potentially bind these methods in a map to improve readability.
        if (request instanceof TradeRequest) {
            return createTradeResponse(jsonResponse, (TradeRequest) request, timestamp);
        } else if (request instanceof CancelRequest) {
            return createCancelResponse(jsonResponse, (CancelRequest) request, timestamp);
        } else if (request instanceof OpenOrderRequest) {
            // TODO(stfinancial): Rename this method if we change the name of OpenOrderResponse
            return createOpenOrderResponse(jsonResponse, (OpenOrderRequest) request, timestamp);
        } else if (request instanceof AccountBalanceRequest) {
            return createAccountBalanceResponse(jsonResponse, (AccountBalanceRequest) request, timestamp);
        } else if (request instanceof OrderBookRequest) {
            return createOrderBookResponse(jsonResponse, (OrderBookRequest) request, timestamp);
        } else if (request instanceof GetPublicLoanOrdersRequest) {
            return createGetPublicLoanOrdersResponse(jsonResponse, (GetPublicLoanOrdersRequest) request, timestamp);
        } else if (request instanceof CreateLoanOfferRequest) {
            // LOL...
            return createCreateLoanOfferResponse(jsonResponse, (CreateLoanOfferRequest) request, timestamp);
        } else if (request instanceof TickerRequest) {
            return createTickerResponse(jsonResponse, (TickerRequest) request, timestamp);
        } else if (request instanceof MoveOrderRequest) {
            // TODO(stfinancial): Potentially rename this, as there is no such thing as a moveorderresponse.
            return createMoveOrderResponse(jsonResponse, (MoveOrderRequest) request, timestamp);
        } else if (request instanceof TradeHistoryRequest) {
            return createTradeHistoryResponse(jsonResponse, (TradeHistoryRequest) request, timestamp);
        } else if (request instanceof MarginPositionRequest) {
            return createMarginPositionResponse(jsonResponse, (MarginPositionRequest) request, timestamp);
        } else if (request instanceof MarginAccountSummaryRequest) {
            return createMarginAccountSummaryResponse(jsonResponse, (MarginAccountSummaryRequest) request, timestamp);
        } else if (request instanceof GetPrivateLoanOffersRequest) {
            return createGetPrivateLoanOffersResponse(jsonResponse, (GetPrivateLoanOffersRequest) request, timestamp);
        } else if (request instanceof TransferBalanceRequest) {
            return createTransferBalanceResponse(jsonResponse, (TransferBalanceRequest) request, timestamp);
        } else if (request instanceof VolumeRequest) {
            return createVolumeResponse(jsonResponse, (VolumeRequest) request, timestamp);
        } else if (request instanceof GetLendingHistoryRequest) {
            return createGetLendingHistoryResponse(jsonResponse, (GetLendingHistoryRequest) request, timestamp);
        } else if (request instanceof FeeRequest) {
            return createFeeResponse(jsonResponse, (FeeRequest) request, timestamp);
        } else if (request instanceof GetActiveLoansRequest) {
            return createGetActiveLoansResponse(jsonResponse, (GetActiveLoansRequest) request, timestamp);
        }
        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }

    // TODO(stfinancial): Why are these methods static? Will this cause error when we have multiple accounts?

    private static MarketResponse createGetPublicLoanOrdersResponse(JsonNode jsonResponse, GetPublicLoanOrdersRequest request, long timestamp) {
        // TODO(stfinanical): Optionally initialize this size to the size specified in the request.
        List<PublicLoanOrder> offers = new ArrayList<>();
        jsonResponse.get("offers").forEach((offer)->{
            offers.add(new PublicLoanOrder(request.getCurrency(), offer.get("rate").asDouble(), offer.get("amount").asDouble(), offer.get("rangeMin").asInt(), offer.get("rangeMax").asInt()));
        });
        List<PublicLoanOrder> demands = new ArrayList<>();
        jsonResponse.get("demands").forEach((demand)->{
            demands.add(new PublicLoanOrder(request.getCurrency(), demand.get("rate").asDouble(), demand.get("amount").asDouble(), demand.get("rangeMin").asInt(), demand.get("rangeMax").asInt()));
        });
        return new GetPublicLoanOrdersResponse(offers, demands, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static MarketResponse createGetPrivateLoanOffersResponse(JsonNode jsonResponse, GetPrivateLoanOffersRequest request, long timestamp) {
        Map<Currency, List<PrivateLoanOrder>> offersMap = new HashMap<>();
        jsonResponse.fields().forEachRemaining((offers) -> {
            List<PrivateLoanOrder> orders = new LinkedList<>();
            offers.getValue().forEach((offer) -> {
                orders.add(new PrivateLoanOrder(new Loan(offer.get("amount").asDouble(), offer.get("rate").asDouble(), Currency.getCanonicalRepresentation(offers.getKey()), LoanType.OFFER), offer.get("id").asText(), PoloniexUtils.getTimestampFromPoloTimestamp(offer.get("date").asText()), offer.get("duration").asInt(), offer.get("autoRenew").asBoolean()));
            });
            offersMap.put(Currency.getCanonicalRepresentation(offers.getKey()), orders);
        });
        return new GetPrivateLoanOffersResponse(offersMap, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static MarketResponse createCreateLoanOfferResponse(JsonNode jsonResponse, CreateLoanOfferRequest request, long timestamp) {
        return new CreateLoanOfferResponse(jsonResponse.get("orderID").asLong(), jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static MarketResponse createMoveOrderResponse(JsonNode jsonResponse, MoveOrderRequest request, long timestamp) {
        // TODO(stfinancial): Still not sure how this can result in trades of other currencies, but we will see.
        Map<CurrencyPair, List<CompletedTrade>> completedTradeMap = new HashMap<>();
        // TODO(stfinancial): This is where an object mapper would come in handy.
        jsonResponse.get("resultingTrades").fields().forEachRemaining((resultingTradeList)->{
            CurrencyPair pair = PoloniexUtils.parseCurrencyPair(resultingTradeList.getKey());
            List<CompletedTrade> completedTrades = new LinkedList<>();
            resultingTradeList.getValue().forEach((completedTrade)->{
                // TODO(stfinancial): Once this shit is under control (maybe don't use lambda function). Fill in the other fields.
                completedTrades.add((new CompletedTrade.Builder(PoloniexUtils.getTradeFromJson(completedTrade, pair), completedTrade.get("tradeID").asText(), PoloniexUtils.getTimestampFromPoloTimestamp(completedTrade.get("date").asText())).build()));
            });
            completedTradeMap.put(pair, completedTrades);
        });
        System.out.println(jsonResponse.toString());
        return new MoveOrderResponse(jsonResponse.get("orderNumber").asText(), completedTradeMap, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static MarketResponse createTradeHistoryResponse(JsonNode jsonResponse, TradeHistoryRequest request, long timestamp) {
        // TODO(stfinancial): Test that this actually works.
        Map<CurrencyPair, List<CompletedTrade>> completedTrades = new HashMap<>();
        if (request.getPair() != null) {
            List<CompletedTrade> trades = new ArrayList<>();
            jsonResponse.elements().forEachRemaining((t) -> {
                CompletedTrade.Builder b = new CompletedTrade.Builder(PoloniexUtils.getTradeFromJson(t, request.getPair()), t.get("tradeID").asText(), PoloniexUtils.getTimestampFromPoloTimestamp(t.get("date").asText()));
                b.category(PoloniexUtils.parseCategory(t.get("category").asText()));
                b.globalTradeId(t.get("globalTradeID").asText());
//                b.fee(t.get("fee").asDouble()); // TODO(stfinancial): Decide whether to convert this to quote or base currency.
                // TODO(stfinancial): Infer isMake from the fee and our fee rate.
                trades.add(b.build());
            });
            completedTrades.put(request.getPair(), trades);
        } else {
            jsonResponse.fields().forEachRemaining((tradesForPairs) -> {
                System.out.println(tradesForPairs.getKey());
                CurrencyPair pair = PoloniexUtils.parseCurrencyPair(tradesForPairs.getKey());
                List<CompletedTrade> trades = new ArrayList<>();
                tradesForPairs.getValue().elements().forEachRemaining((t) -> {
                    // TODO(stfinancial): Ensure that "amount" is before fees.
                    CompletedTrade.Builder b = new CompletedTrade.Builder(PoloniexUtils.getTradeFromJson(t, pair), t.get("tradeID").asText(), PoloniexUtils.getTimestampFromPoloTimestamp(t.get("date").asText()));
                    b.category(PoloniexUtils.parseCategory(t.get("category").asText()));
                    b.globalTradeId(t.get("globalTradeID").asText());
                    b.fee(t.get("fee").asDouble());
                    // TODO(stfinancial): Infer isMake from the fee and our fee rate.
                    trades.add(b.build());
                });
                completedTrades.put(pair, trades);
            });
        }
        return new TradeHistoryResponse(completedTrades, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static MarketResponse createOrderBookResponse(JsonNode jsonResponse, OrderBookRequest request, long timestamp) {
        Map<CurrencyPair, List<Trade>> asksSet = new HashMap<>();
        Map<CurrencyPair, List<Trade>> bidsSet = new HashMap<>();

        if (request.getCurrencyPair().isPresent()) {
            CurrencyPair pair = request.getCurrencyPair().get();
            List<Trade> asks = new ArrayList<>();
            List<Trade> bids = new ArrayList<>();
            jsonResponse.get("asks").forEach((order) -> {
                asks.add(new Trade(order.get(1).asDouble(), order.get(0).asDouble(), pair, TradeType.SELL));
            });
            asksSet.put(pair, asks);
            jsonResponse.get("bids").forEach((order) -> {
                bids.add(new Trade(order.get(1).asDouble(), order.get(0).asDouble(), pair, TradeType.BUY));
            });
            bidsSet.put(pair, bids);
        } else {
            jsonResponse.fields().forEachRemaining((book) -> {
                List<Trade> asks = new ArrayList<>();
                List<Trade> bids = new ArrayList<>();
                CurrencyPair pair = PoloniexUtils.parseCurrencyPair(book.getKey());
                book.getValue().get("asks").forEach((order) -> {
                    asks.add(new Trade(order.get(1).asDouble(), order.get(0).asDouble(), pair, TradeType.SELL));
                });
                asksSet.put(pair, asks);
                book.getValue().get("bids").forEach((order) -> {
                    bids.add(new Trade(order.get(1).asDouble(), order.get(0).asDouble(), pair, TradeType.BUY));
                });
                bidsSet.put(pair, bids);
            });
        }
        return new OrderBookResponse(asksSet, bidsSet, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static MarketResponse createMarginPositionResponse(JsonNode jsonResponse, MarginPositionRequest request, long timestamp) {
        MarginPosition position = new MarginPosition(
                request.getPair(),
                PoloniexUtils.getMarginTypeFromString(jsonResponse.get("type").asText()),
                jsonResponse.get("amount").asDouble(),
                jsonResponse.get("total").asDouble(),
                jsonResponse.get("basePrice").asDouble(),
                jsonResponse.get("liquidationPrice").asDouble(),
                jsonResponse.get("pl").asDouble(),
                jsonResponse.get("lendingFees").asDouble()
        );
        return new MarginPositionResponse(position, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static MarketResponse createMarginAccountSummaryResponse(JsonNode jsonResponse, MarginAccountSummaryRequest request, long timestamp) {
        MarginAccountSummary summary = new MarginAccountSummary(
                jsonResponse.get("totalValue").doubleValue(),
                jsonResponse.get("pl").doubleValue(),
                jsonResponse.get("netValue").doubleValue(),
                jsonResponse.get("lendingFees").doubleValue(),
                jsonResponse.get("totalBorrowedValue").doubleValue(),
                jsonResponse.get("currentMargin").doubleValue()
        );
        return new MarginAccountSummaryResponse(summary, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static MarketResponse createAccountBalanceResponse(JsonNode jsonResponse, AccountBalanceRequest request, long timestamp) {
        // TODO(stfinancial): May consider optimizing this.
        Map<AccountType, Map<Currency, Double>> balances = new HashMap<>();
        // We are more likely to want speed when we specify LOAN, so we'll put that first.
        if (request.getType() == AccountType.LOAN || request.getType() == AccountType.ALL) {
//            System.out.println("Loan balance request.");
            HashMap<Currency, Double> loanBalances = new HashMap<>();
            jsonResponse.get("lending").fields().forEachRemaining((entry)->{
//                System.out.println(entry.toString());
                loanBalances.put(Currency.getCanonicalRepresentation(entry.getKey()), entry.getValue().asDouble());
            });
            balances.put(AccountType.LOAN, loanBalances);
//            for (Map.Entry<Currency, Double> e : loanBalances.entrySet()) {
//                System.out.println(e.getKey().toString() + " : " + e.getValue());
//            }
        }
        if (request.getType() == AccountType.EXCHANGE || request.getType() == AccountType.ALL) {
            HashMap<Currency, Double> exchangeBalances = new HashMap<>();
            jsonResponse.get("exchange").fields().forEachRemaining((entry)->{
                exchangeBalances.put(Currency.getCanonicalRepresentation(entry.getKey()), entry.getValue().asDouble());
            });
            balances.put(AccountType.EXCHANGE, exchangeBalances);
        }
        if (request.getType() == AccountType.MARGIN || request.getType() == AccountType.ALL) {
            HashMap<Currency, Double> marginBalances = new HashMap<>();
            jsonResponse.get("margin").fields().forEachRemaining((entry)->{
                marginBalances.put(Currency.getCanonicalRepresentation(entry.getKey()), entry.getValue().asDouble());
            });
            balances.put(AccountType.MARGIN, marginBalances);
        }

        // TODO(stfinancial): Need to think carefully about whether we want to set the timestamp as current time at the end of this function.
        return new AccountBalanceResponse(balances, jsonResponse, request, timestamp, new RequestStatus(StatusType.SUCCESS));
    }

    // TODO(stfinancial): Deprecate in favor of returnPrivateInfo, which gives stops, loanavailable, and limit.
    private static MarketResponse createOpenOrderResponse(JsonNode jsonResponse, OpenOrderRequest request, long timestamp) {
        // TODO(stfinancial): Careful, this call doesn't actually seem to return stop limits...

        System.out.println(jsonResponse);

        // TODO(stfinancial): Stop limit and loanavailable.
        Map<CurrencyPair, List<TradeOrder>> orders = new HashMap<>();
        if (request.getCurrencyPair() != null) {
            LinkedList<TradeOrder> openOrders = new LinkedList<>();
            jsonResponse.elements().forEachRemaining((openOrder) -> {
                openOrders.add(new TradeOrder(PoloniexUtils.getTradeFromJson(openOrder, request.getCurrencyPair()), openOrder.get("orderNumber").asText(), PoloniexUtils.getTimestampFromPoloTimestamp(openOrder.get("date").asText()), openOrder.get("margin").asBoolean()));
            });
            orders.put(request.getCurrencyPair(), openOrders);
        } else {
            jsonResponse.fields().forEachRemaining((openOrderSet) -> {
                CurrencyPair pair = PoloniexUtils.parseCurrencyPair(openOrderSet.getKey());
                LinkedList<TradeOrder> openOrders = new LinkedList<>();
                openOrderSet.getValue().elements().forEachRemaining((openOrder) -> {
                    // TODO(stfinancial): Figure out what the starting amount field actually does. Is this related to a half filled trade?
                    openOrders.add(new TradeOrder(PoloniexUtils.getTradeFromJson(openOrder, pair), openOrder.get("orderNumber").asText(), PoloniexUtils.getTimestampFromPoloTimestamp(openOrder.get("date").asText()), openOrder.get("margin").asBoolean()));
                });
                orders.put(pair, openOrders);
            });
        }
        return new OpenOrderResponse(orders, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static MarketResponse createCancelResponse(JsonNode jsonResponse, CancelRequest request, long timestamp) {
        return new MarketResponse(jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static TradeResponse createTradeResponse(JsonNode jsonResponse, TradeRequest request, long timestamp) {
        System.out.println("Creating trade response for: \n" + jsonResponse.toString());
        List<CompletedTrade> resultingTrades = new LinkedList<>();
        CurrencyPair pair = request.getTrade().getPair();
        jsonResponse.get("resultingTrades").elements().forEachRemaining((completedTrade) -> {
            CompletedTrade.Builder builder = new CompletedTrade.Builder(PoloniexUtils.getTradeFromJson(completedTrade, pair), completedTrade.get("tradeID").asText(), PoloniexUtils.getTimestampFromPoloTimestamp(completedTrade.get("date").asText()));
            builder.total(completedTrade.get("total").asDouble());
            // TODO(stfinancial): Really, we should be able to just calculate the fee field if it wasn't populated.
            // TODO(stfinancial): Check to see if we should set the fee field as well.
            // TODO(stfinancial): Check what other fields we should be setting here.
            resultingTrades.add(builder.build());
        });
        return new TradeResponse(jsonResponse.get("orderNumber").asText(), resultingTrades, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static TickerResponse createTickerResponse(JsonNode jsonResponse, TickerRequest request, long timestamp) {
        Map<CurrencyPair, Ticker> tickers = new HashMap<>();
        request.getPairs().forEach((pair)->{
            JsonNode j = jsonResponse.get(PoloniexUtils.formatCurrencyPair(pair));
            Ticker.Builder b = new Ticker.Builder(pair, j.get("last").asDouble(), j.get("lowestAsk").asDouble(), j.get("highestBid").asDouble());
            b.baseVolume(j.get("baseVolume").asDouble());
            b.quoteVolume(j.get("quoteVolume").asDouble());
            b.percentChange(j.get("percentChange").asDouble());
            tickers.put(pair, b.build());
        });
        return new TickerResponse(tickers, jsonResponse, request, timestamp, RequestStatus.success());
    }

    // TODO(stfinancial): Could just return a MarketResponse instead since this doesn't really contain anything.
    private static TransferBalanceResponse createTransferBalanceResponse(JsonNode jsonResponse, TransferBalanceRequest request, long timestamp) {
        // TODO(stfinancial): May need to actually check that it was a success... What if we try to transfer out of margin when we can't
        // Will the request be a success but the withdrawal still won't occur?
        return new TransferBalanceResponse(jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static VolumeResponse createVolumeResponse(JsonNode jsonResponse, VolumeRequest request, long timestamp) {
        // TODO(stfinancial): Improve readability of this function a bit.
        Map<CurrencyPair, Double> baseVolumes =  new HashMap<>();
        Map<CurrencyPair, Double> quoteVolumes = new HashMap<>();
        Map<Currency, Double> currencyVolumes = new HashMap<>();
        jsonResponse.fields().forEachRemaining((volume) -> {
//            System.out.println(volume.toString());
            CurrencyPair pair = PoloniexUtils.parseCurrencyPair(volume.getKey());
            if (pair == null) {
//                System.out.println("Null pair: " + volume.getKey());
                // TODO(stfinancial): Does this return from the lambda or the createVolumeResponse?
                return;
            }
//            System.out.println(pair.toString());

            String primaryString = PoloniexUtils.getCurrencyString(pair.getBase());
            double baseVolume = volume.getValue().get(primaryString).asDouble();
            String quoteString = PoloniexUtils.getCurrencyString(pair.getQuote());
            double quoteVolume = volume.getValue().get(quoteString).asDouble();

            // TODO(stfinancial): We don't need to put a null check here, but use optional in the future.
            baseVolumes.put(pair, baseVolume);
            quoteVolumes.put(pair, quoteVolume);

            // TODO(stfinancial): NEED TO CHECK WE ARE NOT DOUBLE COUNTING CURRENCY PAIRS HERE.

            currencyVolumes.put(pair.getBase(), baseVolume + currencyVolumes.getOrDefault(pair.getBase(), 0.0));
            currencyVolumes.put(pair.getQuote(), quoteVolume + currencyVolumes.getOrDefault(pair.getQuote(), 0.0));
        });
        return new VolumeResponse(baseVolumes, quoteVolumes, currencyVolumes, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static GetLendingHistoryResponse createGetLendingHistoryResponse(JsonNode jsonResponse, GetLendingHistoryRequest request, long timestamp) {
//        System.out.println(jsonResponse.toString());
        // TODO(stfinancial): Implement this.
        List<CompletedLoan> completedLoans = new LinkedList<>();
        jsonResponse.forEach((loan) -> {
            CompletedLoan.Builder builder = new CompletedLoan.Builder(new Loan(loan.get("amount").asDouble(), loan.get("rate").asDouble(), Currency.getCanonicalRepresentation(loan.get("currency").asText()), LoanType.OFFER), loan.get("id").asText(), PoloniexUtils.getTimestampFromPoloTimestamp(loan.get("open").asText()), PoloniexUtils.getTimestampFromPoloTimestamp(loan.get("close").asText()), loan.get("fee").asDouble());
            builder.duration(loan.get("duration").asDouble()).earned(loan.get("earned").asDouble()).interest(loan.get("interest").asDouble());
            completedLoans.add(builder.build());
        });
        return new GetLendingHistoryResponse(completedLoans, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static FeeResponse createFeeResponse(JsonNode jsonResponse, FeeRequest request, long timestamp) {
        FeeInfo info = new FeeInfo(jsonResponse.get("makerFee").asDouble(), jsonResponse.get("takerFee").asDouble(), jsonResponse.get("thirtyDayVolume").asDouble());
        Map<CurrencyPair, FeeInfo> infos = new HashMap<>();
        if (!request.getPairs().isEmpty()) {
            request.getPairs().forEach(pair -> infos.put(pair, info));
        } else {
            CurrencyPair.getCurrencyPairSet().forEach(pair -> infos.put(pair, info));
        }
        return new FeeResponse(infos, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static MarketResponse createGetActiveLoansResponse(JsonNode jsonResponse, GetActiveLoansRequest request, long timestamp) {
        System.out.println(jsonResponse);
        Map<Currency, List<ActiveLoan>> provided = new HashMap<>();
        jsonResponse.get("provided").elements().forEachRemaining((loan) -> {
            Currency c = Currency.getCanonicalRepresentation(loan.get("currency").asText());
            if (!provided.containsKey(c)) {
                provided.put(c, new ArrayList<>());
            }
            provided.get(c).add(new ActiveLoan(new Loan(loan.get("amount").asDouble(), loan.get("rate").asDouble(), c, LoanType.OFFER), loan.get("duration").asInt(), PoloniexUtils.getTimestampFromPoloTimestamp(loan.get("date").asText()), loan.get("id").asLong(), loan.get("autoRenew").asInt() == 1));
        });
        Map<Currency, List<ActiveLoan>> used = new HashMap<>();
        jsonResponse.get("used").elements().forEachRemaining((loan) -> {
            Currency c = Currency.getCanonicalRepresentation(loan.get("currency").asText());
            if (!used.containsKey(c)) {
                used.put(c, new ArrayList<>());
            }
            System.out.println(c);
            System.out.println(loan);
            used.get(c).add(new ActiveLoan(new Loan(loan.get("amount").asDouble(), loan.get("rate").asDouble(), c, LoanType.DEMAND), loan.get("duration").asInt(), PoloniexUtils.getTimestampFromPoloTimestamp(loan.get("date").asText()), loan.get("id").asLong(), false));
        });
        return new GetActiveLoansResponse(provided, used, jsonResponse, request, timestamp, RequestStatus.success());
    }
}

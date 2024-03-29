package api.kraken;

import api.*;
import api.Currency;
import api.request.AssetPairRequest;
import api.request.AssetPairResponse;
import api.request.*;
import api.tmp_trade.CompletedTrade;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import com.fasterxml.jackson.databind.JsonNode;
import util.PriceUtil;

import java.util.*;

/**
 * Converts a {@link com.fasterxml.jackson.databind.JsonNode JsonNode} response from {@link Kraken} into a
 * {@link api.Market} agnostic {@link api.request.MarketResponse}.
 */
final class KrakenResponseParser implements ResponseParser {
    // TODO(stfinancial): NOTE - Kraken treats order Ids and TxIds separately. Txids start with a T and Order Ids start with an O (so it appears).

    private final Kraken kraken;

    KrakenResponseParser(Kraken kraken) {
        this.kraken = kraken;
    }

    @Override
    public MarketResponse constructMarketResponse(JsonNode jsonResponse, MarketRequest request, long timestamp) {
        // TODO(stfinancial): Check "error" field to see if the result is an empty array.
        if (jsonResponse.isNull()) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNPARSABLE_RESPONSE));
        }
        // TODO(stfinancial): Get the request status here.


        if (jsonResponse.get("error").has(0) && !jsonResponse.get("error").get(0).asText().isEmpty()) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.MARKET_ERROR, jsonResponse.get("error").get(0).asText()));
        }
        if (request instanceof TradeRequest) {
            return createTradeResponse(jsonResponse, (TradeRequest) request, timestamp);
        } else if (request instanceof CancelRequest) {
            return createCancelResponse(jsonResponse, (CancelRequest) request, timestamp);
        } else if (request instanceof OrderBookRequest) {
            return createOrderBookResponse(jsonResponse, (OrderBookRequest) request, timestamp);
        } else if (request instanceof OpenOrderRequest) {
            return createOpenOrderResponse(jsonResponse, (OpenOrderRequest) request, timestamp);
        } else if (request instanceof AccountBalanceRequest) {
            return createAccountBalanceResponse(jsonResponse, (AccountBalanceRequest) request, timestamp);
        } else if (request instanceof TradeHistoryRequest) {
            return createTradeHistoryResponse(jsonResponse, (TradeHistoryRequest) request, timestamp);
        } else if (request instanceof OrderTradesRequest) {
            return createOrderTradesResponse(jsonResponse, (OrderTradesRequest) request, timestamp);
        } else if (request instanceof TickerRequest) {
            return createTickerResponse(jsonResponse, (TickerRequest) request, timestamp);
        } else if (request instanceof AssetPairRequest) {
            return createAssetPairResponse(jsonResponse, (AssetPairRequest) request, timestamp);
        }
        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }

    private TickerResponse createTickerResponse(JsonNode jsonResponse, TickerRequest request, long timestamp) {
        System.out.println(jsonResponse);
        // TODO(stfinancial): We check that there is a currency pair in the request, does it make sense to be defensive and check here as well?
        Map<CurrencyPair, Ticker> tickers = new HashMap<>();
        request.getPairs().forEach((pair) -> {
            JsonNode j = jsonResponse.get("result").get(KrakenUtils.formatCurrencyPair(pair, false));
            Ticker.Builder b = new Ticker.Builder(pair, j.get("c").get(0).asDouble(), j.get("a").get(0).asDouble(), j.get("b").get(0).asDouble());
            b.percentChange(PriceUtil.getPercentChange(j.get("o").asDouble(), j.get("c").get(0).asDouble()));
            b.baseVolume(j.get("v").get(1).asDouble());
            // TODO(stfinancial): I think that this is right...
            b.quoteVolume(j.get("v").get(1).asDouble() * j.get("p").get(1).asDouble());
            tickers.put(pair, b.build());
        });
        return new TickerResponse(tickers, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private OrderBookResponse createOrderBookResponse(JsonNode jsonResponse, OrderBookRequest request, long timestamp) {
        System.out.println(jsonResponse);
        CurrencyPair pair = request.getCurrencyPair();
        // TODO(stfinancial): We check that there is a currency pair in the request, does it make sense to be defensive and check here as well?
        JsonNode j = jsonResponse.get("result").get(KrakenUtils.formatCurrencyPair(pair, true));
        Map<CurrencyPair, List<Trade>> askMap = new HashMap<>();
        List<Trade> asks = new ArrayList<>();
        j.get("asks").elements().forEachRemaining((order) -> {
            asks.add(new Trade(order.get(1).asDouble(), order.get(0).asDouble(), pair, TradeType.SELL));
        });
        askMap.put(request.getCurrencyPair(), asks);
        Map<CurrencyPair, List<Trade>> bidMap = new HashMap<>();
        List<Trade> bids = new ArrayList<>();
        j.get("bids").elements().forEachRemaining((order) -> {
            bids.add(new Trade(order.get(1).asDouble(), order.get(0).asDouble(), pair, TradeType.BUY));
        });
        bidMap.put(pair, bids);
        return new OrderBookResponse(askMap, bidMap, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private OpenOrderResponse createOpenOrderResponse(JsonNode jsonResponse, OpenOrderRequest request, long timestamp) {
        System.out.println(jsonResponse);
        return new OpenOrderResponse(Collections.emptyMap(), jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
//        Map<CurrencyPair, List<TradeOrder>> orders = new HashMap<>();
//        jsonResponse.get("result").fields().forEachRemaining((order) -> {
//            CurrencyPair pair = kraken.getData().getAssetPairNames().get(order.getValue().get("descr").get("pair").asText());
//            if (!orders.containsKey(pair)) {
//                orders.put(pair, new ArrayList<>());
//            }
//            // TODO(stfinancial): Need to figure out how this works with volume and volume executed.
//            orders.get(pair).add(new TradeOrder(new Trade(order.getValue().order.getValue().get("descr").get("price").asDouble(), pair, KrakenUtils.getTradeTypeFromString(order.getValue().get("descr").get("type").asText())), order.getKey(), order.getValue().get("opentm").asLong(), order.getValue().get("descr").get("leverage").asText("none").equals("none"))));
//        });
//        // TODO(stfinancial): All of the remainder of the fields.
//        return new OpenOrderResponse(orders, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private AccountBalanceResponse createAccountBalanceResponse(JsonNode jsonResponse, AccountBalanceRequest request, long timestamp) {
        System.out.println(jsonResponse);
        Map<AccountType, Map<Currency, Double>> balances = new HashMap<>();
        Map<Currency, Double> exchangeBalances = new HashMap<>();
        jsonResponse.get("result").fields().forEachRemaining((balance) -> {
            exchangeBalances.put(KrakenUtils.parseCurrencyString(balance.getKey()), balance.getValue().asDouble());
        });
        balances.put(AccountType.EXCHANGE, exchangeBalances);

        return new AccountBalanceResponse(balances, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createTradeHistoryResponse(JsonNode jsonResponse, TradeHistoryRequest request, long timestamp) {
        Map<CurrencyPair, List<CompletedTrade>> completedTrades = new HashMap<>();
        jsonResponse.get("result").get("trades").fields().forEachRemaining((trade) -> {
            CurrencyPair pair = kraken.getData().getAssetPairNames().get(trade.getValue().get("pair").asText()).getPair();
            if (!completedTrades.containsKey(pair)) {
                completedTrades.put(pair, new ArrayList<>());
            }
            // TODO(stfinancial): Ensure that "vol" is before fees.
            CompletedTrade.Builder b = new CompletedTrade.Builder(new Trade(trade.getValue().get("vol").asDouble(), trade.getValue().get("price").asDouble(), pair, KrakenUtils.getTradeTypeFromString(trade.getValue().get("type").asText())), trade.getKey(), trade.getValue().get("time").asLong());
            // TODO(stfinancial): Decide whether to convert the fee to quote or base currency.
//            b.fee(trade.getValue().get("fee").asDouble()); // TODO(stfinancial): Confirm that this is in the correct denomination.
            b.category(CompletedTrade.Category.EXCHANGE); // TODO(stfinancial): Is there a way we ccan actually infer this? Maybe from "margin"
            b.total(trade.getValue().get("cost").asDouble());
            completedTrades.get(pair).add(b.build());
        });
        // TODO(stfinancial): Do we actually care about restricting the results if they specified for in the request?
        return new TradeHistoryResponse(completedTrades, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createOrderTradesResponse(JsonNode jsonResponse, OrderTradesRequest request, long timestamp) {
        // TODO(stfinancial): What should this actually contain?
        // TODO(stfinancial): Implement this.
        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }

    private TradeResponse createTradeResponse(JsonNode jsonResponse, TradeRequest request, long timestamp) {
        System.out.println(jsonResponse);
        // TODO(stfinancial): When does it occur that we have multiple txid returned?
        return new TradeResponse(jsonResponse.get("result").get("txid").get(0).textValue(), jsonResponse, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createCancelResponse(JsonNode jsonResponse, CancelRequest request, long timestamp) {
        System.out.println(jsonResponse);
        // TODO(stfinancial): Count and pending.
        return new MarketResponse(jsonResponse, request, timestamp, RequestStatus.success());
    }

    private AssetPairResponse createAssetPairResponse(JsonNode jsonResponse, AssetPairRequest request, long timestamp) {
        List<AssetPair> assetPairs = new ArrayList<>();
        Map<String, CurrencyPair> assetPairNames = new HashMap<>();
        Map<CurrencyPair, String> assetPairKeys = new HashMap<>();
        jsonResponse.get("result").fields().forEachRemaining((assetPair) -> {
            Currency base = Currency.getCanonicalName(assetPair.getValue().get("base").asText());
            if (base == null) {
                base = Currency.getCanonicalName(assetPair.getValue().get("base").asText().substring(1));
            }
            if (base == null) {
                System.out.println("Could not find base currency for: " + assetPair.getValue().get("base").asText());
                return;
            }
            // Remove the currency namespace
            Currency quote = Currency.getCanonicalName(assetPair.getValue().get("quote").asText().substring(1));
            if (quote == null) {
                System.out.println("Could not find quote currency for: " + assetPair.getValue().get("quote").asText());
                return;
            }
//            System.out.println("Base: " + assetPair.getValue().get("base").asText());
//            System.out.println("Quote: " + assetPair.getValue().get("quote").asText().substring(1));
            CurrencyPair pair = CurrencyPair.of(base, quote);
            // Ignore dark pairs for now.
            if (!assetPair.getKey().endsWith(".d")) {
                assetPairKeys.put(pair, assetPair.getKey());
            }
            assetPairs.add((new AssetPair.Builder(pair, assetPair.getValue().get("altname").asText())).build());
            assetPairs.add((new AssetPair.Builder(pair, base.toString() + quote.toString())).build());
            assetPairs.add((new AssetPair.Builder(pair, assetPair.getKey())).build());
            assetPairs.add((new AssetPair.Builder(pair, base.getIsoNamespace() + base.toString() + quote.getIsoNamespace() + quote.toString())).build());
        });
        return new AssetPairResponse(assetPairs, jsonResponse, request, timestamp, RequestStatus.success());
    }
}

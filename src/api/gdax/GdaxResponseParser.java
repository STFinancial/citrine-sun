package api.gdax;

import api.*;
import api.Currency;
import api.request.*;
import api.tmp_trade.CompletedTrade;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

/**
 * Converts a {@link com.fasterxml.jackson.databind.JsonNode JsonNode} response from {@link Gdax} into a
 * {@link api.Market} agnostic {@link api.request.MarketResponse}.
 */
final class GdaxResponseParser implements ResponseParser {

    @Override
    public MarketResponse constructMarketResponse(JsonNode jsonResponse, MarketRequest request, long timestamp) {
        if (jsonResponse.isNull()) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNPARSABLE_RESPONSE));
        }
        // TODO(stfinancial): Get the request status here.
        if (request instanceof TradeRequest) {
            return createTradeResponse(jsonResponse, (TradeRequest) request, timestamp);
        } else if (request instanceof OrderBookRequest) {
            return createOrderBookResponse(jsonResponse, (OrderBookRequest) request, timestamp);
        } else if (request instanceof CancelRequest) {
            return createCancelResponse(jsonResponse, (CancelRequest) request, timestamp);
        } else if (request instanceof TickerRequest) {
            return createTickerResponse(jsonResponse, (TickerRequest) request, timestamp);
        } else if (request instanceof OrderTradesRequest) {
            return createOrderTradesResponse(jsonResponse, (OrderTradesRequest) request, timestamp);
        } else if (request instanceof AccountBalanceRequest) {
            return createAccountBalanceResponse(jsonResponse, (AccountBalanceRequest) request, timestamp);
        } else if (request instanceof TradeHistoryRequest) {
            return createTradeHistoryResponse(jsonResponse, (TradeHistoryRequest) request, timestamp);
        } else if (request instanceof FeeRequest) {
            return createFeeResponse(jsonResponse, (FeeRequest) request, timestamp);
        } else if (request instanceof AssetPairRequest) {
            return createAssetPairResponse(jsonResponse, (AssetPairRequest) request, timestamp);
        }
        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }

    // TODO(stfinancial): Think carefully about what is at stake for returning MarketResponse vs. the specific response type.

    private MarketResponse createTradeResponse(JsonNode jsonResponse, TradeRequest request, long timestamp) {
        if (jsonResponse.has("status") && jsonResponse.get("status").asText().equals("rejected")) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.MARKET_ERROR, jsonResponse.get("reject_reason").asText()));
        }

        // TODO(stfinancial): Amount filled is not set correctly for limit take orders.

        // TODO(stfinancial): Add support for all of the other stuff.
        return new TradeResponse(jsonResponse.get("id").asText(), jsonResponse, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createCancelResponse(JsonNode jsonResponse, CancelRequest request, long timestamp) {
        return new MarketResponse(jsonResponse, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createOrderBookResponse(JsonNode jsonResponse, OrderBookRequest request, long timestamp) {
        CurrencyPair pair = request.getCurrencyPair();
        Map<CurrencyPair, List<Trade>> asksSet = new HashMap<>();
        Map<CurrencyPair, List<Trade>> bidsSet = new HashMap<>();
        List<Trade> asks = new ArrayList<>();
        List<Trade> bids = new ArrayList<>();
        jsonResponse.get("bids").forEach((order) -> {
            bids.add(new Trade(order.get(1).asDouble(), order.get(0).asDouble(), pair, TradeType.BUY));
        });
        bidsSet.put(pair, bids);
        jsonResponse.get("asks").forEach((order) -> {
            asks.add(new Trade(order.get(1).asDouble(), order.get(0).asDouble(), pair, TradeType.SELL));
        });
        asksSet.put(pair, asks);
        return new OrderBookResponse(asksSet, bidsSet, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createOrderTradesResponse(JsonNode jsonResponse, OrderTradesRequest request, long timestamp) {
        List<CompletedTrade> trades = new ArrayList<>();
        System.out.println("CreateOrderTradesResponse - GDAX: " + jsonResponse);
        jsonResponse.forEach((trade) -> {
            // TODO(stfinancial): Clean this up, refactor to utils class for constructing trade from json
            // TODO(stfinancial): See if different markets need to have different interpretations of the fee parameter.
            // TODO(stfinancial): Make sure created_at means what we think it means.
            trades.add(new CompletedTrade.Builder(new Trade(jsonResponse.get("amount").asDouble(), jsonResponse.get("price").asDouble(), GdaxUtils.parseCurrencyPair(jsonResponse.get("product_id").asText()), GdaxUtils.getTradeTypeFromString(jsonResponse.get("side").asText())), jsonResponse.get("trade_id").asText(), GdaxUtils.convertTimestamp(jsonResponse.get("created_at").asText())).fee(jsonResponse.get("fee").asDouble()).build());
        });
        return new OrderTradesResponse(trades, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createTickerResponse(JsonNode jsonResponse, TickerRequest request, long timestamp) {
        Ticker.Builder ticker = new Ticker.Builder(request.getPairs().get(0), jsonResponse.get("price").asDouble(), jsonResponse.get("ask").asDouble(), jsonResponse.get("bid").asDouble());
        ticker.baseVolume(jsonResponse.get("volume").asDouble());
        // TODO(stfinancial): Look at all the problems this stupid optional list is causing.
        Map<CurrencyPair, Ticker> tickers = new HashMap<>();
        tickers.put(request.getPairs().get(0), ticker.build());
        return new TickerResponse(tickers, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createAccountBalanceResponse(JsonNode jsonResponse, AccountBalanceRequest request, long timestamp) {
        Map<AccountType, Map<Currency, Double>> balances = new HashMap<>();
        Map<Currency, Double> exchangeBalances = new HashMap<>();
        Map<Currency, Double> marginBalances = new HashMap<>();
        // TODO(stfinancial): Maybe make a Balance class that has available and total balances as well as other info.
        jsonResponse.forEach((balance) -> {
            if (balance.has("margin_enabled") && balance.get("margin_enabled").asBoolean()) {
                marginBalances.put(Currency.getCanonicalName(balance.get("currency").asText()), balance.get("available").asDouble());
            } else {
                exchangeBalances.put(Currency.getCanonicalName(balance.get("currency").asText()), balance.get("available").asDouble());
            }
        });
        balances.put(AccountType.EXCHANGE, exchangeBalances);
        balances.put(AccountType.MARGIN, marginBalances);
        return new AccountBalanceResponse(balances, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createTradeHistoryResponse(JsonNode jsonResponse, TradeHistoryRequest request, long timestamp) {
        System.out.println(jsonResponse);
        Map<CurrencyPair, List<CompletedTrade>> completedTrades = new HashMap<>();
        jsonResponse.elements().forEachRemaining((trade) -> {
            CurrencyPair pair = GdaxUtils.parseCurrencyPair(trade.get("product_id").asText());
            if (!completedTrades.containsKey(pair)) {
                completedTrades.put(pair, new ArrayList<>());
            }
            // TODO(stfinancial): I'm not sure that "created_at" is the same as what we want. Is this when the "fill" was created? or when the order was placed?
            CompletedTrade.Builder b = new CompletedTrade.Builder(new Trade(trade.get("size").asDouble(), trade.get("price").asDouble(), pair, GdaxUtils.getTradeTypeFromString(trade.get("side").asText())), trade.get("trade_id").asText(), GdaxUtils.convertTimestamp(trade.get("created_at").asText()));
            b.fee(trade.get("fee").asDouble());
            b.isMake(trade.get("liquidity").asText("").equals("M"));
            // TODO(stfinancial): order_id?
            completedTrades.get(pair).add(b.build());
        });
        System.out.println(jsonResponse);
        // TODO(stfinancial): Implement this.
        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }

    private MarketResponse createFeeResponse(JsonNode jsonResponse, FeeRequest request, long timestamp) {
        // TODO(stfinancial): This method is a mess... clean up.
        Map<CurrencyPair, FeeInfo> fees = new HashMap<>();
        if (!request.getPairs().isEmpty()) {
            for (JsonNode feeSet : jsonResponse) {
                if (!request.getPairs().contains(GdaxUtils.parseCurrencyPair(feeSet.get("product_id").asText()))) {
                    continue;
                }
                fees.put(GdaxUtils.parseCurrencyPair(feeSet.get("product_id").asText()), new FeeInfo(0, GdaxUtils.getTakerFeeFromVolumeFraction(feeSet.get("volume").asDouble() / feeSet.get("exchange_volume").asDouble()), feeSet.get("volume").asDouble()));
            }
            return new FeeResponse(fees, jsonResponse, request, timestamp, RequestStatus.success());
        } else {
            for (JsonNode feeSet : jsonResponse) {
                fees.put(GdaxUtils.parseCurrencyPair(feeSet.get("product_id").asText()), new FeeInfo(0, GdaxUtils.getTakerFeeFromVolumeFraction(feeSet.get("volume").asDouble() / feeSet.get("exchange_volume").asDouble()), feeSet.get("volume").asDouble()));
            }
            return new FeeResponse(fees, jsonResponse, request, timestamp, RequestStatus.success());
        }
    }

    private MarketResponse createAssetPairResponse(JsonNode jsonResponse, AssetPairRequest request, long timestamp) {
        // TODO(stfinancial): base_min_size, base_max_size. Make a AssetPairInfo class or something.
        // TODO(stfinancial): Create the map to market name.
        List<AssetPair> assets = new ArrayList<>();
        jsonResponse.forEach((assetPair) -> {
            AssetPair.Builder ap = new AssetPair.Builder(GdaxUtils.parseCurrencyPair(assetPair.get("id").asText()), assetPair.get("id").asText());
            assets.add(ap.build());
        });
        return new AssetPairResponse(assets, jsonResponse, request, timestamp, RequestStatus.success());
    }
}

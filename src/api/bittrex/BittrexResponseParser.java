package api.bittrex;

import api.*;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeOrder;
import api.tmp_trade.TradeType;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts a {@link com.fasterxml.jackson.databind.JsonNode JsonNode} response from {@link Bittrex} into a
 * {@link api.Market} agnostic {@link api.request.MarketResponse}.
 */
final class BittrexResponseParser implements ResponseParser {

    @Override
    public MarketResponse constructMarketResponse(JsonNode jsonResponse, MarketRequest request, long timestamp) {
        if (!jsonResponse.get("success").asBoolean()) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.MARKET_ERROR, jsonResponse.get("message").asText("")));
        }
        System.out.println(jsonResponse);
        if (request instanceof TradeRequest) {
            return createTradeResponse(jsonResponse, (TradeRequest) request, timestamp);
        } else if (request instanceof CancelRequest) {
            return createCancelResponse(jsonResponse, (CancelRequest) request, timestamp);
        } else if (request instanceof OrderBookRequest) {
            return createOrderBookResponse(jsonResponse, (OrderBookRequest) request, timestamp);
        } else if (request instanceof OpenOrderRequest) {
            return createOpenOrderResponse(jsonResponse, (OpenOrderRequest) request, timestamp);
        } else if (request instanceof TickerRequest) {
            return createTickerResponse(jsonResponse, (TickerRequest) request, timestamp);
        } else if (request instanceof AccountBalanceRequest) {
            return createAccountBalanceResponse(jsonResponse, (AccountBalanceRequest) request, timestamp);
        }

        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }

    private MarketResponse createTradeResponse(JsonNode jsonResponse, TradeRequest request, long timestamp) {
        return new TradeResponse(jsonResponse.get("result").get("uuid").asText(), jsonResponse, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createCancelResponse(JsonNode jsonResponse, CancelRequest request, long timestamp) {
        return new MarketResponse(jsonResponse, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createOrderBookResponse(JsonNode jsonResponse, OrderBookRequest request, long timestamp) {
        System.out.println(jsonResponse);
        CurrencyPair pair = request.getCurrencyPair();
        List<Trade> asks = new ArrayList<>();
        List<Trade> bids = new ArrayList<>();
        JsonNode askBook = jsonResponse.get("result").get("sell");
        JsonNode bidBook = jsonResponse.get("result").get("buy");
        for (int depth = 0; depth < request.getDepth(); ++depth) {
            // TODO(stfinancial): Better way than checking these every single time.
            if (depth < askBook.size()) {
                asks.add(new Trade(askBook.get(depth).get("Quantity").asDouble(), askBook.get(depth).get("Rate").asDouble(), pair, TradeType.SELL));
            }
            if (depth < bidBook.size()) {
                bids.add(new Trade(bidBook.get(depth).get("Quantity").asDouble(), bidBook.get(depth).get("Rate").asDouble(), pair, TradeType.BUY));
            }
        }
        return new OrderBookResponse(new HashMap<CurrencyPair, List<Trade>>(){{ put(request.getCurrencyPair(), asks); }}, new HashMap<CurrencyPair, List<Trade>>(){{ put(request.getCurrencyPair(), bids); }}, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createOpenOrderResponse(JsonNode jsonResponse, OpenOrderRequest request, long timestamp) {
        Map<CurrencyPair, List<TradeOrder>> orders = new HashMap<>();
        jsonResponse.get("result").elements().forEachRemaining((order) -> {
            CurrencyPair pair = BittrexUtils.parseCurrencyPair(order.get("Exchange").asText());
            if (!orders.containsKey(pair)) {
                orders.put(pair, new ArrayList<>());
            }
            orders.get(pair).add(new TradeOrder(new Trade(order.get("Quantity").asDouble(), order.get("Limit").asDouble(), pair, BittrexUtils.getTradeTypeFromString(order.get("OrderType").asText())), order.get("OrderUuid").asText(), BittrexUtils.getTimestampFromBittrexTimestamp(order.get("Opened").asText()), false));
        });
        return new OpenOrderResponse(orders, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createTickerResponse(JsonNode jsonResponse, TickerRequest request, long timestamp) {
        if (request.getPairs().size() != 1) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST, "Cannot request ticker for multiple markets."));
        }
        Ticker t = new Ticker.Builder(request.getPairs().get(0), jsonResponse.get("Last").asDouble(), jsonResponse.get("Ask").asDouble(), jsonResponse.get("Bid").asDouble()).build();
        return new TickerResponse(new HashMap<CurrencyPair, Ticker>(){{ put(request.getPairs().get(0), t);}}, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createAccountBalanceResponse(JsonNode jsonResponse, AccountBalanceRequest request, long timestamp) {
        Map<Currency, Double> exchangeBalances = new HashMap<>();
        jsonResponse.get("result").elements().forEachRemaining((balance) -> {
            exchangeBalances.put(Currency.getCanonicalName(balance.get("Currency").asText()), balance.get("Balance").asDouble(0.0));
        });
        return new AccountBalanceResponse(new HashMap<AccountType, Map<Currency, Double>>(){{ put(AccountType.EXCHANGE, exchangeBalances); }}, jsonResponse, request, timestamp, RequestStatus.success());
    }
}

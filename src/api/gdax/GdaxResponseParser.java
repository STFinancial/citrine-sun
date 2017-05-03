package api.gdax;

import api.*;
import api.Currency;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

/**
 * Created by Timothy on 4/23/17.
 */
final class GdaxResponseParser {

    // TODO(stfinancial): Take in isError for now until we switch to using the http response.
    static MarketResponse constructMarketResponse(JsonNode jsonResponse, MarketRequest request, long timestamp, boolean isError) {
        if (jsonResponse.isNull()) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNPARSABLE_RESPONSE));
        }
        // TODO(stfinancial): Get the request status here.

        if (isError) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.MARKET_ERROR, jsonResponse.asText()));
        }
        if (request instanceof TradeRequest) {
            return createTradeResponse(jsonResponse, (TradeRequest) request, timestamp);
        } else if (request instanceof OrderBookRequest) {
            return createOrderBookResponse(jsonResponse, (OrderBookRequest) request, timestamp);
        } else if (request instanceof CancelRequest) {
            return createCancelResponse(jsonResponse, (CancelRequest) request, timestamp);
        } else if (request instanceof TickerRequest) {
            return createTickerResponse(jsonResponse, (TickerRequest) request, timestamp);
        } else if (request instanceof AccountBalanceRequest) {
            return createAccountBalanceResponse(jsonResponse, (AccountBalanceRequest) request, timestamp);
        } else if (request instanceof FeeRequest) {
            return createFeeResponse(jsonResponse, (FeeRequest) request, timestamp);
        }
        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }

    // TODO(stfinancial): Think carefully about what is at stake for returning MarketResponse vs. the specific response type.

    private static MarketResponse createTradeResponse(JsonNode jsonResponse, TradeRequest request, long timestamp) {

        // TODO(stfinancial): Add support for all of the other stuff.
        return new TradeResponse(jsonResponse.get("id").asText(), jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static MarketResponse createCancelResponse(JsonNode jsonResponse, CancelRequest request, long timestamp) {
        return new MarketResponse(jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static MarketResponse createOrderBookResponse(JsonNode jsonResponse, OrderBookRequest request, long timestamp) {
        CurrencyPair pair = request.getCurrencyPair().get();
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

    private static MarketResponse createTickerResponse(JsonNode jsonResponse, TickerRequest request, long timestamp) {
        Ticker.Builder ticker = new Ticker.Builder(request.getPairs().get().get(0), jsonResponse.get("price").asDouble(), jsonResponse.get("ask").asDouble(), jsonResponse.get("bid").asDouble());
        ticker.baseVolume(jsonResponse.get("volume").asDouble());
        // TODO(stfinancial): Look at all the problems this stupid optional list is causing.
        Map<CurrencyPair, Ticker> tickers = new HashMap<>();
        tickers.put(request.getPairs().get().get(0), ticker.build());
        return new TickerResponse(tickers, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static MarketResponse createAccountBalanceResponse(JsonNode jsonResponse, AccountBalanceRequest request, long timestamp) {
        Map<AccountType, Map<Currency, Double>> balances = new HashMap<>();
        Map<Currency, Double> exchangeBalances = new HashMap<>();
        Map<Currency, Double> marginBalances = new HashMap<>();
        System.out.println(jsonResponse);
        // TODO(stfinancial): Maybe make a Balance class that has available and total balances as well as other info.
        jsonResponse.forEach((balance) -> {
            if (balance.has("margin_enabled") && balance.get("margin_enabled").asBoolean()) {
                marginBalances.put(Currency.getCanonicalRepresentation(balance.get("currency").asText()), balance.get("available").asDouble());
            } else {
                exchangeBalances.put(Currency.getCanonicalRepresentation(balance.get("currency").asText()), balance.get("available").asDouble());
            }
        });
        balances.put(AccountType.EXCHANGE, exchangeBalances);
        balances.put(AccountType.MARGIN, marginBalances);
        return new AccountBalanceResponse(balances, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private static MarketResponse createFeeResponse(JsonNode jsonResponse, FeeRequest request, long timestamp) {
        // TODO(stfinancial): This method is a mess... clean up.
        System.out.println(jsonResponse);
        FeeResponse feeResponse;
        for (JsonNode feeSet : jsonResponse) {
            if (GdaxUtils.parseCurrencyPair(feeSet.get("product_id").asText()) != request.getCurrencyPair().get()) {
                continue;
            }
            return new FeeResponse(new FeeInfo(0, GdaxUtils.getTakerFeeFromVolumeFraction(feeSet.get("volume").asDouble() / feeSet.get("exchange_volume").asDouble()), feeSet.get("volume").asDouble()), jsonResponse, request, timestamp, RequestStatus.success());
        }
        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.MARKET_ERROR));
    }

}

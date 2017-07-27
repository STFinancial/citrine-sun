package api.bitfinex;

import api.AccountType;
import api.Currency;
import api.CurrencyPair;
import api.Ticker;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Timothy on 4/13/17.
 */
final class BitfinexResponseParser {
    // TODO(stfinancial): This logic is basically shared with all other markets, maybe reuse it somehow.
    // TODO(stfinancial): Have constructMarketResponse in Market, and have ResponseParser be an interface, call the respective methods.
    MarketResponse constructMarketResponse(JsonNode jsonResponse, MarketRequest request, long timestamp, boolean isError) {
        if (jsonResponse.isNull()) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNPARSABLE_RESPONSE));
        }
        // TODO(stfinancial): Get the request status here.

        if (isError) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.MARKET_ERROR, jsonResponse.asText()));
        }
        if (jsonResponse.has("error") || jsonResponse.get(0).asText("").equals("error")) {
            return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.MARKET_ERROR, jsonResponse.get(2).asText("")));
        }
        if (request instanceof TradeRequest) {
            return createTradeResponse(jsonResponse, (TradeRequest) request, timestamp);
        } else if (request instanceof OrderBookRequest) {
            return createOrderBookResponse(jsonResponse, (OrderBookRequest) request, timestamp);
        } else if (request instanceof TickerRequest) {
            return createTickerResponse(jsonResponse, (TickerRequest) request, timestamp);
        } else if (request instanceof AccountBalanceRequest) {
            return createAccountBalanceResponse(jsonResponse, (AccountBalanceRequest) request, timestamp);
        }
        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }

    private MarketResponse createOrderBookResponse(JsonNode jsonResponse, OrderBookRequest request, long timestamp) {
        // TODO(stfinancial): Again, should we check the optional here to be defensive? Or assume it's caught in the request rewriter?
        CurrencyPair pair = request.getCurrencyPair().get();
        Map<CurrencyPair, List<Trade>> bids = new HashMap<>();
        List<Trade> orders = new ArrayList<>(request.getDepth());
        JsonNode j;
        for (int d = 0; d < request.getDepth(); ++d) {
            j = jsonResponse.get(d);
            orders.add(new Trade(j.get(2).asDouble(), j.get(0).asDouble(), pair, TradeType.BUY));
        }
        bids.put(pair, orders);
        orders = new ArrayList<>(request.getDepth());
        Map<CurrencyPair, List<Trade>> asks = new HashMap<>();
        for (int d = 0; d < request.getDepth(); ++d) {
            j = jsonResponse.get(request.getDepth() + d);
            orders.add(new Trade(j.get(2).asDouble(), j.get(0).asDouble(), pair, TradeType.SELL));
        }
        asks.put(pair, orders);
        return new OrderBookResponse(asks, bids, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createTradeResponse(JsonNode jsonResponse, TradeRequest request, long timestamp) {
        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }

    private TickerResponse createTickerResponse(JsonNode jsonResponse, TickerRequest request, long timestamp) {
        // TODO(stfinancial): Right now we are assuming that we are not asking for tickers with 'f'
        Map<CurrencyPair, Ticker> tickers = new HashMap<>();
        jsonResponse.forEach((t) -> {
            CurrencyPair pair = BitfinexUtils.parseCurrencyPair(t.get(0).asText());
            // TODO(stfinancial): How do we express teh daily change percent properly?
            tickers.put(pair, new Ticker.Builder(pair, t.get(7).asDouble(), t.get(3).asDouble(), t.get(1).asDouble()).percentChange(t.get(6).asDouble()).baseVolume(t.get(8).asDouble()).build());
        });
        return new TickerResponse(tickers, jsonResponse, request, timestamp, RequestStatus.success());
    }

    private MarketResponse createAccountBalanceResponse(JsonNode jsonResponse, AccountBalanceRequest request, long timestamp) {
        // TODO(stfinancial): Does it make sense to respect the account types in the request?
        Map<AccountType, Map<Currency, Double>> accounts = new HashMap<>();
        accounts.put(AccountType.EXCHANGE, new HashMap<>());
        accounts.put(AccountType.LOAN, new HashMap<>());
        accounts.put(AccountType.MARGIN, new HashMap<>());
        jsonResponse.forEach((b) -> {
            accounts.get(BitfinexUtils.parseAccountType(b.get(0).asText())).put(Currency.getCanonicalRepresentation(b.get(1).asText()), b.get(4).asDouble(0.0));
        });
        return new AccountBalanceResponse(accounts, jsonResponse, request, timestamp, RequestStatus.success());
    }
}

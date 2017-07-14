package api.bitfinex;

import api.CurrencyPair;
import api.Ticker;
import api.request.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
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
        if (request instanceof TradeRequest) {
            return createTradeResponse(jsonResponse, (TradeRequest) request, timestamp);
        } else if (request instanceof TickerRequest) {
            return createTickerResponse(jsonResponse, (TickerRequest) request, timestamp);
        }
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

    private MarketResponse createTradeResponse(JsonNode jsonResponse, TradeRequest request, long timestamp) {
        return new MarketResponse(jsonResponse, request, timestamp, new RequestStatus(StatusType.UNSUPPORTED_REQUEST));
    }
}

package api.poloniex;

import api.CurrencyPair;
import api.RequestArgs;
import api.request.*;
import api.request.tmp_loan.CreateLoanOfferRequest;
import api.request.tmp_loan.GetLendingHistoryRequest;
import api.request.tmp_loan.GetPrivateLoanOffersRequest;
import api.request.tmp_loan.GetPublicLoanOrdersRequest;
import api.request.tmp_trade.MoveOrderRequest;
import api.tmp_loan.LoanType;
import api.tmp_loan.PrivateLoanOrder;

/**
 * Converts a {@link MarketRequest} into {@link RequestArgs} which can be used to construct an encoded URL and signed
 * data object used to send a request to Poloniex.
 */
final class PoloniexRequestRewriter {
    // TODO(stfinancial): Separate into public and private method sections. Easier to debug then.
    // TODO(stfinancial): Need a way to gracefully handle nonce here, probably will set request type and is private before calling the other functions... maybe, then we have a pass a Builder which I don't like.
    private static final String PUBLIC_URI = "https://poloniex.com/public";
    private static final String PRIVATE_URI = "https://poloniex.com/tradingApi";
    private static final String COMMAND_STRING = "command";

    /**
     * Converts a {@link MarketRequest} into {@link RequestArgs} which can be used to construct an encoded URL and signed
     * data object used to send a request to Poloniex.
     * @param request The request to be converted to {@code RequestArgs} understood by the {@link Poloniex} market.
     * @return The {@code RequestArgs} containing the command and arguments to construct an encoded URL and signed data
     * object. Returns {@link RequestArgs#unsupported()} if the request is not supported or cannot be converted to a
     * valid command.
     */
    static RequestArgs rewriteRequest(MarketRequest request) {
        // TODO(stfinancial): Could set request type and private in this function.
        if (request instanceof TradeRequest) {
            return rewriteTradeRequest((TradeRequest) request);
        } else if (request instanceof CancelRequest) {
            return rewriteCancelRequest((CancelRequest) request);
        } else if (request instanceof OpenOrderRequest) {
            // TODO(stfinancial): Rename this method if we change the name of OpenOrderResponse
            return rewriteOpenOrderRequest((OpenOrderRequest) request);
        } else if (request instanceof OrderBookRequest) {
            return rewriteOrderBookRequest((OrderBookRequest) request);
        } else if (request instanceof AccountBalanceRequest) {
            return rewriteAccountBalanceRequest((AccountBalanceRequest) request);
        } else if (request instanceof GetPublicLoanOrdersRequest) {
            return rewriteGetPublicLoanOrdersRequest((GetPublicLoanOrdersRequest) request);
        } else if (request instanceof CreateLoanOfferRequest) {
            return rewriteCreateLoanOfferRequest((CreateLoanOfferRequest) request);
        } else if (request instanceof TickerRequest) {
            return rewriteTickerRequest((TickerRequest) request);
        } else if (request instanceof MoveOrderRequest) {
            return rewriteMoveOrderRequest((MoveOrderRequest) request);
        } else if (request instanceof MarginPositionRequest) {
            return rewriteMarginPositionRequest((MarginPositionRequest) request);
        } else if (request instanceof MarginAccountSummaryRequest) {
            return rewriteMarginAccountSummaryRequest((MarginAccountSummaryRequest) request);
        } else if (request instanceof GetPrivateLoanOffersRequest) {
            return rewriteGetPrivateLoanOffersRequest((GetPrivateLoanOffersRequest) request);
        } else if (request instanceof TransferBalanceRequest) {
            return rewriteTransferBalanceRequest((TransferBalanceRequest) request);
        } else if (request instanceof VolumeRequest) {
            return rewriteVolumeRequest((VolumeRequest) request);
        } else if (request instanceof GetLendingHistoryRequest) {
            return rewriteGetLendingHistoryRequest((GetLendingHistoryRequest) request);
        } else if (request instanceof FeeRequest) {
            return rewriteFeeRequest((FeeRequest) request);
        }
        return RequestArgs.unsupported();
    }

    private static RequestArgs rewriteCancelRequest(CancelRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        switch (request.getType()) {
            case TRADE:
                builder.withParam(COMMAND_STRING, "cancelOrder", true, true);
                break;
            case LOAN:
                builder.withParam(COMMAND_STRING, "cancelLoanOffer", true, true);
                break;
            default:
                System.out.println("Invalid CancelType in rewriteCancelRequest: " + request.getType());
                return RequestArgs.unsupported();
        }
        builder.withParam("orderNumber", String.valueOf(request.getId()), true, true);
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()), true, true);
        return builder.build();
    }

    private static RequestArgs rewriteOrderBookRequest(OrderBookRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PUBLIC_URI);
        builder.withParam(COMMAND_STRING, "returnOrderBook", true, true);
        if (request.getCurrencyPair().isPresent()) {
            builder.withParam("currencyPair", PoloniexUtils.formatCurrencyPair(request.getCurrencyPair().get()), true, true);
        } else {
            builder.withParam("currencyPair", "all", true, true);
        }
        builder.withParam("depth", String.valueOf(request.getDepth()), true, true);
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(false);
        return builder.build();
    }

    private static RequestArgs rewriteMarginPositionRequest(MarginPositionRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "getMarginPosition", true, true);
        // TODO(stfinancial): Potentially add support for "all" by making CurrencyPair optional field.
        builder.withParam("currencyPair", PoloniexUtils.formatCurrencyPair(request.getPair()), true, true);
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()), true, true);
        return builder.build();
    }

    private static RequestArgs rewriteMarginAccountSummaryRequest(MarginAccountSummaryRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "returnMarginAccountSummary", true, true);
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()), true, true);
        return builder.build();
    }

    private static RequestArgs rewriteGetPublicLoanOrdersRequest(GetPublicLoanOrdersRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PUBLIC_URI);
        builder.withParam(COMMAND_STRING, "returnLoanOrders", true, true);
        builder.withParam("currency", PoloniexUtils.getCurrencyString(request.getCurrency()), true, true);
        // TODO(stfinancial): Get this value from the request instead.
        builder.withParam("limit", String.valueOf(999999), true, true);
        builder.isPrivate(false);
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        return builder.build();
    }

    private static RequestArgs rewriteGetPrivateLoanOffersRequest(GetPrivateLoanOffersRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "returnOpenLoanOffers", true, true);
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()), true, true);
        return builder.build();
    }

    private static RequestArgs rewriteCreateLoanOfferRequest(CreateLoanOfferRequest request) {
        PrivateLoanOrder order = request.getOrder();
        if (order.getType() != LoanType.OFFER) {
            System.out.println("Cannot create a LoanOffer without a LoanType of OFFER: " + order.getType());
            return RequestArgs.unsupported();
        }
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "createLoanOffer", true, true);
        builder.withParam("currency", PoloniexUtils.getCurrencyString(order.getCurrency()), true, true);
        builder.withParam("amount", String.valueOf(order.getAmount()), true, true);
        builder.withParam("lendingRate", String.valueOf(order.getRate()), true, true);
        builder.withParam("duration", String.valueOf(order.getDuration()), true, true);
        builder.withParam("isAutoRenew", order.isAutoRenew() ? "1" : "0", true, true);
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()), true, true);
        return builder.build();
    }

    private static RequestArgs rewriteAccountBalanceRequest(AccountBalanceRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "returnAvailableAccountBalances", true, true);
        switch (request.getType()) {
            case MARGIN:
                builder.withParam("account", "margin", true, true);
                break;
            case EXCHANGE:
                builder.withParam("account", "exchange", true, true);
                break;
            case LOAN:
                builder.withParam("account", "lending", true, true);
                break;
            case ALL:
                builder.withParam("account", "all", true, true);
                break;
            default:
                builder.withParam("account", "all", true, true);
                break;
        }
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()), true, true);
        return builder.build();
    }

    private static RequestArgs rewriteOpenOrderRequest(OpenOrderRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "returnOpenOrders", true, true);
        // TODO(stfinancial): Replace with returnAllOpenOrders, but check that this works with currencyPair?

        CurrencyPair pair = request.getCurrencyPair();
        if (pair == null) {
            builder.withParam("currencyPair", "all", true, true);
        } else {
            builder.withParam("currencyPair", PoloniexUtils.formatCurrencyPair(request.getCurrencyPair()), true, true);
        }
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()), true, true);
        return builder.build();
    }

    private static RequestArgs rewriteTickerRequest(TickerRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PUBLIC_URI);
        builder.withParam(COMMAND_STRING, "returnTicker", true, true);
        // TODO(stfinancial): Optional pairs from request?
        builder.isPrivate(false);
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        return builder.build();
    }

    private static RequestArgs rewriteTransferBalanceRequest(TransferBalanceRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "transferBalance", true, true);
        builder.withParam("currency", PoloniexUtils.getCurrencyString(request.getCurrency()), true, true);
        builder.withParam("amount", String.valueOf(request.getAmount()), true, true);
        builder.withParam("fromAccount", PoloniexUtils.getNameForAccountType(request.from()), true, true);
        builder.withParam("toAccount", PoloniexUtils.getNameForAccountType(request.to()), true, true);
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()), true, true);
        return builder.build();
    }

    private static RequestArgs rewriteMoveOrderRequest(MoveOrderRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "moveOrder", true, true);
        builder.withParam("orderNumber", String.valueOf(request.getOrderNumber()), true, true);
        builder.withParam("rate", String.valueOf(request.getRate()), true, true);
        builder.withParam("immediateOrCancel", request.isImmediateOrCancel() ? "1" : "0", true, true);
        builder.withParam("postOnly", request.isPostOnly() ? "1" : "0", true, true);
        if (request.getAmount() != 0) {
            builder.withParam("amount", String.valueOf(request.getAmount()), true, true);
        }
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()), true, true);
        return builder.build();
    }

    private static RequestArgs rewriteTradeRequest(TradeRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        // TODO(stfinancial): Handle market type requests somehow.
        if (!request.isStopLimit()) {
            switch (request.getTrade().getType()) {
                case BUY:
                    builder.withParam(COMMAND_STRING, request.isMargin() ? "marginBuy" : "buy", true, true);
                    break;
                case SELL:
                    builder.withParam(COMMAND_STRING, request.isMargin() ? "marginSell" : "sell", true, true);
                    break;
                default:
                    System.out.println("Invalid TradeType in rewriteTradeRequest: " + request.getType());
                    return RequestArgs.unsupported();
            }
        } else {
            switch (request.getTrade().getType()) {
                case BUY:
                    builder.withParam(COMMAND_STRING, request.isMargin() ? "marginStopLimitBuy" : "stopLimitBuy", true, true);
                    break;
                case SELL:
                    builder.withParam(COMMAND_STRING, request.isMargin() ? "marginStopLimitSell" : "stopLimitSell", true, true);
                    break;
                default:
                    System.out.println("Invalid TradeType in rewriteTradeRequest: " + request.getType());
                    return RequestArgs.unsupported();
            }
        }
        if (request.isStopLimit()) {
            builder.withParam("stopRate", String.valueOf(request.getStop()), true, true);
        }
        builder.withParam("amount", String.valueOf(request.getAmount()), true, true);
        builder.withParam("rate", String.valueOf(request.getRate()), true, true);
        builder.withParam("currencyPair", PoloniexUtils.formatCurrencyPair(request.getPair()), true, true);
        builder.withParam("fillOrKill", request.isFillOrKill() ? "1" : "0", true, true);
        builder.withParam("immediateOrCancel", request.isImmediateOrCancel() ? "1" : "0", true, true);
        builder.withParam("postOnly", request.isPostOnly() ? "1" : "0", true, true);
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()), true, true);
        return builder.build();
    }

    private static RequestArgs rewriteVolumeRequest(VolumeRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PUBLIC_URI);
        builder.withParam(COMMAND_STRING, "return24hVolume", true, true);
        builder.isPrivate(false);
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        return builder.build();
    }

    private static RequestArgs rewriteGetLendingHistoryRequest(GetLendingHistoryRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "returnLendingHistory", true, true);
        System.out.println("Start: " + String.valueOf(request.getStart() / 1000L));
        System.out.println("End: " + String.valueOf(request.getEnd() / 1000L));
        builder.withParam("start", String.valueOf(request.getStart() / 1000L), true, true);
        builder.withParam("end", String.valueOf(request.getEnd() / 1000L), true, true);
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()), true, true);
        return builder.build();
    }

    private static RequestArgs rewriteFeeRequest(FeeRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "returnFeeInfo", true, true);
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()), true, true);
        return builder.build();
    }
}

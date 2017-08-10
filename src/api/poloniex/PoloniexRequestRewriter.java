package api.poloniex;

import api.CurrencyPair;
import api.RequestArgs;
import api.request.*;
import api.request.tmp_loan.*;
import api.request.MoveOrderRequest;
import api.tmp_loan.LoanType;
import api.tmp_loan.PrivateLoanOrder;

/**
 * Converts a {@link MarketRequest} into a {@link api.RequestArgs} specific to {@link Poloniex} which can be used to construct an {@link org.apache.http.HttpRequest} and access the API of the website.
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
    RequestArgs rewriteRequest(MarketRequest request) {
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
        } else if (request instanceof TradeHistoryRequest) {
            return rewriteTradeHistoryRequest((TradeHistoryRequest) request);
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
        } else if (request instanceof GetActiveLoansRequest) {
            return rewriteGetActiveLoansRequest((GetActiveLoansRequest) request);
        } else if (request instanceof OrderTradesRequest) {
            return rewriteOrderTradesRequest((OrderTradesRequest) request);
        }
        return RequestArgs.unsupported();
    }

    private RequestArgs rewriteCancelRequest(CancelRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        switch (request.getType()) {
            case TRADE:
                builder.withParam(COMMAND_STRING, "cancelOrder");
                break;
            case LOAN:
                builder.withParam(COMMAND_STRING, "cancelLoanOffer");
                break;
            default:
                System.out.println("Invalid CancelType in rewriteCancelRequest: " + request.getType());
                return RequestArgs.unsupported();
        }
        builder.withParam("orderNumber", String.valueOf(request.getId()));
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        return builder.build();
    }

    private RequestArgs rewriteOrderBookRequest(OrderBookRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PUBLIC_URI);
        builder.withParam(COMMAND_STRING, "returnOrderBook");
        if (request.getCurrencyPair() != null) {
            builder.withParam("currencyPair", PoloniexUtils.formatCurrencyPair(request.getCurrencyPair()));
        } else {
            builder.withParam("currencyPair", "all");
        }
        builder.withParam("depth", String.valueOf(request.getDepth()));
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        builder.isPrivate(false);
        return builder.build();
    }

    private RequestArgs rewriteMarginPositionRequest(MarginPositionRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "getMarginPosition");
        // TODO(stfinancial): Potentially add support for "all" by making CurrencyPair optional field.
        builder.withParam("currencyPair", PoloniexUtils.formatCurrencyPair(request.getPair()));
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        return builder.build();
    }

    private RequestArgs rewriteMarginAccountSummaryRequest(MarginAccountSummaryRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "returnMarginAccountSummary");
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        return builder.build();
    }

    private RequestArgs rewriteGetPublicLoanOrdersRequest(GetPublicLoanOrdersRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PUBLIC_URI);
        builder.withParam(COMMAND_STRING, "returnLoanOrders");
        builder.withParam("currency", PoloniexUtils.getCurrencyString(request.getCurrency()));
        // TODO(stfinancial): Get this value from the request instead.
        builder.withParam("limit", String.valueOf(999999));
        builder.isPrivate(false);
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        return builder.build();
    }

    private RequestArgs rewriteGetPrivateLoanOffersRequest(GetPrivateLoanOffersRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "returnOpenLoanOffers");
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        return builder.build();
    }

    private RequestArgs rewriteCreateLoanOfferRequest(CreateLoanOfferRequest request) {
        PrivateLoanOrder order = request.getOrder();
        if (order.getType() != LoanType.OFFER) {
            System.out.println("Cannot create a LoanOffer without a LoanType of OFFER: " + order.getType());
            return RequestArgs.unsupported();
        }
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "createLoanOffer");
        builder.withParam("currency", PoloniexUtils.getCurrencyString(order.getCurrency()));
        builder.withParam("amount", String.valueOf(order.getAmount()));
        builder.withParam("lendingRate", String.valueOf(order.getRate()));
        builder.withParam("duration", String.valueOf(order.getDuration()));
        builder.withParam("isAutoRenew", order.isAutoRenew() ? "1" : "0");
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        return builder.build();
    }

    private RequestArgs rewriteAccountBalanceRequest(AccountBalanceRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "returnAvailableAccountBalances");
        switch (request.getType()) {
            case MARGIN:
                builder.withParam("account", "margin");
                break;
            case EXCHANGE:
                builder.withParam("account", "exchange");
                break;
            case LOAN:
                builder.withParam("account", "lending");
                break;
            default:
                builder.withParam("account", "all");
                break;
        }
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        return builder.build();
    }

    private RequestArgs rewriteOpenOrderRequest(OpenOrderRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "returnOpenOrders");
        // TODO(stfinancial): This doesn't return loansAvailable and stopLimit

        CurrencyPair pair = request.getCurrencyPair();
        if (pair == null) {
            builder.withParam("currencyPair", "all");
        } else {
            builder.withParam("currencyPair", PoloniexUtils.formatCurrencyPair(request.getCurrencyPair()));
        }
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        return builder.build();
    }

    private RequestArgs rewriteTickerRequest(TickerRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PUBLIC_URI);
        builder.withParam(COMMAND_STRING, "returnTicker");
        builder.isPrivate(false);
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        return builder.build();
    }

    private RequestArgs rewriteTransferBalanceRequest(TransferBalanceRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "transferBalance");
        builder.withParam("currency", PoloniexUtils.getCurrencyString(request.getCurrency()));
        builder.withParam("amount", String.valueOf(request.getAmount()));
        builder.withParam("fromAccount", PoloniexUtils.getNameForAccountType(request.from()));
        builder.withParam("toAccount", PoloniexUtils.getNameForAccountType(request.to()));
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        return builder.build();
    }

    private RequestArgs rewriteMoveOrderRequest(MoveOrderRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "moveOrder");
        builder.withParam("orderNumber", request.getOrderNumber());
        builder.withParam("rate", String.valueOf(request.getRate()));
        builder.withParam("immediateOrCancel", request.isImmediateOrCancel() ? "1" : "0");
        builder.withParam("postOnly", request.isPostOnly() ? "1" : "0");
        if (request.getAmount() != 0) {
            builder.withParam("amount", String.valueOf(request.getAmount()));
        }
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        return builder.build();
    }

    private RequestArgs rewriteTradeHistoryRequest(TradeHistoryRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "returnTradeHistory");
        if (request.getPair() != null) {
            builder.withParam("currencyPair", PoloniexUtils.formatCurrencyPair(request.getPair()));
        } else {
            builder.withParam("currencyPair", "all");
        }
        if (request.getStart() != 0 || request.getEnd() != 0) {
            // TODO(stfinancial): These are not exactly working right, but I think it is Poloniex's fault.
            builder.withParam("start", String.valueOf(request.getStart()));
            builder.withParam("end", String.valueOf(request.getEnd()));
        }
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        return builder.build();
    }

    private RequestArgs rewriteTradeRequest(TradeRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        // TODO(stfinancial): Handle market type requests somehow.
        if (request.isMarket()) {
            return RequestArgs.unsupported();
        }
        if (!request.isStopLimit()) {
            switch (request.getTrade().getType()) {
                case BUY:
                    builder.withParam(COMMAND_STRING, request.isMargin() ? "marginBuy" : "buy");
                    break;
                case SELL:
                    builder.withParam(COMMAND_STRING, request.isMargin() ? "marginSell" : "sell");
                    break;
                default:
                    System.out.println("Invalid TradeType in rewriteTradeRequest: " + request.getType());
                    return RequestArgs.unsupported();
            }
        } else {
            switch (request.getTrade().getType()) {
                case BUY:
                    builder.withParam(COMMAND_STRING, request.isMargin() ? "marginStopLimitBuy" : "stopLimitBuy");
                    break;
                case SELL:
                    builder.withParam(COMMAND_STRING, request.isMargin() ? "marginStopLimitSell" : "stopLimitSell");
                    break;
                default:
                    System.out.println("Invalid TradeType in rewriteTradeRequest: " + request.getType());
                    return RequestArgs.unsupported();
            }
        }
        if (request.isStopLimit()) {
            builder.withParam("stopRate", String.valueOf(request.getStop()));
        }
        builder.withParam("amount", String.valueOf(request.getAmount()));
        builder.withParam("rate", String.valueOf(request.getRate()));
        builder.withParam("currencyPair", PoloniexUtils.formatCurrencyPair(request.getPair()));
        switch (request.getTimeInForce()) {
            case GOOD_TIL_CANCELLED:
                // This is the default, do nothing.
                break;
            case IMMEDIATE_OR_CANCEL:
                builder.withParam("immediateOrCancel", "1");
                break;
            case FILL_OR_KILL:
                builder.withParam("fillOrKill", "1");
                break;
            default:
                System.out.println("Unsupported TimeInForce on Poloniex: " + request.getTimeInForce().toString());
                return RequestArgs.unsupported();
        }
        if (request.isMargin() && request.getMaxRate() != 0) {
            builder.withParam("lendingRate", String.valueOf(request.getMaxRate()));
        }
        builder.withParam("postOnly", request.isPostOnly() ? "1" : "0");
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        return builder.build();
    }

    private RequestArgs rewriteVolumeRequest(VolumeRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PUBLIC_URI);
        builder.withParam(COMMAND_STRING, "return24hVolume");
        builder.isPrivate(false);
        builder.httpRequestType(RequestArgs.HttpRequestType.GET);
        return builder.build();
    }

    private RequestArgs rewriteGetLendingHistoryRequest(GetLendingHistoryRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "returnLendingHistory", true);
        System.out.println("Start: " + String.valueOf(request.getStart() / 1000L));
        System.out.println("End: " + String.valueOf(request.getEnd() / 1000L));
        builder.withParam("start", String.valueOf(request.getStart() / 1000L));
        builder.withParam("end", String.valueOf(request.getEnd() / 1000L));
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        return builder.build();
    }

    private RequestArgs rewriteFeeRequest(FeeRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "returnFeeInfo");
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        return builder.build();
    }

    private RequestArgs rewriteGetActiveLoansRequest(GetActiveLoansRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "returnActiveLoans");
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        return builder.build();
    }

    private RequestArgs rewriteOrderTradesRequest(OrderTradesRequest request) {
        RequestArgs.Builder builder = new RequestArgs.Builder(PRIVATE_URI);
        builder.withParam(COMMAND_STRING, "returnOrderTrades");
        builder.withParam("orderNumber", request.getId());
        builder.isPrivate(true);
        builder.httpRequestType(RequestArgs.HttpRequestType.POST);
        builder.withParam("nonce", String.valueOf(System.currentTimeMillis()));
        return builder.build();
    }
}

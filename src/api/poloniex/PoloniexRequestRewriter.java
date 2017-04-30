package api.poloniex;

import api.CurrencyPair;
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
    /**
     * Converts a {@link MarketRequest} into {@link RequestArgs} which can be used to construct an encoded URL and signed
     * data object used to send a request to Poloniex.
     * @param request The request to be converted to {@code RequestArgs} understood by the {@link Poloniex} market.
     * @return The {@code RequestArgs} containing the command and arguments to construct an encoded URL and signed data
     * object. Returns {@link RequestArgs#unsupported()} if the request is not supported or cannot be converted to a
     * valid command.
     */
    static RequestArgs rewriteRequest(MarketRequest request) {
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
        String command;
        switch (request.getType()) {
            case TRADE:
                command = "cancelOrder";
                break;
            case LOAN:
                command = "cancelLoanOffer";
                break;
            default:
                System.out.println("Invalid CancelType in rewriteCancelRequest: " + request.getType());
                return RequestArgs.unsupported();
        }
        RequestArgs args = new RequestArgs(command);
        args.addParam("orderNumber", String.valueOf(request.getId()));
        return args;
    }

    private static RequestArgs rewriteOrderBookRequest(OrderBookRequest request) {
        RequestArgs args = new RequestArgs("returnOrderBook");
        if (request.getCurrencyPair().isPresent()) {
            args.addParam("currencyPair", PoloniexUtils.formatCurrencyPair(request.getCurrencyPair().get()));
        } else {
            args.addParam("currencyPair", "all");
        }
        args.addParam("depth", String.valueOf(request.getDepth()));
        return args;
    }

    private static RequestArgs rewriteMarginPositionRequest(MarginPositionRequest request) {
        RequestArgs args = new RequestArgs("getMarginPosition");
        // TODO(stfinancial): Potentially add support for "all" by making CurrencyPair optional field.
        args.addParam("currencyPair", PoloniexUtils.formatCurrencyPair(request.getPair()));
        return args;
    }

    private static RequestArgs rewriteMarginAccountSummaryRequest(MarginAccountSummaryRequest request) {
        return new RequestArgs("returnMarginAccountSummary");
    }

    private static RequestArgs rewriteGetPublicLoanOrdersRequest(GetPublicLoanOrdersRequest request) {
        RequestArgs args = new RequestArgs("returnLoanOrders");
        args.addParam("currency", PoloniexUtils.getCurrencyString(request.getCurrency()));
        // TODO(stfinancial): Get this value from the request instead.
        args.addParam("limit", String.valueOf(999999));
        return args;
    }

    private static RequestArgs rewriteGetPrivateLoanOffersRequest(GetPrivateLoanOffersRequest request) {
        RequestArgs args = new RequestArgs("returnOpenLoanOffers");
        return args;
    }

    private static RequestArgs rewriteCreateLoanOfferRequest(CreateLoanOfferRequest request) {
        RequestArgs args = new RequestArgs("createLoanOffer");
        PrivateLoanOrder order = request.getOrder();
        if (order.getType() != LoanType.OFFER) {
            System.out.println("Cannot create a LoanOffer without a LoanType of OFFER: " + order.getType());
            return RequestArgs.unsupported();
        }
        args.addParam("currency", PoloniexUtils.getCurrencyString(order.getCurrency()));
        args.addParam("amount", String.valueOf(order.getAmount()));
        args.addParam("lendingRate", String.valueOf(order.getRate()));
        args.addParam("duration", String.valueOf(order.getDuration()));
        args.addParam("isAutoRenew", order.isAutoRenew() ? "1" : "0");
        return args;
    }

    private static RequestArgs rewriteAccountBalanceRequest(AccountBalanceRequest request) {
        RequestArgs args = new RequestArgs("returnAvailableAccountBalances");
        switch (request.getType()) {
            case MARGIN:
                args.addParam("account", "margin");
                break;
            case EXCHANGE:
                args.addParam("account", "exchange");
                break;
            case LOAN:
                args.addParam("account", "lending");
                break;
            case ALL:
                args.addParam("account", "all");
                break;
            default:
                args.addParam("account", "all");
                break;
        }
        return args;
    }

    private static RequestArgs rewriteOpenOrderRequest(OpenOrderRequest request) {
        // TODO(stfinancial): Replace with returnAllOpenOrders, but check that this works with currencyPair?

        RequestArgs args = new RequestArgs("returnOpenOrders");
        CurrencyPair pair = request.getCurrencyPair();
        if (pair == null) {
            args.addParam("currencyPair", "all");
        } else {
            args.addParam("currencyPair", PoloniexUtils.formatCurrencyPair(request.getCurrencyPair()));
        }
        return args;
    }

    private static RequestArgs rewriteTickerRequest(TickerRequest request) {
        RequestArgs args = new RequestArgs("returnTicker");
        return args;
    }

    private static RequestArgs rewriteTransferBalanceRequest(TransferBalanceRequest request) {
        RequestArgs args = new RequestArgs("transferBalance");
        args.addParam("currency", PoloniexUtils.getCurrencyString(request.getCurrency()));
        args.addParam("amount", String.valueOf(request.getAmount()));
        args.addParam("fromAccount", PoloniexUtils.getNameForAccountType(request.from()));
        args.addParam("toAccount", PoloniexUtils.getNameForAccountType(request.to()));
        return args;
    }

    private static RequestArgs rewriteMoveOrderRequest(MoveOrderRequest request) {
        RequestArgs args = new RequestArgs("moveOrder");
        args.addParam("orderNumber", String.valueOf(request.getOrderNumber()));
        args.addParam("rate", String.valueOf(request.getRate()));
        args.addParam("immediateOrCancel", request.isImmediateOrCancel() ? "1" : "0");
        args.addParam("postOnly", request.isPostOnly() ? "1" : "0");
        if (request.getAmount() != 0) {
            args.addParam("amount", String.valueOf(request.getAmount()));
        }
        return args;
    }

    private static RequestArgs rewriteTradeRequest(TradeRequest request) {
        // TODO(stfinancial): Handle market type requests somehow.
        String command;
        if (!request.isStopLimit()) {
            switch (request.getTrade().getType()) {
                case BUY:
                    command = request.isMargin() ? "marginBuy" : "buy";
                    break;
                case SELL:
                    command = request.isMargin() ? "marginSell" : "sell";
                    break;
                default:
                    System.out.println("Invalid TradeType in rewriteTradeRequest: " + request.getType());
                    return RequestArgs.unsupported();
            }
        } else {
            switch (request.getTrade().getType()) {
                case BUY:
                    command = request.isMargin() ? "marginStopLimitBuy" : "stopLimitBuy";
                    break;
                case SELL:
                    command = request.isMargin() ? "marginStopLimitSell" : "stopLimitSell";
                    break;
                default:
                    System.out.println("Invalid TradeType in rewriteTradeRequest: " + request.getType());
                    return RequestArgs.unsupported();
            }
        }
        RequestArgs args = new RequestArgs(command);
        if (request.isStopLimit()) {
            args.addParam("stopRate", String.valueOf(request.getStop()));
        }
        args.addParam("amount", String.valueOf(request.getAmount()));
        args.addParam("rate", String.valueOf(request.getRate()));
        args.addParam("currencyPair", PoloniexUtils.formatCurrencyPair(request.getPair()));
        args.addParam("fillOrKill", request.isFillOrKill() ? "1" : "0");
        args.addParam("immediateOrCancel", request.isImmediateOrCancel() ? "1" : "0");
        args.addParam("postOnly", request.isPostOnly() ? "1" : "0");
        return args;
    }

    private static RequestArgs rewriteVolumeRequest(VolumeRequest request) {
        RequestArgs args = new RequestArgs("return24hVolume");
        return args;
    }

    private static RequestArgs rewriteGetLendingHistoryRequest(GetLendingHistoryRequest request) {
        RequestArgs args = new RequestArgs("returnLendingHistory");
        System.out.println("Start: " + String.valueOf(request.getStart() / 1000L));
        System.out.println("End: " + String.valueOf(request.getEnd() / 1000L));
        args.addParam("start", String.valueOf(request.getStart() / 1000L));
        args.addParam("end", String.valueOf(request.getEnd() / 1000L));
        return args;
    }

    private static RequestArgs rewriteFeeRequest(FeeRequest request) {
        RequestArgs args = new RequestArgs("returnFeeInfo");
        return args;
    }
}

TODO:
    Need to decide if this method of doing Request-Response is better than having interfaces for the market.
    
Requirements:

Ticker:
* Request
    + Poloniex - 
    + Bitfinex -
    + Kraken -
* Response
    + Poloniex -
    + Bitfinex -
    + Kraken - 
    
Trade History:
* Request
    + Poloniex - CurrencyPair, start and end are all optional, returns 1 day if no timestamps are specified
    + Kraken - CurrencyPair is not allowed in the request, though the response can be filtered if desired, start and end are also optional, though the default behavior is not specified.
* Response
    + Poloniex -
    + Kraken -
    
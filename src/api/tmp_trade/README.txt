This package is likely temporary until I figure out all of the different types of trades and how to represent them all
properly.


**** Types ****

// TODO(stfinancial): Add comments explaining these things in their respective files.

### Trade Request ####
A TradeOrder that we want to place (Private) x
    - CurrencyPair
    - PRice
    - Amount
    - Buy or Sell
    - (optional) Is margin
    - (Optional) Post only, fill or kill, immediate or cancel


### Trade ###
Trades available on the order book (Public) x
    - CurrencyPAir
    - PRice
    - Amount
    - Buy or sell

### Trade Order ###
A trade order we have placed on the order book (Private) x
    - CurrencyPair
    - Price
    - Amount
    - Buy or sell
    - Timestamp
    - (Optional) Rate/Stop

### Completed Trade ###
Completed Trades / Trade History (Private) x
    - CurrencyPair
    - Price
    - Amount
    - Buy or sell
    - Fee
    - (Optional) Is margin
    - Trade timestamp

### Completed Trade ###
Completed Trades (Public)
    - Currency Pair
    - Price
    - Amount
    - Buy or sell
    - trade timestamp
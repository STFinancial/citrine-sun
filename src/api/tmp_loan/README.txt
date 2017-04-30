This package is likely temporary until I sort out the business of loans and what types of loans there are and how
to represent all of them.


(Optional) - This is a Market specific trait.

// TODO(stfinancial): Add comments in the respective files with explanations like this.

** Types **

#### Loan Request ####
A loan offer that we want to place (Private)
    - Currency
    - Lending Rate
    - Amount
    - (Optional) Duration
    - (Optional) Auto Renew

#### PrivateLoanOrder ####
A loan offer that is currently on the books, but not active (Private)
    - Currency
    - Lending Rate
    - Amount
    - Creation timestamp
    - (Optional) Duration
    - (Optional) Auto Renew


Loans that other people have offered (Public)
    - Currency
    - Lending Rate
    - Amount
    - (Optional) Duration [Range]

Loans that other people are demanding (Public)
    - Currency
    - Lending Rate
    - Amount
    - (Optional) Duration


#### ActiveLoan ####
Active loans we are providing (Private) x
    - Currency
    - Lending Rate
    - Amount
    - Loan number
    - (Optional) Duration
    - (Optional) Auto Renew

#### ActiveLoan ####
Active loans we are consuming (Private) x
    - Currency
    - Lending Rate
    - Amount
    - Loan number
    - (Optional) Duration

#### CompletedLoan ####
Personal loan history (Private) x
    - Currency
    - Lending Rate
    - Amount
    - Start timestamp
    - End timestamp
    - Loan id
    - (Optional) Fee taken by the exchange [This can be calculated approximately]
    - (Optional) Amount earned [This can be calculated approximately]
    - (Optional) Originally specified duration [This information is obtained from the active loan]
    - (Optional) Actual duration


Clearly there are a few things to note here:

- We should perhaps separate requests into public and private requests.
- All of these requests have a single thing in common, which is that they have a Currency, Lending Rate, and Amount
- Though all of them have Duration, the public loan offers give a duration range


Considerations:
- Should the loan "kernel" class have a type, that is an orientation of either consumer or provider.

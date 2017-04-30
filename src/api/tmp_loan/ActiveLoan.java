package api.tmp_loan;

/**
 * Represents a {@link Loan} that is currently in progress. That is, it has not been completely repaid.
 */
public final class ActiveLoan {
    // TODO(stfinancial): Perhaps take a reference to the order which created this.
    private Loan loan;
    private int duration;
    private long creationTimestamp;
    private long loanNumber;
    private boolean isAutoRenew;

    // TODO(stfinancial): This constructor is kind of long, do we want to shorten it?
    public ActiveLoan(Loan loan, int duration, long creationTimestamp, long loanNumber, boolean isAutoRenew) {
        this.loan = loan;
        this.duration = duration;
        this.creationTimestamp = creationTimestamp;
        this.loanNumber = loanNumber;
        this.isAutoRenew = isAutoRenew;
    }

    public Loan getLoan() { return loan; }
    public int getDuration() { return duration; }
    public long getCreationTimestamp() { return creationTimestamp; }
    public long getLoanNumber() { return loanNumber; }
    public boolean isAutoRenew() { return isAutoRenew; }
}

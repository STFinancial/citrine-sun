package api.tmp_loan;

/**
 * These are loans that are part of the personal loan history.
 */
public final class CompletedLoan {
    // TODO(stfinancial): Convenience methods to more easily access Loan fields.

    private final Loan loan;
    private final String loanId;
    // TODO(stfinancial): Think about whether this makes sense.
    private double duration; // Loan duration in days. Convert to seconds?
    private final long start;
    private final long end;
    private double interest;
    private final double fee;
    // TODO(stfinancial): Think about whether this makes sense.
    private double earned; // This can derived.

    // TODO(stfinancial): Better way to handle this... possibly builder.
    // Some fields can be computed manually, including interest and earned, if given fee. As well as duration.
    private CompletedLoan(Builder builder) {
        this.loan = builder.loan;
        this.loanId = builder.loanId;
        this.duration = builder.duration;
        this.start = builder.start;
        this.end = builder.end;
        this.interest = builder.interest;
        this.fee = builder.fee;
        this.earned = builder.earned;
    }

    public Loan getLoan() { return loan; }
    public String getLoanId() { return loanId; }
    /** @return The duration in days, of this {@code CompletedLoan} */
    public double getDuration() { return duration; }
    /** @return The UTC timestamp on which this loan began */
    public long getStart() { return start; }
    /** @return The UTC timestamp on which this loan ended */
    public long getEnd() { return end; }
    /** @return The interest amount, before fees, earned in this corresponding {@link Loan}'s {@link api.Currency}. */
    public double getInterest() { return interest; }
    /** @return The amount of fees paid on the {@link api.tmp_loan.CompletedLoan#interest} in this corresponding {@link Loan}'s {@link api.Currency}. NOTE: This value is currently negative. */
    public double getFee() { return fee; }
    /** @return The amount earned, after fees, in the corresponding {@link api.tmp_loan.CompletedLoan#loan} {@link api.Currency}. */
    public double getEarned() { return earned; }

    public static final class Builder {
        private final Loan loan;
        private final String loanId;
        private final long start;
        private final long end;
        private final double fee;
        private double duration;
        private double interest;
        private double earned;

        public Builder(Loan loan, String loanId, long start, long end, double fee) {
            this.loan = loan;
            this.loanId = loanId;
            this.start = start;
            this.end = end;
            this.fee = fee;
        }

        public Builder duration(double duration) {
            this.duration = duration;
            return this;
        }

        public Builder interest(double interest) {
            this.interest = interest;
            return this;
        }

        public Builder earned(double earned) {
            this.earned = earned;
            return this;
        }

        public CompletedLoan build() {
            return new CompletedLoan(this);
        }
    }
}

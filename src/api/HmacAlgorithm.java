package api;

/**
 * Specifies an HMAC algorithm for use with the Mac object.
 */
public enum HmacAlgorithm {
    // TODO(stfinancial): This bit of abstraction may be a bit much.
    HMACMD5("HmacMD5"),
    HMACSHA1("HmacSHA1"),
    HMACSHA256("HmacSHA256"),
    HMACSHA384("HmacSHA384"),
    HMACSHA512("HmacSHA512");

    private String standardName;

    HmacAlgorithm(String standardName) { this.standardName = standardName; }

    /**
     * @return The algorithm name as specified in the Java Cryptography Architecture
     * Standard Algorithm Name Documentation. For use in initializing the Mac object.
     */
    String getStandardName() { return standardName; }
}

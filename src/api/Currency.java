package api;

import com.sun.istack.internal.Nullable;

import java.util.*;

/**
 * {@link Market} agnostic representation of a currency.
 */
public enum Currency {
    // TODO(stfinancial): Review this class and consider EnumMap for mapping currency to representations.

    // TODO(stfinancial): Add human readable names.
    AMP(Arrays.asList("AMP")),
    ARDR(Arrays.asList("ARDR")),
    BBR(Arrays.asList("BBR")),
    BCN(Arrays.asList("BCN")),
    BCY(Arrays.asList("BCY")),
    BELA(Arrays.asList("BELA")),
    BITS(Arrays.asList("BITS")),
    BLK(Arrays.asList("BLK")),
    BTC(Arrays.asList("BTC","XBT")),
    BTCD(Arrays.asList("BTCD")),
    BTS(Arrays.asList("BTS")),
    BTM(Arrays.asList("BTM")),
    BURST(Arrays.asList("BURST")),
    C2(Arrays.asList("C2")),
    CLAM(Arrays.asList("CLAM")),
    CURE(Arrays.asList("CURE")),
    DASH(Arrays.asList("DASH")),
    DCR(Arrays.asList("DCR")),
    DGB(Arrays.asList("DGB")),
    DOGE(Arrays.asList("DOGE")),
    EMC2(Arrays.asList("EMC2")),
    ETC(Arrays.asList("ETC")),
    ETH(Arrays.asList("ETH")),
    EXP(Arrays.asList("EXP")),
    FCT(Arrays.asList("FCT")),
    FLDC(Arrays.asList("FLDC")),
    FLO(Arrays.asList("FLO")),
    GAME(Arrays.asList("GAME")),
    GBP(Arrays.asList("GBP")),
    GNO(Arrays.asList("GNO")),
    GNT(Arrays.asList("GNT")),
    GRC(Arrays.asList("GRC")),
    HUC(Arrays.asList("HUC")),
    HZ(Arrays.asList("HZ")),
    IOC(Arrays.asList("IOC")),
    LBC(Arrays.asList("LBC")),
    LSK(Arrays.asList("LSK")),
    LTC(Arrays.asList("LTC")),
    MAID(Arrays.asList("MAID")),
    MYR(Arrays.asList("MYR")),
    NAUT(Arrays.asList("NAUT")),
    NAV(Arrays.asList("NAV")),
    NEOS(Arrays.asList("NEOS")),
    NMC(Arrays.asList("NMC")),
    NOBL(Arrays.asList("NOBL")),
    NOTE(Arrays.asList("NOTE")),
    NSR(Arrays.asList("NSR")),
    NXC(Arrays.asList("NXC")),
    NXT(Arrays.asList("NXT")),
    OMNI(Arrays.asList("OMNI")),
    PASC(Arrays.asList("PASC")),
    PINK(Arrays.asList("PINK")),
    POT(Arrays.asList("POT")),
    PPC(Arrays.asList("PPC")),
    QBK(Arrays.asList("QBK")),
    QORA(Arrays.asList("QORA")),
    QTL(Arrays.asList("QTL")),
    RADS(Arrays.asList("RADS")),
    RBY(Arrays.asList("RBY")),
    REP(Arrays.asList("REP")),
    RIC(Arrays.asList("RIC")),
    SBD(Arrays.asList("SBD")),
    SC(Arrays.asList("SC")),
    SDC(Arrays.asList("SDC")),
    SJCX(Arrays.asList("SJCX")),
    STEEM(Arrays.asList("STEEM")),
    STR(Arrays.asList("STR","XLN")),
    STRAT(Arrays.asList("STRAT")),
    SYS(Arrays.asList("SYS")),
    UNITY(Arrays.asList("UNITY")),
    USDT(Arrays.asList("USDT")),
    USD(Arrays.asList("USD")),
    VIA(Arrays.asList("VIA")),
    VOX(Arrays.asList("VOX")),
    VRC(Arrays.asList("VRC")),
    VTC(Arrays.asList("VTC")),
    XBC(Arrays.asList("XBC")),
    XCP(Arrays.asList("XCP")),
    XEM(Arrays.asList("XEM")),
    XMG(Arrays.asList("XMG")),
    XMR(Arrays.asList("XMR")),
    XPM(Arrays.asList("XPM")),
    XRP(Arrays.asList("XRP")),
    XVC(Arrays.asList("XVC")),
    ZEC(Arrays.asList("ZEC"));

    /** Contains the canonical currency for a specific coin alias. E.g. "XBT" -> BTC */
    private static final Map<String, Currency> ALIAS_MAP;

    /** The names that a {@link Currency} has across various {@link Market Markets}. */
    private List<String> aliases;

    static {
        // Load the currency's representations into the alias map.
        Map<String, Currency> aliasMap = new HashMap<>();
        for (Currency currency : values()) {
            for (String alias : currency.aliases) {
                aliasMap.put(alias, currency);
            }
        }
        ALIAS_MAP = Collections.unmodifiableMap(aliasMap);
    }

    Currency(List<String> aliases) {
        this.aliases = aliases;
    }

    // TODO(stfinancial): Shorter name to improve readability of entire program. Maybe fromString?
    @Nullable
    /**
     * A {@link Currency} may have different representations on different {@link Market Markets}. For example,
     * some sites use "XBT" for Bitcoin while others use "BTC". To maintain market agnostic currency representations,
     * this function can be used to get the canonoical Currency enum for a given name.
     * @param alias The representation of the currency used on the specific Market.
     * @return The Currency enum corresponding to this alias name, null if the name does not correspond to a Currency.
     */
    public static Currency getCanonicalRepresentation(String alias) {
        // TODO(stfinancial): Does this make sense, or should this be moved to a Market level thing?
//        System.out.println(alias);
        return ALIAS_MAP.get(alias);
    }
}

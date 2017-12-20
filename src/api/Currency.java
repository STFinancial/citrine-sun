package api;

import com.sun.istack.internal.Nullable;

import java.util.*;

/**
 * {@link Market} agnostic representation of a currency.
 */
public enum Currency {
    // TODO(stfinancial): Review this class and consider EnumMap for mapping currency to representations.

    // TODO(stfinancial): Add human readable names.
    // TODO(stfinancial): Make sure that there are no duplicate names here. If there is, we may have to disambiguate with exchange names.
    AMP(Arrays.asList("AMP"), "X"), // Synereo AMP
    ARDR(Arrays.asList("ARDR"), "X"), // Ardor
    BBR(Arrays.asList("BBR"), "X"), // Boolberry
    // TODO(stfinancial): BCC is both Bitconnect and Bitcoin Cash in some markets, need to find a way to handle this.
    BCH(Arrays.asList("BCH", "BCC"), "X"), // Bitcoin Cash
    BCN(Arrays.asList("BCN"), "X"), // Bytecoin
    BCY(Arrays.asList("BCY"), "X"),
    BELA(Arrays.asList("BELA"), "X"),
    BITS(Arrays.asList("BITS"), "X"),
    BLK(Arrays.asList("BLK"), "X"),
    BNT(Arrays.asList("BNT"), "X"), // Bancor Network Token
    BTC(Arrays.asList("BTC","XBT"), "X"),
    BTCD(Arrays.asList("BTCD"), "X"),
    BTS(Arrays.asList("BTS"), "X"), // Bitshares
    BTM(Arrays.asList("BTM"), "X"), // Bitmark
    BURST(Arrays.asList("BURST"), "X"), // Burstcoin
    C2(Arrays.asList("C2"), "X"),
    CAD(Arrays.asList("CAD"), "Z"), // Canadian Dollar
    CLAM(Arrays.asList("CLAM"), "X"),
    CURE(Arrays.asList("CURE"), "X"),
    DAO(Arrays.asList("DAO"), "X"), // The DAO tokens.
    DASH(Arrays.asList("DASH","DSH"), "X"), // Dash
    DCR(Arrays.asList("DCR"), "X"), // Decred
    DGB(Arrays.asList("DGB"), "X"), // Digibyte
    DOGE(Arrays.asList("DOGE","XDG"), "X"), // Dogecoin
    EMC2(Arrays.asList("EMC2"), "X"),
    EOS(Arrays.asList("EOS"), "X"), // Eos
    ETC(Arrays.asList("ETC"), "X"), // Ethereum Classic
    ETH(Arrays.asList("ETH"), "X"), // Ethereum
    EUR(Arrays.asList("EUR"), "Z"), // Euro
    EXP(Arrays.asList("EXP"), "X"), // Expanse
    FCT(Arrays.asList("FCT"), "X"), // Factom
    FLDC(Arrays.asList("FLDC"), "X"), // Folding Coin
    FLO(Arrays.asList("FLO"), "X"),
    GAME(Arrays.asList("GAME"), "X"), // Game Credits
    GBP(Arrays.asList("GBP"), "Z"), // British Pound
    GBYTE(Arrays.asList("GBYTE"), "X"), // Byteball
    GNO(Arrays.asList("GNO"), "X"), // Gnosis
    GNT(Arrays.asList("GNT"), "X"), // Golem Network Token
    GRC(Arrays.asList("GRC"), "X"), // Gridcoin
    HUC(Arrays.asList("HUC"), "X"), // Huntercoin
    HZ(Arrays.asList("HZ"), "X"),
    ICN(Arrays.asList("ICN"), "X"),
    IOC(Arrays.asList("IOC"), "X"),
    IOTA(Arrays.asList("IOTA","IOT"), "X"), // Iota
    JPY(Arrays.asList("JPY"), "X"), // Japanese Yen
    LBC(Arrays.asList("LBC"), "X"), // Library Credits
    LSK(Arrays.asList("LSK"), "X"), // Lisk
    LTC(Arrays.asList("LTC"), "X"), // Litecoin
    MAID(Arrays.asList("MAID"), "X"), // Maidsafe Coin
    MLN(Arrays.asList("MLN"), "X"), // Melonport
    MYR(Arrays.asList("MYR"), "X"),
    NAUT(Arrays.asList("NAUT"), "X"),
    NAV(Arrays.asList("NAV"), "X"),
    NEO(Arrays.asList("NEO", "ANS"), "X"), // NEO/Antshares
    NEOS(Arrays.asList("NEOS"), "X"),
    NMC(Arrays.asList("NMC"), "X"),
    NOBL(Arrays.asList("NOBL"), "X"),
    NOTE(Arrays.asList("NOTE"), "X"),
    NSR(Arrays.asList("NSR"), "X"),
    NXC(Arrays.asList("NXC"), "X"),
    NXT(Arrays.asList("NXT"), "X"),
    OMG(Arrays.asList("OMG"), "X"), // OmiseGO
    OMNI(Arrays.asList("OMNI"), "X"),
    PASC(Arrays.asList("PASC"), "X"), // Pascal
    PINK(Arrays.asList("PINK"), "X"),
    POT(Arrays.asList("POT"), "X"), // Potcoin
    PPC(Arrays.asList("PPC"), "X"),
    PTOY(Arrays.asList("PTOY"), "X"), // Patientory
    QBK(Arrays.asList("QBK"), "X"),
    QORA(Arrays.asList("QORA"), "X"),
    QTL(Arrays.asList("QTL"), "X"),
    QTUM(Arrays.asList("QTUM"), "X"), // Qtum
    PIVX(Arrays.asList("PIVX"), "X"), // Pivx
    RADS(Arrays.asList("RADS"), "X"),
    RBY(Arrays.asList("RBY"), "X"),
    REP(Arrays.asList("REP"), "X"), // Augur
    RIC(Arrays.asList("RIC"), "X"),
    RRT(Arrays.asList("RRT"), "X"),
    SBD(Arrays.asList("SBD"), "X"),
    SC(Arrays.asList("SC"), "X"), // Siacoin
    SDC(Arrays.asList("SDC"), "X"),
    SJCX(Arrays.asList("SJCX"), "X"), // Storjcoin
    SNT(Arrays.asList("SNT"), "X"), // Status Network Token
    STEEM(Arrays.asList("STEEM"), "X"), // Steem
    STRAT(Arrays.asList("STRAT"), "X"), // Stratis
    SYS(Arrays.asList("SYS"), "X"), // Syscoin
    UNITY(Arrays.asList("UNITY"), "X"),
    USDT(Arrays.asList("USDT"), "X"), // USD Tether
    USD(Arrays.asList("USD"), "Z"), // United States Dollar
    VIA(Arrays.asList("VIA"), "X"),
    VOX(Arrays.asList("VOX"), "X"), // Voxels
    VRC(Arrays.asList("VRC"), "X"),
    VTC(Arrays.asList("VTC"), "X"),
    WAVES(Arrays.asList("WAVES"), "X"), // Waves
    XBC(Arrays.asList("XBC"), "X"),
    XCP(Arrays.asList("XCP"), "X"), // Counterparty
    XEM(Arrays.asList("XEM"), "X"), // New Economy Movement
    XLM(Arrays.asList("XLM","STR"), "X"), // Stellar Lumens // TODO(stfinancial): Rename this to XLM
    XMG(Arrays.asList("XMG"), "X"),
    XMR(Arrays.asList("XMR"), "X"), // Monero
    XPM(Arrays.asList("XPM"), "X"),
    XRP(Arrays.asList("XRP"), "X"), // Ripple
    XVC(Arrays.asList("XVC"), "X"),
    ZEC(Arrays.asList("ZEC"), "X"), // Zcash
    ZRX(Arrays.asList("ZRX"), "X"); // 0x

    /** Contains the canonical currency for a specific coin alias. E.g. "XBT" -> BTC */
    private static final Map<String, Currency> ALIAS_MAP;

    /** The names that a {@link Currency} has across various {@link Market Markets}. */
    private List<String> aliases;
    /** See http://www.ifex-project.org/our-proposals/x-iso4217-a3 */
    private final String isoNamespace;

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

    Currency(List<String> aliases, String isoNamespace) {
        this.aliases = Collections.unmodifiableList(aliases);
        this.isoNamespace = isoNamespace;
    }

    public String getIsoNamespace() {
        return isoNamespace;
    }

    // TOOD(stfinancial): I'm not really certain this makes a lot of sense. Shouldn't this be done inside the market?
    // TODO(stfinancial): Maybe fromString?
    /**
     * A {@link Currency} may have different representations on different {@link Market Markets}. For example,
     * some sites use "XBT" for Bitcoin while others use "BTC". To maintain market agnostic currency representations,
     * this function can be used to get the canonoical Currency enum for a given name.
     * @param alias The representation of the currency used on the specific Market.
     * @return The Currency enum corresponding to this alias name, null if the name does not correspond to a Currency.
     */
    @Nullable
    public static Currency getCanonicalName(String alias) {
//        System.out.println(alias);
        return ALIAS_MAP.get(alias);
    }
}

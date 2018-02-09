package api;

import com.sun.istack.internal.Nullable;

import java.lang.reflect.Array;
import java.util.*;

/**
 * {@link Market} agnostic representation of a currency.
 */
public enum Currency {
    // TODO(stfinancial): Review this class and consider EnumMap for mapping currency to representations.

    // TODO(stfinancial): Add human readable names.
    // TODO(stfinancial): Make sure that there are no duplicate names here. If there is, we may have to disambiguate with exchange names.
    ADA(Arrays.asList("ADA"), "X"), // Cardano
    ADX(Arrays.asList("ADX"), "X"), // AdEx
    AION(Arrays.asList("AION"), "X"), // Aion
    AMB(Arrays.asList("AMB"), "X"), // Ambrosus
    AMP(Arrays.asList("AMP"), "X"), // Synereo AMP
    APPC(Arrays.asList("APPC"), "X"), // Appcoins
    ARDR(Arrays.asList("ARDR"), "X"), // Ardor
    ARK(Arrays.asList("ARK"), "X"), // Ark
    ARN(Arrays.asList("ARN"), "X"), // Aeron
    AST(Arrays.asList("AST"), "X"), // Airswap Token
    BAT(Arrays.asList("BAT"), "X"), // Basic Attention Token
    BBR(Arrays.asList("BBR"), "X"), // Boolberry
    // TODO(stfinancial): BCC is both Bitconnect and Bitcoin Cash in some markets, need to find a way to handle this.
    BCH(Arrays.asList("BCH", "BCC"), "X"), // Bitcoin Cash
    BCD(Arrays.asList("BCD"), "X"), // Bitcoin Diamond
    BCN(Arrays.asList("BCN"), "X"), // Bytecoin
    BCPT(Arrays.asList("BCPT"), "X"), // Blockmason Credit Protocol
    BCY(Arrays.asList("BCY"), "X"),
    BELA(Arrays.asList("BELA"), "X"),
    BITS(Arrays.asList("BITS"), "X"),
    BLK(Arrays.asList("BLK"), "X"),
    BNB(Arrays.asList("BNB"), "X"), // Binance Coin
    BNT(Arrays.asList("BNT"), "X"), // Bancor Network Token
    BQX(Arrays.asList("BQX"), "X"),
    BRD(Arrays.asList("BRD"), "X"), // Bread
    BTC(Arrays.asList("BTC","XBT"), "X"),
    BTCD(Arrays.asList("BTCD"), "X"),
    BTG(Arrays.asList("BTG"), "X"), // Bitcoin Gold
    BTS(Arrays.asList("BTS"), "X"), // Bitshares
    BTM(Arrays.asList("BTM"), "X"), // Bitmark
    BURST(Arrays.asList("BURST"), "X"), // Burstcoin
    C2(Arrays.asList("C2"), "X"),
    CAD(Arrays.asList("CAD"), "Z"), // Canadian Dollar
    CDT(Arrays.asList("CDT"), "X"), // Coindash
    CLAM(Arrays.asList("CLAM"), "X"),
    CMT(Arrays.asList("CMT"), "X"), // Cyber Miles
    CND(Arrays.asList("CND"), "X"), // Cindicator
    CTR(Arrays.asList("CTR"), "X"), // Centra
    CURE(Arrays.asList("CURE"), "X"),
    CVC(Arrays.asList("CVC"), "X"), // Civic
    DAO(Arrays.asList("DAO"), "X"), // The DAO tokens.
    DASH(Arrays.asList("DASH","DSH"), "X"), // Dash
    DCR(Arrays.asList("DCR"), "X"), // Decred
    DGB(Arrays.asList("DGB"), "X"), // Digibyte
    DGD(Arrays.asList("DGD"), "X"), // DigixDao
    DLT(Arrays.asList("DLT"), "X"), // Agrello
    DNT(Arrays.asList("DNT"), "X"), // District0x
    DOGE(Arrays.asList("DOGE","XDG"), "X"), // Dogecoin
    EDO(Arrays.asList("EDO"), "X"), // Eidoo
    ELF(Arrays.asList("ELF"), "X"), // aelf
    EMC2(Arrays.asList("EMC2"), "X"),
    ENG(Arrays.asList("ENG"), "X"), // Enigma
    ENJ(Arrays.asList("ENJ"), "X"), // Enjin
    EOS(Arrays.asList("EOS"), "X"), // Eos
    ETC(Arrays.asList("ETC"), "X"), // Ethereum Classic
    ETH(Arrays.asList("ETH"), "X"), // Ethereum
    EUR(Arrays.asList("EUR"), "Z"), // Euro
    EVX(Arrays.asList("EVX"), "X"), // Everex
    EXP(Arrays.asList("EXP"), "X"), // Expanse
    FCT(Arrays.asList("FCT"), "X"), // Factom
    FLDC(Arrays.asList("FLDC"), "X"), // Folding Coin
    FLO(Arrays.asList("FLO"), "X"),
    FUEL(Arrays.asList("FUEL"), "X"), // Fuel
    FUN(Arrays.asList("FUN"), "X"), // Funfair
    GAME(Arrays.asList("GAME"), "X"), // Game Credits
    GAS(Arrays.asList("GAS"), "X"), // NEO Gas
    GBP(Arrays.asList("GBP"), "Z"), // British Pound
    GBYTE(Arrays.asList("GBYTE"), "X"), // Byteball
    GNO(Arrays.asList("GNO"), "X"), // Gnosis
    GNT(Arrays.asList("GNT"), "X"), // Golem Network Token
    GRC(Arrays.asList("GRC"), "X"), // Gridcoin
    GTO(Arrays.asList("GTO"), "X"), // Gifto
    GVT(Arrays.asList("GVT"), "X"), // Genesis Vision
    GXS(Arrays.asList("GXS"), "X"), // GXShares
    HUC(Arrays.asList("HUC"), "X"), // Huntercoin
    HSR(Arrays.asList("HSR"), "X"), // Hshare
    HZ(Arrays.asList("HZ"), "X"),
    ICN(Arrays.asList("ICN"), "X"), // Iconomi
    ICX(Arrays.asList("ICX"), "X"), // Icon
    INS(Arrays.asList("INS"), "X"), // INS Ecosystem
    IOC(Arrays.asList("IOC"), "X"),
    IOTA(Arrays.asList("IOTA","IOT"), "X"), // Iota
    JPY(Arrays.asList("JPY"), "X"), // Japanese Yen
    KMD(Arrays.asList("KMD"), "X"), // Komodo
    KNC(Arrays.asList("KNC"), "X"), // Kyber Network Token
    LBC(Arrays.asList("LBC"), "X"), // Library Credits
    LEND(Arrays.asList("LEND"), "X"), // Ethlend
    LINK(Arrays.asList("LINK"), "X"), // Chainlink
    LRC(Arrays.asList("LRC"), "X"), // Loopring
    LSK(Arrays.asList("LSK"), "X"), // Lisk
    LTC(Arrays.asList("LTC"), "X"), // Litecoin
    LUN(Arrays.asList("LUN"), "X"), // Lunyr
    MAID(Arrays.asList("MAID"), "X"), // Maidsafe Coin
    MANA(Arrays.asList("MANA", "MNA"), "X"), // Decentraland MANA
    MCO(Arrays.asList("MCO"), "X"), // Monaco
    MDA(Arrays.asList("MDA"), "X"), // Moeda Loyalty Points
    MLN(Arrays.asList("MLN"), "X"), // Melonport
    MOD(Arrays.asList("MOD"), "X"), // Modum
    MTH(Arrays.asList("MTH"), "X"), // Monetha
    MTL(Arrays.asList("MTL"), "X"), // Metal
    MYR(Arrays.asList("MYR"), "X"),
    NANO(Arrays.asList("NANO", "XRB"), "X"), // Nano/Raiblocks
    NAUT(Arrays.asList("NAUT"), "X"),
    NAV(Arrays.asList("NAV"), "X"), // Navcoin
    NEBL(Arrays.asList("NEBL"), "X"), // Neblio
    NEO(Arrays.asList("NEO", "ANS"), "X"), // NEO/Antshares
    NEOS(Arrays.asList("NEOS"), "X"),
    NMC(Arrays.asList("NMC"), "X"),
    NOBL(Arrays.asList("NOBL"), "X"),
    NOTE(Arrays.asList("NOTE"), "X"),
    NSR(Arrays.asList("NSR"), "X"),
    NULS(Arrays.asList("NULS"), "X"), // Nuls Protocol
    NXC(Arrays.asList("NXC"), "X"),
    NXT(Arrays.asList("NXT"), "X"),
    OAX(Arrays.asList("OAX"), "X"), // OAX
    OMG(Arrays.asList("OMG"), "X"), // OmiseGO
    OMNI(Arrays.asList("OMNI"), "X"),
    OST(Arrays.asList("OST"), "X"), // Simple Token
    PASC(Arrays.asList("PASC"), "X"), // Pascal
    PINK(Arrays.asList("PINK"), "X"),
    PIVX(Arrays.asList("PIVX"), "X"), // Pivx
    POE(Arrays.asList("POE"), "X"), // Po.et
    POT(Arrays.asList("POT"), "X"), // Potcoin
    POWR(Arrays.asList("POWR"), "X"), // PowerLedger
    PPC(Arrays.asList("PPC"), "X"),
    PPT(Arrays.asList("PPT"), "X"), // Populous
    PTOY(Arrays.asList("PTOY"), "X"), // Patientory
    QASH(Arrays.asList("QASH"), "X"), // Qash
    QBK(Arrays.asList("QBK"), "X"),
    QORA(Arrays.asList("QORA"), "X"),
    QSP(Arrays.asList("QSP"), "X"), // Quantstamp
    QTL(Arrays.asList("QTL"), "X"),
    QTUM(Arrays.asList("QTUM"), "X"), // Qtum
    RADS(Arrays.asList("RADS"), "X"),
    RBY(Arrays.asList("RBY"), "X"),
    RCN(Arrays.asList("RCN"), "X"), // Ripio Credit Network
    RDN(Arrays.asList("RDN"), "X"), // Raiden Network Token
    REP(Arrays.asList("REP"), "X"), // Augur
    REQ(Arrays.asList("REQ"), "X"), // Request Network
    RIC(Arrays.asList("RIC"), "X"),
    RLC(Arrays.asList("RLC"), "X"), // iExec RLC
    RRT(Arrays.asList("RRT"), "X"),
    SALT(Arrays.asList("SALT"), "X"), // Salt Lending
    SBD(Arrays.asList("SBD"), "X"),
    SC(Arrays.asList("SC"), "X"), // Siacoin
    SDC(Arrays.asList("SDC"), "X"),
    SJCX(Arrays.asList("SJCX"), "X"), // Storjcoin (Counterparty Asset)
    SNGLS(Arrays.asList("SNGLS"), "X"), // SingularDTV
    SNM(Arrays.asList("SNM"), "X"), // Supercomputer Organized by Network Mining
    SNT(Arrays.asList("SNT"), "X"), // Status Network Token
    STEEM(Arrays.asList("STEEM"), "X"), // Steem
    STORJ(Arrays.asList("STORJ"), "X"), // Storjcoin (Ethereum Asset)
    STRAT(Arrays.asList("STRAT"), "X"), // Stratis
    SUB(Arrays.asList("SUB"), "X"), // Substratum
    SYS(Arrays.asList("SYS"), "X"), // Syscoin
    TRX(Arrays.asList("TRX"), "X"), // Tron
    TNB(Arrays.asList("TNB"), "X"), // Time New Bank
    TNT(Arrays.asList("TNT"), "X"), // Tierion Network Token
    TRIG(Arrays.asList("TRIG"), "X"), // Triggers
    UNITY(Arrays.asList("UNITY"), "X"),
    USD(Arrays.asList("USD"), "Z"), // United States Dollar
    USDT(Arrays.asList("USDT"), "X"), // USD Tether
    // TODO(stfinancial): Figure out how this is actually going to work.
    USD_ARB(Arrays.asList("USD_ARB"), "X"), // USD, USDT bridge currency for programmatically arbitraging USD and USDT
    VEN(Arrays.asList("VEN"), "X"), // VeChain
    VIA(Arrays.asList("VIA"), "X"),
    VIB(Arrays.asList("VIB"), "X"), // Viberate
    VIBE(Arrays.asList("VIBE"), "X"), // VIBE
    VOX(Arrays.asList("VOX"), "X"), // Voxels
    VRC(Arrays.asList("VRC"), "X"),
    VTC(Arrays.asList("VTC"), "X"),
    WABI(Arrays.asList("WABI"), "X"), // Wabi
    WAVES(Arrays.asList("WAVES"), "X"), // Waves
    WINGS(Arrays.asList("WINGS"), "X"), // Wings DAO
    WTC(Arrays.asList("WTC"), "X"), // Waltonchain
    XBC(Arrays.asList("XBC"), "X"),
    XCP(Arrays.asList("XCP"), "X"), // Counterparty
    XEM(Arrays.asList("XEM"), "X"), // New Economy Movement
    XLM(Arrays.asList("XLM","STR"), "X"), // Stellar Lumens
    XMG(Arrays.asList("XMG"), "X"),
    XMR(Arrays.asList("XMR"), "X"), // Monero
    XPM(Arrays.asList("XPM"), "X"),
    XRP(Arrays.asList("XRP"), "X"), // Ripple
    XVC(Arrays.asList("XVC"), "X"),
    XVG(Arrays.asList("XVG"), "X"), // Verge
    XZC(Arrays.asList("XZC"), "X"), // Zcoin
    YOYO(Arrays.asList("YOYO", "YYW"), "X"), // YOYOW
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

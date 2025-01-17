package com.android.identity.securearea

/**
 * Elliptic curve identifiers.
 *
 * All curve identifiers are from the
 * [IANA COSE registry](https://www.iana.org/assignments/cose/cose.xhtml).
 */
enum class EcCurve(val coseCurveIdentifier: Int) {
    /** The curve identifier for P-256  */
    P256(1),

    /** The curve identifier for P-384  */
    P384(2),

    /** The curve identifier for P-521  */
    P521(3),

    /** The curve identifier for brainpoolP256r1  */
    BRAINPOOLP256R1(-65537),

    /** The curve identifier for brainpoolP320r1  */
    BRAINPOOLP320R1(-65538),

    /** The curve identifier for brainpoolP384r1  */
    BRAINPOOLP384R1(-65539),

    /** The curve identifier for brainpoolP512r1  */
    BRAINPOOLP512R1(-65540),

    /** The curve identifier for Ed25519 (EdDSA only)  */
    ED25519(6),

    /** The curve identifier for X25519 (ECDH only)  */
    X25519(4),

    /** The curve identifier for Ed448 (EdDSA only)  */
    ED448(7),

    /** The curve identifier for X448 (ECDH only)  */
    X448(5);

    companion object {
        fun fromInt(coseCurveIdentifier: Int): EcCurve =
            EcCurve.values().find { it.coseCurveIdentifier == coseCurveIdentifier }
                ?: throw IllegalArgumentException("No curve with COSE identifier $coseCurveIdentifier")
    }

    /**
     * The curve size in bits
     */
    val bitSize: Int
        get() = when (this) {
            P256 -> 256
            P384 -> 384
            P521 -> 521
            BRAINPOOLP256R1 -> 256
            BRAINPOOLP320R1 -> 320
            BRAINPOOLP384R1 -> 384
            BRAINPOOLP512R1 -> 512
            X25519 -> 256
            ED25519 -> 256
            X448 -> 448
            ED448 -> 448
        }

    /**
     * The name of the curve according to [Standards for Efficient Cryptography Group](https://www.secg.org/).
     */
    val SECGName: String
        get() = when (this) {
            P256 -> "secp256r1"
            P384 -> "secp384r1"
            P521 -> "secp521r1"
            BRAINPOOLP256R1 -> "brainpoolP256r1"
            BRAINPOOLP320R1 -> "brainpoolP320r1"
            BRAINPOOLP384R1 -> "brainpoolP384r1"
            BRAINPOOLP512R1 -> "brainpoolP512r1"
            X25519 -> "x25519"
            ED25519 -> "ed25519"
            X448 -> "x448"
            ED448 -> "ed448"
        }
}
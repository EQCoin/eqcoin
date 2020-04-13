/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 * @copyright 2018-present EQcoin Federation All rights reserved...
 * Copyright of all works released by EQcoin Federation or jointly released by
 * EQcoin Federation with cooperative partners are owned by EQcoin Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Federation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqcoin.org
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.eqcoin.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Objects;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.DLTaggedObject;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECCurve.Fp;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;
import org.bouncycastle.util.Properties;

import com.eqcoin.blockchain.lock.EQCLock;
import com.eqcoin.keystore.Keystore;
import com.eqcoin.keystore.Keystore.ECCTYPE;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool;

/**
 * EQCPublicKey is an EQC tool you can use it as PublicKey to verify the
 * signature or get the PublicKey's compressed encode
 * 
 * @author Xun Wang
 * @date Sep 26, 2018
 * @email 10509759@qq.com
 */
public class EQCPublicKey implements PublicKey {
	private static final long serialVersionUID = 1303765568188200263L;
	/**
	 * The parameters of the secp256r1 or secp521r1 curve that EQcoin uses.
	 */
	private static ECDomainParameters CURVE;
	private X9ECParameters CURVE_PARAMS;
	private ECPoint ecPoint;
	private static BigInteger HALF_CURVE_ORDER;

	// For java standard EC
	ECNamedCurveParameterSpec spec;
	ECNamedCurveSpec ecParams;
	ECPublicKey pk;

	public EQCPublicKey(ECCTYPE type) {
		if (type == ECCTYPE.P256) {
			CURVE_PARAMS = SECNamedCurves.getByName(Keystore.SECP256R1);
			HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
			CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
					CURVE_PARAMS.getH());
			spec = ECNamedCurveTable.getParameterSpec(Keystore.SECP256R1);
			ecParams = new ECNamedCurveSpec(Keystore.SECP256R1, spec.getCurve(), spec.getG(), spec.getN());
		} else if (type == ECCTYPE.P521) {
			CURVE_PARAMS = SECNamedCurves.getByName(Keystore.SECP521R1);
			HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
			CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
					CURVE_PARAMS.getH());
			spec = ECNamedCurveTable.getParameterSpec(Keystore.SECP521R1);
			ecParams = new ECNamedCurveSpec(Keystore.SECP521R1, spec.getCurve(), spec.getG(), spec.getN());
		}
	}

	/**
	 * Construct EQPublicKey using the official ECPublicKey of java then you can use
	 * EQPublicKey get the compressed public key
	 * 
	 * @param ecPublicKey The interface to an elliptic curve (EC) public key
	 */
	public void setECPoint(final ECPublicKey ecPublicKey) {
		final java.security.spec.ECPoint publicPointW = ecPublicKey.getW();
		final BigInteger xCoord = publicPointW.getAffineX();
		final BigInteger yCoord = publicPointW.getAffineY();
		ecPoint = CURVE.getCurve().createPoint(xCoord, yCoord);
	}

	/**
	 * Construct an EQPublicKey with a compressed public key then you can use it to
	 * verify the signature
	 * 
	 * @param bytes Compressed public key
	 */
	public void setECPoint(final byte[] compressedPublicKey) {
		ecPoint = CURVE.getCurve().decodePoint(compressedPublicKey);
		ECPointUtil.decodePoint(ecParams.getCurve(), ecPoint.getEncoded(true));
		KeyFactory kf = null;
		try {
			kf = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(
				ECPointUtil.decodePoint(ecParams.getCurve(), ecPoint.getEncoded(true)), ecParams);
		try {
			pk = (ECPublicKey) kf.generatePublic(pubKeySpec);
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}

	/**
	 * @return
	 */
	public byte[] getCompressedPublicKeyEncoded() {
		return ecPoint.getEncoded(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.security.Key#getAlgorithm()
	 */
	@Override
	public String getAlgorithm() {
		// TODO Auto-generated method stub
		return "EC";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.security.Key#getFormat()
	 */
	@Override
	public String getFormat() {
		return "X.509";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.security.Key#getEncoded()
	 */
	@Override
	public byte[] getEncoded() {
		return pk.getEncoded();
	}

	/**
	 * <p>
	 * Given the components of a signature and a selector value, recover and return
	 * the public key that generated the signature according to the algorithm in
	 * SEC1v2 section 4.1.6.
	 * </p>
	 *
	 * <p>
	 * The recId is an index from 0 to 3 which indicates which of the 4 possible
	 * keys is the correct one. Because the key recovery operation yields multiple
	 * potential keys, the correct key must either be stored alongside the
	 * signature, or you must be willing to try each recId in turn until you find
	 * one that outputs the key you are expecting.
	 * </p>
	 *
	 * <p>
	 * If this method returns null it means recovery was not possible and recId
	 * should be iterated.
	 * </p>
	 *
	 * <p>
	 * Given the above two points, a correct usage of this method is inside a for
	 * loop from 0 to 3, and if the output is null OR a key that is not the one you
	 * expect, you try again with the next recId.
	 * </p>
	 *
	 * @param recId      Which possible key to recover.
	 * @param sig        the R and S components of the signature, wrapped.
	 * @param message    Hash of the data that was signed.
	 * @param compressed Whether or not the original pubkey was compressed.
	 * @return An ECKey containing only the public part, or null if recovery wasn't
	 *         possible.
	 * @throws Exception 
	 */
	public byte[] recoverFromSignature(int recId, byte[] signature, byte[] messageHash) throws Exception {
		ECDSASignature ecdsaSignature = ECDSASignature.decodeFromDER(signature);
		// 1.0 For j from 0 to h (h == recId here and the loop is outside this function)
		// 1.1 Let x = r + jn
		BigInteger n = CURVE.getN(); //		//curve order.
		BigInteger i = BigInteger.valueOf((long) recId / 2);
		BigInteger x = ecdsaSignature.getR().add(i.multiply(n));
		// 1.2. Convert the integer x to an octet string X of length mlen using the conversion routine
		// specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen = ⌈m/8⌉.
		// 1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve point R using the
		// conversion routine specified in Section 2.3.4. If this conversion routine outputs "invalid", then
		// do another iteration of Step 1.
		
        // More concisely, what these points mean is to use X as a compressed public key.
        ECCurve.Fp curve =  (Fp) CURVE.getCurve();
        BigInteger prime = curve.getQ();  // Bouncy Castle is not consistent about the letter it uses for the prime.
        if (x.compareTo(prime) >= 0) {
            // Cannot have point co-ordinates larger than this as everything takes place modulo Q.
            return null;
        }
		
		// Compressed keys require you to know an extra bit of data about the y-coord as there are two possibilities.
		// So it's encoded in the recId.
		ECPoint R = decompressKey(x, (recId & 1) == 1);
		// 1.4. If nR != point at infinity, then do another iteration of Step 1 (callers responsibility).
		if (!R.multiply(n).isInfinity())
			return null;
		// 1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
		BigInteger e = new BigInteger(1, messageHash);
		// 1.6. For k from 1 to 2 do the following. (loop is outside this function via iterating recId)
		// 1.6.1. Compute a candidate public key as:
		// Q = mi(r) * (sR - eG)
		//
		// Where mi(x) is the modular multiplicative inverse. We transform this into the following:
		// Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
		// Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n). In the above equation
		// ** is point multiplication and + is point addition (the EC group operator).
		//
		// We can find the additive inverse by subtracting e from zero then taking the mod. For example the additive
		// inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and -3 mod 11 = 8.
		BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
		BigInteger rInv = ecdsaSignature.getR().modInverse(n);
		BigInteger srInv = rInv.multiply(ecdsaSignature.getS()).mod(n);
		BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
		ECPoint q = ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);
		// result sanity check: point must not be at infinity
		if(q.isInfinity()) {
			return null;
		}
		return q.getEncoded(true);
	}

    /**
     * Returns the recovery publickey if doesn't exists return null
     * @param messageHash
     * @param sig
     * @param lockHash
     * @return
     * @throws Exception 
     */
    public byte[] recoveryPublickey(byte[] messageHash, byte[] signature, byte[] compressedPublickey1) throws Exception {//EQCLock eqcLock) {
    	byte[] compressedPublickey = null;
        for (byte i = 0; i < 2; i++) {
            compressedPublickey = recoverFromSignature(i, signature, messageHash);
            if(Arrays.equals(compressedPublickey, compressedPublickey1)) {
            	break;
            }
//            if(LockTool.verifyEQCLockAndPublickey(eqcLock, compressedPublickey)) {
//            	break;
//            }
        }
        return compressedPublickey;
    }
	
	/** Decompress a compressed public key (x co-ord and low-bit of y-coord). */
	private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
		X9IntegerConverter x9 = new X9IntegerConverter();
		byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
		compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
		return CURVE.getCurve().decodePoint(compEnc);
	}

}

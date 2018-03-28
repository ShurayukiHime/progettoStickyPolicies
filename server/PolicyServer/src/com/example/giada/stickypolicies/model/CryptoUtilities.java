package com.example.giada.stickypolicies.model;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.DigestException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class CryptoUtilities {
	/**
	 * Generate a self signed X509 certificate with Bouncy Castle.
	 */

	private final static String CNTrustAuthority = "TrustAuthority";
	private static final String BC = org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;
	private static String encryptionAlgorithm = "RSA";
	private static KeyPair taKeys;
	private static X509Certificate taCertificate;
	
    private static String signatureAlgorithm = "MD5WithRSA";
    private static String keyGenerationAlgorithm = "AES";
    private static String symmetricEncrAlgorithm = "AES_256/CBC/PKCS5PADDING";
    private static String digestAlgorithm = "SHA-256";

	public static KeyPair getKeys() throws NoSuchAlgorithmException, NoSuchProviderException {
		if (taKeys == null) {
			Security.addProvider(new BouncyCastleProvider());
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(encryptionAlgorithm, BC);
			keyPairGenerator.initialize(1024, new SecureRandom());
			taKeys = keyPairGenerator.generateKeyPair();
			return taKeys;
		} else
			return taKeys;
	}

	private static void generateSelfSignedX509Certificate() throws Exception {
		if (taKeys == null)
			getKeys();

		// Using the current timestamp as the certificate serial number
		BigInteger certSerialNumber = new BigInteger(Long.toString(System.currentTimeMillis()));
		Date validityBeginDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000); // yesterday
		Date validityEndDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000); 
			//in one year
		String signatureAlgorithm = "SHA256WithRSA"; 
			//use appropriate signature alg based on your key algorithm
		ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).setProvider(BC)
				.build(taKeys.getPrivate());

		JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
				new X500Principal("CN=" + CNTrustAuthority), certSerialNumber, validityBeginDate, validityEndDate,
				new X500Principal("CN=" + CNTrustAuthority), taKeys.getPublic());

		// Basic Constraints
		BasicConstraints basicConstraints = new BasicConstraints(true);
		// true for CA, false for end entity
		certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints); 

		taCertificate = new JcaX509CertificateConverter().setProvider(BC)
				.getCertificate(certBuilder.build(contentSigner));
	}

	public static X509Certificate getCertificate() throws Exception {
		if (taCertificate == null)
			generateSelfSignedX509Certificate();
		return taCertificate;
	}

	public static String getPEMCertificate(X509Certificate certificate) throws IOException {
		StringWriter sw = new StringWriter();
		JcaPEMWriter pw = new JcaPEMWriter(sw);
		pw.writeObject(certificate);
		pw.flush();
		String pemData = sw.toString();
		pw.close();
		return pemData;
	}

	public static byte[] calculateDigest(byte[] text) throws DigestException {
        try {
            MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
            md.update(text);
            return md.digest();
        } catch (NoSuchAlgorithmException cnse) {
            throw new DigestException("Couldn't make digest of partial content " + cnse.getMessage());
        }
    }
	
	public static int getDigestSize() {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(digestAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Couldn't instantiate digest.");
			e.printStackTrace();
		}
		return md.getDigestLength();
	}

	public static boolean compareDigests (byte[] first, byte[] second) {
		return MessageDigest.isEqual(first, second);
	}

	public static byte[] sign (byte[] text) throws SignatureException {
        try {
            // signature
            Signature sig = Signature.getInstance(signatureAlgorithm);
            sig.initSign(taKeys.getPrivate());
            sig.update(text);
            byte[] signatureBytes = sig.sign();

            return signatureBytes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SignatureException(e.getMessage());
        }
    }

	public static boolean verify(PublicKey pubKey, byte[] allegedSignature, byte[] textToVerify) {
        try {
            Signature sig = Signature.getInstance(signatureAlgorithm);
            sig.initVerify(pubKey);
            sig.update(textToVerify);

            return sig.verify(allegedSignature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

	public static byte[] encryptAsymmetric (PublicKey publicKey, byte[] plaintext) {
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance(encryptionAlgorithm);
            c.init(Cipher.ENCRYPT_MODE, publicKey);
            encodedBytes = c.doFinal(plaintext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodedBytes;
    }

	public static byte[] decryptAsymmetric (PrivateKey privateKey, byte[] encodedBytes) {
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance(encryptionAlgorithm);
            c.init(Cipher.DECRYPT_MODE, privateKey);
            decodedBytes = c.doFinal(encodedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decodedBytes;
    }

	public static byte[] generateSymmetricRandomKey() {
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance(keyGenerationAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGen.init(256); // for example
        	// will fire error for "limited strength jurisdiction" or something
        	// because key is too long. try with 128 and it'll work...
        SecretKey secretKey = keyGen.generateKey();
        return secretKey.getEncoded();
    }

	public static String getAlgorithmsSecProv() {
		KeyGenerator keyGen = null;
		StringBuilder sb = new StringBuilder();
        try {
            keyGen = KeyGenerator.getInstance("AES");
            Provider bc = keyGen.getProvider();
            sb.append("Provider name: " + bc.getName() + "\n");
            Set<Provider.Service> services = bc.getServices();
            for (Service s : services) {
            	sb.append(s.getAlgorithm() + " " + s.toString() + "\n");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sb.toString();
	}
	
	public static byte[] encryptSymmetric(byte[] encodedSymmKey, byte[] clearText) {
        SecretKeySpec skeySpec = new SecretKeySpec(encodedSymmKey, keyGenerationAlgorithm);
        byte[] encryptedText = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance(symmetricEncrAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            encryptedText = cipher.doFinal(clearText);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return encryptedText;
    }
}

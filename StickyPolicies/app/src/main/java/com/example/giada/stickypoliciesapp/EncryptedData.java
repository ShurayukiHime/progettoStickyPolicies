package com.example.giada.stickypoliciesapp;

import java.util.Arrays;

/**
 * Created by Giada on 24/03/2018.
 */
class EncryptedData {
    private String stickyPolicy;
    private byte[] keyAndHashEncrypted;
    private byte[] signedEncrkeyAndHash;
    private byte[] encryptedPii;

    public EncryptedData() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EncryptedData other = (EncryptedData) obj;
        if (!Arrays.equals(encryptedPii, other.encryptedPii))
            return false;
        if (!Arrays.equals(keyAndHashEncrypted, other.keyAndHashEncrypted))
            return false;
        if (!Arrays.equals(signedEncrkeyAndHash, other.signedEncrkeyAndHash))
            return false;
        if (stickyPolicy == null) {
            if (other.stickyPolicy != null)
                return false;
        } else if (!stickyPolicy.equals(other.stickyPolicy))
            return false;
        return true;
    }

    public EncryptedData(String stickyPolicy, byte[] keyAndHashEncrypted, byte[] signedEncrkeyAndHash, byte[] encryptedPii) {
        this.stickyPolicy = stickyPolicy;
        this.keyAndHashEncrypted = keyAndHashEncrypted;
        this.signedEncrkeyAndHash = signedEncrkeyAndHash;
        this.encryptedPii = encryptedPii;
    }

    public String getStickyPolicy() {
        return stickyPolicy;
    }

    public void setStickyPolicy(String stickyPolicy) {
        this.stickyPolicy = stickyPolicy;
    }

    public byte[] getKeyAndHashEncrypted() {
        return keyAndHashEncrypted;
    }

    public void setKeyAndHashEncrypted(byte[] keyAndHashEncrypted) {
        this.keyAndHashEncrypted = keyAndHashEncrypted;
    }

    public byte[] getSignedEncrkeyAndHash() {
        return signedEncrkeyAndHash;
    }

    public void setSignedEncrkeyAndHash(byte[] signedEncrkeyAndHash) {
        this.signedEncrkeyAndHash = signedEncrkeyAndHash;
    }

    public byte[] getEncryptedPii() {
        return encryptedPii;
    }

    public void setEncryptedPii(byte[] encryptedPii) {
        this.encryptedPii = encryptedPii;
    }
}

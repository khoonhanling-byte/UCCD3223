package com.example.passwordmanage.crypto;

import android.util.Base64;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

public class CryptoManager {
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "PM_AES_KEY";
    private static final int GCM_TAG_LENGTH = 128;

    private static SecretKey getOrCreateKey() throws Exception {
        KeyStore ks = KeyStore.getInstance(ANDROID_KEYSTORE);
        ks.load(null);

        if (ks.containsAlias(KEY_ALIAS)) {
            return ((KeyStore.SecretKeyEntry) ks.getEntry(KEY_ALIAS, null)).getSecretKey();
        }

        KeyGenerator kg = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
        );

        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build();

        kg.init(spec);
        return kg.generateKey();
    }

    public static String encrypt(String plain) {
        try {
            if (plain == null) plain = "";

            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] iv = cipher.getIV();
            byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            ByteBuffer bb = ByteBuffer.allocate(4 + iv.length + ct.length);
            bb.putInt(iv.length);
            bb.put(iv);
            bb.put(ct);

            return Base64.encodeToString(bb.array(), Base64.NO_WRAP);
        } catch (Exception e) {
            return "";
        }
    }

    public static String decrypt(String enc) {
        try {
            if (enc == null || enc.isEmpty()) {
                return "";
            }

            byte[] data = Base64.decode(enc, Base64.NO_WRAP);
            ByteBuffer bb = ByteBuffer.wrap(data);

            int ivLen = bb.getInt();
            if (ivLen <= 0 || ivLen > 32) {
                return "";
            }

            byte[] iv = new byte[ivLen];
            bb.get(iv);

            byte[] ct = new byte[bb.remaining()];
            bb.get(ct);

            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] pt = cipher.doFinal(ct);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}
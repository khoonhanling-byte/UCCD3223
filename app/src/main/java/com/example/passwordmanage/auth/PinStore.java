package com.example.passwordmanage.auth;

import android.content.Context;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class PinStore {
    private static final String FILE = "secure_prefs";
    private static final String KEY_PIN = "pin_hash";

    private static String hash(String pin) {
        // Simple hash for assignment (PIN is also stored inside encrypted prefs)
        return Integer.toHexString(pin.hashCode());
    }

    public static boolean hasPin(Context c) {
        try {
            return getPrefs(c).contains(KEY_PIN);
        } catch (Exception e) {
            return false;
        }
    }

    public static void setPin(Context c, String pin) throws GeneralSecurityException, IOException {
        getPrefs(c).edit().putString(KEY_PIN, hash(pin)).apply();
    }

    public static boolean verify(Context c, String pin) {
        try {
            String saved = getPrefs(c).getString(KEY_PIN, null);
            return saved != null && saved.equals(hash(pin));
        } catch (Exception e) {
            return false;
        }
    }

    private static android.content.SharedPreferences getPrefs(Context c)
            throws GeneralSecurityException, IOException {
        MasterKey key = new MasterKey.Builder(c)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                c,
                FILE,
                key,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }
}
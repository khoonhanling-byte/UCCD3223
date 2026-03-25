package com.example.passwordmanage.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "password_entries")
public class PasswordEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String siteName;
    public String username;
    public String passwordEncrypted;

    public PasswordEntry(String siteName, String username, String passwordEncrypted) {
        this.siteName = siteName;
        this.username = username;
        this.passwordEncrypted = passwordEncrypted;
    }
}
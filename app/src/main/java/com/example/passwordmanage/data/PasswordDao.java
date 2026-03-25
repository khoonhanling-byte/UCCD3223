package com.example.passwordmanage.data;

import androidx.room.*;
import java.util.List;

@Dao
public interface PasswordDao {

    @Query("SELECT * FROM password_entries ORDER BY id DESC")
    List<PasswordEntry> getAll();

    @Insert
    long insert(PasswordEntry e);

    @Update
    void update(PasswordEntry e);

    @Delete
    void delete(PasswordEntry e);

    @Query("SELECT * FROM password_entries WHERE id = :id LIMIT 1")
    PasswordEntry findById(int id);


    @Query("SELECT * FROM password_entries WHERE LOWER(siteName) = LOWER(:site) LIMIT 1")
    PasswordEntry findBySite(String site);
}
package com.example.passwordmanage.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.passwordmanage.crypto.CryptoManager;
import com.example.passwordmanage.data.AppDatabase;
import com.example.passwordmanage.data.PasswordDao;
import com.example.passwordmanage.data.PasswordEntry;
import com.example.passwordmanage.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding b;
    private PasswordDao dao;
    private PasswordAdapter adapter;

    private final List<PasswordEntry> fullList = new ArrayList<>();
    private boolean demoInsertedThisSession = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        dao = AppDatabase.get(this).dao();

        adapter = new PasswordAdapter(new PasswordAdapter.Listener() {
            @Override
            public void onEdit(PasswordEntry e) {
                Intent i = new Intent(MainActivity.this, AddEditActivity.class);
                i.putExtra("id", e.id);
                startActivity(i);
            }

            @Override
            public void onDelete(PasswordEntry e) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete?")
                        .setMessage("Delete " + e.siteName + "?")
                        .setPositiveButton("Delete", (d, w) -> {
                            Executors.newSingleThreadExecutor().execute(() -> {
                                dao.delete(e);
                                load();
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        b.rv.setLayoutManager(new LinearLayoutManager(this));
        b.rv.setAdapter(adapter);

        b.btnAdd.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddEditActivity.class))
        );

        b.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        load();
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<PasswordEntry> list = dao.getAll();

            if (list.isEmpty() && !demoInsertedThisSession) {
                demoInsertedThisSession = true;

                dao.insert(new PasswordEntry("Gmail", "user@gmail.com",
                        CryptoManager.encrypt("gmail123")));
                dao.insert(new PasswordEntry("Facebook", "john123",
                        CryptoManager.encrypt("fb123")));
                dao.insert(new PasswordEntry("Instagram", "insta_user",
                        CryptoManager.encrypt("ig123")));
                dao.insert(new PasswordEntry("Shopee", "buyer88",
                        CryptoManager.encrypt("shop123")));
                dao.insert(new PasswordEntry("UTAR Portal", "student001",
                        CryptoManager.encrypt("utar123")));

                list = dao.getAll();
            }

            List<PasswordEntry> finalList = list;

            runOnUiThread(() -> {
                fullList.clear();
                fullList.addAll(finalList);

                String keyword = b.etSearch.getText().toString().trim();
                if (keyword.isEmpty()) {
                    adapter.setData(fullList);
                } else {
                    filterList(keyword);
                }
            });
        });
    }

    private void filterList(String text) {
        List<PasswordEntry> filtered = new ArrayList<>();

        for (PasswordEntry e : fullList) {
            if (e.siteName != null &&
                    e.siteName.toLowerCase().contains(text.toLowerCase())) {
                filtered.add(e);
            }
        }

        adapter.setData(filtered);
    }
}
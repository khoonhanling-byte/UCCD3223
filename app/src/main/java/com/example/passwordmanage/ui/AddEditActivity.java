package com.example.passwordmanage.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.passwordmanage.crypto.CryptoManager;
import com.example.passwordmanage.data.AppDatabase;
import com.example.passwordmanage.data.PasswordDao;
import com.example.passwordmanage.data.PasswordEntry;
import com.example.passwordmanage.databinding.ActivityAddEditBinding;

import java.util.concurrent.Executors;

public class AddEditActivity extends AppCompatActivity {

    private ActivityAddEditBinding b;
    private PasswordDao dao;
    private int editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAddEditBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        dao = AppDatabase.get(this).dao();

        editId = getIntent().getIntExtra("id", -1);
        if (editId != -1) {
            loadForEdit(editId);
        }

        b.btnSave.setOnClickListener(v -> save());
    }

    private void loadForEdit(int id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            PasswordEntry e = dao.findById(id);

            runOnUiThread(() -> {
                if (e == null) return;

                b.etSite.setText(e.siteName);
                b.etUser.setText(e.username);

                String decrypted = CryptoManager.decrypt(
                        e.passwordEncrypted == null ? "" : e.passwordEncrypted
                );
                b.etPass.setText(decrypted);
            });
        });
    }

    private void save() {
        String site = b.etSite.getText().toString().trim();
        String user = b.etUser.getText().toString().trim();
        String pass = b.etPass.getText().toString();

        if (site.isEmpty() || user.isEmpty()) {
            Toast.makeText(this, "Site and Username required", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            PasswordEntry existing = dao.findBySite(site);

            if (existing != null && existing.id != editId) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Site already exists", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            String enc = CryptoManager.encrypt(pass);

            if (editId == -1) {
                dao.insert(new PasswordEntry(site, user, enc));
            } else {
                PasswordEntry e = dao.findById(editId);
                if (e != null) {
                    e.siteName = site;
                    e.username = user;
                    e.passwordEncrypted = enc;
                    dao.update(e);
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
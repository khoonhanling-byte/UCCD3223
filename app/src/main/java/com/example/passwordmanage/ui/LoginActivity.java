package com.example.passwordmanage.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.passwordmanage.auth.PinStore;
import com.example.passwordmanage.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        boolean hasPin = PinStore.hasPin(this);
        b.etPinConfirm.setVisibility(hasPin ? View.GONE : View.VISIBLE);
        b.tvHint.setText(hasPin ? "Enter PIN" : "Set a new PIN (first time)");

        b.btnLogin.setOnClickListener(v -> {
            String pin = b.etPin.getText().toString().trim();

            if (pin.length() != 4) {
                Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!hasPin) {
                String confirm = b.etPinConfirm.getText().toString().trim();
                if (!pin.equals(confirm)) {
                    Toast.makeText(this, "PIN not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    PinStore.setPin(this, pin);
                    openMain();
                } catch (Exception e) {
                    Toast.makeText(this, "Error saving PIN", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (PinStore.verify(this, pin)) openMain();
                else Toast.makeText(this, "Wrong PIN", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
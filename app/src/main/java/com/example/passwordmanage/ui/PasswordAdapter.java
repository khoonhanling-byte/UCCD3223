package com.example.passwordmanage.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.passwordmanage.auth.PinStore;
import com.example.passwordmanage.crypto.CryptoManager;
import com.example.passwordmanage.data.PasswordEntry;
import com.example.passwordmanage.databinding.ItemEntryBinding;

import java.util.ArrayList;
import java.util.List;

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.VH> {

    public interface Listener {
        void onEdit(PasswordEntry e);
        void onDelete(PasswordEntry e);
    }

    private final Listener listener;
    private final List<PasswordEntry> list = new ArrayList<>();

    public PasswordAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setData(List<PasswordEntry> data) {
        list.clear();
        list.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEntryBinding b = ItemEntryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        PasswordEntry e = list.get(position);

        holder.b.tvSite.setText(e.siteName);
        holder.b.tvUser.setText(e.username);

        holder.b.btnEdit.setOnClickListener(v -> listener.onEdit(e));
        holder.b.btnDelete.setOnClickListener(v -> listener.onDelete(e));
        holder.b.btnReveal.setOnClickListener(v -> askPinAndReveal(holder.b.getRoot().getContext(), e));
    }

    private void askPinAndReveal(Context c, PasswordEntry e) {
        EditText et = new EditText(c);
        et.setHint("Enter PIN");
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Authentication Required")
                .setView(et)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String pin = et.getText().toString().trim();

            if (!PinStore.verify(c, pin)) {
                Toast.makeText(c, "Wrong PIN", Toast.LENGTH_SHORT).show();
                return;
            }

            String plain = CryptoManager.decrypt(e.passwordEncrypted);

            if (plain == null || plain.isEmpty()) {
                plain = "No password saved";
            }

            dialog.dismiss();

            new AlertDialog.Builder(c)
                    .setTitle("Password for " + e.siteName)
                    .setMessage("Username: " + e.username + "\nPassword: " + plain)
                    .setPositiveButton("Close", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ItemEntryBinding b;

        VH(ItemEntryBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
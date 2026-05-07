package com.financeapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DebtActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecyclerView rvDebts;
    private TextView tvNoDebts;
    private TextView tvTotalDebt;
    private DebtAdapter adapter;
    private List<Debt> debtList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt);

        db = DatabaseHelper.getInstance(this);

        tvNoDebts = findViewById(R.id.tvNoDebts);
        tvTotalDebt = findViewById(R.id.tvTotalDebt);
        rvDebts = findViewById(R.id.rvDebts);
        rvDebts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DebtAdapter(debtList, this::showDeleteConfirm, this::showEditDebtDialog);
        rvDebts.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddDebt).setOnClickListener(v -> showAddDebtDialog());

        loadDebts();
    }

    private void loadDebts() {
        debtList.clear();
        SQLiteDatabase sqlDb = db.getReadableDatabase();
        Cursor c = sqlDb.query(DatabaseHelper.TABLE_DEBTS, null, null, null, null, null, DatabaseHelper.COL_TIMESTAMP + " DESC");
        while (c.moveToNext()) {
            Debt debt = new Debt();
            debt.id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
            debt.creditorName = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_CREDITOR));
            debt.amount = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_AMOUNT));
            debt.description = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION));
            debt.timestamp = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_TIMESTAMP));
            debt.date = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_DATE));
            debt.isPaid = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_IS_PAID)) == 1;
            debtList.add(debt);
        }
        c.close();

        adapter.notifyDataSetChanged();
        updateTotalDebt();

        if (debtList.isEmpty()) {
            tvNoDebts.setVisibility(View.VISIBLE);
            rvDebts.setVisibility(View.GONE);
        } else {
            tvNoDebts.setVisibility(View.GONE);
            rvDebts.setVisibility(View.VISIBLE);
        }
    }

    private void updateTotalDebt() {
        double total = 0;
        for (Debt d : debtList) {
            if (!d.isPaid) total += d.amount;
        }
        tvTotalDebt.setText(FormatUtils.formatRupiah(total));
    }

    private void showAddDebtDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeOverlay_FinanceApp_Dialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_debt, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        EditText etCreditor = view.findViewById(R.id.etCreditorName);
        EditText etAmount = view.findViewById(R.id.etDebtAmount);
        EditText etDesc = view.findViewById(R.id.etDebtDescription);

        view.findViewById(R.id.btnDebtCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnDebtSave).setOnClickListener(v -> {
            String creditor = etCreditor.getText().toString().trim();
            String amtStr = etAmount.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            if (creditor.isEmpty()) { etCreditor.setError("Masukkan nama kreditur"); return; }
            if (amtStr.isEmpty()) { etAmount.setError("Masukkan nominal"); return; }

            try {
                double amount = Double.parseDouble(amtStr);
                if (amount <= 0) { etAmount.setError("Nominal harus lebih dari 0"); return; }

                SQLiteDatabase sqlDb = db.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COL_CREDITOR, creditor);
                cv.put(DatabaseHelper.COL_AMOUNT, amount);
                cv.put(DatabaseHelper.COL_DESCRIPTION, desc);
                cv.put(DatabaseHelper.COL_TIMESTAMP, System.currentTimeMillis());
                cv.put(DatabaseHelper.COL_DATE, FormatUtils.getCurrentDate());
                cv.put(DatabaseHelper.COL_IS_PAID, 0);
                sqlDb.insert(DatabaseHelper.TABLE_DEBTS, null, cv);

                dialog.dismiss();
                loadDebts();
            } catch (NumberFormatException e) {
                etAmount.setError("Nominal tidak valid");
            }
        });
    }

    private void showEditDebtDialog(Debt debt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeOverlay_FinanceApp_Dialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_debt, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        TextView tvTitle = view.findViewById(R.id.tvDebtDialogTitle);
        if (tvTitle != null) tvTitle.setText("EDIT HUTANG");

        EditText etCreditor = view.findViewById(R.id.etCreditorName);
        EditText etAmount = view.findViewById(R.id.etDebtAmount);
        EditText etDesc = view.findViewById(R.id.etDebtDescription);

        etCreditor.setText(debt.creditorName);
        etAmount.setText(String.valueOf(debt.amount));
        etDesc.setText(debt.description);

        view.findViewById(R.id.btnDebtCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnDebtSave).setOnClickListener(v -> {
            String creditor = etCreditor.getText().toString().trim();
            String amtStr = etAmount.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            if (creditor.isEmpty()) { etCreditor.setError("Masukkan nama kreditur"); return; }
            if (amtStr.isEmpty()) { etAmount.setError("Masukkan nominal"); return; }

            try {
                double amount = Double.parseDouble(amtStr);
                if (amount <= 0) { etAmount.setError("Nominal harus lebih dari 0"); return; }

                SQLiteDatabase sqlDb = db.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COL_CREDITOR, creditor);
                cv.put(DatabaseHelper.COL_AMOUNT, amount);
                cv.put(DatabaseHelper.COL_DESCRIPTION, desc);
                sqlDb.update(DatabaseHelper.TABLE_DEBTS, cv, DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(debt.id)});

                dialog.dismiss();
                loadDebts();
            } catch (NumberFormatException e) {
                etAmount.setError("Nominal tidak valid");
            }
        });
    }

    private void showDeleteConfirm(Debt debt) {
        String message = "Hapus hutang ke " + debt.creditorName + "?";
        if (debt.isPaid) {
            message += "\n\nMenghapus hutang lunas akan memotong Saldo Tersedia sebesar " + FormatUtils.formatRupiah(debt.amount);
        }

        new AlertDialog.Builder(this, R.style.ThemeOverlay_FinanceApp_Dialog)
                .setTitle("Hapus Hutang?")
                .setMessage(message)
                .setPositiveButton("Hapus", (d, w) -> {
                    if (debt.isPaid) {
                        double available = db.getAvailableBalance();
                        db.setAvailableBalance(available - debt.amount);
                        String transNote = "Pelunasan: " + debt.creditorName + " : " +
                                (debt.description != null && !debt.description.isEmpty() ? debt.description : "Pelunasan");
                        db.addTransaction("expense", debt.amount, transNote, "bills", System.currentTimeMillis(), FormatUtils.getCurrentDate());
                    }
                    db.getWritableDatabase().delete(DatabaseHelper.TABLE_DEBTS, DatabaseHelper.COL_ID + "=?",
                            new String[]{String.valueOf(debt.id)});
                    loadDebts();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // Mark debt as paid / unpaid
    void togglePaid(Debt debt) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_IS_PAID, debt.isPaid ? 0 : 1);
        db.getWritableDatabase().update(DatabaseHelper.TABLE_DEBTS, cv, DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(debt.id)});
        loadDebts();
    }

    // ---- Data class ----
    static class Debt {
        long id;
        String creditorName;
        double amount;
        String description;
        long timestamp;
        String date;
        boolean isPaid;
    }

    // ---- Adapter ----
    interface OnDeleteClick { void onClick(Debt d); }
    interface OnEditClick { void onClick(Debt d); }

    class DebtAdapter extends RecyclerView.Adapter<DebtAdapter.VH> {
        private final List<Debt> items;
        private final OnDeleteClick onDelete;
        private final OnEditClick onEdit;

        DebtAdapter(List<Debt> items, OnDeleteClick onDelete, OnEditClick onEdit) {
            this.items = items;
            this.onDelete = onDelete;
            this.onEdit = onEdit;
        }

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_debt, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            Debt d = items.get(pos);
            h.tvCreditor.setText(d.creditorName);
            h.tvAmount.setText(FormatUtils.formatRupiah(d.amount));
            h.tvDate.setText(FormatUtils.formatDateFull(d.date));
            h.tvDesc.setText(d.description != null && !d.description.isEmpty()
                    ? d.description : "Tidak ada catatan");
            h.tvDesc.setTextColor(d.description != null && !d.description.isEmpty()
                    ? getColor(R.color.text_secondary) : getColor(R.color.text_hint));

            // Paid state
            if (d.isPaid) {
                h.tvAmount.setTextColor(getColor(R.color.text_hint));
                h.tvPaidBadge.setVisibility(View.VISIBLE);
                h.tvCreditor.setAlpha(0.5f);
            } else {
                h.tvAmount.setTextColor(getColor(R.color.negative));
                h.tvPaidBadge.setVisibility(View.GONE);
                h.tvCreditor.setAlpha(1f);
            }

            h.tvMarkPaid.setText(d.isPaid ? "Belum Lunas" : "Tandai Lunas");
            h.tvMarkPaid.setOnClickListener(v -> togglePaid(d));
            h.tvEdit.setOnClickListener(v -> onEdit.onClick(d));
            h.tvDelete.setOnClickListener(v -> onDelete.onClick(d));
        }

        @Override
        public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvCreditor, tvAmount, tvDate, tvDesc, tvPaidBadge, tvMarkPaid, tvEdit, tvDelete;
            VH(View v) {
                super(v);
                tvCreditor = v.findViewById(R.id.tvDebtCreditor);
                tvAmount = v.findViewById(R.id.tvDebtAmount);
                tvDate = v.findViewById(R.id.tvDebtDate);
                tvDesc = v.findViewById(R.id.tvDebtDescription);
                tvPaidBadge = v.findViewById(R.id.tvPaidBadge);
                tvMarkPaid = v.findViewById(R.id.tvMarkPaid);
                tvEdit = v.findViewById(R.id.tvDebtEdit);
                tvDelete = v.findViewById(R.id.tvDebtDelete);
            }
        }
    }
}

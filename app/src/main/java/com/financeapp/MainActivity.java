package com.financeapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private TextView tvAvailableBalance;
    private TextView tvSavingsBalance;
    private TextView tvGreeting;
    private TextView tvDate;
    private TextView tvTodayTotal;
    private LinearLayout llTransactions;
    private TextView tvNoTransactions;
    private ImageView btnToggleVisibility;
    private boolean isBalanceVisible = false;

    private final ActivityResultLauncher<Intent> pinLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    showAddBalanceDialogActual();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DatabaseHelper.getInstance(this);
        db.seedHistoricalData(db.getWritableDatabase());
        isBalanceVisible = false; // Always hidden on app launch

        // One-time cleanup for previous income transactions as requested
        // db.clearAllIncomeTransactions(); // Disabled so it doesn't clear every time

        initViews();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    private void initViews() {
        tvAvailableBalance = findViewById(R.id.tvAvailableBalance);
        tvSavingsBalance = findViewById(R.id.tvSavingsBalance);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvDate = findViewById(R.id.tvDate);
        tvTodayTotal = findViewById(R.id.tvTodayTotal);
        llTransactions = findViewById(R.id.llTransactions);
        tvNoTransactions = findViewById(R.id.tvNoTransactions);
        btnToggleVisibility = findViewById(R.id.btnToggleVisibility);
    }

    private void setupClickListeners() {
        // Visibility Toggle
        btnToggleVisibility.setOnClickListener(v -> {
            isBalanceVisible = !isBalanceVisible;
            refreshUI();
        });

        // Available Balance Actions
        findViewById(R.id.btnAddBalance).setOnClickListener(v -> showAddBalanceDialog());
        findViewById(R.id.btnDailyExpense).setOnClickListener(v -> showExpenseDialog());
        findViewById(R.id.btnSummary).setOnClickListener(v -> {
            startActivity(new Intent(this, SummaryActivity.class));
        });

        // Savings Card (PIN protected)
        findViewById(R.id.cardSavings).setOnClickListener(v -> openSavingsWithPin());
        findViewById(R.id.btnDebt).setOnClickListener(v -> {
            startActivity(new Intent(this, DebtActivity.class));
        });
    }

    private void refreshUI() {
        // Greeting
        tvGreeting.setText(FormatUtils.getGreeting());
        tvDate.setText(FormatUtils.getDateDisplay());

        // Balances
        double available = db.getAvailableBalance();
        if (isBalanceVisible) {
            tvAvailableBalance.setText(FormatUtils.formatRupiah(available));
            btnToggleVisibility.setImageResource(R.drawable.ic_visibility_off);
        } else {
            tvAvailableBalance.setText("••••••••");
            btnToggleVisibility.setImageResource(R.drawable.ic_visibility);
        }
        tvSavingsBalance.setText("••••••••");

        // Today's transactions
        String today = FormatUtils.getCurrentDate();
        List<DatabaseHelper.Transaction> transactions = db.getTransactionsByDate(today);
        double todayTotal = db.getTotalExpenseBetween(today, today);

        if (todayTotal > 0) {
            tvTodayTotal.setText("− " + FormatUtils.formatShort(todayTotal));
        } else {
            tvTodayTotal.setText("");
        }

        llTransactions.removeAllViews();
        if (transactions.isEmpty()) {
            tvNoTransactions.setVisibility(View.VISIBLE);
        } else {
            tvNoTransactions.setVisibility(View.GONE);
            for (DatabaseHelper.Transaction t : transactions) {
                addTransactionView(t);
            }
        }
    }

    private void addTransactionView(DatabaseHelper.Transaction t) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_transaction, llTransactions, false);
        TextView tvIcon = itemView.findViewById(R.id.tvIcon);
        TextView tvNote = itemView.findViewById(R.id.tvNote);
        TextView tvTime = itemView.findViewById(R.id.tvTime);
        TextView tvAmount = itemView.findViewById(R.id.tvAmount);

        // Icon based on category
        String icon = getCategoryIcon(t.category);
        tvIcon.setText(icon);
        tvIcon.setBackgroundResource(getCategoryBackground(t.category));
        tvIcon.setTextColor(getColor(R.color.text_primary));

        String note = (t.note != null && !t.note.isEmpty()) ? t.note : getCategoryName(t.category);
        tvNote.setText(note);
        tvTime.setText(FormatUtils.formatTime(t.timestamp));

        if ("expense".equals(t.type)) {
            tvAmount.setText("− " + FormatUtils.formatShort(t.amount));
            tvAmount.setTextColor(getColor(R.color.negative));
        } else {
            tvAmount.setText("+ " + FormatUtils.formatShort(t.amount));
            tvAmount.setTextColor(getColor(R.color.positive));
        }

        itemView.setOnLongClickListener(v -> {
            showDeleteConfirmation(t);
            return true;
        });

        // Divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(getColor(R.color.divider));

        llTransactions.addView(divider);
        llTransactions.addView(itemView);
    }

    private void showDeleteConfirmation(DatabaseHelper.Transaction t) {
        new AlertDialog.Builder(this, R.style.ThemeOverlay_FinanceApp_Dialog)
                .setTitle("Hapus Transaksi")
                .setMessage("Apakah Anda yakin ingin menghapus transaksi ini?")
                .setPositiveButton("Hapus", (d, w) -> {
                    db.deleteTransaction(t);
                    refreshUI();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private String getCategoryIcon(String category) {
        if (category == null) return "○";
        switch (category) {
            case "food": return "◉";
            case "transport": return "◈";
            case "entertainment": return "◎";
            case "bills": return "▣";
            case "shopping": return "⬙";
            case "health": return "✚";
            case "income": return "◆";
            default: return "○";
        }
    }

    private int getCategoryBackground(String category) {
        if (category == null) return R.drawable.bg_card_elevated;
        switch (category) {
            case "food": return R.drawable.bg_icon_food;
            case "transport": return R.drawable.bg_icon_transport;
            case "entertainment": return R.drawable.bg_icon_entertainment;
            case "bills": return R.drawable.bg_icon_bills;
            case "shopping": return R.drawable.bg_icon_shopping;
            case "health": return R.drawable.bg_icon_bills; // reuse green for health
            case "income": return R.drawable.bg_icon_bills; // reuse green for income
            default: return R.drawable.bg_card_elevated;
        }
    }

    private String getCategoryName(String category) {
        if (category == null) return "Pengeluaran";
        switch (category) {
            case "food": return "Makanan";
            case "transport": return "Transport";
            case "entertainment": return "Hiburan";
            case "bills": return "Tagihan";
            case "shopping": return "Belanja";
            case "health": return "Kesehatan";
            case "income": return "Pendapatan";
            default: return "Lainnya";
        }
    }

    private void showAddBalanceDialog() {
        if (!db.hasPin()) {
            showAddBalanceDialogActual();
        } else {
            Intent intent = new Intent(this, PinActivity.class);
            intent.putExtra("mode", "action_verify");
            pinLauncher.launch(intent);
        }
    }

    private void showAddBalanceDialogActual() {
        showAmountDialog("Ubah Saldo Tersedia", "Simpan", (amount, note) -> {
            db.setAvailableBalance(amount);
            refreshUI();
        });
    }

    private void showExpenseDialog() {
        // Show category bottom sheet style dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeOverlay_FinanceApp_Dialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_expense, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        // Category selection
        final String[] selectedCategory = {"other"};
        View[] catViews = {
                dialogView.findViewById(R.id.catFood),
                dialogView.findViewById(R.id.catTransport),
                dialogView.findViewById(R.id.catEntertainment),
                dialogView.findViewById(R.id.catBills),
                dialogView.findViewById(R.id.catShopping),
                dialogView.findViewById(R.id.catOther)
        };
        String[] catKeys = {"food", "transport", "entertainment", "bills", "shopping", "other"};

        for (int i = 0; i < catViews.length; i++) {
            final int idx = i;
            catViews[i].setOnClickListener(v -> {
                selectedCategory[0] = catKeys[idx];
                for (View cv : catViews) {
                    cv.setAlpha(0.4f);
                }
                catViews[idx].setAlpha(1f);
            });
        }
        catViews[5].setAlpha(1f); // default: other (index 5 now)

        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etNote = dialogView.findViewById(R.id.etNote);

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String amtStr = etAmount.getText().toString().trim();
            if (amtStr.isEmpty()) { etAmount.setError("Masukkan nominal"); return; }
            try {
                double amount = Double.parseDouble(amtStr);
                if (amount <= 0) { etAmount.setError("Nominal harus lebih dari 0"); return; }
                double available = db.getAvailableBalance();
                if (amount > available) {
                    etAmount.setError("Saldo tidak cukup");
                    return;
                }
                db.setAvailableBalance(available - amount);
                String note = etNote.getText().toString().trim();
                db.addTransaction("expense", amount, note, selectedCategory[0],
                        System.currentTimeMillis(), FormatUtils.getCurrentDate());
                dialog.dismiss();
                refreshUI();
            } catch (NumberFormatException e) {
                etAmount.setError("Nominal tidak valid");
            }
        });
    }

    private void openSavingsWithPin() {
        if (!db.hasPin()) {
            // Setup PIN first
            Intent intent = new Intent(this, SetupPinActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, PinActivity.class);
            startActivity(intent);
        }
    }

    interface OnAmountEntered {
        void onEntered(double amount, String note);
    }

    private void showAmountDialog(String title, String btnText, OnAmountEntered callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeOverlay_FinanceApp_Dialog);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int p = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(p, p, p, p);
        layout.setBackgroundResource(R.drawable.bg_card);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(getColor(R.color.text_primary));
        tvTitle.setTextSize(14f);
        tvTitle.setLetterSpacing(0.1f);
        layout.addView(tvTitle);

        EditText etAmount = new EditText(this);
        etAmount.setHint("Nominal (Rp)");
        etAmount.setText(String.valueOf(db.getAvailableBalance()));
        etAmount.setHintTextColor(getColor(R.color.text_hint));
        etAmount.setTextColor(getColor(R.color.text_primary));
        etAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etAmount.setBackgroundColor(getColor(android.R.color.transparent));
        int mt = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = mt;
        etAmount.setLayoutParams(lp);
        layout.addView(etAmount);

        builder.setView(layout);
        builder.setPositiveButton(btnText, (d, w) -> {
            String s = etAmount.getText().toString().trim();
            if (!s.isEmpty()) {
                try {
                    callback.onEntered(Double.parseDouble(s), "");
                } catch (Exception ignored) {}
            }
        });
        builder.setNegativeButton("Batal", null);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.positive));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.text_hint));
    }
}

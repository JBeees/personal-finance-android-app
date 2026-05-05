package com.financeapp;

import android.app.AlertDialog;
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

import java.util.List;

public class SavingsActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private TextView tvSavingsAmount;
    private RecyclerView rvHistory;
    private TextView tvNoHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings);

        db = DatabaseHelper.getInstance(this);

        tvSavingsAmount = findViewById(R.id.tvSavingsAmount);
        rvHistory = findViewById(R.id.rvSavingsHistory);
        tvNoHistory = findViewById(R.id.tvNoHistory);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddToSavings).setOnClickListener(v -> showAddSavingsDialog());

        refreshUI();
    }

    private void refreshUI() {
        double savings = db.getSavingsBalance();
        tvSavingsAmount.setText(FormatUtils.formatRupiah(savings));

        List<DatabaseHelper.Transaction> history = db.getSavingsHistory();
        if (history.isEmpty()) {
            tvNoHistory.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
        } else {
            tvNoHistory.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            rvHistory.setAdapter(new SavingsAdapter(history));
        }
    }

    private void showAddSavingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeOverlay_FinanceApp_Dialog);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int p = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(p, p, p, p);
        layout.setBackgroundResource(R.drawable.bg_card);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("TAMBAH TABUNGAN");
        tvTitle.setTextColor(getColor(R.color.text_primary));
        tvTitle.setTextSize(12f);
        tvTitle.setLetterSpacing(0.2f);
        layout.addView(tvTitle);

        TextView tvLabel = new TextView(this);
        tvLabel.setText("NOMINAL");
        tvLabel.setTextColor(getColor(R.color.text_hint));
        tvLabel.setTextSize(9f);
        tvLabel.setLetterSpacing(0.15f);
        int mt = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.topMargin = mt;
        tvLabel.setLayoutParams(lp1);
        layout.addView(tvLabel);

        EditText etAmount = new EditText(this);
        etAmount.setHint("0");
        etAmount.setHintTextColor(getColor(R.color.text_hint));
        etAmount.setTextColor(getColor(R.color.text_primary));
        etAmount.setTextSize(22f);
        etAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etAmount.setBackgroundColor(getColor(android.R.color.transparent));
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp2.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
        etAmount.setLayoutParams(lp2);
        layout.addView(etAmount);

        EditText etNote = new EditText(this);
        etNote.setHint("Catatan (opsional)");
        etNote.setHintTextColor(getColor(R.color.text_hint));
        etNote.setTextColor(getColor(R.color.text_primary));
        etNote.setTextSize(13f);
        etNote.setBackgroundColor(getColor(android.R.color.transparent));
        LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp3.topMargin = mt;
        etNote.setLayoutParams(lp3);
        layout.addView(etNote);

        builder.setView(layout);
        builder.setPositiveButton("Simpan", (d, w) -> {
            String s = etAmount.getText().toString().trim();
            if (s.isEmpty()) return;
            try {
                double amount = Double.parseDouble(s);
                if (amount <= 0) return;
                double newSavings = db.getSavingsBalance() + amount;
                db.setSavingsBalance(newSavings);
                String note = etNote.getText().toString().trim();
                db.addSavingsEntry(amount, note, System.currentTimeMillis(), FormatUtils.getCurrentDate());
                refreshUI();
            } catch (NumberFormatException ignored) {}
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

    // Inner adapter for savings history
    class SavingsAdapter extends RecyclerView.Adapter<SavingsAdapter.VH> {
        private List<DatabaseHelper.Transaction> items;

        SavingsAdapter(List<DatabaseHelper.Transaction> items) {
            this.items = items;
        }

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            DatabaseHelper.Transaction t = items.get(position);
            holder.tvIcon.setText("◈");
            holder.tvNote.setText(t.note != null && !t.note.isEmpty() ? t.note : "Tabungan");
            holder.tvTime.setText(FormatUtils.formatDateFull(t.date));
            holder.tvAmount.setText("+ " + FormatUtils.formatRupiah(t.amount));
            holder.tvAmount.setTextColor(getColor(R.color.positive));
        }

        @Override
        public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvIcon, tvNote, tvTime, tvAmount;
            VH(View v) {
                super(v);
                tvIcon = v.findViewById(R.id.tvIcon);
                tvNote = v.findViewById(R.id.tvNote);
                tvTime = v.findViewById(R.id.tvTime);
                tvAmount = v.findViewById(R.id.tvAmount);
            }
        }
    }
}

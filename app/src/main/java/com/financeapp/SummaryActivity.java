package com.financeapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class SummaryActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private ViewPager2 viewPager;

    // Tab views
    private TextView tabWeekly, tabMonthly, tabYearly;
    private View indWeekly, indMonthly, indYearly;

    private final String[] periods = {"weekly", "monthly", "yearly"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        db = DatabaseHelper.getInstance(this);

        initViews();
        setupViewPager();
        setupTabs();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        tabWeekly = findViewById(R.id.tabWeekly);
        tabMonthly = findViewById(R.id.tabMonthly);
        tabYearly = findViewById(R.id.tabYearly);
        indWeekly = findViewById(R.id.indicatorWeekly);
        indMonthly = findViewById(R.id.indicatorMonthly);
        indYearly = findViewById(R.id.indicatorYearly);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupViewPager() {
        SummaryPagerAdapter adapter = new SummaryPagerAdapter();
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setActiveTab(periods[position]);
            }
        });
    }

    private void setupTabs() {
        tabWeekly.setOnClickListener(v -> viewPager.setCurrentItem(0));
        tabMonthly.setOnClickListener(v -> viewPager.setCurrentItem(1));
        tabYearly.setOnClickListener(v -> viewPager.setCurrentItem(2));
    }

    private void setActiveTab(String period) {
        int activeColor = getColor(R.color.text_primary);
        int inactiveColor = getColor(R.color.text_hint);
        int activeIndicator = R.drawable.tab_indicator;

        tabWeekly.setTextColor("weekly".equals(period) ? activeColor : inactiveColor);
        tabMonthly.setTextColor("monthly".equals(period) ? activeColor : inactiveColor);
        tabYearly.setTextColor("yearly".equals(period) ? activeColor : inactiveColor);

        indWeekly.setBackgroundResource("weekly".equals(period) ? activeIndicator : android.R.color.transparent);
        indMonthly.setBackgroundResource("monthly".equals(period) ? activeIndicator : android.R.color.transparent);
        indYearly.setBackgroundResource("yearly".equals(period) ? activeIndicator : android.R.color.transparent);
    }

    private void setupChart(BarChart barChart) {
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setTouchEnabled(false);
        barChart.setBackgroundColor(Color.TRANSPARENT);
        barChart.setNoDataText("Tidak ada data");
        barChart.setNoDataTextColor(Color.parseColor("#616161"));

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#616161"));
        xAxis.setTextSize(9f);
        xAxis.setAxisLineColor(Color.parseColor("#2C2C2C"));

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#1F1F1F"));
        leftAxis.setTextColor(Color.parseColor("#616161"));
        leftAxis.setTextSize(9f);
        leftAxis.setAxisLineColor(Color.parseColor("#2C2C2C"));
        leftAxis.setAxisMinimum(0f);

        barChart.getAxisRight().setEnabled(false);
    }

    private void loadDataForViewHolder(SummaryViewHolder holder, String period) {
        String[] range;
        int avgDivisor;

        switch (period) {
            case "monthly":
                range = FormatUtils.getMonthRange();
                avgDivisor = 30;
                break;
            case "yearly":
                range = FormatUtils.getYearRange();
                avgDivisor = 365;
                break;
            default: // weekly
                range = FormatUtils.getWeekRange();
                avgDivisor = 7;
                break;
        }

        String startDate = range[0];
        String endDate = range[1];

        double totalExpense = db.getTotalExpenseBetween(startDate, endDate);
        double avgExpense = totalExpense / avgDivisor;

        holder.tvTotalExpense.setText(FormatUtils.formatRupiah(totalExpense));
        holder.tvAvgExpense.setText(FormatUtils.formatRupiah(avgExpense));

        // Chart data
        List<DatabaseHelper.DailyTotal> dailyTotals = db.getDailyTotalsBetween(startDate, endDate);
        updateChart(holder.barChart, dailyTotals);

        // Category breakdown
        List<DatabaseHelper.CategoryTotal> categoryTotals = db.getCategoryTotalsBetween(startDate, endDate);
        updateCategories(holder.llCategories, categoryTotals, totalExpense);

        // Transactions
        List<DatabaseHelper.Transaction> transactions = db.getTransactionsBetween(startDate, endDate);
        updateTransactions(holder.llTransactions, transactions);

        boolean hasData = totalExpense > 0;
        holder.tvNoData.setVisibility(hasData ? View.GONE : View.VISIBLE);
    }

    private void updateChart(BarChart barChart, List<DatabaseHelper.DailyTotal> dailyTotals) {
        if (dailyTotals.isEmpty()) {
            barChart.clear();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < dailyTotals.size(); i++) {
            DatabaseHelper.DailyTotal dt = dailyTotals.get(i);
            entries.add(new BarEntry(i, (float) dt.total));
            labels.add(FormatUtils.formatDate(dt.date));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Pengeluaran");
        dataSet.setColor(Color.parseColor("#424242"));
        dataSet.setHighlightEnabled(false);
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setLabelCount(Math.min(labels.size(), 7));
        barChart.setData(barData);
        barChart.invalidate();
    }

    private void updateCategories(LinearLayout llCategories, List<DatabaseHelper.CategoryTotal> categories, double total) {
        llCategories.removeAllViews();
        if (categories.isEmpty() || total == 0) return;

        for (DatabaseHelper.CategoryTotal cat : categories) {
            // Build custom category row
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            int p = (int) (4 * getResources().getDisplayMetrics().density);
            layout.setPadding(0, p, 0, p);

            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setWeightSum(1f);

            TextView tvCat = new TextView(this);
            tvCat.setText(getCategoryName(cat.category));
            tvCat.setTextColor(getColor(R.color.text_primary));
            tvCat.setTextSize(13f);
            LinearLayout.LayoutParams lpCat = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvCat.setLayoutParams(lpCat);
            topRow.addView(tvCat);

            TextView tvAmt = new TextView(this);
            tvAmt.setText(FormatUtils.formatRupiah(cat.total));
            tvAmt.setTextColor(getColor(R.color.text_secondary));
            tvAmt.setTextSize(13f);
            topRow.addView(tvAmt);

            layout.addView(topRow);

            // Progress bar
            int percent = (int) (cat.total / total * 100);
            View progressBg = new View(this);
            LinearLayout.LayoutParams lpBg = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (3 * getResources().getDisplayMetrics().density));
            lpBg.topMargin = (int) (6 * getResources().getDisplayMetrics().density);
            lpBg.bottomMargin = (int) (8 * getResources().getDisplayMetrics().density);
            progressBg.setLayoutParams(lpBg);
            progressBg.setBackgroundColor(getColor(R.color.bg_elevated));
            layout.addView(progressBg);

            View progressFill = new View(this);
            LinearLayout.LayoutParams lpFill = new LinearLayout.LayoutParams(
                    0, (int) (3 * getResources().getDisplayMetrics().density));
            lpFill.topMargin = -(int) (9 * getResources().getDisplayMetrics().density);
            lpFill.bottomMargin = (int) (8 * getResources().getDisplayMetrics().density);
            progressFill.setLayoutParams(lpFill);
            progressFill.setBackgroundColor(getColor(R.color.accent_gray));
            layout.addView(progressFill);

            // Animate progress
            progressFill.post(() -> {
                int maxWidth = progressBg.getWidth();
                int fillWidth = (int) (maxWidth * percent / 100f);
                progressFill.getLayoutParams().width = fillWidth;
                progressFill.requestLayout();
            });

            llCategories.addView(layout);

            // Divider
            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(getColor(R.color.divider));
            llCategories.addView(divider);
        }
    }

    private void updateTransactions(LinearLayout llTransactions, List<DatabaseHelper.Transaction> transactions) {
        llTransactions.removeAllViews();
        for (DatabaseHelper.Transaction t : transactions) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            int pv = (int) (12 * getResources().getDisplayMetrics().density);
            row.setPadding(0, pv, 0, pv);

            TextView tvDate = new TextView(this);
            tvDate.setText(FormatUtils.formatDate(t.date));
            tvDate.setTextColor(getColor(R.color.text_hint));
            tvDate.setTextSize(11f);
            LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                    (int) (56 * getResources().getDisplayMetrics().density),
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tvDate.setLayoutParams(lp1);
            row.addView(tvDate);

            TextView tvNote = new TextView(this);
            String note = (t.note != null && !t.note.isEmpty()) ? t.note : getCategoryName(t.category);
            tvNote.setText(note);
            tvNote.setTextColor(getColor(R.color.text_primary));
            tvNote.setTextSize(13f);
            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvNote.setLayoutParams(lp2);
            row.addView(tvNote);

            TextView tvAmt = new TextView(this);
            tvAmt.setTextSize(13f);
            if ("expense".equals(t.type)) {
                tvAmt.setText("−" + FormatUtils.formatShort(t.amount));
                tvAmt.setTextColor(getColor(R.color.negative));
            } else {
                tvAmt.setText("+" + FormatUtils.formatShort(t.amount));
                tvAmt.setTextColor(getColor(R.color.positive));
            }
            row.addView(tvAmt);

            llTransactions.addView(row);

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(getColor(R.color.divider));
            llTransactions.addView(divider);
        }
    }

    private String getCategoryName(String category) {
        if (category == null) return "Lainnya";
        switch (category) {
            case "food": return "Makanan";
            case "transport": return "Transport";
            case "entertainment": return "Hiburan";
            case "income": return "Pemasukan";
            default: return "Lainnya";
        }
    }

    private class SummaryPagerAdapter extends RecyclerView.Adapter<SummaryViewHolder> {
        @NonNull
        @Override
        public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_summary_page, parent, false);
            return new SummaryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
            loadDataForViewHolder(holder, periods[position]);
        }

        @Override
        public int getItemCount() {
            return periods.length;
        }
    }

    private class SummaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTotalExpense, tvAvgExpense, tvNoData;
        BarChart barChart;
        LinearLayout llCategories, llTransactions;

        SummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTotalExpense = itemView.findViewById(R.id.tvTotalExpense);
            tvAvgExpense = itemView.findViewById(R.id.tvAvgExpense);
            barChart = itemView.findViewById(R.id.barChart);
            llCategories = itemView.findViewById(R.id.llCategories);
            llTransactions = itemView.findViewById(R.id.llTransactions);
            tvNoData = itemView.findViewById(R.id.tvNoData);
            setupChart(barChart);
        }
    }
}

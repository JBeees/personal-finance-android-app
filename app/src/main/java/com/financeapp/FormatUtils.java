package com.financeapp;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtils {

    private static final NumberFormat IDR_FORMAT;

    static {
        IDR_FORMAT = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        IDR_FORMAT.setMaximumFractionDigits(0);
    }

    public static String formatRupiah(double amount) {
        return IDR_FORMAT.format(amount);
    }

    public static String formatShort(double amount) {
        if (amount >= 1_000_000_000) {
            double value = amount / 1_000_000_000.0;
            if (value % 1 == 0) return String.format(Locale.US, "Rp %.0fM", value);
            return String.format(Locale.US, "Rp %.1fM", value).replace(".", ",");
        } else if (amount >= 1_000_000) {
            double value = amount / 1_000_000.0;
            if (value % 1 == 0) return String.format(Locale.US, "Rp %.0fjt", value);
            return String.format(Locale.US, "Rp %.1fjt", value).replace(".", ",");
        } else if (amount >= 1_000) {
            double value = amount / 1_000.0;
            if (value % 1 == 0) {
                return String.format(Locale.US, "Rp %.0frb", value);
            }
            return String.format(Locale.US, "Rp %.1frb", value).replace(".", ",");
        }
        return formatRupiah(amount);
    }

    public static String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public static String formatDate(String dateStr) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("d MMM", new Locale("id"));
            return output.format(input.parse(dateStr));
        } catch (Exception e) {
            return dateStr;
        }
    }

    public static String formatDateFull(String dateStr) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("d MMMM yyyy", new Locale("id"));
            return output.format(input.parse(dateStr));
        } catch (Exception e) {
            return dateStr;
        }
    }

    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String getGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Selamat pagi";
        if (hour < 15) return "Selamat siang";
        if (hour < 18) return "Selamat sore";
        return "Selamat malam";
    }

    public static String getDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM", new Locale("id"));
        return sdf.format(new Date());
    }

    // Week start/end (Mon-Sun)
    public static String[] getWeekRange() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String start = sdf.format(cal.getTime());
        cal.add(java.util.Calendar.DAY_OF_WEEK, 6);
        String end = sdf.format(cal.getTime());
        return new String[]{start, end};
    }

    // Month start/end
    public static String[] getMonthRange() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String start = sdf.format(cal.getTime());
        cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        String end = sdf.format(cal.getTime());
        return new String[]{start, end};
    }

    // Year start/end
    public static String[] getYearRange() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int year = cal.get(java.util.Calendar.YEAR);
        return new String[]{year + "-01-01", year + "-12-31"};
    }
}

package com.financeapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "vault.db";
    private static final int DB_VERSION = 3;

    // Tables
    static final String TABLE_TRANSACTIONS = "transactions";
    static final String TABLE_SAVINGS = "savings";
    static final String TABLE_SETTINGS = "settings";
    static final String TABLE_DEBTS = "debts";

    // Transaction columns
    static final String COL_ID = "id";
    static final String COL_TYPE = "type"; // "expense", "income"
    static final String COL_AMOUNT = "amount";
    static final String COL_NOTE = "note";
    static final String COL_CATEGORY = "category";
    static final String COL_TIMESTAMP = "timestamp";
    static final String COL_DATE = "date"; // YYYY-MM-DD

    // Settings columns
    static final String COL_KEY = "key";
    static final String COL_VALUE = "value";

    // Debt columns
    static final String COL_CREDITOR = "creditor";
    static final String COL_DESCRIPTION = "description";
    static final String COL_IS_PAID = "is_paid";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TYPE + " TEXT NOT NULL, " +
                COL_AMOUNT + " REAL NOT NULL, " +
                COL_NOTE + " TEXT, " +
                COL_CATEGORY + " TEXT, " +
                COL_TIMESTAMP + " INTEGER NOT NULL, " +
                COL_DATE + " TEXT NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_SAVINGS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_AMOUNT + " REAL NOT NULL, " +
                COL_NOTE + " TEXT, " +
                COL_TIMESTAMP + " INTEGER NOT NULL, " +
                COL_DATE + " TEXT NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_SETTINGS + " (" +
                COL_KEY + " TEXT PRIMARY KEY, " +
                COL_VALUE + " TEXT" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_DEBTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CREDITOR + " TEXT NOT NULL, " +
                COL_AMOUNT + " REAL NOT NULL, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_TIMESTAMP + " INTEGER NOT NULL, " +
                COL_DATE + " TEXT NOT NULL, " +
                COL_IS_PAID + " INTEGER DEFAULT 0" +
                ")");

        // Default settings
        db.execSQL("INSERT INTO " + TABLE_SETTINGS + " VALUES ('available_balance', '0')");
        db.execSQL("INSERT INTO " + TABLE_SETTINGS + " VALUES ('savings_balance', '0')");
        db.execSQL("INSERT INTO " + TABLE_SETTINGS + " VALUES ('pin', '')");
        db.execSQL("INSERT INTO " + TABLE_SETTINGS + " VALUES ('balance_visible', '0')");

        seedHistoricalData(db);
    }

    public void seedHistoricalData(SQLiteDatabase db) {
        // Check if data already exists to avoid duplicates
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TRANSACTIONS + " WHERE " + COL_DATE + " IN ('2026-05-04', '2026-05-05', '2026-05-06')", null);
        if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
            cursor.close();
            return;
        }
        cursor.close();

        Object[][] data = {
                {"2026-05-06", 4000.0, "Lainnya", "other"},
                {"2026-05-06", 12000.0, "Sarapan", "food"},
                {"2026-05-06", 14000.0, "makan siang", "food"},
                {"2026-05-06", 14000.0, "makan + minum", "food"},
                {"2026-05-05", 14000.0, "Makan siang", "food"},
                {"2026-05-05", 14000.0, "Makan + minum", "food"},
                {"2026-05-04", 5500.0, "gojek", "transport"},
                {"2026-05-04", 12000.0, "Makan siang", "food"},
                {"2026-05-04", 14000.0, "Makan + minum", "food"}
        };

        for (Object[] row : data) {
            ContentValues cv = new ContentValues();
            cv.put(COL_TYPE, "expense");
            cv.put(COL_DATE, (String) row[0]);
            cv.put(COL_AMOUNT, (Double) row[1]);
            cv.put(COL_NOTE, (String) row[2]);
            cv.put(COL_CATEGORY, (String) row[3]);
            cv.put(COL_TIMESTAMP, System.currentTimeMillis());
            db.insert(TABLE_TRANSACTIONS, null, cv);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVINGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEBTS);
            onCreate(db);
        } else if (oldVersion == 2) {
            // Version 2 to 3: Add debts table or add missing columns
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DEBTS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_CREDITOR + " TEXT NOT NULL, " +
                    COL_AMOUNT + " REAL NOT NULL, " +
                    COL_DESCRIPTION + " TEXT, " +
                    COL_TIMESTAMP + " INTEGER NOT NULL, " +
                    COL_DATE + " TEXT NOT NULL, " +
                    COL_IS_PAID + " INTEGER DEFAULT 0" +
                    ")");

            // Check if creditor exists, if not add it (in case it was created partially before)
            try {
                db.execSQL("ALTER TABLE " + TABLE_DEBTS + " ADD COLUMN " + COL_CREDITOR + " TEXT NOT NULL DEFAULT ''");
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE " + TABLE_DEBTS + " ADD COLUMN " + COL_IS_PAID + " INTEGER DEFAULT 0");
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // =====================
    // Settings Methods
    // =====================

    public String getSetting(String key) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_SETTINGS, new String[]{COL_VALUE},
                COL_KEY + "=?", new String[]{key}, null, null, null);
        String value = "";
        if (c.moveToFirst()) value = c.getString(0);
        c.close();
        return value;
    }

    public void setSetting(String key, String value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_KEY, key);
        cv.put(COL_VALUE, value);
        db.replace(TABLE_SETTINGS, null, cv);
    }

    public double getAvailableBalance() {
        try { return Double.parseDouble(getSetting("available_balance")); } catch (Exception e) { return 0; }
    }

    public void setAvailableBalance(double amount) {
        setSetting("available_balance", String.valueOf(amount));
    }

    public double getSavingsBalance() {
        try { return Double.parseDouble(getSetting("savings_balance")); } catch (Exception e) { return 0; }
    }

    public void setSavingsBalance(double amount) {
        setSetting("savings_balance", String.valueOf(amount));
    }

    public String getPin() {
        return getSetting("pin");
    }

    public void setPin(String pin) {
        setSetting("pin", pin);
    }

    public boolean hasPin() {
        String pin = getPin();
        return pin != null && !pin.isEmpty();
    }

    public boolean isBalanceVisible() {
        return "1".equals(getSetting("balance_visible"));
    }

    public void setBalanceVisible(boolean visible) {
        setSetting("balance_visible", visible ? "1" : "0");
    }

    // =====================
    // Transaction Methods
    // =====================

    public long addTransaction(String type, double amount, String note, String category, long timestamp, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TYPE, type);
        cv.put(COL_AMOUNT, amount);
        cv.put(COL_NOTE, note);
        cv.put(COL_CATEGORY, category);
        cv.put(COL_TIMESTAMP, timestamp);
        cv.put(COL_DATE, date);
        return db.insert(TABLE_TRANSACTIONS, null, cv);
    }

    public void deleteTransaction(Transaction t) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, COL_ID + "=?", new String[]{String.valueOf(t.id)});

        // Revert balance change
        double available = getAvailableBalance();
        if ("expense".equals(t.type)) {
            setAvailableBalance(available + t.amount);
        } else if ("income".equals(t.type)) {
            setAvailableBalance(available - t.amount);
        }
    }

    public void clearAllIncomeTransactions() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, COL_TYPE + "=?", new String[]{"income"});
    }

    public List<Transaction> getTransactionsByDate(String date) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TRANSACTIONS, null,
                COL_DATE + "=?", new String[]{date},
                null, null, COL_TIMESTAMP + " DESC");
        return cursorToTransactions(c);
    }

    public List<Transaction> getTransactionsBetween(String startDate, String endDate) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TRANSACTIONS, null,
                COL_DATE + " >= ? AND " + COL_DATE + " <= ?",
                new String[]{startDate, endDate},
                null, null, COL_TIMESTAMP + " ASC");
        return cursorToTransactions(c);
    }

    public double getTotalExpenseBetween(String startDate, String endDate) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COL_TYPE + "='expense' AND " + COL_DATE + " >= ? AND " + COL_DATE + " <= ?",
                new String[]{startDate, endDate});
        double total = 0;
        if (c.moveToFirst() && !c.isNull(0)) total = c.getDouble(0);
        c.close();
        return total;
    }

    public double getTotalIncomeBetween(String startDate, String endDate) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COL_TYPE + "='income' AND " + COL_DATE + " >= ? AND " + COL_DATE + " <= ?",
                new String[]{startDate, endDate});
        double total = 0;
        if (c.moveToFirst() && !c.isNull(0)) total = c.getDouble(0);
        c.close();
        return total;
    }

    // Get daily totals for chart
    public List<DailyTotal> getDailyTotalsBetween(String startDate, String endDate) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT " + COL_DATE + ", SUM(" + COL_AMOUNT + ") as total " +
                "FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COL_TYPE + "='expense' AND " + COL_DATE + " >= ? AND " + COL_DATE + " <= ?" +
                " GROUP BY " + COL_DATE + " ORDER BY " + COL_DATE + " ASC",
                new String[]{startDate, endDate});
        List<DailyTotal> list = new ArrayList<>();
        while (c.moveToNext()) {
            list.add(new DailyTotal(c.getString(0), c.getDouble(1)));
        }
        c.close();
        return list;
    }

    // Get category totals
    public List<CategoryTotal> getCategoryTotalsBetween(String startDate, String endDate) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT " + COL_CATEGORY + ", SUM(" + COL_AMOUNT + ") as total " +
                "FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COL_TYPE + "='expense' AND " + COL_DATE + " >= ? AND " + COL_DATE + " <= ?" +
                " GROUP BY " + COL_CATEGORY + " ORDER BY total DESC",
                new String[]{startDate, endDate});
        List<CategoryTotal> list = new ArrayList<>();
        while (c.moveToNext()) {
            list.add(new CategoryTotal(c.getString(0), c.getDouble(1)));
        }
        c.close();
        return list;
    }

    private List<Transaction> cursorToTransactions(Cursor c) {
        List<Transaction> list = new ArrayList<>();
        while (c.moveToNext()) {
            Transaction t = new Transaction();
            t.id = c.getLong(c.getColumnIndexOrThrow(COL_ID));
            t.type = c.getString(c.getColumnIndexOrThrow(COL_TYPE));
            t.amount = c.getDouble(c.getColumnIndexOrThrow(COL_AMOUNT));
            t.note = c.getString(c.getColumnIndexOrThrow(COL_NOTE));
            t.category = c.getString(c.getColumnIndexOrThrow(COL_CATEGORY));
            t.timestamp = c.getLong(c.getColumnIndexOrThrow(COL_TIMESTAMP));
            t.date = c.getString(c.getColumnIndexOrThrow(COL_DATE));
            list.add(t);
        }
        c.close();
        return list;
    }

    // =====================
    // Savings History
    // =====================

    public long addSavingsEntry(double amount, String note, long timestamp, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_AMOUNT, amount);
        cv.put(COL_NOTE, note);
        cv.put(COL_TIMESTAMP, timestamp);
        cv.put(COL_DATE, date);
        return db.insert(TABLE_SAVINGS, null, cv);
    }

    public List<Transaction> getSavingsHistory() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_SAVINGS, null, null, null, null, null, COL_TIMESTAMP + " DESC");
        List<Transaction> list = new ArrayList<>();
        while (c.moveToNext()) {
            Transaction t = new Transaction();
            t.id = c.getLong(c.getColumnIndexOrThrow(COL_ID));
            t.type = "savings";
            t.amount = c.getDouble(c.getColumnIndexOrThrow(COL_AMOUNT));
            t.note = c.getString(c.getColumnIndexOrThrow(COL_NOTE));
            t.timestamp = c.getLong(c.getColumnIndexOrThrow(COL_TIMESTAMP));
            t.date = c.getString(c.getColumnIndexOrThrow(COL_DATE));
            list.add(t);
        }
        c.close();
        return list;
    }

    // Inner data classes
    public static class Transaction {
        public long id;
        public String type;
        public double amount;
        public String note;
        public String category;
        public long timestamp;
        public String date;
    }

    public static class DailyTotal {
        public String date;
        public double total;
        DailyTotal(String d, double t) { date = d; total = t; }
    }

    public static class CategoryTotal {
        public String category;
        public double total;
        CategoryTotal(String c, double t) { category = c; total = t; }
    }
}

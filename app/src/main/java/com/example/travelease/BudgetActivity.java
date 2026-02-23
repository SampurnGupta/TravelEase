package com.example.travelease;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.components.AxisBase;
import java.util.*;

public class BudgetActivity extends AppCompatActivity {
    private EditText etAmount, etDate;
    private Spinner spCategory;
    private Button btnAddExpense;
    private PieChart pieChart;
    private BarChart barChart;
    private ExpenseDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        etAmount = findViewById(R.id.et_amount);
        etDate = findViewById(R.id.et_date);
        spCategory = findViewById(R.id.sp_category);
        btnAddExpense = findViewById(R.id.btn_add_expense);
        pieChart = findViewById(R.id.pie_chart);
        barChart = findViewById(R.id.bar_chart);

        dbHelper = new ExpenseDbHelper(this);

        // Setup category spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.expense_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        // Date Picker for etDate
        etDate.setFocusable(false);
        etDate.setClickable(true);
        etDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(BudgetActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                        etDate.setText(dateStr);
                    }, year, month, day);
            datePickerDialog.show();
        });

        btnAddExpense.setOnClickListener(v -> addExpense());
        loadExpenseCharts();
    }

    private void addExpense() {
        String category = spCategory.getSelectedItem().toString();
        String date = etDate.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(date) || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.addExpense(category, amount, date);
        if (success) {
            Toast.makeText(this, "Expense Added!", Toast.LENGTH_SHORT).show();
            etAmount.setText("");
            etDate.setText("");
            loadExpenseCharts();
        } else {
            Toast.makeText(this, "Error Adding Expense!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadExpenseCharts() {
        Map<String, Float> categoryTotals = dbHelper.getCategoryTotals();
        List<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Expenses by Category");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

        // Bar chart by date (total per date)
        Map<String, Float> dateTotals = dbHelper.getDateTotals();
        List<BarEntry> barEntries = new ArrayList<>();
        List<String> dates = new ArrayList<>(dateTotals.keySet());
        Collections.sort(dates);
        for (int i = 0; i < dates.size(); i++) {
            barEntries.add(new BarEntry(i, dateTotals.get(dates.get(i))));
        }
        BarDataSet barDataSet = new BarDataSet(barEntries, "Expenses by Date");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        // Use a ValueFormatter subclass for X axis (shows MM-DD)
        barChart.getXAxis().setValueFormatter(new DateAxisValueFormatter(dates));
        barChart.invalidate();
    }

    // Custom ValueFormatter for bar chart X axis (shows MM-DD)
    static class DateAxisValueFormatter extends ValueFormatter {
        private final List<String> mDates;
        DateAxisValueFormatter(List<String> dates) {
            this.mDates = dates;
        }
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            int index = (int) value;
            if (index >= 0 && index < mDates.size()) {
                String date = mDates.get(index);
                // Return MM-DD if possible
                if (date.length() >= 10) {
                    return date.substring(5, 10);
                } else {
                    return date;
                }
            } else {
                return "";
            }
        }
    }

    // --- Modular SQLite Helper ---
    static class ExpenseDbHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "expenses.db";
        private static final int DB_VERSION = 1;

        ExpenseDbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE expenses (id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT, amount REAL, date TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS expenses");
            onCreate(db);
        }

        boolean addExpense(String category, double amount, String date) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("category", category);
            values.put("amount", amount);
            values.put("date", date);
            long result = db.insert("expenses", null, values);
            db.close();
            return result != -1;
        }

        Map<String, Float> getCategoryTotals() {
            SQLiteDatabase db = getReadableDatabase();
            Map<String, Float> totals = new HashMap<>();
            Cursor cursor = db.rawQuery("SELECT category, SUM(amount) as total FROM expenses GROUP BY category", null);
            if (cursor.moveToFirst()) {
                do {
                    String category = cursor.getString(0);
                    float total = cursor.getFloat(1);
                    totals.put(category, total);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            return totals;
        }

        Map<String, Float> getDateTotals() {
            SQLiteDatabase db = getReadableDatabase();
            Map<String, Float> totals = new HashMap<>();
            Cursor cursor = db.rawQuery("SELECT date, SUM(amount) as total FROM expenses GROUP BY date", null);
            if (cursor.moveToFirst()) {
                do {
                    String date = cursor.getString(0);
                    float total = cursor.getFloat(1);
                    totals.put(date, total);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            return totals;
        }
    }
}
package com.example.travelease;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CurrencyConverterActivity extends AppCompatActivity {

    private TextInputEditText amountInput;
    private Spinner fromCurrencySpinner, toCurrencySpinner;
    private TextView resultText;

    // Add or remove currencies as needed
    private static final String[] CURRENCIES = {"USD", "EUR", "INR", "JPY", "GBP", "AUD", "CAD", "SGD", "CNY"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_converter);

        amountInput = findViewById(R.id.amountInput);
        fromCurrencySpinner = findViewById(R.id.fromCurrencySpinner);
        toCurrencySpinner = findViewById(R.id.toCurrencySpinner);
        resultText = findViewById(R.id.resultText);
        Button convertButton = findViewById(R.id.convertButton);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, CURRENCIES
        );
        fromCurrencySpinner.setAdapter(adapter);
        toCurrencySpinner.setAdapter(adapter);

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountStr = amountInput.getText() != null ? amountInput.getText().toString() : "";
                if (amountStr.isEmpty()) {
                    resultText.setText("Please enter an amount.");
                    return;
                }
                double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    resultText.setText("Invalid amount.");
                    return;
                }
                String from = fromCurrencySpinner.getSelectedItem().toString();
                String to = toCurrencySpinner.getSelectedItem().toString();
                fetchExchangeRate(from, to, amount);
            }
        });
    }

    private void fetchExchangeRate(String base, String target, double amount) {
        resultText.setText("Converting...");
        new Thread(() -> {
            try {
                String apiUrl = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/"
                        + base.toLowerCase() + ".json";
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONObject rates = json.getJSONObject(base.toLowerCase());
                double rate = rates.getDouble(target.toLowerCase());
                double result = amount * rate;

                runOnUiThread(() -> {
                    resultText.setText(String.format("%.2f %s = %.2f %s", amount, base, result, target));
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> resultText.setText("Error fetching rates."));
            }
        }).start();
    }
}
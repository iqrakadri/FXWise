package com.example.fxwise;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class CompareActivity extends AppCompatActivity {

    private Button tvFromCountry;
    private Button tvToCountry;
    private Button btnBack;
    private Button btnFindOptions;

    private TextView tvCurrency;

    private EditText etAmount;

    private RadioGroup priorityGroup;

    private String fromCountry = "";
    private String toCountry = "";

    private String fromCurrency = "";
    private String toCurrency = "";

    private final List<String> countryNames = new ArrayList<>();
    private final List<String> countryCodes = new ArrayList<>();
    private final List<String> currencies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_compare);

        // Connect XML views
        tvFromCountry = findViewById(R.id.tvFromCountry);
        tvToCountry = findViewById(R.id.tvToCountry);

        btnBack = findViewById(R.id.btnBack);
        btnFindOptions = findViewById(R.id.btnFindOptions);

        tvCurrency = findViewById(R.id.tvCurrency);

        etAmount = findViewById(R.id.etAmount);

        priorityGroup = findViewById(R.id.priorityGroup);

        // Load countries and currencies
        loadCountries();

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Sending country
        tvFromCountry.setOnClickListener(v -> showCountryPicker(true));

        // Receiving country
        tvToCountry.setOnClickListener(v -> showCountryPicker(false));

        // Compare button
        btnFindOptions.setOnClickListener(v -> {

            String amount = etAmount.getText().toString().trim();

            // Check sending country
            if (fromCountry.isEmpty()) {
                tvFromCountry.setError("Select sending country");
                return;
            }

            // Check receiving country
            if (toCountry.isEmpty()) {
                tvToCountry.setError("Select receiving country");
                return;
            }

            // Check amount
            if (amount.isEmpty()) {
                etAmount.setError("Enter amount");
                return;
            }

            // Check priority
            if (priorityGroup.getCheckedRadioButtonId() == -1) {

                new AlertDialog.Builder(this)
                        .setTitle("Select a priority")
                        .setMessage(
                                "Choose what matters most for your transfer."
                        )
                        .setPositiveButton("OK", null)
                        .show();

                return;
            }

            int selectedPriority =
                    priorityGroup.getCheckedRadioButtonId();

            String priority;

            if (selectedPriority == R.id.rbLowestCost) {

                priority = "Lowest overall cost";

            } else if (selectedPriority == R.id.rbFastest) {

                priority = "Fastest transfer";

            } else if (selectedPriority == R.id.rbMostReceived) {

                priority = "Most money received";

            } else {

                priority = "Balanced";
            }

            Intent intent =
                    new Intent(
                            CompareActivity.this,
                            ResultActivity.class
                    );

            intent.putExtra(
                    "fromCountry",
                    fromCountry
            );

            intent.putExtra(
                    "toCountry",
                    toCountry
            );

            intent.putExtra(
                    "fromCurrency",
                    fromCurrency
            );

            intent.putExtra(
                    "toCurrency",
                    toCurrency
            );

            intent.putExtra(
                    "amount",
                    amount
            );

            intent.putExtra(
                    "priority",
                    priority
            );

            startActivity(intent);
        });
    }

    // Load available countries and their currencies
    private void loadCountries() {

        String[] isoCountries = Locale.getISOCountries();

        for (String countryCode : isoCountries) {

            try {

                Locale locale = new Locale.Builder()
                        .setRegion(countryCode)
                        .build();

                String countryName =
                        locale.getDisplayCountry(Locale.ENGLISH);

                Currency currency =
                        Currency.getInstance(locale);

                String currencyCode =
                        currency.getCurrencyCode();

                if (!countryName.isEmpty()
                        && !currencyCode.isEmpty()) {

                    countryNames.add(countryName);
                    countryCodes.add(countryCode);
                    currencies.add(currencyCode);
                }

            } catch (Exception ignored) {
                // Ignore countries without a standard currency
            }
        }

        sortCountries();
    }

    // Sort countries alphabetically
    private void sortCountries() {

        for (int i = 0; i < countryNames.size() - 1; i++) {

            for (int j = i + 1;
                 j < countryNames.size();
                 j++) {

                if (countryNames.get(i)
                        .compareToIgnoreCase(
                                countryNames.get(j)
                        ) > 0) {

                    swap(countryNames, i, j);
                    swap(countryCodes, i, j);
                    swap(currencies, i, j);
                }
            }
        }
    }

    // Swap two items in a list
    private void swap(
            List<String> list,
            int first,
            int second) {

        String temporary = list.get(first);

        list.set(first, list.get(second));
        list.set(second, temporary);
    }

    // Show country selection dialog
    private void showCountryPicker(boolean selectingFrom) {

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                selectingFrom
                                        ? "Select sending country"
                                        : "Select receiving country"
                        )
                        .create();

        // Main dialog layout
        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                24,
                10,
                24,
                10
        );

        // Search box
        EditText searchBox =
                new EditText(this);

        searchBox.setHint("Search country");
        searchBox.setSingleLine(true);

        layout.addView(
                searchBox,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        // Country list
        ListView listView =
                new ListView(this);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        new ArrayList<>(countryNames)
                );

        listView.setAdapter(adapter);

        layout.addView(
                listView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        600
                )
        );

        dialog.setView(layout);

        // Search functionality
        searchBox.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        adapter.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );

        // Country selection
        listView.setOnItemClickListener(
                (parent, view, position, id) -> {

                    String selectedCountry =
                            adapter.getItem(position);

                    if (selectedCountry == null) {
                        return;
                    }

                    int originalIndex =
                            countryNames.indexOf(
                                    selectedCountry
                            );

                    if (originalIndex == -1) {
                        return;
                    }

                    String selectedCurrency =
                            currencies.get(originalIndex);

                    if (selectingFrom) {

                        fromCountry =
                                selectedCountry;

                        fromCurrency =
                                selectedCurrency;

                        tvFromCountry.setText(
                                selectedCountry
                        );

                        // Update currency beside amount
                        tvCurrency.setText(
                                selectedCurrency
                        );

                    } else {

                        toCountry =
                                selectedCountry;

                        toCurrency =
                                selectedCurrency;

                        tvToCountry.setText(
                                selectedCountry
                        );
                    }

                    dialog.dismiss();
                }
        );

        dialog.show();
    }

    // Temporary result summary
    private void showTransferSummary(
            String amount,
            String priority) {

        String message =
                "From: " + fromCountry +
                        "\nCurrency: " + fromCurrency +
                        "\n\nTo: " + toCountry +
                        "\nCurrency: " + toCurrency +
                        "\n\nAmount: " + fromCurrency +
                        " " + amount +
                        "\n\nPriority: " + priority;

        new AlertDialog.Builder(this)
                .setTitle("Transfer details")
                .setMessage(message)
                .setPositiveButton(
                        "Continue",
                        null
                )
                .show();
    }
}
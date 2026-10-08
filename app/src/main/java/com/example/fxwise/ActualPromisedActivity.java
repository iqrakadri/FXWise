package com.example.fxwise;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActualPromisedActivity extends AppCompatActivity {

    private Button btnBack;

    private Button btnFromCountry;
    private Button btnToCountry;

    private TextView tvFromCurrency;
    private TextView tvToCurrency;

    private Spinner spinnerProvider;

    private EditText etAmountSent;
    private EditText etPromisedRate;
    private EditText etActualReceived;
    private EditText etExtraFee;

    private Button btnTransactionDate;

    private Spinner spinnerReceivingMethod;
    private Spinner spinnerRateSource;

    private Button btnUploadEvidence;
    private Button btnRemoveEvidence;

    private TextView tvEvidenceStatus;
    private TextView tvVerificationStatus;

    private Button btnCalculate;
    private Button btnSubmit;

    private LinearLayout resultCard;

    private TextView tvPromisedAmount;
    private TextView tvActualAmount;
    private TextView tvDifference;
    private TextView tvReferenceRate;
    private TextView tvRateDifference;
    private TextView tvExtraFeeResult;

    private LinearLayout receivingInstitutionContainer;
    private TextView tvReceivingInstitutionLabel;
    private EditText etReceivingInstitution;

    private FirebaseFirestore db;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private String fromCountry = "";
    private String toCountry = "";

    private String fromCurrency = "";
    private String toCurrency = "";

    private String transactionDate = "";

    private String evidenceUri = "";

    private Double latestReferenceRate = null;

    private double lastPromisedAmount = 0;
    private double lastActualAmount = 0;
    private double lastDifference = 0;
    private double lastExtraFee = 0;

    /*
     * True only after the current form
     * has been successfully calculated.
     */
    private boolean calculationValid = false;

    private final List<String> countryNames =
            new ArrayList<>();

    private final List<String> countryCodes =
            new ArrayList<>();

    private final List<String> currencies =
            new ArrayList<>();

    private final DecimalFormat decimalFormat =
            new DecimalFormat("#,##0.00");

    /*
     * Evidence picker.
     *
     * Currently this only selects the evidence file.
     * Firebase Storage upload can be added later.
     */
    private final ActivityResultLauncher<String> evidencePicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri != null) {

                            evidenceUri =
                                    uri.toString();

                            updateEvidenceUI();

                            Toast.makeText(
                                    this,
                                    "Evidence selected.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_actual_promised
        );

        db =
                FirebaseFirestore.getInstance();

        initializeViews();

        loadCountries();

        setupCountryButtons();

        setupTransactionDetails();

        setupButtons();

        setupInputChangeListeners();

        updateEvidenceUI();
    }

    private void initializeViews() {

        btnBack =
                findViewById(R.id.btnBack);

        btnFromCountry =
                findViewById(R.id.btnFromCountry);

        btnToCountry =
                findViewById(R.id.btnToCountry);

        tvFromCurrency =
                findViewById(R.id.tvFromCurrency);

        tvToCurrency =
                findViewById(R.id.tvToCurrency);

        spinnerProvider =
                findViewById(R.id.spinnerProvider);

        etAmountSent =
                findViewById(R.id.etAmountSent);

        etPromisedRate =
                findViewById(R.id.etPromisedRate);

        etActualReceived =
                findViewById(R.id.etActualReceived);

        etExtraFee =
                findViewById(R.id.etExtraFee);

        btnTransactionDate =
                findViewById(R.id.btnTransactionDate);

        spinnerReceivingMethod =
                findViewById(
                        R.id.spinnerReceivingMethod
                );

        spinnerRateSource =
                findViewById(
                        R.id.spinnerRateSource
                );

        btnUploadEvidence =
                findViewById(
                        R.id.btnUploadEvidence
                );

        btnRemoveEvidence =
                findViewById(
                        R.id.btnRemoveEvidence
                );

        tvEvidenceStatus =
                findViewById(
                        R.id.tvEvidenceStatus
                );

        tvVerificationStatus =
                findViewById(
                        R.id.tvVerificationStatus
                );

        btnCalculate =
                findViewById(
                        R.id.btnCalculate
                );

        btnSubmit =
                findViewById(
                        R.id.btnSubmit
                );

        resultCard =
                findViewById(
                        R.id.resultCard
                );

        tvPromisedAmount =
                findViewById(
                        R.id.tvPromisedAmount
                );

        tvActualAmount =
                findViewById(
                        R.id.tvActualAmount
                );

        tvDifference =
                findViewById(
                        R.id.tvDifference
                );

        tvReferenceRate =
                findViewById(
                        R.id.tvReferenceRate
                );

        tvRateDifference =
                findViewById(
                        R.id.tvRateDifference
                );

        tvExtraFeeResult =
                findViewById(
                        R.id.tvExtraFeeResult
                );

        if (resultCard != null) {

            resultCard.setVisibility(
                    View.GONE
            );
        }

        if (btnSubmit != null) {

            btnSubmit.setEnabled(false);
        }
    }

    /*
     * =========================================================
     * COUNTRY BUTTONS
     * =========================================================
     */

    private void setupCountryButtons() {

        btnBack.setOnClickListener(
                v -> finish()
        );

        btnFromCountry.setOnClickListener(
                v -> showCountryPicker(true)
        );

        btnToCountry.setOnClickListener(
                v -> showCountryPicker(false)
        );
    }

    /*
     * =========================================================
     * TRANSACTION DETAILS
     * =========================================================
     */

    private void setupTransactionDetails() {

        setupProviderSpinner();

        setupReceivingMethodSpinner();

        setupRateSourceSpinner();

        setupReceivingInstitutionField();
    }

    /*
     * =========================================================
     * PROVIDER
     * =========================================================
     */

    private void setupProviderSpinner() {

        String[] providers = {
                "Select Provider",
                "Wise",
                "Remitly",
                "Western Union",
                "Instarem",
                "MoneyGram",
                "WorldRemit",
                "Xoom",
                "Ria",
                "Other"
        };

        ArrayAdapter<String> providerAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        providers
                );

        providerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerProvider.setAdapter(
                providerAdapter
        );

        spinnerProvider.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        invalidateCalculation();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {

                        invalidateCalculation();
                    }
                }
        );
    }

    /*
     * =========================================================
     * RECEIVING METHOD
     * =========================================================
     */

    private void setupReceivingMethodSpinner() {

        String[] methods = {
                "Select receiving method",
                "Bank account",
                "Cash pickup",
                "Mobile wallet",
                "Other"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        methods
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerReceivingMethod.setAdapter(
                adapter
        );

        spinnerReceivingMethod.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        updateReceivingInstitutionField();

                        invalidateCalculation();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {

                        hideReceivingInstitutionField();

                        invalidateCalculation();
                    }
                }
        );
    }

    /*
     * =========================================================
     * RATE SOURCE
     * =========================================================
     */

    private void setupRateSourceSpinner() {

        String[] sources = {
                "Select source",
                "Provider app or website",
                "Agent or physical branch",
                "Advertisement or promotion",
                "Other"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        sources
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerRateSource.setAdapter(
                adapter
        );

        spinnerRateSource.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        invalidateCalculation();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {

                        invalidateCalculation();
                    }
                }
        );
    }

    /*
     * =========================================================
     * RECEIVING INSTITUTION
     * =========================================================
     */

    private void setupReceivingInstitutionField() {

        receivingInstitutionContainer =
                findViewById(
                        R.id.receivingInstitutionContainer
                );

        tvReceivingInstitutionLabel =
                findViewById(
                        R.id.tvReceivingInstitutionLabel
                );

        etReceivingInstitution =
                findViewById(
                        R.id.etReceivingInstitution
                );
    }

    private void updateReceivingInstitutionField() {

        if (receivingInstitutionContainer == null ||
                tvReceivingInstitutionLabel == null ||
                etReceivingInstitution == null) {

            return;
        }

        if (spinnerReceivingMethod
                .getSelectedItem() == null) {

            hideReceivingInstitutionField();

            return;
        }

        String method =
                spinnerReceivingMethod
                        .getSelectedItem()
                        .toString();

        if (method.equals("Bank account")) {

            tvReceivingInstitutionLabel.setText(
                    "Receiving bank"
            );

            etReceivingInstitution.setHint(
                    "Enter receiving bank"
            );

            receivingInstitutionContainer.setVisibility(
                    View.VISIBLE
            );

        } else if (method.equals("Mobile wallet")) {

            tvReceivingInstitutionLabel.setText(
                    "Wallet provider"
            );

            etReceivingInstitution.setHint(
                    "Enter wallet provider"
            );

            receivingInstitutionContainer.setVisibility(
                    View.VISIBLE
            );

        } else if (method.equals("Cash pickup")) {

            tvReceivingInstitutionLabel.setText(
                    "Pickup provider"
            );

            etReceivingInstitution.setHint(
                    "Enter pickup network or provider"
            );

            receivingInstitutionContainer.setVisibility(
                    View.VISIBLE
            );

        } else if (method.equals("Other")) {

            tvReceivingInstitutionLabel.setText(
                    "Receiving institution / provider"
            );

            etReceivingInstitution.setHint(
                    "Optional"
            );

            receivingInstitutionContainer.setVisibility(
                    View.VISIBLE
            );

        } else {

            hideReceivingInstitutionField();
        }
    }

    private void hideReceivingInstitutionField() {

        if (receivingInstitutionContainer != null) {

            receivingInstitutionContainer.setVisibility(
                    View.GONE
            );
        }
    }

    private boolean validateReceivingInstitution(
            boolean required
    ) {

        if (spinnerReceivingMethod
                .getSelectedItem() == null) {

            return false;
        }

        String method =
                spinnerReceivingMethod
                        .getSelectedItem()
                        .toString();

        if (method.equals(
                "Select receiving method")) {

            return false;
        }

        String institution =
                etReceivingInstitution
                        .getText()
                        .toString()
                        .trim();

        /*
         * "Other" is optional.
         */
        if (method.equals("Other")) {

            etReceivingInstitution.setError(null);

            return true;
        }

        if (!required) {

            return true;
        }

        if (institution.isEmpty()) {

            etReceivingInstitution.setError(
                    getInstitutionErrorMessage(
                            method
                    )
            );

            etReceivingInstitution.requestFocus();

            return false;
        }

        etReceivingInstitution.setError(null);

        return true;
    }

    private String getInstitutionErrorMessage(
            String method
    ) {

        if (method.equals("Bank account")) {

            return "Enter the receiving bank";
        }

        if (method.equals("Mobile wallet")) {

            return "Enter the wallet provider";
        }

        if (method.equals("Cash pickup")) {

            return "Enter the pickup provider";
        }

        return "Enter the institution or provider";
    }

    private String getReceivingInstitution() {

        if (etReceivingInstitution == null) {

            return "";
        }

        return etReceivingInstitution
                .getText()
                .toString()
                .trim();
    }

    private String getReceivingInstitutionType() {

        if (spinnerReceivingMethod
                .getSelectedItem() == null) {

            return "";
        }

        String method =
                spinnerReceivingMethod
                        .getSelectedItem()
                        .toString();

        switch (method) {

            case "Bank account":
                return "BANK";

            case "Mobile wallet":
                return "MOBILE_WALLET";

            case "Cash pickup":
                return "CASH_PICKUP";

            case "Other":
                return "OTHER";

            default:
                return "";
        }
    }

    /*
     * =========================================================
     * BUTTONS
     * =========================================================
     */

    private void setupButtons() {

        btnTransactionDate.setOnClickListener(
                v -> showDatePicker()
        );

        btnUploadEvidence.setOnClickListener(
                v -> evidencePicker.launch("*/*")
        );

        btnRemoveEvidence.setOnClickListener(
                v -> removeEvidence()
        );

        btnCalculate.setOnClickListener(
                v -> calculateDifference()
        );

        btnSubmit.setOnClickListener(
                v -> submitReport()
        );
    }

    /*
     * =========================================================
     * INPUT LISTENERS
     * =========================================================
     */

    private void setupInputChangeListeners() {

        addInputWatcher(etAmountSent);

        addInputWatcher(etPromisedRate);

        addInputWatcher(etActualReceived);

        addInputWatcher(etExtraFee);

        if (etReceivingInstitution != null) {

            addInputWatcher(
                    etReceivingInstitution
            );
        }
    }

    private void addInputWatcher(
            EditText editText
    ) {

        if (editText == null) {

            return;
        }

        editText.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        editText.setError(null);

                        invalidateCalculation();
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s
                    ) {
                    }
                }
        );
    }

    private void invalidateCalculation() {

        calculationValid = false;

        if (btnSubmit != null) {

            btnSubmit.setEnabled(false);
        }

        if (resultCard != null) {

            resultCard.setVisibility(
                    View.GONE
            );
        }
    }

    /*
     * =========================================================
     * EVIDENCE
     * =========================================================
     */

    private void updateEvidenceUI() {

        if (evidenceUri == null ||
                evidenceUri.isEmpty()) {

            if (tvEvidenceStatus != null) {

                tvEvidenceStatus.setText(
                        "No evidence selected"
                );
            }

            if (tvVerificationStatus != null) {

                tvVerificationStatus.setText(
                        "Unverified"
                );
            }

            if (btnUploadEvidence != null) {

                btnUploadEvidence.setText(
                        "Upload Evidence"
                );
            }

            if (btnRemoveEvidence != null) {

                btnRemoveEvidence.setVisibility(
                        View.GONE
                );
            }

            return;
        }

        if (tvEvidenceStatus != null) {

            tvEvidenceStatus.setText(
                    "Evidence attached"
            );
        }

        /*
         * Evidence attachment does NOT mean
         * the evidence has been verified.
         */
        if (tvVerificationStatus != null) {

            tvVerificationStatus.setText(
                    "Evidence attached - not verified"
            );
        }

        if (btnUploadEvidence != null) {

            btnUploadEvidence.setText(
                    "Change Evidence"
            );
        }

        if (btnRemoveEvidence != null) {

            btnRemoveEvidence.setVisibility(
                    View.VISIBLE
            );
        }
    }

    private void removeEvidence() {

        if (evidenceUri == null ||
                evidenceUri.isEmpty()) {

            Toast.makeText(
                    this,
                    "No evidence is selected.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        "Remove evidence?"
                )
                .setMessage(
                        "The selected evidence will be removed from this report."
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Remove",
                        (dialog, which) -> {

                            evidenceUri = "";

                            updateEvidenceUI();

                            Toast.makeText(
                                    this,
                                    "Evidence removed.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .show();
    }

    /*
     * =========================================================
     * COUNTRIES
     * =========================================================
     */

    private void loadCountries() {

        String[] isoCountries =
                Locale.getISOCountries();

        for (String countryCode :
                isoCountries) {

            try {

                Locale locale =
                        new Locale.Builder()
                                .setRegion(countryCode)
                                .build();

                String countryName =
                        locale.getDisplayCountry(
                                Locale.ENGLISH
                        );

                Currency currency =
                        Currency.getInstance(
                                locale
                        );

                String currencyCode =
                        currency.getCurrencyCode();

                if (!countryName.isEmpty() &&
                        !currencyCode.isEmpty()) {

                    countryNames.add(
                            countryName
                    );

                    countryCodes.add(
                            countryCode
                    );

                    currencies.add(
                            currencyCode
                    );
                }

            } catch (Exception ignored) {
            }
        }

        sortCountries();
    }

    private void sortCountries() {

        for (int i = 0;
             i < countryNames.size() - 1;
             i++) {

            for (int j = i + 1;
                 j < countryNames.size();
                 j++) {

                if (countryNames
                        .get(i)
                        .compareToIgnoreCase(
                                countryNames.get(j)
                        ) > 0) {

                    swap(
                            countryNames,
                            i,
                            j
                    );

                    swap(
                            countryCodes,
                            i,
                            j
                    );

                    swap(
                            currencies,
                            i,
                            j
                    );
                }
            }
        }
    }

    private void swap(
            List<String> list,
            int first,
            int second
    ) {

        String temporary =
                list.get(first);

        list.set(
                first,
                list.get(second)
        );

        list.set(
                second,
                temporary
        );
    }

    private void showCountryPicker(
            boolean selectingFrom
    ) {

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                selectingFrom
                                        ? "Select sending country"
                                        : "Select receiving country"
                        )
                        .create();

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                dp(24),
                dp(10),
                dp(24),
                dp(10)
        );

        EditText searchBox =
                new EditText(this);

        searchBox.setHint(
                "Search country"
        );

        searchBox.setSingleLine(
                true
        );

        searchBox.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS
        );

        layout.addView(
                searchBox,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        android.widget.ListView listView =
                new android.widget.ListView(this);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        new ArrayList<>(
                                countryNames
                        )
                );

        listView.setAdapter(
                adapter
        );

        layout.addView(
                listView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(600)
                )
        );

        dialog.setView(
                layout
        );

        searchBox.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        adapter
                                .getFilter()
                                .filter(s);
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s
                    ) {
                    }
                }
        );

        listView.setOnItemClickListener(
                (parent,
                 view,
                 position,
                 id) -> {

                    String selectedCountry =
                            adapter.getItem(
                                    position
                            );

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
                            currencies.get(
                                    originalIndex
                            );

                    if (selectingFrom) {

                        fromCountry =
                                selectedCountry;

                        fromCurrency =
                                selectedCurrency;

                        btnFromCountry.setText(
                                selectedCountry
                        );

                        btnFromCountry.setError(
                                null
                        );

                        tvFromCurrency.setText(
                                selectedCurrency
                        );

                    } else {

                        toCountry =
                                selectedCountry;

                        toCurrency =
                                selectedCurrency;

                        btnToCountry.setText(
                                selectedCountry
                        );

                        btnToCountry.setError(
                                null
                        );

                        tvToCurrency.setText(
                                selectedCurrency
                        );

                        if (etReceivingInstitution != null) {

                            etReceivingInstitution.setText(
                                    ""
                            );

                            etReceivingInstitution.setError(
                                    null
                            );
                        }
                    }

                    latestReferenceRate = null;

                    invalidateCalculation();

                    dialog.dismiss();
                }
        );

        dialog.show();
    }

    /*
     * =========================================================
     * DATE PICKER
     * =========================================================
     */

    private void showDatePicker() {

        Calendar calendar =
                Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view,
                         year,
                         month,
                         dayOfMonth) -> {

                            Calendar selected =
                                    Calendar.getInstance();

                            selected.set(
                                    year,
                                    month,
                                    dayOfMonth,
                                    0,
                                    0,
                                    0
                            );

                            selected.set(
                                    Calendar.MILLISECOND,
                                    0
                            );

                            Calendar today =
                                    Calendar.getInstance();

                            today.set(
                                    Calendar.HOUR_OF_DAY,
                                    0
                            );

                            today.set(
                                    Calendar.MINUTE,
                                    0
                            );

                            today.set(
                                    Calendar.SECOND,
                                    0
                            );

                            today.set(
                                    Calendar.MILLISECOND,
                                    0
                            );

                            if (selected.after(today)) {

                                btnTransactionDate.setError(
                                        "Invalid transaction date"
                                );

                                Toast.makeText(
                                        this,
                                        "Transaction date cannot be in the future.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            SimpleDateFormat format =
                                    new SimpleDateFormat(
                                            "dd MMM yyyy",
                                            Locale.ENGLISH
                                    );

                            transactionDate =
                                    format.format(
                                            selected.getTime()
                                    );

                            btnTransactionDate.setText(
                                    transactionDate
                            );

                            btnTransactionDate.setError(
                                    null
                            );

                            invalidateCalculation();
                        },
                        calendar.get(
                                Calendar.YEAR
                        ),
                        calendar.get(
                                Calendar.MONTH
                        ),
                        calendar.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        dialog.getDatePicker().setMaxDate(
                System.currentTimeMillis()
        );

        dialog.show();
    }

    /*
     * =========================================================
     * BASIC VALIDATION
     * =========================================================
     */

    private boolean validateBasicFields() {

        if (fromCountry.isEmpty()) {

            btnFromCountry.setError(
                    "Select sending country"
            );

            Toast.makeText(
                    this,
                    "Please select the sending country.",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        if (toCountry.isEmpty()) {

            btnToCountry.setError(
                    "Select receiving country"
            );

            Toast.makeText(
                    this,
                    "Please select the receiving country.",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        if (fromCountry.equals(toCountry)) {

            btnToCountry.setError(
                    "Select a different country"
            );

            Toast.makeText(
                    this,
                    "Sending and receiving countries must be different.",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        if (spinnerProvider.getSelectedItemPosition() == 0) {

            Toast.makeText(
                    this,
                    "Please select a provider.",
                    Toast.LENGTH_SHORT
            ).show();

            spinnerProvider.requestFocus();

            return false;
        }

        if (transactionDate.isEmpty()) {

            btnTransactionDate.setError(
                    "Select transaction date"
            );

            Toast.makeText(
                    this,
                    "Select transaction date.",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        if (spinnerReceivingMethod
                .getSelectedItemPosition() == 0) {

            Toast.makeText(
                    this,
                    "Select receiving method.",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        if (spinnerRateSource
                .getSelectedItemPosition() == 0) {

            Toast.makeText(
                    this,
                    "Select where the promised rate was shown.",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        return true;
    }

    /*
     * =========================================================
     * TRANSACTION NUMBER VALIDATION
     * =========================================================
     */

    private boolean validateTransactionNumbers() {

        String amountText =
                etAmountSent
                        .getText()
                        .toString()
                        .trim();

        String promisedRateText =
                etPromisedRate
                        .getText()
                        .toString()
                        .trim();

        String actualReceivedText =
                etActualReceived
                        .getText()
                        .toString()
                        .trim();

        String extraFeeText =
                etExtraFee
                        .getText()
                        .toString()
                        .trim();

        if (amountText.isEmpty()) {

            etAmountSent.setError(
                    "Enter amount sent"
            );

            etAmountSent.requestFocus();

            return false;
        }

        if (promisedRateText.isEmpty()) {

            etPromisedRate.setError(
                    "Enter promised rate"
            );

            etPromisedRate.requestFocus();

            return false;
        }

        if (actualReceivedText.isEmpty()) {

            etActualReceived.setError(
                    "Enter actual received amount"
            );

            etActualReceived.requestFocus();

            return false;
        }

        double amountSent;
        double promisedRate;
        double actualReceived;
        double extraFee = 0;

        try {

            amountSent =
                    Double.parseDouble(
                            amountText
                    );

            promisedRate =
                    Double.parseDouble(
                            promisedRateText
                    );

            actualReceived =
                    Double.parseDouble(
                            actualReceivedText
                    );

            if (!extraFeeText.isEmpty()) {

                extraFee =
                        Double.parseDouble(
                                extraFeeText
                        );
            }

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Please enter valid numeric values.",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        /*
         * Amount sent.
         */
        if (Double.isNaN(amountSent) ||
                Double.isInfinite(amountSent) ||
                amountSent <= 0) {

            etAmountSent.setError(
                    "Amount must be greater than 0"
            );

            etAmountSent.requestFocus();

            return false;
        }

        /*
         * Promised exchange rate.
         */
        if (Double.isNaN(promisedRate) ||
                Double.isInfinite(promisedRate) ||
                promisedRate <= 0) {

            etPromisedRate.setError(
                    "Rate must be greater than 0"
            );

            etPromisedRate.requestFocus();

            return false;
        }

        /*
         * Actual received amount.
         */
        if (Double.isNaN(actualReceived) ||
                Double.isInfinite(actualReceived) ||
                actualReceived <= 0) {

            etActualReceived.setError(
                    "Received amount must be greater than 0"
            );

            etActualReceived.requestFocus();

            return false;
        }

        /*
         * Extra fee.
         */
        if (Double.isNaN(extraFee) ||
                Double.isInfinite(extraFee) ||
                extraFee < 0) {

            etExtraFee.setError(
                    "Fee cannot be negative"
            );

            etExtraFee.requestFocus();

            return false;
        }

        /*
         * Calculate promised amount.
         */
        double promisedAmount =
                amountSent *
                        promisedRate;

        if (Double.isNaN(promisedAmount) ||
                Double.isInfinite(promisedAmount) ||
                promisedAmount <= 0) {

            etPromisedRate.setError(
                    "Please check the amount and exchange rate"
            );

            etPromisedRate.requestFocus();

            return false;
        }

        /*
         * Sanity check.
         *
         * A report with an actual received amount
         * above 3x the promised amount is treated
         * as a likely data-entry error.
         *
         * This is a data-quality check, not a claim
         * about the provider.
         */
        double maximumReasonableReceived =
                promisedAmount * 3.0;

        if (actualReceived >
                maximumReasonableReceived) {

            etActualReceived.setError(
                    "Please check the received amount"
            );

            etActualReceived.requestFocus();

            new AlertDialog.Builder(this)
                    .setTitle(
                            "Please review the amount"
                    )
                    .setMessage(
                            "The actual received amount is unusually high compared with the promised amount. Please check the exchange rate and received amount before continuing."
                    )
                    .setPositiveButton(
                            "Review",
                            null
                    )
                    .show();

            return false;
        }

        return true;
    }

    /*
     * =========================================================
     * DIFFERENCE PERCENTAGE
     * =========================================================
     */

    private double calculateDifferencePercentage(
            double promisedAmount,
            double actualReceived
    ) {

        if (promisedAmount <= 0) {

            return 0;
        }

        return (
                (promisedAmount - actualReceived)
                        / promisedAmount
        ) * 100.0;
    }

    /*
     * =========================================================
     * CALCULATE
     * =========================================================
     */

    private void calculateDifference() {

        if (!validateBasicFields()) {

            return;
        }

        if (!validateReceivingInstitution(true)) {

            return;
        }

        if (!validateTransactionNumbers()) {

            return;
        }

        String amountText =
                etAmountSent
                        .getText()
                        .toString()
                        .trim();

        String promisedRateText =
                etPromisedRate
                        .getText()
                        .toString()
                        .trim();

        String actualReceivedText =
                etActualReceived
                        .getText()
                        .toString()
                        .trim();

        String extraFeeText =
                etExtraFee
                        .getText()
                        .toString()
                        .trim();

        double amountSent =
                Double.parseDouble(
                        amountText
                );

        double promisedRate =
                Double.parseDouble(
                        promisedRateText
                );

        double actualReceived =
                Double.parseDouble(
                        actualReceivedText
                );

        double extraFee = 0;

        if (!extraFeeText.isEmpty()) {

            extraFee =
                    Double.parseDouble(
                            extraFeeText
                    );
        }

        lastPromisedAmount =
                amountSent *
                        promisedRate;

        lastActualAmount =
                actualReceived;

        lastDifference =
                lastPromisedAmount -
                        actualReceived;

        lastExtraFee =
                extraFee;

        double differencePercentage =
                calculateDifferencePercentage(
                        lastPromisedAmount,
                        actualReceived
                );

        /*
         * Promised amount.
         */
        tvPromisedAmount.setText(
                fromCurrency +
                        " " +
                        decimalFormat.format(
                                amountSent
                        ) +
                        " x " +
                        decimalFormat.format(
                                promisedRate
                        ) +
                        " = " +
                        toCurrency +
                        " " +
                        decimalFormat.format(
                                lastPromisedAmount
                        )
        );

        /*
         * Actual amount.
         */
        tvActualAmount.setText(
                toCurrency +
                        " " +
                        decimalFormat.format(
                                lastActualAmount
                        )
        );

        /*
         * Difference.
         */
        if (lastDifference > 0) {

            tvDifference.setText(
                    "Shortfall: " +
                            toCurrency +
                            " " +
                            decimalFormat.format(
                                    lastDifference
                            ) +
                            " (" +
                            decimalFormat.format(
                                    Math.abs(
                                            differencePercentage
                                    )
                            ) +
                            "%)"
            );

        } else if (lastDifference < 0) {

            tvDifference.setText(
                    "Actual amount was higher by: " +
                            toCurrency +
                            " " +
                            decimalFormat.format(
                                    Math.abs(
                                            lastDifference
                                    )
                            ) +
                            " (" +
                            decimalFormat.format(
                                    Math.abs(
                                            differencePercentage
                                    )
                            ) +
                            "%)"
            );

        } else {

            tvDifference.setText(
                    "No difference: " +
                            toCurrency +
                            " 0.00"
            );
        }

        /*
         * Extra fee.
         */
        tvExtraFeeResult.setText(
                toCurrency +
                        " " +
                        decimalFormat.format(
                                lastExtraFee
                        )
        );

        /*
         * Reference rate.
         */
        if (latestReferenceRate != null) {

            updateReferenceRateUI();

        } else {

            tvReferenceRate.setText(
                    "Reference rate unavailable"
            );

            tvRateDifference.setText(
                    "Reference comparison unavailable"
            );
        }

        /*
         * Large difference warning.
         *
         * This does NOT accuse the provider.
         * It simply asks the user to review
         * unusual data.
         */
        if (Math.abs(
                differencePercentage
        ) >= 50) {

            Toast.makeText(
                    this,
                    "Large difference detected. Please review the entered transaction details.",
                    Toast.LENGTH_LONG
            ).show();
        }

        calculationValid = true;

        btnSubmit.setEnabled(true);

        resultCard.setVisibility(
                View.VISIBLE
        );

        /*
         * Fetch latest reference FX rate.
         */
        fetchReferenceRate();

        Toast.makeText(
                this,
                "Calculation completed.",
                Toast.LENGTH_SHORT
        ).show();
    }

    /*
     * =========================================================
     * REFERENCE FX RATE
     * =========================================================
     */

    private void fetchReferenceRate() {

        if (fromCurrency.isEmpty() ||
                toCurrency.isEmpty()) {

            return;
        }

        if (fromCurrency.equals(
                toCurrency
        )) {

            latestReferenceRate =
                    1.0;

            updateReferenceRateUI();

            return;
        }

        executorService.execute(() -> {

            HttpURLConnection connection =
                    null;

            try {

                String apiUrl =
                        "https://api.frankfurter.dev/v2/rate/"
                                + fromCurrency
                                + "/"
                                + toCurrency;

                URL url =
                        new URL(apiUrl);

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod(
                        "GET"
                );

                connection.setConnectTimeout(
                        8000
                );

                connection.setReadTimeout(
                        8000
                );

                int responseCode =
                        connection.getResponseCode();

                if (responseCode !=
                        HttpURLConnection.HTTP_OK) {

                    throw new Exception(
                            "HTTP " +
                                    responseCode
                    );
                }

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        connection
                                                .getInputStream()
                                )
                        );

                StringBuilder response =
                        new StringBuilder();

                String line;

                while ((line =
                        reader.readLine()) != null) {

                    response.append(line);
                }

                reader.close();

                JSONObject json =
                        new JSONObject(
                                response.toString()
                        );

                double rate =
                        json.getDouble(
                                "rate"
                        );

                latestReferenceRate =
                        rate;

                runOnUiThread(
                        this::updateReferenceRateUI
                );

            } catch (Exception e) {

                latestReferenceRate =
                        null;

                runOnUiThread(() -> {

                    tvReferenceRate.setText(
                            "Reference rate unavailable"
                    );

                    tvRateDifference.setText(
                            "Reference comparison unavailable"
                    );
                });

            } finally {

                if (connection != null) {

                    connection.disconnect();
                }
            }
        });
    }

    private void updateReferenceRateUI() {

        if (latestReferenceRate == null) {

            return;
        }

        tvReferenceRate.setText(
                "1 " +
                        fromCurrency +
                        " = " +
                        decimalFormat.format(
                                latestReferenceRate
                        ) +
                        " " +
                        toCurrency
        );

        String promisedRateText =
                etPromisedRate
                        .getText()
                        .toString()
                        .trim();

        if (!promisedRateText.isEmpty()) {

            try {

                double promisedRate =
                        Double.parseDouble(
                                promisedRateText
                        );

                if (latestReferenceRate <= 0) {

                    return;
                }

                double difference =
                        promisedRate -
                                latestReferenceRate;

                double percentage =
                        (
                                difference /
                                        latestReferenceRate
                        ) * 100;

                tvRateDifference.setText(
                        "Promised rate difference: " +
                                decimalFormat.format(
                                        difference
                                ) +
                                " (" +
                                decimalFormat.format(
                                        percentage
                                ) +
                                "%)"
                );

            } catch (NumberFormatException ignored) {
            }
        }
    }

    /*
     * =========================================================
     * SUBMIT
     * =========================================================
     */

    private void submitReport() {

        if (!validateBasicFields()) {

            return;
        }

        if (!validateReceivingInstitution(true)) {

            return;
        }

        /*
         * Calculation must happen before submission.
         */
        if (!calculationValid) {

            new AlertDialog.Builder(this)
                    .setTitle(
                            "Calculation required"
                    )
                    .setMessage(
                            "Please complete the transaction details and calculate the result before submitting."
                    )
                    .setPositiveButton(
                            "OK",
                            null
                    )
                    .show();

            return;
        }

        if (!validateTransactionNumbers()) {

            return;
        }

        String amountText =
                etAmountSent
                        .getText()
                        .toString()
                        .trim();

        String promisedRateText =
                etPromisedRate
                        .getText()
                        .toString()
                        .trim();

        String actualReceivedText =
                etActualReceived
                        .getText()
                        .toString()
                        .trim();

        String extraFeeText =
                etExtraFee
                        .getText()
                        .toString()
                        .trim();

        double amountSent =
                Double.parseDouble(
                        amountText
                );

        double promisedRate =
                Double.parseDouble(
                        promisedRateText
                );

        double actualReceived =
                Double.parseDouble(
                        actualReceivedText
                );

        double extraFee = 0;

        if (!extraFeeText.isEmpty()) {

            extraFee =
                    Double.parseDouble(
                            extraFeeText
                    );
        }

        double promisedAmount =
                amountSent *
                        promisedRate;

        double difference =
                promisedAmount -
                        actualReceived;

        double differencePercentage =
                calculateDifferencePercentage(
                        promisedAmount,
                        actualReceived
                );

        /*
         * Final sanity check before Firestore.
         */
        if (actualReceived >
                promisedAmount * 3.0) {

            new AlertDialog.Builder(this)
                    .setTitle(
                            "Please review the report"
                    )
                    .setMessage(
                            "The actual received amount is unusually high compared with the promised amount. Please verify your entries before submitting."
                    )
                    .setPositiveButton(
                            "Review",
                            null
                    )
                    .show();

            return;
        }

        /*
         * Verify that nothing changed after calculation.
         */
        if (Math.abs(
                promisedAmount -
                        lastPromisedAmount
        ) > 0.000001 ||
                Math.abs(
                        actualReceived -
                                lastActualAmount
                ) > 0.000001 ||
                Math.abs(
                        difference -
                                lastDifference
                ) > 0.000001 ||
                Math.abs(
                        extraFee -
                                lastExtraFee
                ) > 0.000001) {

            invalidateCalculation();

            new AlertDialog.Builder(this)
                    .setTitle(
                            "Recalculate required"
                    )
                    .setMessage(
                            "The transaction details have changed. Please calculate the result again before submitting."
                    )
                    .setPositiveButton(
                            "OK",
                            null
                    )
                    .show();

            return;
        }

        /*
         * Selected provider.
         */
        String selectedProvider =
                spinnerProvider
                        .getSelectedItem()
                        .toString();

        if (selectedProvider.equals(
                "Select Provider"
        )) {

            Toast.makeText(
                    this,
                    "Please select a provider.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Create Firestore report.
         */
        Map<String, Object> report =
                new HashMap<>();

        report.put(
                "fromCountry",
                fromCountry
        );

        report.put(
                "toCountry",
                toCountry
        );

        report.put(
                "fromCurrency",
                fromCurrency
        );

        report.put(
                "toCurrency",
                toCurrency
        );

        report.put(
                "provider",
                selectedProvider
        );

        report.put(
                "amountSent",
                amountSent
        );

        report.put(
                "promisedRate",
                promisedRate
        );

        report.put(
                "promisedAmount",
                promisedAmount
        );

        report.put(
                "actualReceived",
                actualReceived
        );

        report.put(
                "difference",
                difference
        );

        /*
         * NEW:
         * Percentage difference makes
         * community analytics easier.
         */
        report.put(
                "differencePercentage",
                differencePercentage
        );

        report.put(
                "extraFee",
                extraFee
        );

        report.put(
                "transactionDate",
                transactionDate
        );

        report.put(
                "receivingMethod",
                spinnerReceivingMethod
                        .getSelectedItem()
                        .toString()
        );

        report.put(
                "receivingInstitution",
                getReceivingInstitution()
        );

        report.put(
                "receivingInstitutionType",
                getReceivingInstitutionType()
        );

        report.put(
                "rateSource",
                spinnerRateSource
                        .getSelectedItem()
                        .toString()
        );

        report.put(
                "referenceRate",
                latestReferenceRate != null
                        ? latestReferenceRate
                        : 0
        );

        boolean hasEvidence =
                evidenceUri != null &&
                        !evidenceUri.isEmpty();

        report.put(
                "hasEvidence",
                hasEvidence
        );

        /*
         * IMPORTANT:
         * Evidence being attached does not mean
         * it has been independently verified.
         */
        report.put(
                "verificationStatus",
                hasEvidence
                        ? "Evidence attached - not verified"
                        : "Unverified"
        );

        /*
         * Used later for community confidence.
         */
        report.put(
                "reportConfidence",
                hasEvidence
                        ? "Evidence attached"
                        : "User reported"
        );

        report.put(
                "evidenceUri",
                evidenceUri
        );

        report.put(
                "createdAt",
                new Date()
        );

        /*
         * Save to Firestore.
         */
        db.collection(
                        "actual_promised_reports"
                )
                .add(report)
                .addOnSuccessListener(
                        documentReference -> {

                            Toast.makeText(
                                    this,
                                    "Report submitted successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            btnSubmit.setEnabled(
                                    false
                            );

                            calculationValid =
                                    false;

                            if (tvVerificationStatus != null) {

                                tvVerificationStatus.setText(
                                        hasEvidence
                                                ? "Evidence attached - not verified"
                                                : "Unverified"
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Submission failed: " +
                                            e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    /*
     * =========================================================
     * DP TO PIXELS
     * =========================================================
     */

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    /*
     * =========================================================
     * ACTIVITY DESTROY
     * =========================================================
     */

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executorService.shutdownNow();
    }
}
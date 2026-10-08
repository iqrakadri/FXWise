package com.example.fxwise;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class CommunityReportsActivity extends AppCompatActivity {

    private Button btnBack;
    private Button btnFromCountry;
    private Button btnToCountry;
    private Button btnLoadReports;

    private TextView tvRoute;
    private TextView tvFromCurrency;
    private TextView tvToCurrency;

    private TextView tvReportCount;
    private TextView tvEvidenceCount;
    private TextView tvEvidencePercentage;
    private TextView tvDataConfidence;
    private TextView tvAverageDifference;
    private TextView tvAverageExtraFee;
    private TextView tvEmptyReports;

    private Spinner spinnerProvider;

    private LinearLayout reportsContainer;

    private FirebaseFirestore db;

    private String selectedFromCountry = "";
    private String selectedToCountry = "";

    private String selectedFromCurrency = "";
    private String selectedToCurrency = "";

    private final List<String> providerList =
            new ArrayList<>();

    private final String[] providers = {
            "All providers",
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

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_community_reports
        );

        db =
                FirebaseFirestore.getInstance();

        initializeViews();

        setupProviders();

        setupListeners();

        setInitialState();
    }

    // ============================================================
    // INITIALIZE VIEWS
    // ============================================================

    private void initializeViews() {

        btnBack =
                findViewById(R.id.btnBack);

        btnFromCountry =
                findViewById(R.id.btnFromCountry);

        btnToCountry =
                findViewById(R.id.btnToCountry);

        btnLoadReports =
                findViewById(R.id.btnLoadReports);

        tvRoute =
                findViewById(R.id.tvRoute);

        tvFromCurrency =
                findViewById(R.id.tvFromCurrency);

        tvToCurrency =
                findViewById(R.id.tvToCurrency);

        spinnerProvider =
                findViewById(R.id.spinnerProvider);

        tvReportCount =
                findViewById(R.id.tvReportCount);

        tvEvidenceCount =
                findViewById(R.id.tvEvidenceCount);

        tvEvidencePercentage =
                findViewById(R.id.tvEvidencePercentage);

        tvDataConfidence =
                findViewById(R.id.tvDataConfidence);

        tvAverageDifference =
                findViewById(R.id.tvAverageDifference);

        tvAverageExtraFee =
                findViewById(R.id.tvAverageExtraFee);

        tvEmptyReports =
                findViewById(R.id.tvEmptyReports);

        reportsContainer =
                findViewById(R.id.reportsContainer);
    }

    // ============================================================
    // INITIAL STATE
    // ============================================================

    private void setInitialState() {

        selectedFromCountry = "";
        selectedToCountry = "";

        selectedFromCurrency = "";
        selectedToCurrency = "";

        btnFromCountry.setText(
                "From country"
        );

        btnToCountry.setText(
                "To country"
        );

        tvFromCurrency.setText(
                "Sending currency"
        );

        tvToCurrency.setText(
                "Receiving currency"
        );

        tvRoute.setText(
                "Choose your transfer route"
        );

        tvReportCount.setText(
                "0"
        );

        tvAverageDifference.setText(
                "0"
        );

        tvAverageExtraFee.setText(
                "0"
        );

        tvEvidenceCount.setText(
                "0"
        );

        tvEvidencePercentage.setText(
                "0% with evidence"
        );

        tvDataConfidence.setText(
                "Select a route and view community data."
        );

        /*
         * Reports are hidden when the page first opens.
         */
        tvEmptyReports.setVisibility(
                View.GONE
        );

        reportsContainer.setVisibility(
                View.GONE
        );

        reportsContainer.removeAllViews();
    }

    // ============================================================
    // PROVIDER DROPDOWN
    // ORIGINAL VERSION
    // ============================================================

    private void setupProviders() {

        providerList.clear();

        Collections.addAll(
                providerList,
                providers
        );

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        providerList
                ) {

                    @Override
                    public View getView(
                            int position,
                            View convertView,
                            ViewGroup parent
                    ) {

                        TextView textView =
                                (TextView) super.getView(
                                        position,
                                        convertView,
                                        parent
                                );

                        textView.setTextColor(
                                Color.WHITE
                        );

                        textView.setTextSize(
                                14
                        );

                        textView.setGravity(
                                Gravity.CENTER_VERTICAL
                        );

                        textView.setPadding(
                                dpToPx(16),
                                0,
                                dpToPx(16),
                                0
                        );

                        return textView;
                    }
                };

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerProvider.setAdapter(
                adapter
        );
    }

    // ============================================================
    // LISTENERS
    // ============================================================

    private void setupListeners() {

        btnBack.setOnClickListener(
                v -> finish()
        );

        btnFromCountry.setOnClickListener(
                v -> showCountryPicker(true)
        );

        btnToCountry.setOnClickListener(
                v -> showCountryPicker(false)
        );

        btnLoadReports.setOnClickListener(
                v -> loadReports()
        );
    }

    // ============================================================
    // COUNTRY PICKER
    // ============================================================

    private void showCountryPicker(
            boolean isFromCountry
    ) {

        final List<String> countries =
                getCountryList();

        /*
         * Main dialog layout.
         */
        LinearLayout mainLayout =
                new LinearLayout(this);

        mainLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        mainLayout.setPadding(
                dpToPx(20),
                dpToPx(10),
                dpToPx(20),
                dpToPx(14)
        );

        /*
         * SOLID dark background.
         */
        mainLayout.setBackground(
                createDarkBackground(
                        Color.rgb(
                                17,
                                24,
                                43
                        ),
                        Color.rgb(
                                37,
                                50,
                                76
                        ),
                        20
                )
        );

        // ========================================================
        // SEARCH BOX
        // ========================================================

        EditText searchBox =
                new EditText(this);

        searchBox.setHint(
                "Search country"
        );

        searchBox.setSingleLine(
                true
        );

        searchBox.setTextColor(
                Color.WHITE
        );

        searchBox.setHintTextColor(
                Color.rgb(
                        150,
                        165,
                        190
                )
        );

        searchBox.setTextSize(
                15
        );

        searchBox.setPadding(
                dpToPx(18),
                0,
                dpToPx(18),
                0
        );

        searchBox.setBackground(
                createDarkBackground(
                        Color.rgb(
                                21,
                                31,
                                53
                        ),
                        Color.rgb(
                                77,
                                141,
                                255
                        ),
                        16
                )
        );

        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(54)
                );

        mainLayout.addView(
                searchBox,
                searchParams
        );

        // ========================================================
        // COUNTRY LIST
        // ========================================================

        ListView listView =
                new ListView(this);

        listView.setDivider(
                null
        );

        listView.setDividerHeight(
                0
        );

        /*
         * Solid dark country list.
         */
        listView.setBackgroundColor(
                Color.rgb(
                        17,
                        24,
                        43
                )
        );

        LinearLayout.LayoutParams listParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(500)
                );

        listParams.topMargin =
                dpToPx(10);

        mainLayout.addView(
                listView,
                listParams
        );

        // ========================================================
        // FILTERED COUNTRY LIST
        // ========================================================

        final List<String> filteredCountries =
                new ArrayList<>(
                        countries
                );

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_list_item_1,
                        filteredCountries
                ) {

                    @Override
                    public View getView(
                            int position,
                            View convertView,
                            ViewGroup parent
                    ) {

                        TextView textView =
                                (TextView) super.getView(
                                        position,
                                        convertView,
                                        parent
                                );

                        textView.setTextColor(
                                Color.WHITE
                        );

                        textView.setTextSize(
                                15
                        );

                        textView.setGravity(
                                Gravity.CENTER_VERTICAL
                        );

                        textView.setPadding(
                                dpToPx(18),
                                dpToPx(14),
                                dpToPx(18),
                                dpToPx(14)
                        );

                        /*
                         * Solid country row.
                         */
                        textView.setBackgroundColor(
                                Color.rgb(
                                        17,
                                        24,
                                        43
                                )
                        );

                        return textView;
                    }
                };

        listView.setAdapter(
                adapter
        );

        // ========================================================
        // DIALOG
        // ========================================================

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                isFromCountry
                                        ? "Select sending country"
                                        : "Select receiving country"
                        )
                        .setView(
                                mainLayout
                        )
                        .create();

        // ========================================================
        // COUNTRY SELECTION
        // ========================================================

        listView.setOnItemClickListener(
                (parent, view, position, id) -> {

                    String country =
                            filteredCountries.get(
                                    position
                            );

                    if (isFromCountry) {

                        selectedFromCountry =
                                country;

                        selectedFromCurrency =
                                getCurrencyForCountry(
                                        country
                                );

                        btnFromCountry.setText(
                                country
                        );

                        tvFromCurrency.setText(
                                selectedFromCurrency
                        );

                    } else {

                        selectedToCountry =
                                country;

                        selectedToCurrency =
                                getCurrencyForCountry(
                                        country
                                );

                        btnToCountry.setText(
                                country
                        );

                        tvToCurrency.setText(
                                selectedToCurrency
                        );
                    }

                    updateRouteText();

                    resetCommunityResults();

                    dialog.dismiss();
                }
        );

        // ========================================================
        // SEARCH FILTER
        // ========================================================

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

                        String query =
                                s.toString()
                                        .trim()
                                        .toLowerCase(
                                                Locale.ENGLISH
                                        );

                        filteredCountries.clear();

                        for (
                                String country :
                                countries
                        ) {

                            if (
                                    country
                                            .toLowerCase(
                                                    Locale.ENGLISH
                                            )
                                            .contains(
                                                    query
                                            )
                            ) {

                                filteredCountries.add(
                                        country
                                );
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );

        // ========================================================
        // SHOW DIALOG
        // ========================================================

        dialog.show();

        Window window =
                dialog.getWindow();

        if (window != null) {

            /*
             * Solid dialog background.
             */
            window.setBackgroundDrawable(
                    createDarkBackground(
                            Color.rgb(
                                    17,
                                    24,
                                    43
                            ),
                            Color.rgb(
                                    37,
                                    50,
                                    76
                            ),
                            20
                    )
            );

            window.setLayout(
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels
                                    * 0.92
                    ),
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .heightPixels
                                    * 0.82
                    )
            );
        }

        /*
         * Keyboard does not automatically open.
         */
        searchBox.clearFocus();
    }

    // ============================================================
    // COUNTRY LIST
    // ============================================================

    private List<String> getCountryList() {

        List<String> countries =
                new ArrayList<>();

        String[] countryCodes =
                Locale.getISOCountries();

        for (
                String code :
                countryCodes
        ) {

            Locale locale =
                    new Locale(
                            "",
                            code
                    );

            String countryName =
                    locale.getDisplayCountry(
                            Locale.ENGLISH
                    );

            if (
                    countryName != null
                            && !countryName
                            .trim()
                            .isEmpty()
            ) {

                countries.add(
                        countryName
                );
            }
        }

        Collections.sort(
                countries,
                String.CASE_INSENSITIVE_ORDER
        );

        return countries;
    }

    // ============================================================
    // COUNTRY TO CURRENCY
    // ============================================================

    private String getCurrencyForCountry(
            String countryName
    ) {

        String[] countryCodes =
                Locale.getISOCountries();

        for (
                String code :
                countryCodes
        ) {

            Locale locale =
                    new Locale(
                            "",
                            code
                    );

            String displayName =
                    locale.getDisplayCountry(
                            Locale.ENGLISH
                    );

            if (
                    displayName != null
                            && displayName
                            .equalsIgnoreCase(
                                    countryName
                            )
            ) {

                try {

                    Currency currency =
                            Currency.getInstance(
                                    locale
                            );

                    return currency
                            .getCurrencyCode();

                } catch (Exception e) {

                    return "Currency";
                }
            }
        }

        return "Currency";
    }

    // ============================================================
    // ROUTE TEXT
    // ============================================================

    private void updateRouteText() {

        if (
                selectedFromCountry.isEmpty()
                        || selectedToCountry.isEmpty()
        ) {

            tvRoute.setText(
                    "Choose your transfer route"
            );

            return;
        }

        tvRoute.setText(
                selectedFromCountry
                        + " to "
                        + selectedToCountry
        );
    }

    // ============================================================
    // RESET RESULTS
    // ============================================================

    private void resetCommunityResults() {

        tvReportCount.setText(
                "0"
        );

        tvAverageDifference.setText(
                "0"
        );

        tvAverageExtraFee.setText(
                "0"
        );

        tvEvidenceCount.setText(
                "0"
        );

        tvEvidencePercentage.setText(
                "0% with evidence"
        );

        tvDataConfidence.setText(
                "Select View community data to load reports."
        );

        tvEmptyReports.setVisibility(
                View.GONE
        );

        /*
         * Hide old report cards until the user
         * presses View community data again.
         */
        reportsContainer.setVisibility(
                View.GONE
        );

        reportsContainer.removeAllViews();
    }

    // ============================================================
    // LOAD REPORTS
    // ============================================================

    private void loadReports() {

        if (
                selectedFromCountry.isEmpty()
                        || selectedToCountry.isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Please select both countries.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (
                selectedFromCountry
                        .equalsIgnoreCase(
                                selectedToCountry
                        )
        ) {

            Toast.makeText(
                    this,
                    "Sending and receiving countries must be different.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Loading state.
         */
        btnLoadReports.setEnabled(
                false
        );

        btnLoadReports.setText(
                "Loading community data..."
        );

        reportsContainer.removeAllViews();

        tvEmptyReports.setVisibility(
                View.GONE
        );

        String selectedProvider =
                spinnerProvider
                        .getSelectedItem()
                        .toString();

        db.collection(
                        "actual_promised_reports"
                )
                .whereEqualTo(
                        "fromCountry",
                        selectedFromCountry
                )
                .whereEqualTo(
                        "toCountry",
                        selectedToCountry
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            List<DocumentSnapshot> reports =
                                    new ArrayList<>();

                            for (
                                    DocumentSnapshot document :
                                    querySnapshot
                                            .getDocuments()
                            ) {

                                String provider =
                                        document.getString(
                                                "provider"
                                        );

                                if (
                                        selectedProvider
                                                .equals(
                                                        "All providers"
                                                )
                                                || selectedProvider
                                                .equalsIgnoreCase(
                                                        provider == null
                                                                ? ""
                                                                : provider
                                                )
                                ) {

                                    reports.add(
                                            document
                                    );
                                }
                            }

                            displayReports(
                                    reports
                            );

                            btnLoadReports.setEnabled(
                                    true
                            );

                            btnLoadReports.setText(
                                    "View community data"
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            btnLoadReports.setEnabled(
                                    true
                            );

                            btnLoadReports.setText(
                                    "View community data"
                            );

                            Toast.makeText(
                                    this,
                                    "Unable to load community data.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            tvDataConfidence.setText(
                                    "Could not load community data."
                            );
                        }
                );
    }

    // ============================================================
    // DISPLAY REPORTS
    // ============================================================

    private void displayReports(
            List<DocumentSnapshot> reports
    ) {

        reportsContainer.removeAllViews();

        /*
         * Results are shown only because the user
         * clicked View community data.
         */
        reportsContainer.setVisibility(
                View.VISIBLE
        );

        int totalReports =
                reports.size();

        int evidenceCount = 0;

        double differenceSum = 0;

        double extraFeeSum = 0;

        for (
                DocumentSnapshot document :
                reports
        ) {

            if (
                    hasEvidence(
                            document
                    )
            ) {

                evidenceCount++;
            }

            differenceSum +=
                    getDouble(
                            document,
                            "difference"
                    );

            extraFeeSum +=
                    getDouble(
                            document,
                            "extraFee"
                    );
        }

        double averageDifference =
                totalReports == 0
                        ? 0
                        : differenceSum
                        / totalReports;

        double averageExtraFee =
                totalReports == 0
                        ? 0
                        : extraFeeSum
                        / totalReports;

        tvReportCount.setText(
                String.valueOf(
                        totalReports
                )
        );

        tvAverageDifference.setText(
                formatCurrency(
                        selectedToCurrency,
                        averageDifference
                )
        );

        tvAverageExtraFee.setText(
                formatCurrency(
                        selectedToCurrency,
                        averageExtraFee
                )
        );

        tvEvidenceCount.setText(
                String.valueOf(
                        evidenceCount
                )
        );

        int evidencePercentage =
                totalReports == 0
                        ? 0
                        : (int) Math.round(
                        evidenceCount
                                * 100.0
                                / totalReports
                );

        tvEvidencePercentage.setText(
                evidencePercentage
                        + "% with evidence"
        );

        updateDataConfidence(
                totalReports,
                evidenceCount
        );

        if (
                totalReports == 0
        ) {

            tvEmptyReports.setVisibility(
                    View.VISIBLE
            );

            return;
        }

        tvEmptyReports.setVisibility(
                View.GONE
        );

        /*
         * Newest report first.
         */
        Collections.sort(
                reports,
                new Comparator<DocumentSnapshot>() {

                    @Override
                    public int compare(
                            DocumentSnapshot a,
                            DocumentSnapshot b
                    ) {

                        String dateA =
                                a.getString(
                                        "transactionDate"
                                );

                        String dateB =
                                b.getString(
                                        "transactionDate"
                                );

                        if (dateA == null) {
                            dateA = "";
                        }

                        if (dateB == null) {
                            dateB = "";
                        }

                        return dateB.compareTo(
                                dateA
                        );
                    }
                }
        );

        for (
                DocumentSnapshot document :
                reports
        ) {

            addReportCard(
                    document
            );
        }
    }

    // ============================================================
    // REPORT CARD
    // ============================================================

    private void addReportCard(
            DocumentSnapshot document
    ) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dpToPx(18),
                dpToPx(18),
                dpToPx(18),
                dpToPx(18)
        );

        card.setBackground(
                createDarkBackground(
                        Color.rgb(
                                17,
                                24,
                                43
                        ),
                        Color.rgb(
                                29,
                                41,
                                66
                        ),
                        20
                )
        );

        String provider =
                document.getString(
                        "provider"
                );

        String transactionDate =
                document.getString(
                        "transactionDate"
                );

        String receivingMethod =
                document.getString(
                        "receivingMethod"
                );

        String rateSource =
                document.getString(
                        "rateSource"
                );

        double amountSent =
                getDouble(
                        document,
                        "amountSent"
                );

        double promisedRate =
                getDouble(
                        document,
                        "promisedRate"
                );

        double actualReceived =
                getDouble(
                        document,
                        "actualReceived"
                );

        double difference =
                getDouble(
                        document,
                        "difference"
                );

        double extraFee =
                getDouble(
                        document,
                        "extraFee"
                );

        TextView providerView =
                createText(
                        provider == null
                                ? "Unknown provider"
                                : provider,
                        18,
                        Color.WHITE,
                        true
                );

        card.addView(
                providerView
        );

        TextView evidenceView =
                createText(
                        hasEvidence(
                                document
                        )
                                ? "Evidence provided"
                                : "User-reported",
                        11,
                        hasEvidence(
                                document
                        )
                                ? Color.rgb(
                                111,
                                165,
                                255
                        )
                                : Color.rgb(
                                152,
                                166,
                                190
                        ),
                        false
                );

        evidenceView.setPadding(
                0,
                dpToPx(6),
                0,
                dpToPx(12)
        );

        card.addView(
                evidenceView
        );

        addDetail(
                card,
                "Route",
                selectedFromCountry
                        + " to "
                        + selectedToCountry
        );

        addDetail(
                card,
                "Amount sent",
                formatCurrency(
                        selectedFromCurrency,
                        amountSent
                )
        );

        addDetail(
                card,
                "Promised rate",
                String.format(
                        Locale.US,
                        "%.4f",
                        promisedRate
                )
        );

        addDetail(
                card,
                "Actually received",
                formatCurrency(
                        selectedToCurrency,
                        actualReceived
                )
        );

        addDetail(
                card,
                "Reported difference",
                formatCurrency(
                        selectedToCurrency,
                        difference
                )
        );

        addDetail(
                card,
                "Extra fee",
                formatCurrency(
                        selectedToCurrency,
                        extraFee
                )
        );

        addDetail(
                card,
                "Receiving method",
                receivingMethod == null
                        ? "Not specified"
                        : receivingMethod
        );

        addDetail(
                card,
                "Rate source",
                rateSource == null
                        ? "Not specified"
                        : rateSource
        );

        addDetail(
                card,
                "Transaction date",
                transactionDate == null
                        ? "Not specified"
                        : transactionDate
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                0,
                0,
                dpToPx(14)
        );

        reportsContainer.addView(
                card,
                params
        );
    }

    // ============================================================
    // DETAIL ROW
    // ============================================================

    private void addDetail(
            LinearLayout parent,
            String label,
            String value
    ) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setPadding(
                0,
                dpToPx(5),
                0,
                dpToPx(5)
        );

        TextView labelView =
                createText(
                        label,
                        11,
                        Color.rgb(
                                113,
                                128,
                                154
                        ),
                        false
                );

        TextView valueView =
                createText(
                        value,
                        12,
                        Color.WHITE,
                        false
                );

        LinearLayout.LayoutParams labelParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0.43f
                );

        LinearLayout.LayoutParams valueParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0.57f
                );

        row.addView(
                labelView,
                labelParams
        );

        row.addView(
                valueView,
                valueParams
        );

        parent.addView(
                row
        );
    }

    // ============================================================
    // TEXT HELPER
    // ============================================================

    private TextView createText(
            String text,
            float size,
            int color,
            boolean bold
    ) {

        TextView view =
                new TextView(this);

        view.setText(
                text
        );

        view.setTextSize(
                size
        );

        view.setTextColor(
                color
        );

        if (bold) {

            view.setTypeface(
                    null,
                    Typeface.BOLD
            );
        }

        return view;
    }

    // ============================================================
    // EVIDENCE CHECK
    // ============================================================

    private boolean hasEvidence(
            DocumentSnapshot document
    ) {

        Boolean evidence =
                document.getBoolean(
                        "hasEvidence"
                );

        if (
                Boolean.TRUE.equals(
                        evidence
                )
        ) {

            return true;
        }

        String verificationStatus =
                document.getString(
                        "verificationStatus"
                );

        if (
                "Evidence provided"
                        .equalsIgnoreCase(
                                verificationStatus == null
                                        ? ""
                                        : verificationStatus
                        )
        ) {

            return true;
        }

        String evidenceUri =
                document.getString(
                        "evidenceUri"
                );

        return evidenceUri != null
                && !evidenceUri
                .trim()
                .isEmpty();
    }

    // ============================================================
    // DATA CONFIDENCE
    // ============================================================

    private void updateDataConfidence(
            int reportCount,
            int evidenceCount
    ) {

        if (
                reportCount == 0
        ) {

            tvDataConfidence.setText(
                    "No community data available for this route."
            );

            return;
        }

        if (
                evidenceCount == 0
        ) {

            tvDataConfidence.setText(
                    "User-reported data. No evidence attached."
            );

            return;
        }

        int percentage =
                (int) Math.round(
                        evidenceCount
                                * 100.0
                                / reportCount
                );

        if (
                percentage < 50
        ) {

            tvDataConfidence.setText(
                    "Community data with limited evidence coverage."
            );

        } else {

            tvDataConfidence.setText(
                    "Evidence attached to "
                            + percentage
                            + "% of community reports."
            );
        }
    }

    // ============================================================
    // FIRESTORE NUMBER HELPER
    // ============================================================

    private double getDouble(
            DocumentSnapshot document,
            String field
    ) {

        Object value =
                document.get(
                        field
                );

        if (
                value instanceof Number
        ) {

            return (
                    (Number) value
            ).doubleValue();
        }

        if (
                value instanceof String
        ) {

            try {

                return Double.parseDouble(
                        (String) value
                );

            } catch (Exception ignored) {
            }
        }

        return 0;
    }

    // ============================================================
    // CURRENCY FORMAT
    // ============================================================

    private String formatCurrency(
            String currencyCode,
            double amount
    ) {

        if (
                currencyCode == null
                        || currencyCode
                        .trim()
                        .isEmpty()
        ) {

            return String.format(
                    Locale.US,
                    "%.2f",
                    amount
            );
        }

        try {

            NumberFormat format =
                    NumberFormat.getCurrencyInstance(
                            Locale.US
                    );

            format.setCurrency(
                    Currency.getInstance(
                            currencyCode
                    )
            );

            return format.format(
                    amount
            );

        } catch (Exception e) {

            return currencyCode
                    + " "
                    + String.format(
                    Locale.US,
                    "%.2f",
                    amount
            );
        }
    }

    // ============================================================
    // DARK BACKGROUND HELPER
    // ============================================================

    private GradientDrawable createDarkBackground(
            int fillColor,
            int strokeColor,
            int radiusDp
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(
                fillColor
        );

        drawable.setCornerRadius(
                dpToPx(
                        radiusDp
                )
        );

        drawable.setStroke(
                dpToPx(1),
                strokeColor
        );

        return drawable;
    }

    // ============================================================
    // DP TO PX
    // ============================================================

    private int dpToPx(
            int dp
    ) {

        return (int) (
                dp
                        * getResources()
                        .getDisplayMetrics()
                        .density
                        + 0.5f
        );
    }
}
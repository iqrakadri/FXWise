package com.example.fxwise;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fxwise.data.CorridorData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResultActivity extends AppCompatActivity {

    private static final String TAG = "FXWiseResult";

    private TextView btnBack;

    // What-If Simulator button
    private Button btnWhatIf;

    private TextView tvRoute;
    private TextView tvPriority;
    private TextView tvWhy;
    private TextView tvBestMatch;

    private TextView tvHeroAmount;
    private TextView tvReferenceRate;

    private TextView tvFee;
    private TextView tvFxImpact;
    private TextView tvTotalCost;
    private TextView tvAdditionalCharges;
    private TextView tvReceive1;

    private TextView tvProvider1;
    private TextView tvReceiveProvider1;
    private TextView tvFeeProvider1;
    private TextView tvTotalCostProvider1;
    private TextView tvSpeed1;
    private TextView tvCommunity1;

    private TextView tvProvider2;
    private TextView tvReceive2;
    private TextView tvFeeProvider2;
    private TextView tvTotalCostProvider2;
    private TextView tvSpeed2;
    private TextView tvCommunity2;

    private TextView tvProvider3;
    private TextView tvReceive3;
    private TextView tvFeeProvider3;
    private TextView tvTotalCostProvider3;
    private TextView tvSpeed3;
    private TextView tvCommunity3;

    private TextView tvHealth1, tvHealth2, tvHealth3;
    private TextView tvHiddenWarning1, tvHiddenWarning2, tvHiddenWarning3;
    private TextView tvPromo1, tvPromo2, tvPromo3;

    private TextView tvWarning;

    private FirebaseFirestore db;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private String fromCountry = "";
    private String toCountry = "";

    private String fromCurrency = "";
    private String toCurrency = "";

    private double sendAmount = 0;

    private String priority = "Balanced";

    private Double referenceRate = null;

    private final DecimalFormat decimalFormat =
            new DecimalFormat("#,##0.00");

    private final Map<String, CommunityStats> communityStatsMap =
            new HashMap<>();

    private final List<Provider> currentProviders =
            new ArrayList<>();

    private boolean communityDataLoaded = false;


    // ============================================================
    // PROVIDER MODEL
    // ============================================================

    private static class Provider {

        String name;
        String corridorKey;
        String speed;
        String receivingMethod;
        String dataSource;

        boolean isEstimate;

        double feePercentage;
        double fxMarkupPercentage;

        double intermediaryCharge;
        double receivingCharge;

        double baseFee;
        double baseFxImpact;
        double baseTotalCost;
        double baseReceive;

        double fee;
        double fxImpact;
        double totalCost;
        double receive;

        double communityDifference;
        double communityExtraFee;
        int communityReportCount;

        double balancedScore;

        boolean promoAvailable;
        double promoFee;
        double regularFee;
        double promoSavings;

        int speedRank;


        Provider(
                String name,
                String corridorKey,
                String speed,
                String receivingMethod,
                String dataSource,
                boolean isEstimate,
                double feePercentage,
                double fxMarkupPercentage,
                double intermediaryCharge,
                double receivingCharge,
                double fee,
                double fxImpact,
                double totalCost,
                double receive,
                int speedRank
        ) {

            this.name = name;
            this.corridorKey = corridorKey;
            this.speed = speed;
            this.receivingMethod = receivingMethod;
            this.dataSource = dataSource;
            this.isEstimate = isEstimate;

            this.feePercentage = feePercentage;
            this.fxMarkupPercentage = fxMarkupPercentage;

            this.intermediaryCharge = intermediaryCharge;
            this.receivingCharge = receivingCharge;

            this.fee = fee;
            this.fxImpact = fxImpact;
            this.totalCost = totalCost;
            this.receive = receive;

            this.baseFee = fee;
            this.baseFxImpact = fxImpact;
            this.baseTotalCost = totalCost;
            this.baseReceive = receive;

            this.regularFee = fee;
            this.promoFee = fee;
            this.promoSavings = 0;
            this.promoAvailable = false;

            this.communityDifference = 0;
            this.communityExtraFee = 0;
            this.communityReportCount = 0;

            this.speedRank = speedRank;
        }
    }


    // ============================================================
    // COMMUNITY STATISTICS
    // ============================================================

    private static class CommunityStats {

        int reportCount = 0;

        double differenceSum = 0;
        double extraFeeSum = 0;

        double averageDifference = 0;
        double averageExtraFee = 0;


        void addReport(
                double difference,
                double extraFee
        ) {

            reportCount++;

            differenceSum += Math.max(
                    0,
                    difference
            );

            extraFeeSum += Math.max(
                    0,
                    extraFee
            );
        }


        void calculateAverage() {

            if (reportCount > 0) {

                averageDifference =
                        differenceSum / reportCount;

                averageExtraFee =
                        extraFeeSum / reportCount;
            }
        }
    }


    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_results);

        db = FirebaseFirestore.getInstance();

        getIntentData();

        initializeViews();

        setupBackButton();

        // ========================================================
        // WHAT-IF SIMULATOR NAVIGATION
        // ========================================================

        btnWhatIf.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ResultActivity.this,
                    WhatIfActivity.class
            );

            // Current route
            intent.putExtra(
                    "fromCountry",
                    fromCountry
            );

            intent.putExtra(
                    "toCountry",
                    toCountry
            );

            // Current currencies
            intent.putExtra(
                    "fromCurrency",
                    fromCurrency
            );

            intent.putExtra(
                    "toCurrency",
                    toCurrency
            );

            // Current amount
            intent.putExtra(
                    "amount",
                    sendAmount
            );

            // Current priority
            intent.putExtra(
                    "priority",
                    priority
            );

            startActivity(intent);
        });

        calculateResults();

        loadCommunityData();

        fetchReferenceRate();
    }


    // ============================================================
    // GET INTENT DATA
    // ============================================================

    private void getIntentData() {

        fromCountry =
                getIntent().getStringExtra("fromCountry");

        toCountry =
                getIntent().getStringExtra("toCountry");

        fromCurrency =
                getIntent().getStringExtra("fromCurrency");

        toCurrency =
                getIntent().getStringExtra("toCurrency");


        if (getIntent().hasExtra("amount")) {

            try {

                sendAmount =
                        getIntent().getDoubleExtra(
                                "amount",
                                0
                        );

            } catch (Exception e) {

                sendAmount = 0;
            }
        }


        if (sendAmount <= 0) {

            String amountString =
                    getIntent().getStringExtra("amount");

            if (amountString != null) {

                try {

                    amountString =
                            amountString
                                    .replace(",", "")
                                    .trim();

                    sendAmount =
                            Double.parseDouble(
                                    amountString
                            );

                } catch (Exception ignored) {

                    sendAmount = 0;
                }
            }
        }


        priority =
                getIntent().getStringExtra("priority");


        if (fromCountry == null ||
                fromCountry.trim().isEmpty()) {

            fromCountry = "United States";
        }


        if (toCountry == null ||
                toCountry.trim().isEmpty()) {

            toCountry = "India";
        }


        if (fromCurrency == null ||
                fromCurrency.trim().isEmpty()) {

            fromCurrency = "USD";
        }


        if (toCurrency == null ||
                toCurrency.trim().isEmpty()) {

            toCurrency = "INR";
        }


        if (priority == null ||
                priority.trim().isEmpty()) {

            priority = "Balanced";
        }


        Log.d(
                TAG,
                "Amount = " + sendAmount +
                        ", From = " + fromCurrency +
                        ", To = " + toCurrency +
                        ", Priority = " + priority
        );
    }


    // ============================================================
    // INITIALIZE VIEWS
    // ============================================================

    private void initializeViews() {

        btnBack = findViewById(R.id.btnBack);

        // What-If Simulator button
        btnWhatIf = findViewById(R.id.btnWhatIf);

        tvRoute = findViewById(R.id.tvRoute);
        tvPriority = findViewById(R.id.tvPriority);
        tvWhy = findViewById(R.id.tvWhy);
        tvBestMatch = findViewById(R.id.tvBestMatch);

        tvHeroAmount = findViewById(R.id.tvHeroAmount);

        tvReferenceRate =
                findViewById(R.id.tvReferenceRate);

        tvFee =
                findViewById(R.id.tvFee);

        tvFxImpact =
                findViewById(R.id.tvFxImpact);

        tvTotalCost =
                findViewById(R.id.tvTotalCost);

        tvAdditionalCharges =
                findViewById(R.id.tvAdditionalCharges);

        tvReceive1 =
                findViewById(R.id.tvReceive1);


        tvProvider1 =
                findViewById(R.id.tvProvider1);

        tvReceiveProvider1 =
                findViewById(R.id.tvReceiveProvider1);

        tvFeeProvider1 =
                findViewById(R.id.tvFeeProvider1);

        tvTotalCostProvider1 =
                findViewById(R.id.tvTotalCostProvider1);

        tvSpeed1 =
                findViewById(R.id.tvSpeed1);

        tvCommunity1 =
                findViewById(R.id.tvCommunity1);


        tvProvider2 =
                findViewById(R.id.tvProvider2);

        tvReceive2 =
                findViewById(R.id.tvReceive2);

        tvFeeProvider2 =
                findViewById(R.id.tvFeeProvider2);

        tvTotalCostProvider2 =
                findViewById(R.id.tvTotalCostProvider2);

        tvSpeed2 =
                findViewById(R.id.tvSpeed2);

        tvCommunity2 =
                findViewById(R.id.tvCommunity2);


        tvProvider3 =
                findViewById(R.id.tvProvider3);

        tvReceive3 =
                findViewById(R.id.tvReceive3);

        tvFeeProvider3 =
                findViewById(R.id.tvFeeProvider3);

        tvTotalCostProvider3 =
                findViewById(R.id.tvTotalCostProvider3);

        tvSpeed3 =
                findViewById(R.id.tvSpeed3);

        tvCommunity3 =
                findViewById(R.id.tvCommunity3);

        tvHealth1 = findViewById(R.id.tvHealth1);
        tvHealth2 = findViewById(R.id.tvHealth2);
        tvHealth3 = findViewById(R.id.tvHealth3);

        tvHiddenWarning1 = findViewById(R.id.tvHiddenWarning1);
        tvHiddenWarning2 = findViewById(R.id.tvHiddenWarning2);
        tvHiddenWarning3 = findViewById(R.id.tvHiddenWarning3);

        tvPromo1 = findViewById(R.id.tvPromo1);
        tvPromo2 = findViewById(R.id.tvPromo2);
        tvPromo3 = findViewById(R.id.tvPromo3);


        tvWarning =
                findViewById(R.id.tvWarning);


        tvCommunity1.setText(
                "Loading community data..."
        );

        tvCommunity2.setText(
                "Loading community data..."
        );

        tvCommunity3.setText(
                "Loading community data..."
        );
    }


    // ============================================================
    // BACK
    // ============================================================

    private void setupBackButton() {

        btnBack.setOnClickListener(
                v -> finish()
        );
    }


    // ============================================================
    // CALCULATE RESULTS
    // ============================================================

    private void calculateResults() {

        if (sendAmount <= 0) {

            showZeroAmountState();

            return;
        }


        double rate;

        if (referenceRate != null &&
                referenceRate > 0) {

            rate = referenceRate;

        } else {

            rate =
                    getFallbackRate(
                            fromCurrency,
                            toCurrency
                    );
        }


        if (rate <= 0) {

            tvReferenceRate.setText(
                    "Reference rate unavailable"
            );

            return;
        }


        String corridorKey =
                createCorridorKey(
                        fromCountry,
                        toCountry
                );


        List<Provider> providers =
                createProvidersForCorridor(
                        corridorKey,
                        rate
                );


        if (providers.size() < 3) {

            tvWarning.setText(
                    "Not enough provider data available for this corridor."
            );

            return;
        }


        if (communityDataLoaded) {

            applyCommunityAdjustments(
                    providers,
                    rate
            );
        }


        calculateBalancedScores(providers);

        sortProviders(providers);


        currentProviders.clear();

        currentProviders.addAll(providers);


        displayPriorityInformation(
                providers.get(0)
        );


        displaySummary(
                providers.get(0)
        );


        displayProvider(
                providers.get(0),
                1
        );


        displayProvider(
                providers.get(1),
                2
        );


        displayProvider(
                providers.get(2),
                3
        );


        applyCommunityData();
    }


    // ============================================================
    // CREATE CORRIDOR KEY
    // ============================================================

    private String createCorridorKey(
            String from,
            String to
    ) {

        return from.trim().toLowerCase()
                + "->"
                + to.trim().toLowerCase();
    }


    // ============================================================
    // CREATE PROVIDERS FOR CORRIDOR
    // ============================================================

    private List<Provider> createProvidersForCorridor(
            String corridorKey,
            double rate
    ) {

        List<Provider> providers =
                new ArrayList<>();


        String[] corridorParts =
                corridorKey.split("->", 2);


        if (corridorParts.length != 2) {

            return providers;
        }


        String fromCountry =
                corridorParts[0].trim();

        String toCountry =
                corridorParts[1].trim();


        List<CorridorData.ProviderProfile> profiles =
                CorridorData.getProviders(
                        fromCountry,
                        toCountry
                );


        for (
                CorridorData.ProviderProfile profile :
                profiles
        ) {

            Provider provider =
                    createProvider(
                            profile.name,
                            corridorKey,
                            profile.feePercentage,
                            profile.fxMarkupPercentage,
                            profile.speed,
                            profile.receivingMethod,
                            "FXWise prototype estimate",
                            profile.intermediaryCharge,
                            profile.receivingCharge,
                            rate
                    );


            providers.add(provider);
        }


        return providers;
    }


    // ============================================================
    // CREATE PROVIDER
    // ============================================================

    private Provider createProvider(
            String name,
            String corridorKey,
            double feePercentage,
            double fxMarkupPercentage,
            String speed,
            String receivingMethod,
            String dataSource,
            double intermediaryCharge,
            double receivingCharge,
            double rate
    ) {

        int speedRank =
                getSpeedRank(speed);


        double fee =
                sendAmount *
                        (feePercentage / 100.0);


        double amountAfterFee =
                sendAmount - fee;


        double providerRate =
                rate *
                        (1 -
                                fxMarkupPercentage / 100.0);


        double receiveBeforeCharges =
                amountAfterFee *
                        providerRate;


        double receive =
                receiveBeforeCharges
                        -
                        intermediaryCharge
                        -
                        receivingCharge;


        if (receive < 0) {

            receive = 0;
        }


        double referenceReceive =
                amountAfterFee *
                        rate;


        double fxImpact =
                referenceReceive -
                        receiveBeforeCharges;


        if (fxImpact < 0) {

            fxImpact = 0;
        }


        double fxImpactInSendingCurrency =
                rate > 0
                        ? fxImpact / rate
                        : 0;


        double totalCost =
                fee
                        +
                        fxImpactInSendingCurrency
                        +
                        intermediaryCharge
                        +
                        receivingCharge;


        Provider provider = new Provider(
                name,
                corridorKey,
                speed,
                receivingMethod,
                dataSource,
                true,
                feePercentage,
                fxMarkupPercentage,
                intermediaryCharge,
                receivingCharge,
                fee,
                fxImpact,
                totalCost,
                receive,
                speedRank
        );

        applyPrototypePromoScenario(provider);

        return provider;
    }


    // ============================================================
    // APPLY COMMUNITY ADJUSTMENTS
    // ============================================================

    private void applyCommunityAdjustments(
            List<Provider> providers,
            double rate
    ) {

        if (!communityDataLoaded) {

            return;
        }


        for (Provider provider :
                providers) {

            String normalizedProvider =
                    normalizeProviderName(
                            provider.name
                    );


            CommunityStats stats =
                    communityStatsMap.get(
                            normalizedProvider
                    );


            provider.fee =
                    provider.baseFee;

            provider.fxImpact =
                    provider.baseFxImpact;

            provider.totalCost =
                    provider.baseTotalCost;

            provider.receive =
                    provider.baseReceive;

            provider.communityDifference = 0;
            provider.communityExtraFee = 0;
            provider.communityReportCount = 0;


            if (stats == null ||
                    stats.reportCount == 0) {

                continue;
            }


            double communityDifference =
                    Math.max(
                            0,
                            stats.averageDifference
                    );


            double communityExtraFee =
                    Math.max(
                            0,
                            stats.averageExtraFee
                    );


            double adjustedReceive =
                    provider.baseReceive
                            -
                            communityDifference;


            if (adjustedReceive < 0) {

                adjustedReceive = 0;
            }


            double communityCostImpact =
                    rate > 0
                            ? communityDifference / rate
                            : 0;


            provider.receive =
                    adjustedReceive;


            provider.totalCost =
                    provider.baseTotalCost
                            +
                            communityCostImpact;


            provider.communityDifference =
                    communityDifference;


            provider.communityExtraFee =
                    communityExtraFee;


            provider.communityReportCount =
                    stats.reportCount;


            provider.dataSource =
                    "Prototype estimate + community reports";


            Log.d(
                    TAG,
                    "Community adjustment for "
                            + provider.name
                            + ": reports="
                            + stats.reportCount
                            + ", difference="
                            + communityDifference
                            + ", extraFee="
                            + communityExtraFee
            );
        }
    }


    // ============================================================
    // SPEED RANK
    // ============================================================

    private int getSpeedRank(
            String speed
    ) {

        if (speed == null) {

            return 2;
        }


        String value =
                speed.toLowerCase();


        if (value.contains("minutes")) {

            return 0;
        }


        if (value.contains("1 day")) {

            return 1;
        }


        return 2;
    }


    // ============================================================
    // BALANCED SCORE
    // ============================================================

    private void calculateBalancedScores(
            List<Provider> providers
    ) {

        double maxReceive = 0;

        double minCost =
                Double.MAX_VALUE;


        for (Provider provider :
                providers) {

            maxReceive =
                    Math.max(
                            maxReceive,
                            provider.receive
                    );

            minCost =
                    Math.min(
                            minCost,
                            provider.totalCost
                    );
        }


        for (Provider provider :
                providers) {

            double receiveScore =
                    maxReceive > 0
                            ? provider.receive /
                            maxReceive
                            : 0;


            double costScore =
                    provider.totalCost > 0
                            ? minCost /
                            provider.totalCost
                            : 0;


            double speedScore;


            if (provider.speedRank == 0) {

                speedScore = 1.0;

            } else if (
                    provider.speedRank == 1
            ) {

                speedScore = 0.8;

            } else {

                speedScore = 0.6;
            }


            provider.balancedScore =
                    (receiveScore * 0.50)
                            +
                            (costScore * 0.30)
                            +
                            (speedScore * 0.20);
        }
    }


    // ============================================================
    // SORT
    // ============================================================

    private void sortProviders(
            List<Provider> providers
    ) {

        if (priority.equalsIgnoreCase(
                "Fastest"
        )) {

            Collections.sort(
                    providers,
                    Comparator.comparingInt(
                            p -> p.speedRank
                    )
            );

        } else if (
                priority.equalsIgnoreCase(
                        "Lowest overall cost"
                )
        ) {

            Collections.sort(
                    providers,
                    Comparator.comparingDouble(
                            p -> p.totalCost
                    )
            );

        } else if (
                priority.equalsIgnoreCase(
                        "Most money received"
                )
        ) {

            Collections.sort(
                    providers,
                    (p1, p2) ->
                            Double.compare(
                                    p2.receive,
                                    p1.receive
                            )
            );

        } else {

            Collections.sort(
                    providers,
                    (p1, p2) ->
                            Double.compare(
                                    p2.balancedScore,
                                    p1.balancedScore
                            )
            );
        }
    }


    // ============================================================
    // PRIORITY INFORMATION
    // ============================================================

    private void displayPriorityInformation(
            Provider bestProvider
    ) {

        tvPriority.setText(
                "Priority: " + priority
        );


        if (priority.equalsIgnoreCase(
                "Fastest"
        )) {

            tvWhy.setText(
                    "Speed is your priority. FXWise ranks options by estimated transfer time."
            );

            tvBestMatch.setText(
                    "Recommended for you: "
                            + bestProvider.name
                            + "\n"
                            + "Reason: fastest estimated transfer time"
            );

        } else if (
                priority.equalsIgnoreCase(
                        "Lowest overall cost"
                )
        ) {

            tvWhy.setText(
                    "Cost is your priority. FXWise considers transfer fees, FX impact, known charges and community-reported shortfalls."
            );

            tvBestMatch.setText(
                    "Recommended for you: "
                            + bestProvider.name
                            + "\n"
                            + "Reason: lowest estimated total cost"
            );

        } else if (
                priority.equalsIgnoreCase(
                        "Most money received"
                )
        ) {

            tvWhy.setText(
                    "Recipient amount is your priority. FXWise ranks options using estimated recipient amount and available community experience."
            );

            tvBestMatch.setText(
                    "Recommended for you: "
                            + bestProvider.name
                            + "\n"
                            + "Reason: highest estimated recipient amount"
            );

        } else {

            tvWhy.setText(
                    "Balanced ranking considers recipient amount, total cost, transfer speed and available community experience."
            );

            tvBestMatch.setText(
                    "Recommended for you: "
                            + bestProvider.name
                            + "\n"
                            + "Reason: strongest overall balance of cost, amount received and speed"
            );
        }
    }


    // ============================================================
    // SUMMARY
    // ============================================================

    private void displaySummary(
            Provider bestProvider
    ) {

        tvRoute.setText(
                fromCountry +
                        " to " +
                        toCountry
        );


        tvHeroAmount.setText(
                toCurrency +
                        " " +
                        decimalFormat.format(
                                bestProvider.receive
                        )
        );


        tvFee.setText(
                fromCurrency +
                        " " +
                        decimalFormat.format(
                                bestProvider.fee
                        )
        );


        tvFxImpact.setText(
                toCurrency +
                        " " +
                        decimalFormat.format(
                                bestProvider.fxImpact
                        )
        );


        tvTotalCost.setText(
                fromCurrency +
                        " " +
                        decimalFormat.format(
                                bestProvider.totalCost
                        )
        );


        String additionalChargesText =
                "Receiving method: "
                        +
                        bestProvider.receivingMethod
                        +
                        "\n"
                        +
                        "Data source: "
                        +
                        bestProvider.dataSource;


        if (bestProvider.communityReportCount > 0) {

            additionalChargesText +=
                    "\n"
                            +
                            "Community reports: "
                            +
                            bestProvider.communityReportCount
                            +
                            "\n"
                            +
                            "Reported average shortfall: "
                            +
                            toCurrency
                            +
                            " "
                            +
                            decimalFormat.format(
                                    bestProvider.communityDifference
                            )
                            +
                            "\n"
                            +
                            "Reported average extra fee: "
                            +
                            toCurrency
                            +
                            " "
                            +
                            decimalFormat.format(
                                    bestProvider.communityExtraFee
                            );
        }


        tvAdditionalCharges.setText(
                additionalChargesText
        );


        tvReceive1.setText(
                toCurrency +
                        " " +
                        decimalFormat.format(
                                bestProvider.receive
                        )
        );


        tvWarning.setText(
                "Check the final provider quote before sending. "
                        +
                        "Community figures are based on user-reported transactions and may not represent every transfer. "
                        +
                        "Prototype provider values may also differ from the final provider quote."
        );
    }


    // ============================================================
    // ZERO AMOUNT
    // ============================================================

    private void showZeroAmountState() {

        tvHeroAmount.setText(
                toCurrency + " 0.00"
        );

        tvFee.setText(
                fromCurrency + " 0.00"
        );

        tvFxImpact.setText(
                toCurrency + " 0.00"
        );

        tvTotalCost.setText(
                fromCurrency + " 0.00"
        );

        tvReceive1.setText(
                toCurrency + " 0.00"
        );

        tvReferenceRate.setText(
                "Amount could not be read"
        );

        tvWarning.setText(
                "The transfer amount was not received correctly. Please go back and enter the amount again."
        );
    }


    // ============================================================
    // DISPLAY PROVIDER
    // ============================================================

    private void displayProvider(
            Provider provider,
            int position
    ) {

        TextView providerName;
        TextView receive;
        TextView fee;
        TextView totalCost;
        TextView speed;


        if (position == 1) {

            providerName = tvProvider1;
            receive = tvReceiveProvider1;
            fee = tvFeeProvider1;
            totalCost = tvTotalCostProvider1;
            speed = tvSpeed1;

        } else if (position == 2) {

            providerName = tvProvider2;
            receive = tvReceive2;
            fee = tvFeeProvider2;
            totalCost = tvTotalCostProvider2;
            speed = tvSpeed2;

        } else {

            providerName = tvProvider3;
            receive = tvReceive3;
            fee = tvFeeProvider3;
            totalCost = tvTotalCostProvider3;
            speed = tvSpeed3;
        }


        providerName.setText(
                provider.name
        );


        String receiveText =
                "Recipient receives: " +
                        toCurrency +
                        " " +
                        decimalFormat.format(
                                provider.receive
                        );


        if (provider.communityReportCount > 0) {

            receiveText +=
                    "\nCommunity-adjusted estimate";
        }


        receive.setText(
                receiveText
        );


        fee.setText(
                "Transfer fee: " +
                        fromCurrency +
                        " " +
                        decimalFormat.format(
                                provider.fee
                        )
        );


        totalCost.setText(
                "Total known cost: " +
                        fromCurrency +
                        " " +
                        decimalFormat.format(
                                provider.totalCost
                        )
        );


        speed.setText(
                provider.speed
                        +
                        " | "
                        +
                        provider.receivingMethod
        );

        TextView healthView =
                position == 1
                        ? tvHealth1
                        : position == 2
                        ? tvHealth2
                        : tvHealth3;

        TextView warningView =
                position == 1
                        ? tvHiddenWarning1
                        : position == 2
                        ? tvHiddenWarning2
                        : tvHiddenWarning3;

        TextView promoView =
                position == 1
                        ? tvPromo1
                        : position == 2
                        ? tvPromo2
                        : tvPromo3;


        healthView.setText(
                "Cost Health Score: " +
                        calculateCostHealthScore(provider) +
                        "/100"
        );


        warningView.setText(
                generateHiddenFeeWarning(provider)
        );


        promoView.setText(
                generatePromoMessage(provider)
        );
    }


    // ============================================================
    // COST HEALTH SCORE
    // ============================================================

    private int calculateCostHealthScore(
            Provider provider
    ) {

        double costScore = 40.0;

        double fxScore = 25.0;

        double chargeScore = 15.0;

        double speedScore =
                provider.speedRank == 0
                        ? 10.0
                        : provider.speedRank == 1
                        ? 8.0
                        : 6.0;

        double communityScore =
                provider.communityReportCount > 0
                        ? 10.0
                        : 5.0;


        if (sendAmount > 0) {

            double costRatio =
                    provider.totalCost /
                            sendAmount;

            costScore =
                    Math.max(
                            0,
                            40.0 -
                                    Math.min(
                                            40.0,
                                            costRatio * 400.0
                                    )
                    );
        }


        if (provider.fxMarkupPercentage > 0) {

            fxScore =
                    Math.max(
                            0,
                            25.0 -
                                    Math.min(
                                            25.0,
                                            provider.fxMarkupPercentage * 5.0
                                    )
                    );
        }


        double additionalCharges =
                provider.intermediaryCharge +
                        provider.receivingCharge;


        if (sendAmount > 0) {

            chargeScore =
                    Math.max(
                            0,
                            15.0 -
                                    Math.min(
                                            15.0,
                                            (additionalCharges /
                                                    sendAmount)
                                                    * 150.0
                                    )
                    );
        }


        if (provider.communityReportCount > 0) {

            double shortfallRatio =
                    provider.communityDifference /
                            Math.max(
                                    1.0,
                                    provider.receive
                            );

            communityScore =
                    shortfallRatio > 0.03
                            ? 4.0
                            : 7.0;
        }


        return (int) Math.round(
                Math.max(
                        0,
                        Math.min(
                                100,
                                costScore +
                                        fxScore +
                                        chargeScore +
                                        speedScore +
                                        communityScore
                        )
                )
        );
    }


    // ============================================================
    // PROMO TRAP DETECTOR
    // ============================================================

    private void applyPrototypePromoScenario(
            Provider provider
    ) {

        /*
         * Prototype-only promotional model.
         * These values are NOT live provider offers and do not
         * change the ranking. Replace them later with verified
         * provider/API data.
         */

        String providerName =
                provider.name == null
                        ? ""
                        : provider.name.toLowerCase();


        double discount = 0;


        if (providerName.contains("wise")) {

            discount = 0.50;

        } else if (
                providerName.contains("remitly")
        ) {

            discount = 1.00;

        } else if (
                providerName.contains("western union")
        ) {

            discount = 0.50;
        }


        if (discount <= 0 ||
                provider.baseFee <= 0) {

            provider.promoAvailable = false;

            provider.regularFee =
                    provider.baseFee;

            provider.promoFee =
                    provider.baseFee;

            provider.promoSavings = 0;

            return;
        }


        provider.promoAvailable = true;

        provider.regularFee =
                provider.baseFee;

        provider.promoFee =
                provider.baseFee *
                        (1.0 - discount);

        provider.promoSavings =
                provider.regularFee -
                        provider.promoFee;
    }


    private CharSequence generatePromoMessage(
            Provider provider
    ) {

        if (!provider.promoAvailable) {

            return "PROMO CHECK\n"
                    +
                    "No promotional pricing is configured for this provider in the current prototype.";
        }


        double savingPercentage =
                provider.regularFee > 0
                        ? (
                        provider.promoSavings /
                                provider.regularFee
                ) * 100.0
                        : 0;


        double promoTotalCost =
                Math.max(
                        0,
                        provider.totalCost -
                                provider.promoSavings
                );


        double bestAlternativeCost =
                Double.MAX_VALUE;


        for (Provider other :
                currentProviders) {

            if (other == provider) {

                continue;
            }


            bestAlternativeCost =
                    Math.min(
                            bestAlternativeCost,
                            other.totalCost
                    );
        }


        String verdict;

        String explanation;


        if (provider.promoSavings <= 0) {

            verdict =
                    "NO CLEAR PROMO BENEFIT";

            explanation =
                    "The promotional price does not create a meaningful saving for this transfer.";

        } else if (
                savingPercentage < 10.0
        ) {

            verdict =
                    "SMALL PROMO BENEFIT";

            explanation =
                    "You save only "
                            +
                            decimalFormat.format(
                                    provider.promoSavings
                            )
                            +
                            " "
                            +
                            fromCurrency
                            +
                            ". The difference is small, so compare the final amount received.";

        } else if (
                bestAlternativeCost !=
                        Double.MAX_VALUE
                        &&
                        promoTotalCost <=
                                bestAlternativeCost
        ) {

            verdict =
                    "GOOD FOR YOUR FIRST TRANSFER";

            explanation =
                    "The promotion makes this option competitive for this transfer. "
                            +
                            "It may become less attractive when the promotion ends.";

        } else if (
                bestAlternativeCost !=
                        Double.MAX_VALUE
                        &&
                        provider.totalCost >
                                bestAlternativeCost
        ) {

            verdict =
                    "PROMO HELPS, BUT ANOTHER OPTION MAY BE BETTER";

            explanation =
                    "The discount lowers the fee, but another option still has a lower "
                            +
                            "estimated overall cost. Check the amount the recipient receives too.";

        } else {

            verdict =
                    "GOOD NOW, LESS ATTRACTIVE LATER";

            explanation =
                    "You save on the first transfer, but the regular estimated cost is higher "
                            +
                            "after the promotion ends.";
        }


        String fullText =
                "PROMO CHECK\n"
                        +
                        verdict
                        +
                        "\n\n"
                        +
                        "FIRST TRANSFER\n"
                        +
                        fromCurrency
                        +
                        " "
                        +
                        decimalFormat.format(
                                provider.promoFee
                        )
                        +
                        " fee\n\n"
                        +
                        "REGULAR ESTIMATE\n"
                        +
                        fromCurrency
                        +
                        " "
                        +
                        decimalFormat.format(
                                provider.regularFee
                        )
                        +
                        " fee\n\n"
                        +
                        "YOU SAVE\n"
                        +
                        fromCurrency
                        +
                        " "
                        +
                        decimalFormat.format(
                                provider.promoSavings
                        )
                        +
                        " ("
                        +
                        decimalFormat.format(
                                savingPercentage
                        )
                        +
                        "%)\n\n"
                        +
                        "WHAT THIS MEANS FOR YOU\n"
                        +
                        explanation
                        +
                        "\n\n"
                        +
                        "Promotional scenario only. Verify the current provider offer before sending.";


        SpannableStringBuilder styled =
                new SpannableStringBuilder(
                        fullText
                );


        styleLine(
                styled,
                fullText,
                "PROMO CHECK",
                true,
                0xFFB8B6FF
        );


        styleLine(
                styled,
                fullText,
                verdict,
                true,
                0xFFFFFFFF
        );


        styleLine(
                styled,
                fullText,
                "FIRST TRANSFER",
                true,
                0xFFB8B6FF
        );


        styleLine(
                styled,
                fullText,
                "REGULAR ESTIMATE",
                true,
                0xFFB8B6FF
        );


        styleLine(
                styled,
                fullText,
                "YOU SAVE",
                true,
                0xFFB8B6FF
        );


        styleLine(
                styled,
                fullText,
                "WHAT THIS MEANS FOR YOU",
                true,
                0xFFB8B6FF
        );


        int noteStart =
                fullText.lastIndexOf(
                        "Promotional scenario only."
                );


        if (noteStart >= 0) {

            styled.setSpan(
                    new ForegroundColorSpan(
                            0xFF8D99B5
                    ),
                    noteStart,
                    fullText.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }


        return styled;
    }


    private void styleLine(
            SpannableStringBuilder text,
            String fullText,
            String line,
            boolean bold,
            int color
    ) {

        int start =
                fullText.indexOf(line);


        if (start < 0) {

            return;
        }


        int end =
                start + line.length();


        if (bold) {

            text.setSpan(
                    new StyleSpan(
                            Typeface.BOLD
                    ),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }


        text.setSpan(
                new ForegroundColorSpan(
                        color
                ),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }


    // ============================================================
    // HIDDEN FEE WARNING SYSTEM
    // ============================================================

    private String generateHiddenFeeWarning(
            Provider provider
    ) {

        List<String> warnings =
                new ArrayList<>();


        if (provider.receivingCharge > 0) {

            warnings.add(
                    "Receiving-side charges may reduce the final amount."
            );
        }


        if (provider.intermediaryCharge > 0) {

            warnings.add(
                    "Intermediary bank charges are included in the estimate."
            );
        }


        if (provider.fxMarkupPercentage > 0) {

            warnings.add(
                    "The provider's FX markup affects the amount received."
            );
        }


        if (
                provider.communityReportCount > 0
                        &&
                        (
                                provider.communityDifference > 0
                                        ||
                                        provider.communityExtraFee > 0
                        )
        ) {

            warnings.add(
                    "Community reports indicate that actual received amounts may differ from the initial estimate."
            );
        }


        if (warnings.isEmpty()) {

            return "No additional charge signal detected from the available data.";
        }


        StringBuilder result =
                new StringBuilder(
                        "CHECK BEFORE SENDING\n"
                );


        for (int i = 0;
             i < warnings.size();
             i++) {

            if (i > 0) {

                result.append("\n");
            }

            result.append(
                    warnings.get(i)
            );
        }


        return result.toString();
    }


    // ============================================================
    // LOAD COMMUNITY DATA
    // ============================================================

    private void loadCommunityData() {

        if (fromCountry.isEmpty()
                || toCountry.isEmpty()) {

            setCommunityUnavailable();

            return;
        }


        communityDataLoaded = false;


        db.collection(
                        "actual_promised_reports"
                )
                .whereEqualTo(
                        "fromCountry",
                        fromCountry
                )
                .whereEqualTo(
                        "toCountry",
                        toCountry
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            communityStatsMap.clear();


                            for (
                                    QueryDocumentSnapshot document :
                                    querySnapshot
                            ) {

                                String provider =
                                        document.getString(
                                                "provider"
                                        );


                                if (provider == null
                                        || provider.trim().isEmpty()) {

                                    continue;
                                }


                                provider =
                                        normalizeProviderName(
                                                provider
                                        );


                                Double differenceValue =
                                        getDouble(
                                                document,
                                                "difference"
                                        );


                                Double extraFeeValue =
                                        getDouble(
                                                document,
                                                "extraFee"
                                        );


                                double difference =
                                        differenceValue != null
                                                ? differenceValue
                                                : 0.0;


                                double extraFee =
                                        extraFeeValue != null
                                                ? extraFeeValue
                                                : 0.0;


                                CommunityStats stats =
                                        communityStatsMap.get(
                                                provider
                                        );


                                if (stats == null) {

                                    stats =
                                            new CommunityStats();

                                    communityStatsMap.put(
                                            provider,
                                            stats
                                    );
                                }


                                stats.addReport(
                                        difference,
                                        extraFee
                                );
                            }


                            for (
                                    CommunityStats stats :
                                    communityStatsMap.values()
                            ) {

                                stats.calculateAverage();
                            }


                            communityDataLoaded = true;


                            refreshResultsWithCommunityData();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Log.e(
                                    TAG,
                                    "Community data loading failed",
                                    e
                            );

                            communityDataLoaded = false;

                            setCommunityUnavailable();
                        }
                );
    }


    // ============================================================
    // REFRESH RESULTS AFTER COMMUNITY DATA
    // ============================================================

    private void refreshResultsWithCommunityData() {

        if (sendAmount <= 0) {

            return;
        }


        double rate;

        if (referenceRate != null &&
                referenceRate > 0) {

            rate = referenceRate;

        } else {

            rate =
                    getFallbackRate(
                            fromCurrency,
                            toCurrency
                    );
        }


        if (rate <= 0) {

            return;
        }


        String corridorKey =
                createCorridorKey(
                        fromCountry,
                        toCountry
                );


        List<Provider> providers =
                createProvidersForCorridor(
                        corridorKey,
                        rate
                );


        if (providers.size() < 3) {

            return;
        }


        applyCommunityAdjustments(
                providers,
                rate
        );


        calculateBalancedScores(
                providers
        );


        sortProviders(
                providers
        );


        currentProviders.clear();

        currentProviders.addAll(
                providers
        );


        displayPriorityInformation(
                providers.get(0)
        );


        displaySummary(
                providers.get(0)
        );


        displayProvider(
                providers.get(0),
                1
        );


        displayProvider(
                providers.get(1),
                2
        );


        displayProvider(
                providers.get(2),
                3
        );


        applyCommunityData();


        Log.d(
                TAG,
                "Results refreshed using community data."
        );
    }


    // ============================================================
    // GET FIRESTORE NUMBER
    // ============================================================

    private Double getDouble(
            QueryDocumentSnapshot document,
            String field
    ) {

        Object value =
                document.get(field);


        if (value instanceof Number) {

            return ((Number) value).doubleValue();
        }


        if (value instanceof String) {

            try {

                return Double.parseDouble(
                        (String) value
                );

            } catch (Exception ignored) {
            }
        }


        return null;
    }


    // ============================================================
    // NORMALIZE PROVIDER
    // ============================================================

    private String normalizeProviderName(
            String provider
    ) {

        if (provider == null) {

            return "";
        }


        String normalized =
                provider
                        .trim()
                        .toLowerCase()
                        .replace("-", " ")
                        .replace("_", " ");


        switch (normalized) {

            case "wise":
            case "wise transfer":
            case "wise money transfer":
                return "Wise";

            case "remitly":
            case "remitly transfer":
                return "Remitly";

            case "western union":
            case "westernunion":
            case "western union transfer":
                return "Western Union";

            case "instarem":
            case "instarem transfer":
                return "Instarem";

            case "moneygram":
            case "money gram":
                return "MoneyGram";

            case "worldremit":
            case "world remit":
                return "WorldRemit";

            case "xoom":
            case "xoom transfer":
                return "Xoom";

            case "ria":
            case "ria money transfer":
                return "Ria";

            default:
                return provider.trim();
        }
    }


    // ============================================================
    // APPLY COMMUNITY DATA TO CARDS
    // ============================================================

    private void applyCommunityData() {

        if (!communityDataLoaded) {

            return;
        }


        if (currentProviders.size() < 3) {

            return;
        }


        setCommunityText(
                tvCommunity1,
                currentProviders.get(0)
        );


        setCommunityText(
                tvCommunity2,
                currentProviders.get(1)
        );


        setCommunityText(
                tvCommunity3,
                currentProviders.get(2)
        );
    }


    // ============================================================
    // COMMUNITY TEXT
    // ============================================================

    private void setCommunityText(
            TextView textView,
            Provider provider
    ) {

        if (textView == null
                || provider == null) {

            return;
        }


        String normalizedProvider =
                normalizeProviderName(
                        provider.name
                );


        CommunityStats stats =
                communityStatsMap.get(
                        normalizedProvider
                );


        if (stats == null
                || stats.reportCount == 0) {

            textView.setText(
                    "No community data yet"
            );

            return;
        }


        textView.setText(
                stats.reportCount
                        +
                        (
                                stats.reportCount == 1
                                        ? " report"
                                        : " reports"
                        )
                        +
                        "\nAverage reported difference: "
                        +
                        toCurrency
                        +
                        " "
                        +
                        decimalFormat.format(
                                stats.averageDifference
                        )
                        +
                        "\nAverage extra fee: "
                        +
                        toCurrency
                        +
                        " "
                        +
                        decimalFormat.format(
                                stats.averageExtraFee
                        )
                        +
                        "\nUsed in comparison"
        );
    }


    // ============================================================
    // COMMUNITY UNAVAILABLE
    // ============================================================

    private void setCommunityUnavailable() {

        if (tvCommunity1 != null) {

            tvCommunity1.setText(
                    "Community data unavailable"
            );
        }


        if (tvCommunity2 != null) {

            tvCommunity2.setText(
                    "Community data unavailable"
            );
        }


        if (tvCommunity3 != null) {

            tvCommunity3.setText(
                    "Community data unavailable"
            );
        }
    }


    // ============================================================
    // FETCH REFERENCE RATE
    // ============================================================

    private void fetchReferenceRate() {

        if (fromCurrency.isEmpty()
                || toCurrency.isEmpty()) {

            return;
        }


        if (fromCurrency.equalsIgnoreCase(
                toCurrency
        )) {

            referenceRate = 1.0;


            runOnUiThread(() -> {

                updateReferenceRateUI();

                calculateResults();
            });


            return;
        }


        executorService.execute(() -> {

            HttpURLConnection connection = null;


            try {

                String apiUrl =
                        "https://api.frankfurter.dev/v2/rate/"
                                +
                                fromCurrency
                                +
                                "/"
                                +
                                toCurrency;


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
                                        connection.getInputStream()
                                )
                        );


                StringBuilder response =
                        new StringBuilder();


                String line;


                while (
                        (line = reader.readLine())
                                != null
                ) {

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


                if (rate <= 0) {

                    throw new Exception(
                            "Invalid exchange rate"
                    );
                }


                referenceRate = rate;


                runOnUiThread(() -> {

                    updateReferenceRateUI();

                    calculateResults();
                });


            } catch (Exception e) {

                Log.e(
                        TAG,
                        "Exchange rate error",
                        e
                );


                referenceRate =
                        getFallbackRate(
                                fromCurrency,
                                toCurrency
                        );


                runOnUiThread(() -> {

                    updateReferenceRateUI();

                    calculateResults();
                });


            } finally {

                if (connection != null) {

                    connection.disconnect();
                }
            }
        });
    }


    // ============================================================
    // UPDATE RATE
    // ============================================================

    private void updateReferenceRateUI() {

        if (referenceRate == null
                || referenceRate <= 0) {

            tvReferenceRate.setText(
                    "Reference rate unavailable"
            );

            return;
        }


        tvReferenceRate.setText(
                "1 "
                        +
                        fromCurrency
                        +
                        " = "
                        +
                        decimalFormat.format(
                                referenceRate
                        )
                        +
                        " "
                        +
                        toCurrency
        );
    }


    // ============================================================
    // FALLBACK RATE
    // ============================================================

    private double getFallbackRate(
            String from,
            String to
    ) {

        String pair =
                from.toUpperCase()
                        +
                        "/"
                        +
                        to.toUpperCase();


        switch (pair) {

            case "USD/INR":
                return 83.00;

            case "INR/USD":
                return 1.0 / 83.00;

            case "EUR/INR":
                return 90.00;

            case "GBP/INR":
                return 105.00;

            case "AED/INR":
                return 22.60;

            case "CAD/INR":
                return 61.00;

            case "AUD/INR":
                return 54.00;

            case "SGD/INR":
                return 65.00;

            case "INR/SGD":
                return 1.0 / 65.00;

            default:
                return 0;
        }
    }


    // ============================================================
    // DESTROY
    // ============================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executorService.shutdownNow();
    }
}
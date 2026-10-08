package com.example.fxwise;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fxwise.data.CorridorData;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WhatIfActivity extends AppCompatActivity {

    private TextView tvRoute;
    private TextView tvCurrencyRoute;
    private TextView tvAmountLabel;

    private TextView tvVerdict;
    private TextView tvReason;
    private TextView tvReceive;
    private TextView tvCost;
    private TextView tvSpeed;
    private TextView tvDecisionInsight;

    private SeekBar seekAmount;
    private RadioGroup priorityGroup;

    private String fromCountry = "United Arab Emirates";
    private String toCountry = "India";

    private String fromCurrency = "AED";
    private String toCurrency = "INR";

    private double currentAmount = 1000;

    private String priority = "Balanced";

    private Double referenceRate = null;

    private final DecimalFormat df =
            new DecimalFormat("#,##0.00");

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();


    private static class ScenarioProvider {

        String name;
        String speed;
        String receivingMethod;

        double fee;
        double receive;
        double totalCost;

        int speedRank;
        double score;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_what_if);

        readIntent();

        bindViews();

        setupControls();

        fetchRateAndSimulate();
    }


    private void readIntent() {

        if (getIntent() == null) {
            return;
        }


        String receivedFromCountry =
                getIntent().getStringExtra("fromCountry");

        String receivedToCountry =
                getIntent().getStringExtra("toCountry");

        String receivedFromCurrency =
                getIntent().getStringExtra("fromCurrency");

        String receivedToCurrency =
                getIntent().getStringExtra("toCurrency");

        double receivedAmount =
                getIntent().getDoubleExtra(
                        "amount",
                        1000
                );

        String receivedPriority =
                getIntent().getStringExtra("priority");


        if (receivedFromCountry != null
                && !receivedFromCountry.trim().isEmpty()) {

            fromCountry = receivedFromCountry;
        }


        if (receivedToCountry != null
                && !receivedToCountry.trim().isEmpty()) {

            toCountry = receivedToCountry;
        }


        if (receivedFromCurrency != null
                && !receivedFromCurrency.trim().isEmpty()) {

            fromCurrency = receivedFromCurrency;
        }


        if (receivedToCurrency != null
                && !receivedToCurrency.trim().isEmpty()) {

            toCurrency = receivedToCurrency;
        }


        if (receivedAmount > 0) {

            currentAmount = receivedAmount;
        }


        if (receivedPriority != null
                && !receivedPriority.trim().isEmpty()) {

            priority = receivedPriority;
        }
    }


    private void bindViews() {

        Button back =
                findViewById(R.id.btnBackWhatIf);

        tvRoute =
                findViewById(R.id.tvWhatIfRoute);

        tvCurrencyRoute =
                findViewById(R.id.tvWhatIfCurrencyRoute);

        tvAmountLabel =
                findViewById(R.id.tvWhatIfAmountLabel);

        tvVerdict =
                findViewById(R.id.tvScenarioVerdict);

        tvReason =
                findViewById(R.id.tvScenarioReason);

        tvReceive =
                findViewById(R.id.tvScenarioReceive);

        tvCost =
                findViewById(R.id.tvScenarioCost);

        tvSpeed =
                findViewById(R.id.tvScenarioSpeed);

        tvDecisionInsight =
                findViewById(R.id.tvDecisionInsight);

        seekAmount =
                findViewById(R.id.seekAmount);

        priorityGroup =
                findViewById(R.id.priorityGroup);


        tvRoute.setText(
                fromCountry + " to " + toCountry
        );


        tvCurrencyRoute.setText(
                fromCurrency + " to " + toCurrency
        );


        back.setOnClickListener(
                v -> finish()
        );
    }


    private void setupControls() {

        int initialProgress =
                (int) Math.round(
                        currentAmount - 100
                );


        initialProgress =
                Math.max(
                        0,
                        Math.min(
                                4500,
                                initialProgress
                        )
                );


        seekAmount.setProgress(
                initialProgress
        );


        /*
         * Select the priority received
         * from the Results page.
         */

        if (priority.equalsIgnoreCase(
                "Lowest overall cost")) {

            priorityGroup.check(
                    R.id.rbLowestCost
            );

        } else if (
                priority.equalsIgnoreCase(
                        "Fastest")) {

            priorityGroup.check(
                    R.id.rbFastest
            );

        } else if (
                priority.equalsIgnoreCase(
                        "Most money received")) {

            priorityGroup.check(
                    R.id.rbMostReceived
            );

        } else {

            priority =
                    "Balanced";

            priorityGroup.check(
                    R.id.rbBalanced
            );
        }


        /*
         * Amount slider
         */

        seekAmount.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser) {

                        currentAmount =
                                100 + progress;

                        updateAmountLabel();

                        if (fromUser) {

                            fetchRateAndSimulate();
                        }
                    }


                    @Override
                    public void onStartTrackingTouch(
                            SeekBar seekBar) {
                    }


                    @Override
                    public void onStopTrackingTouch(
                            SeekBar seekBar) {
                    }
                }
        );


        /*
         * Priority selection
         *
         * RadioGroup guarantees that
         * only ONE RadioButton is selected.
         */

        priorityGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    if (checkedId ==
                            R.id.rbLowestCost) {

                        priority =
                                "Lowest overall cost";

                    } else if (
                            checkedId ==
                                    R.id.rbFastest) {

                        priority =
                                "Fastest";

                    } else if (
                            checkedId ==
                                    R.id.rbMostReceived) {

                        priority =
                                "Most money received";

                    } else if (
                            checkedId ==
                                    R.id.rbBalanced) {

                        priority =
                                "Balanced";
                    }


                    fetchRateAndSimulate();
                }
        );


        updateAmountLabel();
    }


    private void updateAmountLabel() {

        tvAmountLabel.setText(
                fromCurrency
                        + " "
                        + df.format(currentAmount)
        );
    }


    private void fetchRateAndSimulate() {

        if (fromCurrency.equalsIgnoreCase(
                toCurrency)) {

            referenceRate = 1.0;

            runOnUiThread(
                    this::simulate
            );

            return;
        }


        executor.execute(() -> {

            Double rate =
                    fetchReferenceRate();


            if (rate == null) {

                rate =
                        fallbackRate();
            }


            referenceRate = rate;


            runOnUiThread(
                    this::simulate
            );
        });
    }


    private Double fetchReferenceRate() {

        HttpURLConnection connection =
                null;

        try {

            URL url =
                    new URL(
                            "https://api.frankfurter.dev/v2/rate/"
                                    + fromCurrency
                                    + "/"
                                    + toCurrency
                    );


            connection =
                    (HttpURLConnection)
                            url.openConnection();


            connection.setRequestMethod(
                    "GET"
            );

            connection.setConnectTimeout(
                    5000
            );

            connection.setReadTimeout(
                    5000
            );


            if (connection.getResponseCode()
                    != HttpURLConnection.HTTP_OK) {

                return null;
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


            if (json.has("rate")) {

                return json.getDouble(
                        "rate"
                );
            }


        } catch (Exception ignored) {

        } finally {

            if (connection != null) {

                connection.disconnect();
            }
        }


        return null;
    }


    private double fallbackRate() {

        if (fromCurrency.equals("AED")
                && toCurrency.equals("INR")) {

            return 22.60;
        }


        if (fromCurrency.equals("USD")
                && toCurrency.equals("INR")) {

            return 83.0;
        }


        if (fromCurrency.equals("EUR")
                && toCurrency.equals("INR")) {

            return 90.0;
        }


        if (fromCurrency.equals("GBP")
                && toCurrency.equals("INR")) {

            return 105.0;
        }


        if (fromCurrency.equals("CAD")
                && toCurrency.equals("INR")) {

            return 61.0;
        }


        if (fromCurrency.equals("AUD")
                && toCurrency.equals("INR")) {

            return 54.0;
        }


        if (fromCurrency.equals("SGD")
                && toCurrency.equals("INR")) {

            return 65.0;
        }


        if (fromCurrency.equals("INR")
                && toCurrency.equals("AED")) {

            return 1.0 / 22.60;
        }


        if (fromCurrency.equals("INR")
                && toCurrency.equals("USD")) {

            return 1.0 / 83.0;
        }


        return 1.0;
    }


    private void simulate() {

        if (referenceRate == null
                || referenceRate <= 0) {

            return;
        }


        List<ScenarioProvider> providers =
                new ArrayList<>();


        List<CorridorData.ProviderProfile> profiles =
                CorridorData.getProviders(
                        fromCountry,
                        toCountry
                );


        if (profiles == null
                || profiles.isEmpty()) {

            tvVerdict.setText(
                    "No provider scenario available"
            );


            tvReason.setText(
                    "Add corridor provider data to simulate this route."
            );


            tvReceive.setText(
                    toCurrency + " 0.00"
            );


            tvCost.setText(
                    fromCurrency + " 0.00"
            );


            tvSpeed.setText(
                    "Unavailable"
            );


            tvDecisionInsight.setText(
                    "FXWise does not currently have provider data for this corridor."
            );


            return;
        }


        for (
                CorridorData.ProviderProfile profile :
                profiles
        ) {

            ScenarioProvider p =
                    new ScenarioProvider();


            p.name =
                    profile.name;


            p.speed =
                    profile.speed;


            p.receivingMethod =
                    profile.receivingMethod;


            p.fee =
                    currentAmount
                            * profile.feePercentage
                            / 100.0;


            double afterFee =
                    currentAmount - p.fee;


            double providerRate =
                    referenceRate
                            * (
                            1
                                    - profile.fxMarkupPercentage
                                    / 100.0
                    );


            double beforeCharges =
                    afterFee * providerRate;


            p.receive =
                    Math.max(
                            0,
                            beforeCharges
                                    - profile.intermediaryCharge
                                    - profile.receivingCharge
                    );


            double fxImpactSending =
                    Math.max(
                            0,
                            afterFee * referenceRate
                                    - beforeCharges
                    ) / referenceRate;


            p.totalCost =
                    p.fee
                            + fxImpactSending
                            + profile.intermediaryCharge
                            + profile.receivingCharge;


            p.speedRank =
                    getSpeedRank(
                            profile.speed
                    );


            providers.add(p);
        }


        calculateBalancedScores(
                providers
        );


        sortProviders(
                providers
        );


        ScenarioProvider best =
                providers.get(0);


        display(best);
    }


    private int getSpeedRank(
            String speed) {

        if (speed == null) {

            return 2;
        }


        String s =
                speed.toLowerCase();


        if (s.contains("minutes")) {

            return 0;
        }


        if (s.contains("1 day")) {

            return 1;
        }


        return 2;
    }


    private void calculateBalancedScores(
            List<ScenarioProvider> providers) {

        double maxReceive = 0;

        double minCost =
                Double.MAX_VALUE;


        for (ScenarioProvider p :
                providers) {

            maxReceive =
                    Math.max(
                            maxReceive,
                            p.receive
                    );


            minCost =
                    Math.min(
                            minCost,
                            p.totalCost
                    );
        }


        for (ScenarioProvider p :
                providers) {

            double receiveScore =
                    maxReceive > 0
                            ? p.receive
                            / maxReceive
                            : 0;


            double costScore =
                    p.totalCost > 0
                            ? minCost
                            / p.totalCost
                            : 0;


            double speedScore =
                    p.speedRank == 0
                            ? 1.0
                            : (
                            p.speedRank == 1
                                    ? 0.8
                                    : 0.6
                    );


            p.score =
                    receiveScore * 0.50
                            + costScore * 0.30
                            + speedScore * 0.20;
        }
    }


    private void sortProviders(
            List<ScenarioProvider> providers) {

        if (priority.equalsIgnoreCase(
                "Fastest")) {

            Collections.sort(
                    providers,
                    Comparator.comparingInt(
                            p -> p.speedRank
                    )
            );


        } else if (
                priority.equalsIgnoreCase(
                        "Lowest overall cost")) {

            Collections.sort(
                    providers,
                    Comparator.comparingDouble(
                            p -> p.totalCost
                    )
            );


        } else if (
                priority.equalsIgnoreCase(
                        "Most money received")) {

            Collections.sort(
                    providers,
                    (a, b) ->
                            Double.compare(
                                    b.receive,
                                    a.receive
                            )
            );


        } else {

            Collections.sort(
                    providers,
                    (a, b) ->
                            Double.compare(
                                    b.score,
                                    a.score
                            )
            );
        }
    }


    private void display(
            ScenarioProvider best) {

        tvVerdict.setText(
                best.name
        );


        String reason;


        if (priority.equalsIgnoreCase(
                "Fastest")) {

            reason =
                    "This scenario prioritizes transfer speed. "
                            + best.name
                            + " has the strongest estimated speed ranking.";


        } else if (
                priority.equalsIgnoreCase(
                        "Lowest overall cost")) {

            reason =
                    "This scenario prioritizes total known cost. "
                            + best.name
                            + " has the lowest estimated cost.";


        } else if (
                priority.equalsIgnoreCase(
                        "Most money received")) {

            reason =
                    "This scenario prioritizes the recipient amount. "
                            + best.name
                            + " gives the highest estimated amount received.";


        } else {

            reason =
                    "This scenario balances recipient amount, "
                            + "total known cost and estimated transfer speed.";
        }


        tvReason.setText(reason);


        tvReceive.setText(
                toCurrency
                        + " "
                        + df.format(
                        best.receive
                )
        );


        tvCost.setText(
                fromCurrency
                        + " "
                        + df.format(
                        best.totalCost
                )
        );


        tvSpeed.setText(
                best.speed
                        + " | "
                        + best.receivingMethod
        );


        updateDecisionInsight(best);
    }


    private void updateDecisionInsight(
            ScenarioProvider best) {

        String insight;


        if (priority.equalsIgnoreCase(
                "Fastest")) {

            insight =
                    "You chose speed as your priority. "
                            + best.name
                            + " is ranked highest for the estimated delivery time in this scenario.";


        } else if (
                priority.equalsIgnoreCase(
                        "Lowest overall cost")) {

            insight =
                    "You chose cost as your priority. "
                            + best.name
                            + " currently gives the lowest estimated total cost for this transfer amount.";


        } else if (
                priority.equalsIgnoreCase(
                        "Most money received")) {

            insight =
                    "You chose recipient value as your priority. "
                            + best.name
                            + " currently gives the highest estimated amount to the recipient.";


        } else {

            insight =
                    "For this amount, FXWise selected "
                            + best.name
                            + " because it provides the strongest balance between recipient amount, total cost and speed.";
        }


        tvDecisionInsight.setText(
                insight
        );
    }


    @Override
    protected void onDestroy() {

        executor.shutdownNow();

        super.onDestroy();
    }
}
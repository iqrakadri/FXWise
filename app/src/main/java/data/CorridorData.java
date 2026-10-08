package com.example.fxwise.data;

import java.util.ArrayList;
import java.util.List;

public class CorridorData {

    public static class ProviderProfile {

        public String name;
        public String speed;
        public String receivingMethod;
        public double feePercentage;
        public double fxMarkupPercentage;
        public double intermediaryCharge;
        public double receivingCharge;
        public int speedRank;

        public ProviderProfile(
                String name,
                String speed,
                String receivingMethod,
                double feePercentage,
                double fxMarkupPercentage,
                double intermediaryCharge,
                double receivingCharge,
                int speedRank
        ) {
            this.name = name;
            this.speed = speed;
            this.receivingMethod = receivingMethod;
            this.feePercentage = feePercentage;
            this.fxMarkupPercentage = fxMarkupPercentage;
            this.intermediaryCharge = intermediaryCharge;
            this.receivingCharge = receivingCharge;
            this.speedRank = speedRank;
        }
    }

    public static List<ProviderProfile> getProviders(
            String fromCountry,
            String toCountry
    ) {

        List<ProviderProfile> providers = new ArrayList<>();

        String corridor =
                fromCountry.trim().toLowerCase()
                        + "->"
                        + toCountry.trim().toLowerCase();

        switch (corridor) {

            case "united states->india":

                providers.add(new ProviderProfile(
                        "Wise",
                        "1–2 days",
                        "Bank transfer",
                        1.0,
                        1.2,
                        0,
                        0,
                        2
                ));

                providers.add(new ProviderProfile(
                        "Remitly",
                        "Minutes",
                        "Bank transfer / Cash pickup",
                        0.6,
                        1.8,
                        0,
                        0,
                        0
                ));

                providers.add(new ProviderProfile(
                        "Western Union",
                        "1 day",
                        "Bank transfer / Cash pickup",
                        0.0,
                        2.8,
                        0,
                        0,
                        1
                ));

                break;

            case "united kingdom->india":

                providers.add(new ProviderProfile(
                        "Wise",
                        "1–2 days",
                        "Bank transfer",
                        0.8,
                        1.1,
                        0,
                        0,
                        2
                ));

                providers.add(new ProviderProfile(
                        "Remitly",
                        "Minutes",
                        "Bank transfer / Cash pickup",
                        0.5,
                        1.7,
                        0,
                        0,
                        0
                ));

                providers.add(new ProviderProfile(
                        "Western Union",
                        "1 day",
                        "Bank transfer / Cash pickup",
                        0.0,
                        2.6,
                        0,
                        0,
                        1
                ));

                break;

            case "united arab emirates->india":

                providers.add(new ProviderProfile(
                        "Wise",
                        "1–2 days",
                        "Bank transfer",
                        0.8,
                        1.0,
                        0,
                        0,
                        2
                ));

                providers.add(new ProviderProfile(
                        "Remitly",
                        "Minutes",
                        "Bank transfer / Cash pickup",
                        0.5,
                        1.6,
                        0,
                        0,
                        0
                ));

                providers.add(new ProviderProfile(
                        "Western Union",
                        "1 day",
                        "Bank transfer / Cash pickup",
                        0.0,
                        2.5,
                        0,
                        0,
                        1
                ));

                break;

            case "canada->india":

                providers.add(new ProviderProfile(
                        "Wise",
                        "1–2 days",
                        "Bank transfer",
                        0.9,
                        1.1,
                        0,
                        0,
                        2
                ));

                providers.add(new ProviderProfile(
                        "Remitly",
                        "Minutes",
                        "Bank transfer / Cash pickup",
                        0.6,
                        1.7,
                        0,
                        0,
                        0
                ));

                providers.add(new ProviderProfile(
                        "Western Union",
                        "1 day",
                        "Bank transfer / Cash pickup",
                        0.0,
                        2.7,
                        0,
                        0,
                        1
                ));

                break;

            case "singapore->india":

                providers.add(new ProviderProfile(
                        "Wise",
                        "1–2 days",
                        "Bank transfer",
                        0.8,
                        1.0,
                        0,
                        0,
                        2
                ));

                providers.add(new ProviderProfile(
                        "Remitly",
                        "Minutes",
                        "Bank transfer / Cash pickup",
                        0.5,
                        1.5,
                        0,
                        0,
                        0
                ));

                providers.add(new ProviderProfile(
                        "Western Union",
                        "1 day",
                        "Bank transfer / Cash pickup",
                        0.0,
                        2.4,
                        0,
                        0,
                        1
                ));

                break;

            case "australia->india":

                providers.add(new ProviderProfile(
                        "Wise",
                        "1–2 days",
                        "Bank transfer",
                        0.8,
                        1.0,
                        0,
                        0,
                        2
                ));

                providers.add(new ProviderProfile(
                        "Remitly",
                        "Minutes",
                        "Bank transfer / Cash pickup",
                        0.5,
                        1.6,
                        0,
                        0,
                        0
                ));

                providers.add(new ProviderProfile(
                        "Western Union",
                        "1 day",
                        "Bank transfer / Cash pickup",
                        0.0,
                        2.6,
                        0,
                        0,
                        1
                ));

                break;

            default:

                // Generic prototype estimates for unsupported corridors

                providers.add(new ProviderProfile(
                        "Wise",
                        "1–2 days",
                        "Bank transfer",
                        1.0,
                        1.2,
                        0,
                        0,
                        2
                ));

                providers.add(new ProviderProfile(
                        "Remitly",
                        "Minutes",
                        "Bank transfer / Cash pickup",
                        0.6,
                        1.8,
                        0,
                        0,
                        0
                ));

                providers.add(new ProviderProfile(
                        "Western Union",
                        "1 day",
                        "Bank transfer / Cash pickup",
                        0.0,
                        2.8,
                        0,
                        0,
                        1
                ));

                break;
        }

        return providers;
    }
}
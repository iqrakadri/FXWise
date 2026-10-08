package com.example.fxwise;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Compare Transfers
        TextView compareButton = findViewById(R.id.btnCompare);

        // Actual vs Promised
        Button actualPromisedButton =
                findViewById(R.id.btnActualPromised);

        // Community Reports
        Button communityReportsButton =
                findViewById(R.id.btnCommunityReports);


        // Open Compare screen
        compareButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    CompareActivity.class
            );

            startActivity(intent);
        });


        // Open Actual vs Promised screen
        actualPromisedButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    ActualPromisedActivity.class
            );

            startActivity(intent);
        });


        // Open Community Reports screen
        communityReportsButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    CommunityReportsActivity.class
            );

            startActivity(intent);
        });
    }
}
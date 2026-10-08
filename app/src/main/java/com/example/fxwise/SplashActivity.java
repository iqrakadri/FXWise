package com.example.fxwise;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        View splashContent =
                findViewById(R.id.splashContent);

        // Initial animation state
        splashContent.setAlpha(0f);
        splashContent.setScaleX(0.92f);
        splashContent.setScaleY(0.92f);

        // Smooth entrance animation
        splashContent.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .start();

        // Open Home after splash
        new Handler(
                Looper.getMainLooper()
        ).postDelayed(() -> {

            Intent intent =
                    new Intent(
                            SplashActivity.this,
                            MainActivity.class
                    );

            startActivity(intent);

            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );

            finish();

        }, SPLASH_DURATION);
    }
}
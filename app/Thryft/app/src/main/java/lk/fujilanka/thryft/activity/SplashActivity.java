package lk.fujilanka.thryft.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import lk.fujilanka.thryft.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 5000;


    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            getWindow().setDecorFitsSystemWindows(false);

            WindowInsetsController controller = getWindow().getInsetsController();
            if(controller != null){
                controller.hide(WindowInsets.Type.systemBars() | WindowInsets.Type.navigationBars());
            }
        }else{
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        }

        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.splashProgressBar).setVisibility(View.VISIBLE);
                    }
                }, 1000);

        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.splashProgressBar).setVisibility(View.INVISIBLE);
                        navigateToNextScreen();
                        finish();
                    }
                }, 5000);



        ImageView logo = findViewById(R.id.splashLogo);
        TextView appName = findViewById(R.id.tv_app_name);
        TextView tagline = findViewById(R.id.tv_tagline);

        logo.setAlpha(0f);
        appName.setAlpha(0f);
        tagline.setAlpha(0f);

        // Animate after a delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(1000).start();
            appName.animate().alpha(1f).setStartDelay(300).setDuration(800).start();
            tagline.animate().alpha(1f).setStartDelay(400).setDuration(800).start();
        }, 200);

        // Navigate after delay

    }

    private void navigateToNextScreen() {

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void startAnimations() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.d("SplashActivity", "Starting animations...");

            ImageView logo = findViewById(R.id.splashLogo);
            if (logo != null) {
                Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.splash_logo_animation);
                logo.startAnimation(logoAnim);
                Log.d("SplashActivity", "Logo animation started");
            } else {
                Log.e("SplashActivity", "Logo view is NULL!");
            }

            TextView appName = findViewById(R.id.tv_app_name);
            TextView tagline = findViewById(R.id.tv_tagline);
            if (appName != null && tagline != null) {
                Animation textAnim = AnimationUtils.loadAnimation(this, R.anim.splash_text_animation);
                appName.startAnimation(textAnim);
                tagline.startAnimation(textAnim);
                Log.d("SplashActivity", "Text animations started");
            } else {
                Log.e("SplashActivity", "Text views are NULL!");
            }
        }, 100);
    }
}
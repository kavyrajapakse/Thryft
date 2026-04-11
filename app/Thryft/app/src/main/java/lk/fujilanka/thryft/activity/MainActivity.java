package lk.fujilanka.thryft.activity;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;

import lk.fujilanka.thryft.R;
import lk.fujilanka.thryft.fragment.AlertFragment;
import lk.fujilanka.thryft.fragment.CartFragment;
import lk.fujilanka.thryft.fragment.HomeFragment;
import lk.fujilanka.thryft.fragment.OrderFragment;
import lk.fujilanka.thryft.fragment.ProductDetailsFragment;
import lk.fujilanka.thryft.fragment.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import android.content.Intent;

import java.util.HashSet;
import java.util.Set;

import android.content.IntentFilter;
import lk.fujilanka.thryft.receiver.NetworkReceiver;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener{

    ConstraintLayout constraintLayout;
    MaterialToolbar toolbar;

    MaterialCardView searchBar;
    BottomNavigationView bottomNavigationView;

    EditText etSearch;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorEventListener;

    private float lastX, lastY, lastZ;
    private long lastUpdate;

    private NetworkReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        constraintLayout = findViewById(R.id.main);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        etSearch = findViewById(R.id.et_search);
        searchBar = findViewById(R.id.search_bar);

        setSupportActionBar(toolbar);

        searchBar.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);


        });

        if (savedInstanceState == null){
            loadFragment(new HomeFragment());
        }

        if (getIntent().hasExtra("openProduct")) {

            String productId = getIntent().getStringExtra("openProduct");

            Bundle bundle = new Bundle();
            bundle.putString("productId", productId);

            ProductDetailsFragment fragment = new ProductDetailsFragment();
            fragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorEventListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                long currentTime = System.currentTimeMillis();

                if ((currentTime - lastUpdate) > 200) {

                    long diffTime = currentTime - lastUpdate;
                    lastUpdate = currentTime;

                    float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

                    if (speed > 800) {

                        Toast.makeText(MainActivity.this,"Refreshing products...",Toast.LENGTH_SHORT).show();

                        Fragment fragment = getSupportFragmentManager()
                                .findFragmentById(R.id.fragmentContainer);

                        if(fragment instanceof HomeFragment){

                            ((HomeFragment) fragment).refreshProducts();

                        }
                    }

                    lastX = x;
                    lastY = y;
                    lastZ = z;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}



        };



        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        return;
                    }

                    String token = task.getResult();

                    Log.d("FCM_TOKEN", token);

                });

        // Start listening for notifications
        listenForNotifications();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{
                    android.Manifest.permission.POST_NOTIFICATIONS
            }, 101);
        }

    }

    private void loadFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_home) {

            loadFragment(new HomeFragment());

        } else if (id == R.id.nav_cart) {

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                showLoginDialog();
                return false;
            }

            loadFragment(new CartFragment());

        } else if (id == R.id.nav_orders) {

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                showLoginDialog();
                return false;
            }

            loadFragment(new OrderFragment());

        } else if (id == R.id.nav_alerts) {

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                showLoginDialog();
                return false;
            }

            loadFragment(new AlertFragment());

        } else if (id == R.id.nav_profile) {

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                showLoginDialog();
                return false;
            }

            loadFragment(new ProfileFragment());
        }

        return true;
    }

    private void showLoginDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Login Required")
                .setMessage("Please login to access this feature.")
                .setPositiveButton("Login", (dialog, which) -> {
                    startActivity(new Intent(this, LoginActivity.class));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void listenForNotifications() {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("userId", uid)
                .whereEqualTo("read", false)
                .addSnapshotListener((snapshots, error) -> {

                    if (error != null || snapshots == null) return;

                    snapshots.getDocumentChanges().forEach(change -> {

                        if (change.getType() == DocumentChange.Type.ADDED) {

                            String title = change.getDocument().getString("title");
                            String message = change.getDocument().getString("message");

                            showLocalNotification(title, message, change.getDocument().getId());

                            // mark as read so it won't show again
                            change.getDocument().getReference().update("read", true);
                        }

                    });

                });
    }

    private void showLocalNotification(String title, String message, String notificationId) {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "order_channel";

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            NotificationChannel channel =
                    new NotificationChannel(channelId, "Order Notifications", NotificationManager.IMPORTANCE_HIGH);

            channel.setSound(soundUri, audioAttributes);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );


        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification_order)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setSound(soundUri)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        manager.notify(notificationId.hashCode(), builder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        if (networkReceiver == null) {
            networkReceiver = new NetworkReceiver();
        }

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(sensorEventListener);

        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
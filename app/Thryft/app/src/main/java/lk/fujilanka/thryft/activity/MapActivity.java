package lk.fujilanka.thryft.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.maps.android.PolyUtil;

import java.util.List;

import lk.fujilanka.thryft.R;
import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private static final LatLng STORE_LOCATION = new LatLng(6.9271, 79.8612);

    private static final String API_KEY = "";

    private Polyline currentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        findViewById(R.id.btn_get_directions).setOnClickListener(v -> openNavigation());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        mMap.addMarker(new MarkerOptions()
                .position(STORE_LOCATION)
                .title("Thryft Store - Colombo"));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(STORE_LOCATION, 15));

        enableUserLocation();
    }

    private void enableUserLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {

            if (location != null) {
                Log.d("MAP_TEST","Location found");

                LatLng userLocation = new LatLng(
                        location.getLatitude(),
                        location.getLongitude());

                drawRoute(userLocation, STORE_LOCATION);

            } else {
                Log.d("MAP_TEST","Location NULL");
            }
        });
    }

    private void drawRoute(LatLng start, LatLng end) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DirectionsService service = retrofit.create(DirectionsService.class);

        String origin = start.latitude + "," + start.longitude;
        String destination = end.latitude + "," + end.longitude;

        service.getDirections(origin, destination, "driving", API_KEY)
                .enqueue(new Callback<DirectionsResponse>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<DirectionsResponse> call,
                            @NonNull Response<DirectionsResponse> response) {

                        Log.d("MAP_TEST", "Response received");
                        if (response.body() != null &&
                                response.body().routes.size() > 0) {

                            Log.d("MAP_TEST", "Routes size: " + response.body().routes.size());
                            String polyline =
                                    response.body()
                                            .routes
                                            .get(0)
                                            .overview_polyline
                                            .points;

                            List<LatLng> points = PolyUtil.decode(polyline);

                            if (currentPolyline != null) {
                                currentPolyline.remove();
                            }

                            PolylineOptions options = new PolylineOptions()
                                    .addAll(points)
                                    .width(10)
                                    .color(Color.BLUE);

                            currentPolyline = mMap.addPolyline(options);

                            Toast.makeText(
                                    MapActivity.this,
                                    "Route loaded",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            Log.d("MAP_TEST", "Response body NULL");
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<DirectionsResponse> call,
                            @NonNull Throwable t) {

                        Toast.makeText(
                                MapActivity.this,
                                "Route failed",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openNavigation() {

        Uri uri = Uri.parse(
                "google.navigation:q=" +
                        STORE_LOCATION.latitude +
                        "," +
                        STORE_LOCATION.longitude);

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");

        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            enableUserLocation();
        }
    }

    // -------- Retrofit API --------

    public interface DirectionsService {

        @GET("maps/api/directions/json")
        Call<DirectionsResponse> getDirections(
                @Query("origin") String origin,
                @Query("destination") String destination,
                @Query("mode") String mode,
                @Query("key") String key
        );
    }

    // -------- Response Models --------

    public static class DirectionsResponse {
        public List<Route> routes;
    }

    public static class Route {
        public OverviewPolyline overview_polyline;
    }

    public static class OverviewPolyline {
        public String points;
    }
}
package lk.fujilanka.thryft.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class NetworkReceiver extends BroadcastReceiver {

    private static Boolean lastStatus = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("NETWORK_RECEIVER", "Receiver Triggered");

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        // Only show toast if state changed
        if (lastStatus == null || lastStatus != isConnected) {

            lastStatus = isConnected;

            if (isConnected) {

                Log.d("NETWORK_RECEIVER", "Internet Connected");
                Toast.makeText(context, "Internet Connected", Toast.LENGTH_SHORT).show();

            } else {

                Log.d("NETWORK_RECEIVER", "No Internet Connection");
                Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
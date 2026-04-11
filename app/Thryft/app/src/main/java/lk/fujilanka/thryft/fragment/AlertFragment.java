package lk.fujilanka.thryft.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lk.fujilanka.thryft.R;
import lk.fujilanka.thryft.adapter.NotificationAdapter;
import lk.fujilanka.thryft.model.NotificationModel;

public class AlertFragment extends Fragment {

    RecyclerView recyclerView;
    LinearLayout emptyView;
    TextView tvMarkAllRead;

    NotificationAdapter adapter;
    List<NotificationModel> list = new ArrayList<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String uid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alert, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rv_notifications);
        emptyView = view.findViewById(R.id.ll_empty_notifications);
        tvMarkAllRead = view.findViewById(R.id.tv_mark_all_read);

        adapter = new NotificationAdapter(list);
        recyclerView.setAdapter(adapter);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Load notifications
        loadNotifications();

        // Mark all as read
        tvMarkAllRead.setOnClickListener(v -> markAllAsRead());
    }

    private void loadNotifications() {
        db.collection("notifications")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    list.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        NotificationModel notification = doc.toObject(NotificationModel.class);
                        if (notification != null) {
                            notification.setId(doc.getId()); // Save doc ID for updates
                            list.add(notification);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    // Handle Empty State
                    if (list.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void markAllAsRead() {
        db.collection("notifications")
                .whereEqualTo("userId", uid)
                .whereEqualTo("read", false) // only unread
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().update("read", true);
                    }

                    // Clear local notified IDs to prevent repeated FCM notifications
                    var prefs = getActivity().getSharedPreferences("notifications_prefs", Context.MODE_PRIVATE);
                    prefs.edit().putStringSet("notified_ids", new HashSet<>()).apply();

                    adapter.notifyDataSetChanged();

                    Toast.makeText(getContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
                });
    }
}
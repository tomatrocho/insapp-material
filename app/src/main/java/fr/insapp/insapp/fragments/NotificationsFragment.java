package fr.insapp.insapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import auto.parcelgson.gson.AutoParcelGsonTypeAdapterFactory;
import fr.insapp.insapp.R;
import fr.insapp.insapp.activities.EventActivity;
import fr.insapp.insapp.activities.PostActivity;
import fr.insapp.insapp.adapters.NotificationRecyclerViewAdapter;
import fr.insapp.insapp.http.ServiceGenerator;
import fr.insapp.insapp.models.Notification;
import fr.insapp.insapp.models.Notifications;
import fr.insapp.insapp.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by thomas on 27/10/2016.
 */

public class NotificationsFragment extends Fragment {

    private NotificationRecyclerViewAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // adapter

        this.adapter = new NotificationRecyclerViewAdapter(getContext());
        adapter.setOnItemClickListener(new NotificationRecyclerViewAdapter.OnNotificationItemClickListener() {
            @Override
            public void onNotificationItemClick(Notification notification) {
                switch (notification.getType()) {
                    case "tag":
                        startActivity(new Intent(getContext(), PostActivity.class).putExtra("post", notification.getPost()).putExtra("taggedCommentID", notification.getComment().getId()));
                        break;

                    case "post":
                        startActivity(new Intent(getContext(), PostActivity.class).putExtra("post", notification.getPost()));
                        break;

                    case "event":
                        startActivity(new Intent(getContext(), EventActivity.class).putExtra("event", notification.getEvent()));
                        break;

                    default:
                        break;
                }

                // mark notification as seen

                final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new AutoParcelGsonTypeAdapterFactory()).create();
                final User user = gson.fromJson(getContext().getSharedPreferences("User", Context.MODE_PRIVATE).getString("user", ""), User.class);

                Call<Notifications> call = ServiceGenerator.create().markNotificationAsSeen(user.getId(), notification.getId());
                call.enqueue(new Callback<Notifications>() {
                    @Override
                    public void onResponse(@NonNull Call<Notifications> call, @NonNull Response<Notifications> response) {
                        if (response.isSuccessful()) {
                            final Notifications notifications = response.body();

                            adapter.setItems(notifications.getNotifications());
                        }
                        else {
                            Toast.makeText(getContext(), "NotificationsFragment", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Notifications> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "NotificationsFragment", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        generateNotifications();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_notifications);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void generateNotifications() {
        final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new AutoParcelGsonTypeAdapterFactory()).create();
        final User user = gson.fromJson(getContext().getSharedPreferences("User", Context.MODE_PRIVATE).getString("user", ""), User.class);

        Call<Notifications> call = ServiceGenerator.create().getNotificationsForUser(user.getId());
        call.enqueue(new Callback<Notifications>() {
            @Override
            public void onResponse(@NonNull Call<Notifications> call, @NonNull Response<Notifications> response) {
                if (response.isSuccessful()) {
                    final Notifications notifications = response.body();

                    if (notifications.getNotifications() != null) {
                        for (final Notification notification : notifications.getNotifications()) {
                            notification.generateContent();

                            adapter.addItem(notification);
                        }
                    }
                }
                else {
                    Toast.makeText(getContext(), "NotificationsFragment", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Notifications> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "NotificationsFragment", Toast.LENGTH_LONG).show();
            }
        });
    }
}
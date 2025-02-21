package download_manager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;

public class ForGroundService extends Service {

    private static final String CHANNEL_ID = "DownloadChannel";
    private static final int NOTIFICATION_ID = 7;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BackgroundService", "Service Created! Registering receivers...");

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Download Progress",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "START_DOWNLOAD".equals(intent.getAction())) {
            showProgressNotification();
        } else if (intent != null && "DOWNLOAD_COMPLETE".equals(intent.getAction())) {
            completeNotification();
        }

        return START_STICKY;
    }

    private void showProgressNotification() {
        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Downloading File")
                .setContentText("Download in progress...")
                .setSmallIcon(R.drawable.baseline_download_24)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);

        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void completeNotification() {
        NotificationCompat.Builder completeNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Download Complete")
                .setContentText("File downloaded successfully")
                .setSmallIcon(R.drawable.baseline_download_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        notificationManager.notify(NOTIFICATION_ID + 1, completeNotification.build());
        stopForeground(true);
        stopSelf();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
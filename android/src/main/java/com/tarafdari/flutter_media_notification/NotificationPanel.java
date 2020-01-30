package com.tarafdari.flutter_media_notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class NotificationPanel extends Service {
    public static int NOTIFICATION_ID = 1;
    public static final String CHANNEL_ID = "flutter_media_notification";
    public static final String MEDIA_SESSION_TAG = "flutter_media_notification";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean isPlaying = intent.getBooleanExtra("isPlaying", true);
        String title = intent.getStringExtra("title");
        String author = intent.getStringExtra("author");
        String artUri = intent.getStringExtra("artUri");

        Bitmap artBitmap;
        if (null == artUri || artUri.isEmpty()) {
            artBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo);
        } else {
            artBitmap = this.loadArtBitmap(artUri);
        }

        createNotificationChannel();


        MediaSessionCompat mediaSession = new MediaSessionCompat(this, MEDIA_SESSION_TAG);


        int iconPlayPause = R.drawable.play_icon_white;
        String titlePlayPause = "pause";
        if (isPlaying) {
            iconPlayPause = R.drawable.pause_icon_white;
            titlePlayPause = "play";
        }

        Intent toggleIntent = new Intent(this, NotificationReturnSlot.class)
                .setAction("toggle")
                .putExtra("title", title)
                .putExtra("author", author)
                .putExtra("artUri", artUri)
                .putExtra("play", !isPlaying);
        PendingIntent pendingToggleIntent = PendingIntent.getBroadcast(this, 0, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        MediaButtonReceiver.handleIntent(mediaSession, toggleIntent);

        //TODO(ALI): add media mediaSession Buttons and handle them
//        Intent nextIntent = new Intent(this, NotificationReturnSlot.class)
//                .setAction("next");
//        PendingIntent pendingNextIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        MediaButtonReceiver.handleIntent(mediaSession, nextIntent);

//        Intent prevIntent = new Intent(this, NotificationReturnSlot.class)
//                .setAction("prev");
//        PendingIntent pendingPrevIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        MediaButtonReceiver.handleIntent(mediaSession, prevIntent);

        Intent closeIntent = new Intent(this, NotificationReturnSlot.class)
                .setAction("close");
        PendingIntent pendingCloseIntent = PendingIntent.getBroadcast(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent selectIntent = new Intent(this, NotificationReturnSlot.class)
                .setAction("select");
        PendingIntent selectPendingIntent = PendingIntent.getBroadcast(this, 0, selectIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//        MediaButtonReceiver.handleIntent(mediaSession, selectIntent);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .addAction(R.drawable.baseline_skip_previous_black_48, "prev", pendingPrevIntent)
                .addAction(iconPlayPause, titlePlayPause, pendingToggleIntent)
//                .addAction(R.drawable.baseline_skip_next_black_48, "next", pendingNextIntent)
                .addAction(R.drawable.close_icon_white, "close", pendingCloseIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1)
                        .setShowCancelButton(true)
                        .setMediaSession(mediaSession.getSessionToken()))
                .setSmallIcon(R.drawable.logo)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setVibrate(new long[]{0L})
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentTitle(title)
//                .setColor()
                .setContentText(author)
                .setSubText(title)
                .setContentIntent(selectPendingIntent)
                .setLargeIcon(artBitmap)
                .build();

        startForeground(NOTIFICATION_ID, notification);
        if (!isPlaying) {
            stopForeground(false);
        }

        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("flutter_media_notification");
            serviceChannel.setShowBadge(false);
            serviceChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopForeground(true);
    }


    synchronized Bitmap loadArtBitmap(String url) {
        Bitmap bitmap = null;

        try {
            InputStream in = new URL(url).openConnection().getInputStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}


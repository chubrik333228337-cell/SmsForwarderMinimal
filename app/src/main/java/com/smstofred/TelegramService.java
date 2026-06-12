package com.smstofred;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TelegramService extends Service {
    private static final String CHANNEL_ID = "sms_channel";
    private static final int NOTIF_ID = 1;
    private static final String BOT_TOKEN = TelegramSender.getBotToken();
    private Thread worker;
    private volatile boolean running = true;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIF_ID, buildNotification());

        long MY_ID = 6660506530L; // ЗАМЕНИ НА СВОЙ chat_id, баля
        PreferencesHelper.setAdminId(this, MY_ID);
        if (!PreferencesHelper.getSubscribers(this).contains(MY_ID)) {
            PreferencesHelper.addSubscriber(this, MY_ID);
        }
        startPolling();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SMS Forwarder",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("SMS Forwarder")
                    .setContentText("Активен и слушает команды")
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setPriority(Notification.PRIORITY_LOW)
                    .build();
        } else {
            return new Notification.Builder(this)
                    .setContentTitle("SMS Forwarder")
                    .setContentText("Активен и слушает команды")
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setPriority(Notification.PRIORITY_LOW)
                    .build();
        }
    }

    private void startPolling() {
        worker = new Thread(() -> {
            while (running) {
                try {
                    int offset = PreferencesHelper.getLastUpdateId(this) + 1;
                    String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/getUpdates?offset=" + offset + "&timeout=30";
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setConnectTimeout(35000);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();
                    conn.disconnect();

                    JSONObject resp = new JSONObject(sb.toString());
                    if (resp.getBoolean("ok")) {
                        JSONArray updates = resp.getJSONArray("result");
                        for (int i = 0; i < updates.length(); i++) {
                            JSONObject upd = updates.getJSONObject(i);
                            int updateId = upd.getInt("update_id");
                            PreferencesHelper.setLastUpdateId(this, updateId);

                            if (upd.has("message")) {
                                JSONObject msg = upd.getJSONObject("message");
                                // Правильный парсинг chat_id
                                JSONObject chat = msg.getJSONObject("chat");
                                long chatId = chat.getLong("id");

                                if (chatId == PreferencesHelper.getAdminId(this) && msg.has("text")) {
                                    String text = msg.getString("text");
                                    handleCommand(text);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("TelegramService", "Poll error", e);
                }
            }
        });
        worker.start();
    }

    private void handleCommand(String cmd) {
        long admin = PreferencesHelper.getAdminId(this);
        if (cmd.startsWith("/add")) {
            String arg = cmd.substring(4).trim();
            try {
                long id = Long.parseLong(arg);
                PreferencesHelper.addSubscriber(this, id);
                TelegramSender.sendToChat(admin, "✅ Добавлен " + id);
                TelegramSender.sendToChat(id, "📢 Ты теперь получаешь уведомления с телефона Алексея.");
            } catch (NumberFormatException e) {
                TelegramSender.sendToChat(admin, "❌ /add <числовой chat_id>");
            }
        } else if (cmd.startsWith("/remove")) {
            String arg = cmd.substring(7).trim();
            try {
                long id = Long.parseLong(arg);
                PreferencesHelper.removeSubscriber(this, id);
                TelegramSender.sendToChat(admin, "❌ Удалён " + id);
            } catch (NumberFormatException e) {
                TelegramSender.sendToChat(admin, "❌ /remove <числовой chat_id>");
            }
        } else if (cmd.equals("/list")) {
            StringBuilder sb = new StringBuilder("📋 Подписчики:\n");
            for (long id : PreferencesHelper.getSubscribers(this)) {
                sb.append(id).append("\n");
            }
            TelegramSender.sendToChat(admin, sb.toString());
        } else if (cmd.equals("/start")) {
            TelegramSender.sendToChat(admin, "👋 Бот админки работает. Команды: /add, /remove, /list");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false;
        if (worker != null) worker.interrupt();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

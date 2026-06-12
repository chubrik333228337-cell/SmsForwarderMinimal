package com.smstofred;

import android.content.Context;
import android.util.Log;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;

public class TelegramSender {
    private static final String TAG = "TelegramSender";
    private static final String BOT_TOKEN = "8648796997:AAHzQLjzUoFtTDSEEQf0sjr-OyY73Jfm7Hg"; // замени на свой

    public static String getBotToken() { return BOT_TOKEN; }

    public static void sendToChat(long chatId, String text) {
        Log.d(TAG, "sendToChat called for " + chatId);
        new Thread(() -> {
            try {
                String encoded = URLEncoder.encode(text, "UTF-8");
                String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + chatId + "&text=" + encoded;
                Log.d(TAG, "URL: " + url);
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                int code = conn.getResponseCode();
                Log.d(TAG, "Response code: " + code);
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error sending", e);
            }
        }).start();
    }

    public static void sendToAllSubscribers(Context ctx, String text) {
        Set<Long> subs = PreferencesHelper.getSubscribers(ctx);
        Log.d(TAG, "Subscribers count: " + (subs == null ? "null" : subs.size()));
        if (subs == null || subs.isEmpty()) {
            Log.e(TAG, "No subscribers!");
            return;
        }
        for (long id : subs) {
            Log.d(TAG, "Sending to " + id);
            sendToChat(id, text);
        }
    }
}

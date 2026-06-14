package com.smstofred;

import android.util.Log;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TelegramSender {
    private static final String TAG = "TelegramSender";
    private static final String BOT_TOKEN = "8648796997:AAHzQLjzUoFtTDSEEQf0sjr-OyY73Jfm7Hg";
    
    // СПИСОК ПОЛУЧАТЕЛЕЙ - убедись, что ID друга тут
    private static final long[] CHAT_IDS = {
        6660506530L,   // твой
        8230567876L    // ID друга (замени на реальный)
    };

    public static void sendMessage(String text) {
        Log.d(TAG, "sendMessage called, text length: " + text.length());
        for (long chatId : CHAT_IDS) {
            Log.d(TAG, "Sending to chatId: " + chatId);
            sendToChat(chatId, text);
        }
    }

    private static void sendToChat(long chatId, String text) {
        new Thread(() -> {
            try {
                String encoded = URLEncoder.encode(text, "UTF-8");
                String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + chatId + "&text=" + encoded;
                Log.d(TAG, "URL: " + url);
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                int code = conn.getResponseCode();
                Log.d(TAG, "Response code for " + chatId + ": " + code);
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error sending to " + chatId, e);
            }
        }).start();
    }
}

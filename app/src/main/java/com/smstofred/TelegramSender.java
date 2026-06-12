package com.smstofred;
import android.content.Context;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TelegramSender {
    private static final String BOT_TOKEN = "8262219232:AAFR1sA9XrGTua8unBdA0FG20bbAjBlF0q8"; // замени на свой, баля

    public static String getBotToken() {
        return BOT_TOKEN;
    }

    public static void sendToChat(long chatId, String text) {
        new Thread(() -> {
            try {
                String encoded = URLEncoder.encode(text, "UTF-8");
                String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + chatId + "&text=" + encoded;
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception ignored) {}
        }).start();
    }

    public static void sendToAllSubscribers(Context ctx, String text) {
        for (long id : PreferencesHelper.getSubscribers(ctx)) {
            sendToChat(id, text);
        }
    }
}

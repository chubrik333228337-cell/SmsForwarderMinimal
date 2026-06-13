package com.smstofred;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TelegramSender {
    // ЗАМЕНИ НА СВОЙ ТОКЕН
    private static final String BOT_TOKEN = "8648796997:AAHzQLjzUoFtTDSEEQf0sjr-OyY73Jfm7Hg";
    // ЗАМЕНИ НА СВОЙ CHAT_ID (число, с L в конце)
    private static final long ADMIN_CHAT_ID = 6660506530L;

    public static void sendMessage(String text) {
        new Thread(() -> {
            try {
                String encoded = URLEncoder.encode(text, "UTF-8");
                String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + ADMIN_CHAT_ID + "&text=" + encoded;
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

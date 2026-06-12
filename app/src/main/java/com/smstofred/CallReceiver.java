package com.smstofred;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startForegroundService(new Intent(context, TelegramService.class));
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state) && number != null) {
            TelegramSender.sendToAllSubscribers(context, "📞 Звонок от: " + number);
        }
    }
}

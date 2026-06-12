package com.smstofred;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.lang.reflect.Method;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Запускаем сервис для постоянной работы
        context.startForegroundService(new Intent(context, KeepAliveService.class));
        
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        Log.d(TAG, "state=" + state + ", number=" + number);

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state) && number != null && !number.isEmpty()) {
            String text = "📞 Звонок от: " + number;
            TelegramSender.sendToAllSubscribers(context, text);
            endCall(context);
        }
    }

    private void endCall(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> telephonyClass = Class.forName(tm.getClass().getName());
            Method getITelephonyMethod = telephonyClass.getDeclaredMethod("getITelephony");
            getITelephonyMethod.setAccessible(true);
            Object telephonyInterface = getITelephonyMethod.invoke(tm);
            Class<?> telephonyInterfaceClass = Class.forName(telephonyInterface.getClass().getName());
            Method endCallMethod = telephonyInterfaceClass.getDeclaredMethod("endCall");
            endCallMethod.setAccessible(true);
            endCallMethod.invoke(telephonyInterface);
            Log.d(TAG, "Звонок успешно отклонён");
        } catch (Exception e) {
            Log.e(TAG, "Не удалось отклонить звонок", e);
        }
    }
}

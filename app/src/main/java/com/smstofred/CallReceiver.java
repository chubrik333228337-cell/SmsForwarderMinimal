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
        Log.d(TAG, "onReceive вызван, action=" + intent.getAction());

        // Запускаем сервис (на всякий случай)
        context.startForegroundService(new Intent(context, TelegramService.class));

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        Log.d(TAG, "state=" + state + ", number=" + number);

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state) && number != null && !number.isEmpty()) {
            String text = "📞 Звонок от: " + number;
            Log.d(TAG, "Отправляем: " + text);
            TelegramSender.sendToAllSubscribers(context, text);

            // Отклоняем звонок
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
            Log.d(TAG, "Звонок успешно отклонён через отражение");
        } catch (Exception e) {
            Log.e(TAG, "Не удалось отклонить звонок", e);
        }
    }
}

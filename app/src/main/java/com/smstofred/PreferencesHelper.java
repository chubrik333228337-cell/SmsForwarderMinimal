package com.smstofred;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class PreferencesHelper {
    private static final String PREFS = "smsprefs";
    private static final String KEY_SUBS = "subscribers";
    private static final String KEY_ADMIN = "admin_id";
    private static final String KEY_LAST_UPDATE = "last_update_id";

    public static void setAdminId(Context ctx, long id) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong(KEY_ADMIN, id).apply();
    }
    public static long getAdminId(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_ADMIN, 0);
    }
    public static void addSubscriber(Context ctx, long id) {
        Set<String> subs = getSubsSet(ctx);
        subs.add(String.valueOf(id));
        saveSubs(ctx, subs);
    }
    public static void removeSubscriber(Context ctx, long id) {
        Set<String> subs = getSubsSet(ctx);
        subs.remove(String.valueOf(id));
        saveSubs(ctx, subs);
    }
    public static Set<Long> getSubscribers(Context ctx) {
        Set<Long> result = new HashSet<>();
        for (String s : getSubsSet(ctx)) result.add(Long.parseLong(s));
        return result;
    }
    private static Set<String> getSubsSet(Context ctx) {
        return new HashSet<>(ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getStringSet(KEY_SUBS, new HashSet<>()));
    }
    private static void saveSubs(Context ctx, Set<String> subs) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putStringSet(KEY_SUBS, subs).apply();
    }
    public static int getLastUpdateId(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_LAST_UPDATE, 0);
    }
    public static void setLastUpdateId(Context ctx, int id) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_LAST_UPDATE, id).apply();
    }
}

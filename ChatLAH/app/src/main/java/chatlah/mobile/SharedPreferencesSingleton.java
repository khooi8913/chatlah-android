package chatlah.mobile;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SharedPreferencesSingleton {

    private static SharedPreferencesSingleton ourInstance;
    private static Context mContext;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private static String PACKAGE_NAME;
    public static final String CHAT_SESSION_START = "CHAT_SESSION_START";
    public static final String CONVERSATION_ZONE = "CONVERSATION_ZONE";
    public static final String SHUT_DOWN_PROPERLY = "SHUT_DOWN_PROPERLY";

    private SharedPreferencesSingleton(Context context) {
        mContext = context.getApplicationContext();
        PACKAGE_NAME = mContext.getPackageName();

        sharedPreferences = context.getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    public static SharedPreferencesSingleton getInstance(Context context){
        if (ourInstance == null)    ourInstance = new SharedPreferencesSingleton(context);
        return ourInstance;
    }

    public static String getSharedPrefStringVal(String sharedPrefKey){
        return sharedPreferences.getString(sharedPrefKey, null);
    }

    public static void setSharedPrefStringVal(String sharedPrefKey, String value){
        editor.putString(sharedPrefKey, value);
        editor.apply();
    }

    public static boolean getSharedPrefBool(String sharedPrefKey){
        return sharedPreferences.getBoolean(sharedPrefKey, false);
    }

    public static void setSharedPrefBool(String sharedPrefBool, boolean value){
        editor.putBoolean(sharedPrefBool, value);
        editor.apply();
    }

    public static float getSharedPrefFloat (String sharedPrefKey){
        return sharedPreferences.getFloat(sharedPrefKey, 0);
    }

    public static void setSharedPrefFloat (String sharedPrefBool, float value){
        editor.putFloat(sharedPrefBool, value);
        editor.apply();
    }

    public static void clearSharedPrefs(){
        editor.clear();
        editor.apply();
    }

}

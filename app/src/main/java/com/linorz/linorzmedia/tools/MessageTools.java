package com.linorz.linorzmedia.tools;

import android.content.Context;
import android.content.Intent;

import com.linorz.linorzmedia.main.activity.PlayActivity;
import com.linorz.linorzmedia.main.activity.SettingActivity;
import com.linorz.linorzmedia.main.activity.WebActivity;

/**
 * Created by linorz on 2017/8/29.
 */

public class MessageTools {
    public static void ToWebActivity(Context context, CharSequence content) {
        Intent intent = new Intent(context, WebActivity.class);
        if (content != null)
            intent.putExtra("search_content", content);
        context.startActivity(intent);
    }

    public static void ToHome(Context context) {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(home);
    }

    public static void ToSettingActivity(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }

    public static void ToVideoActivity(Context context,String path) {
        Intent intent = new Intent(context, PlayActivity.class);
        intent.putExtra("path", path);
        intent.putExtra("type", 1);
        StaticMethod.currentDuration = -1;
        context.startActivity(intent);
    }
}

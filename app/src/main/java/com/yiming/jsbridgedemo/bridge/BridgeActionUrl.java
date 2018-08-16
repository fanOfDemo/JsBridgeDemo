package com.yiming.jsbridgedemo.bridge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;

import com.yiming.jsbridge.Callback;
import com.yiming.jsbridge.IBridge;
import com.yiming.jsbridge.ResultFactory;

import org.json.JSONObject;

public class BridgeActionUrl implements IBridge {


    public static void startActivity(WebView webView, JSONObject param, final Callback callback) {
        try {

            Intent intent = new Intent();
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setAction(Intent.ACTION_VIEW);

            String action = param.optString("action");
            Uri.Builder builder = Uri.parse("jsbridgedemo" + "://" + action)
                    .buildUpon();
            intent.setData(builder.build());

            Context context = webView.getContext();
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                activity.startActivity(intent);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
            if (callback != null) {
                callback.apply(ResultFactory.getJSONObject(0, "ok", null));
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.apply(ResultFactory.getJSONObject(-1, e.toString(), null));
            }

        }
    }


}

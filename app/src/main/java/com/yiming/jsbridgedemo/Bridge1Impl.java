package com.yiming.jsbridgedemo;

import android.webkit.WebView;
import android.widget.Toast;

import com.yiming.jsbridge.Callback;
import com.yiming.jsbridge.IBridge;
import com.yiming.jsbridge.ResultFactory;

import org.json.JSONObject;

public class Bridge1Impl implements IBridge {

    public static void test1(WebView webView, JSONObject param, final Callback callback) {
        String message = param.optString("msg");
        Toast.makeText(webView.getContext(), message, Toast.LENGTH_SHORT).show();
        if (null != callback) {
            try {
                JSONObject object = new JSONObject();
                object.put("key", "test1");
                object.put("key1", "test2");
                callback.apply(ResultFactory.getJSONObject(0, "ok", object));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}

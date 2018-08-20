package com.yiming.jsbridge;

import android.util.Log;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class JSBridgeWebChromeClient extends WebChromeClient {
    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        Log.e("JS onJsPrompt",message);
        result.confirm(JSBridge.callJava(view, message));
        return true;
    }


}

package com.yiming.jsbridgedemo.jsbridge;

import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class JSBridgeWebChromeClient extends WebChromeClient {
    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        result.confirm(JSBridge.callJava(view, message));
        return true;
    }


}

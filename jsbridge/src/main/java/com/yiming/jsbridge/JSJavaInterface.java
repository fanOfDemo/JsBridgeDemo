package com.yiming.jsbridge;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.yiming.jsbridge.JSBridge;

import java.lang.ref.WeakReference;

public class JSJavaInterface {
    private WeakReference<WebView> mWebViewRef;

    public JSJavaInterface(WebView view) {
        mWebViewRef = new WeakReference<>(view);
    }


    //android 4.2 开始要求加上这个注解，4.2之前无该注解都是不安全的
    @JavascriptInterface
    public  void call(String params){
        JSBridge.callJava(mWebViewRef.get(),params);
    }
}

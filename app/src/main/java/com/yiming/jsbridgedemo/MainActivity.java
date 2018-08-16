package com.yiming.jsbridgedemo;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.yiming.jsbridge.JSBridge;
import com.yiming.jsbridge.JSBridgeWebChromeClient;
import com.yiming.jsbridgedemo.bridge.BridgeActionUrl;
import com.yiming.jsbridgedemo.bridge.BridgeImpl;
import com.yiming.jsbridge.JSJavaInterface;


public class MainActivity extends AppCompatActivity {
    WebView mWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JSBridge.register("bridge", BridgeImpl.class);
        JSBridge.register("actionUrl", BridgeActionUrl.class);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mWebView = findViewById(R.id.webView);

        WebSettings settings = mWebView.getSettings();


        //方法一 ：通过 addJavascriptInterface 定义一个接口 call 方法实现
        if (Build.VERSION.SDK_INT >= 11) {
            // 移除系统自带的JS接口,这个漏洞曾经导致过各种病毒
            mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        }
        settings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JSJavaInterface(mWebView),"JSInterface");

        //方法二：通过 WebChromeClient 的 onJsPrompt 实现
        mWebView.setWebChromeClient(new JSBridgeWebChromeClient());

        //理论上方法一快于方法二，但是方法一在android4.2下不安全，如果你的产品不需要兼容到4.2以下，推荐使用方法一实现jsBridge

        mWebView.loadUrl("file:///android_asset/index.html");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

# JsBridgeDemo
a sample jsBridge



##bridge两种实现方式：

   ### native：

         //方法一 ：通过 addJavascriptInterface 定义一个接口 call 方法实现
        if (Build.VERSION.SDK_INT >= 11) {
            // 移除系统自带的JS接口,这个漏洞曾经导致过各种病毒
            mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        }
        settings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JSJavaInterface(mWebView),"JSInterface");

        //方法二：通过 WebChromeClient 的 onJsPrompt 实现
        mWebView.setWebChromeClient(new JSBridgeWebChromeClient());

        //理论上方法一快于方法二，但是方法一在android4.2下不安全，如果你的项目不需要兼容到4.2以下，推荐使用方法一实现jsBridge


   ### js:

         //window.JSInterface.call(uri);// 方法一
           window.prompt(uri, "");//方法二



##bridge协议：


         JSBridge + '://' + obj + ':' + callbackId + '/' + method + '?' + params



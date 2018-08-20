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


        方法三 通过setWebViewClient拦截url实现，该方法的优点是Android和IOS可以保持代码一致性

              WebViewClient() {
                          @Override
                          public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                  Log.e("JS iframe",request.getUrl().toString());
                                  JSBridge.callJava(mWebView, request.getUrl().toString());
                              }
                              return true;
                          }

                          @Override
                          public boolean shouldOverrideUrlLoading(WebView view, String url) {
                              Log.e("JS iframe",url);
                              JSBridge.callJava(mWebView, url);
                              return true;
                          }
                      }


   ### js:

         //window.JSInterface.call(uri);// 方法一
           window.prompt(uri, "");//方法二

        //创建隐藏iframe过程
           var messagingIframe = document.createElement('iframe');
           messagingIframe.style.display = 'none';
           document.documentElement.appendChild(messagingIframe);
           messagingIframe.src = uri;


##bridge协议：

         JSBridge + '://' + obj + ':' + callbackId + '/' + method + '?' + params




核心JS代码：

            (function (win) {
                var hasOwnProperty = Object.prototype.hasOwnProperty;
                var JSBridge = win.JSBridge || (win.JSBridge = {});
                var JSBRIDGE_PROTOCOL = 'JSBridge';
                var jsType = 0;

                //创建隐藏iframe过程
                var messagingIframe = document.createElement('iframe');
                messagingIframe.style.display = 'none';
                document.documentElement.appendChild(messagingIframe);



                var Inner = {
                    callbacks: {},//用于缓存 callbackId 的map
                    //call和onFinish方法，在call方法中，我们调用Util.getPort()获得了port值，然后将callback对象存储在了callbacks中的port位置，
                    //接着调用Util.getUri()将参数传递过去，将返回结果赋值给uri，调用window.prompt(uri, “”)将uri传递到native层

                    changeJSType: function (type){
                        console.log(" type:"+type);
                        jsType = type;
                    },

                    call: function (obj, method, params, callback) {
                        console.log(obj+" "+method+" "+params+" "+callback);
                        var callbackId = Util.getCallbackId();
                        console.log(callbackId);
                        this.callbacks[callbackId] = callback;
                        var uri = Util.getUri(obj,method,params,callbackId);
                        console.log(uri);
                        console.log(" jsType"+jsType);

                        switch(jsType){
                            case 0:
                              window.JSInterface.call(uri);// 方法一
                              break;
                            case 1:
                              window.prompt(uri, "");//方法二
                              break;
                            case 2:
                              //进行url scheme传值的iframe 方法三
                              messagingIframe.src = uri;
                              break;
                            default:
                              messagingIframe.src = uri;
                              break;
                        }
                    },
                    onFinish: function (callbackId, jsonObj){
                        var callback = this.callbacks[callbackId];
                        callback && callback(jsonObj);
                        delete this.callbacks[callbackId];
                    },
                };
                var Util = {
                    //getCallbackId()用于随机生成 callbackId
                    getCallbackId: function () {
                        return Math.floor(Math.random() * (1 << 30));
                    },
                    //getUri()用于生成native需要的协议uri,主要做字符串拼接的工作
                    getUri:function(obj, method, params, callbackId){
                        params = this.getParam(params);
                        var uri = JSBRIDGE_PROTOCOL + '://' + obj + ':' + callbackId + '/' + method + '?' + params;
                        return uri;
                    },
                    //getParam()用于生成json字符串
                    getParam:function(obj){
                        if (obj && typeof obj === 'object') {
                            return JSON.stringify(obj);
                        } else {
                            return obj || '';
                        }
                    }
                };
                for (var key in Inner) {
                    if (!hasOwnProperty.call(JSBridge, key)) {
                        console.log(key+": "+Inner[key]);
                        JSBridge[key] = Inner[key];
                    }
                }
            })(window);


Android 核心代码：将每个Bridge类注入到 exposedMethods 的map中，当 onJsPrompt 或 JavascriptInterface 触发时调用callJava方法执行对应的className、对应method的方法


            public class JSBridge {
                private static Map<String, HashMap<String, Method>> exposedMethods = new HashMap<>();
            ​
                public static void register(String exposedName, Class<? extends IBridge> clazz) {
                    if (!exposedMethods.containsKey(exposedName)) {
                        try {
                            exposedMethods.put(exposedName, getAllMethod(clazz));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            ​
                private static HashMap<String, Method> getAllMethod(Class injectedCls) throws Exception {
                    HashMap<String, Method> mMethodsMap = new HashMap<>();
                    Method[] methods = injectedCls.getDeclaredMethods();
                    for (Method method : methods) {
                        String name;
                        if (method.getModifiers() != (Modifier.PUBLIC | Modifier.STATIC) || (name = method.getName()) == null) {
                            continue;
                        }
                        Class[] parameters = method.getParameterTypes();
                        if (null != parameters && parameters.length == 3) {
                            if (parameters[0] == WebView.class && parameters[1] == JSONObject.class && parameters[2] == Callback.class) {
                                mMethodsMap.put(name, method);
                            }
                        }
                    }
                    return mMethodsMap;
                }
            ​
                public static String callJava(WebView webView, String uriString) {
                    String methodName = "";
                    String className = "";
                    String param = "{}";
                    String callbackId = "";
                    if (!TextUtils.isEmpty(uriString) && uriString.startsWith("JSBridge")) {
                        Uri uri = Uri.parse(uriString);
                        className = uri.getHost();
                        param = uri.getQuery();
                        callbackId = uri.getPort() + "";
                        String path = uri.getPath();
                        if (!TextUtils.isEmpty(path)) {
                            methodName = path.replace("/", "");
                        }
                    }
            ​
            ​
                    if (exposedMethods.containsKey(className)) {
                        HashMap<String, Method> methodHashMap = exposedMethods.get(className);
            ​
                        if (methodHashMap != null && methodHashMap.size() != 0 && methodHashMap.containsKey(methodName)) {
                            Method method = methodHashMap.get(methodName);
                            if (method != null) {
                                try {
                                    method.invoke(null, webView, new JSONObject(param), new Callback(webView, callbackId));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    return null;
                }
            ​
            }


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
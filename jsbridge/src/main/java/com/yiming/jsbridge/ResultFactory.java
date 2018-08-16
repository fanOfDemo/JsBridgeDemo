package com.yiming.jsbridge;

import org.json.JSONException;
import org.json.JSONObject;

public class ResultFactory {

    public static JSONObject getJSONObject(int code, String msg, JSONObject result) {
        JSONObject object = new JSONObject();
        try {
            object.put("code", code);
            object.put("msg", msg);
            object.putOpt("data", result);
            return object;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}

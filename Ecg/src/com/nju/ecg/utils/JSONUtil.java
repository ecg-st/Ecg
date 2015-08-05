package com.nju.ecg.utils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JSONUtil {

    public static JSONObject newObject() {
        return new JSONObject();
    }

    public static void putPairIntoJsonObject(JSONObject object, String key, Object value) {
        try {
            object.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject newObject(String content) {
        JSONObject retObj = null;
        try {
//            retObj = new JSONObject(StringUtil.filterHtmlTag(content));
            retObj = new JSONObject(content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retObj;
    }

    public static JSONArray newJsonArray(String content) {
        JSONArray jArray = null;
        try {
            jArray = new JSONArray(content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jArray;
    }

    public static JSONArray getJsonArray(JSONObject parentObj, String key) {
        JSONArray jArray = null;
        try {
            jArray = parentObj.getJSONArray(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jArray;
    }

    public static JSONObject getJsonObject(JSONArray parentArray, int index) {
        JSONObject retObj = null;
        try {
            retObj = parentArray.getJSONObject(index);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retObj;
    }

    public static JSONObject getJsonObject(JSONObject parentObj, String key) {
        JSONObject retObj = null;
        try {
            retObj = parentObj.getJSONObject(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retObj;
    }

    public static String getString(JSONObject parentObj, String key) {
        String retStr = "";
        try {
            retStr = parentObj.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retStr;
    }

}

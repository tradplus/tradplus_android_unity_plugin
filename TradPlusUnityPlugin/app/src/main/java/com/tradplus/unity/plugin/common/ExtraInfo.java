package com.tradplus.unity.plugin.common;

import android.util.Log;

import com.tradplus.ads.common.serialization.JSON;
import com.tradplus.ads.common.serialization.JSONArray;
import com.tradplus.ads.common.serialization.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class ExtraInfo {

    public ExtraInfo(){

    }

    private String className;
    private boolean isAutoload;
    private boolean closeAutoShow;
    private float x;
    private float y;
    private float width;
    private float height;
    private int adPosition;
    private boolean isSimpleListener;

    public boolean isAutoload() {
        return isAutoload;
    }

    public void setAutoload(boolean autoload) {
        isAutoload = autoload;
    }

    public String getCustomData() {
        return customData;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }

    public Map<String, String> getCustomMap() {
        return customMap;
    }

    public void setCustomMap(Map<String, String> customMap) {
        this.customMap = customMap;
    }

    public Map<String, Object> getLocalParams() {

        return localParams;
    }

    public void setLocalParams(Map<String, Object> localParams) {
        try {
            if(localParams != null){
                for(Map.Entry<String, Object> entry:localParams.entrySet()){
                    Object value = entry.getValue();
                    String key = entry.getKey();

                    if(value != null && value instanceof JSONArray){
                        ArrayList<Object> arrayList = new ArrayList<>();

                        for(int i = 0; i < ((JSONArray)value).size();i++){
                            arrayList.add(((JSONArray)value).get(i));
                        }

                        entry.setValue(arrayList);
//                    localParams.put(key,arrayList);
                    }
                }

            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }

        this.localParams = localParams;
    }

    private String userId;
    private String customData;
    private Map<String,String> customMap;
    private Map<String,Object> localParams;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isCloseAutoShow() {
        return closeAutoShow;
    }

    public void setCloseAutoShow(boolean closeAutoShow) {
        this.closeAutoShow = closeAutoShow;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public int getAdPosition() {
        return adPosition;
    }

    public void setAdPosition(int adPosition) {
        this.adPosition = adPosition;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isSimpleListener() {
        return isSimpleListener;
    }

    public void setSimpleListener(boolean simpleListener) {
        isSimpleListener = simpleListener;
    }
}

package com.tradplus.unity.plugin;

import android.util.Log;

import com.tradplus.ads.base.Const;
import com.tradplus.ads.base.bean.TPAdInfo;
import com.tradplus.ads.base.common.TPDataManager;
import com.tradplus.ads.base.common.TPDiskManager;
import com.tradplus.ads.base.common.TPPrivacyManager;
import com.tradplus.ads.base.util.SegmentUtils;
import com.tradplus.ads.base.util.TestDeviceUtil;
import com.tradplus.ads.common.serialization.JSON;
import com.tradplus.ads.common.serialization.JSONArray;
import com.tradplus.ads.common.util.Json;
import com.tradplus.ads.core.AdCacheManager;
import com.tradplus.ads.core.GlobalImpressionManager;
import com.tradplus.meditaiton.utils.ImportSDKUtil;
import com.tradplus.unity.plugin.common.BaseUnityPlugin;

import java.util.ArrayList;
import java.util.Map;

public class TradPlusSdk extends BaseUnityPlugin {

    private static TradPlusSdk sInstance;

    private TradPlusSdk() {
    }

    public synchronized static TradPlusSdk getInstance() {
        if (sInstance == null) {
            sInstance = new TradPlusSdk();
        }
        return sInstance;
    }

    public static void initSdk(String appId, TPInitListener tradPlusInitListener) {
        Log.i("TradPlus", "init Start");
        com.tradplus.ads.open.TradPlusSdk.setTradPlusInitListener(new com.tradplus.ads.open.TradPlusSdk.TradPlusInitListener() {
            @Override
            public void onInitSuccess() {
                Log.i("TradPlus", "init success tradPlusInitListener = " + tradPlusInitListener);
                if (tradPlusInitListener != null) {
                    tradPlusInitListener.onResult("true");
                }
            }
        });

        com.tradplus.ads.open.TradPlusSdk.initSdk(getActivity(), appId);
    }

    public static void initCustomMap(String map) {
        try {
            SegmentUtils.initCustomMap(Json.jsonStringToMap(map));

        } catch (Throwable throwable) {

        }
    }

    public static void setSettingDataParam(String map) {
        try {
            Map<String,Object> temp = JSON.parseObject(map);

            if(temp != null){
                for(Map.Entry<String, Object> entry:temp.entrySet()){
                    Object value = entry.getValue();
                    String key = entry.getKey();

                    if(value != null && value instanceof JSONArray){
                        ArrayList<Object> arrayList = new ArrayList<>();

                        for(int i = 0; i < ((JSONArray)value).size();i++){
                            arrayList.add(((JSONArray)value).get(i));
                        }

                        entry.setValue(arrayList);
                    }
                }

            }
            com.tradplus.ads.open.TradPlusSdk.setSettingDataParam(temp);

        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }


    public static void checkCurrentArea(TPPrivacyRegionListener listener) {
        com.tradplus.ads.open.TradPlusSdk.checkCurrentArea(getActivity(), new TPPrivacyManager.OnPrivacyRegionListener() {
            @Override
            public void onSuccess(boolean isEu, boolean isCn, boolean isCa) {

                if (listener != null) {
                    listener.onSuccess(isEu, isCn, isCa);
                }
            }

            @Override
            public void onFailed() {
                if (listener != null) {
                    listener.onFailed();
                }
            }
        });

    }


    public static void showGDPRDialog(TPGDPRDialogListener tpgdprAuthListener,String url) {
        com.tradplus.ads.open.TradPlusSdk.showUploadDataNotifyDialog(getActivity(), new com.tradplus.ads.open.TradPlusSdk.TPGDPRAuthListener() {
            @Override
            public void onAuthResult(int i) {
                if (tpgdprAuthListener != null) {
                    tpgdprAuthListener.onAuthResult(i);
                }
            }
        },url);
    }

    public static String getSdkVersion() {
        try {
            return com.tradplus.ads.open.TradPlusSdk.getSdkVersion();
        } catch (Throwable throwable) {

        }

        return "";
    }

    public static boolean isEUTraffic() {
        return com.tradplus.ads.open.TradPlusSdk.isEUTraffic(getActivity());
    }

    public static boolean isCalifornia() {
        return com.tradplus.ads.open.TradPlusSdk.isCalifornia(getActivity());
    }

    public static void setGDPRDataCollection(boolean canDataCollection) {
        com.tradplus.ads.open.TradPlusSdk.setGDPRDataCollection(getActivity(), canDataCollection ? 0 : 1);
    }

    public static int getGDPRDataCollection() {
        return com.tradplus.ads.open.TradPlusSdk.getGDPRDataCollection(getActivity());
    }

    public static void setLGPDConsent(boolean consent) {
        com.tradplus.ads.open.TradPlusSdk.setLGPDConsent(getActivity(), consent ? 0 : 1);
    }

    public static int getLGPDConsent() {
        return com.tradplus.ads.open.TradPlusSdk.getLGPDConsent(getActivity());
    }

    public static void setCCPADoNotSell(boolean canDataCollection) {
        com.tradplus.ads.open.TradPlusSdk.setCCPADoNotSell(getActivity(), canDataCollection);
    }

    public static int isCCPADoNotSell() {
        return com.tradplus.ads.open.TradPlusSdk.isCCPADoNotSell(getActivity()) == 1 ? 0 : 1;
    }

    public static void setCOPPAIsAgeRestrictedUser(boolean isChild) {
        com.tradplus.ads.open.TradPlusSdk.setCOPPAIsAgeRestrictedUser(getActivity(), isChild);
    }

    public static int isCOPPAAgeRestrictedUser() {
        return com.tradplus.ads.open.TradPlusSdk.isCOPPAAgeRestrictedUser(getActivity()) == 1 ? 0 : 1;
    }


    public static void setFirstShowGDPR(boolean first) {
        com.tradplus.ads.open.TradPlusSdk.setIsFirstShowGDPR(getActivity(), first);

    }

    public static boolean isFirstShowGDPR() {
        return com.tradplus.ads.open.TradPlusSdk.isFirstShowGDPR(getActivity());
    }

    public static void setOpenPersonalizedAd(boolean open) {
        com.tradplus.ads.open.TradPlusSdk.setOpenPersonalizedAd(open);
    }

    public static boolean isOpenPersonalizedAd() {
        return com.tradplus.ads.open.TradPlusSdk.isOpenPersonalizedAd();
    }

    public static void clearCache(String unitId) {
        com.tradplus.ads.open.TradPlusSdk.clearCache(unitId);
    }

    public static void setPrivacyUserAgree(boolean open) {
        com.tradplus.ads.open.TradPlusSdk.setPrivacyUserAgree(open);
    }

    public static boolean isPrivacyUserAgree() {
        return com.tradplus.ads.open.TradPlusSdk.isPrivacyUserAgree();
    }

    public static void setMaxDatabaseSize(long size) {
        try {
            TPDiskManager.getInstance().setMaxDatabaseSize(size);

        } catch (Throwable throwable) {

        }
    }

    public static void setAutoExpiration(boolean isAuto) {
        com.tradplus.ads.open.TradPlusSdk.setAutoExpiration(isAuto);
    }

    public static void checkAutoExpiration() {
        com.tradplus.ads.open.TradPlusSdk.checkAutoExpiration();
    }

    public static void setCnServer(boolean isCn) {
        com.tradplus.ads.open.TradPlusSdk.setCnServer(isCn);
    }

    public static void setOpenDelayLoadAds(boolean isOpen) {
        com.tradplus.ads.open.TradPlusSdk.setOpenDelayLoadAds(isOpen);
    }

    public static void setGlobalImpressionListener(TPGlobalImpressionListener tPGlobalImpressionListener) {
        com.tradplus.ads.open.TradPlusSdk.setGlobalImpressionListener(new GlobalImpressionManager.GlobalImpressionListener() {
            @Override
            public void onImpressionSuccess(TPAdInfo tpAdInfo) {
                if (tPGlobalImpressionListener != null) {
                    tPGlobalImpressionListener.onImpressionSuccess(JSON.toJSONString(tpAdInfo));
                }
            }
        });
    }

    public static void openTradPlusTool(String APPID) {
        try {
            ImportSDKUtil.getInstance().showTestTools(getActivity(), APPID);
        } catch (Throwable throwable) {
            Log.i("TradPlus", "Not import tools sdk");

        }
    }

}

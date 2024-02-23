package com.tradplus.unity.plugin.splash;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.tradplus.ads.base.bean.TPAdError;
import com.tradplus.ads.base.bean.TPAdInfo;
import com.tradplus.ads.base.bean.TPBaseAd;
import com.tradplus.ads.base.util.SegmentUtils;
import com.tradplus.ads.common.serialization.JSON;
import com.tradplus.ads.open.DownloadListener;
import com.tradplus.ads.open.LoadAdEveryLayerListener;
import com.tradplus.ads.open.splash.SplashAdListener;
import com.tradplus.ads.open.splash.TPSplash;
import com.tradplus.unity.plugin.common.BaseUnityPlugin;
import com.tradplus.unity.plugin.common.ExtraInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TPSplashManager extends BaseUnityPlugin {
    private static TPSplashManager sInstance;

    private TPSplashManager() {
    }

    public synchronized static TPSplashManager getInstance() {
        if (sInstance == null) {
            sInstance = new TPSplashManager();
        }
        return sInstance;
    }

    // 保存广告位对象
    private Map<String, TPSplash> mTPSplash = new ConcurrentHashMap<>();

    public void loadAd(String unitId, String data, TPSplashListener listener) {
        TPSplash tpSplash = getTPSplash(unitId, data, listener);

        ExtraInfo extraInfo = ExtraInfo.getExtraInfo(data);
        if (extraInfo != null) {
            tpSplash.setAutoLoadCallback(extraInfo.isOpenAutoLoadCallback());
        }

        if (tpSplash != null) {
            tpSplash.loadAd(null, extraInfo == null ? 0f : extraInfo.getMaxWaitTime());
        }

    }

    public void showAd(String unitId, String sceneId) {
        TPSplash tpSplash = getTPSplash(unitId);

        if (tpSplash != null) {
            if (tpSplash.isReady()) {
                showSplash(unitId, sceneId);

            }
        }

    }

    private void showSplash(String unitId, String sceneId) {
        Intent i = new Intent(getActivity(), TPSplashShowActivity.class);
        i.putExtra("unitId", unitId);
        i.putExtra("sceneId", sceneId);
        getActivity().startActivity(i);
    }

    public void entryAdScenario(String unitId, String sceneId) {
        TPSplash tpSplash = getTPSplash(unitId);

        if (tpSplash != null) {
            tpSplash.entryAdScenario(sceneId);
        }

    }

    public boolean isReady(String unitId) {
        TPSplash tpSplash = getTPSplash(unitId);

        if (tpSplash != null) {
            return tpSplash.isReady();
        }

        return false;
    }


    public void setCustomShowData(String adUnitId, String data) {
        TPSplash tpSplash = getTPSplash(adUnitId);

        if (tpSplash != null) {
            tpSplash.setCustomShowData(JSON.parseObject(data));
        }
    }


    public TPSplash getTPSplash(String adUnitId) {
        Log.i("tradplus", "adUnitId = " + adUnitId);
        if (TextUtils.isEmpty(adUnitId)) return null;
        return mTPSplash.get(adUnitId);
    }

    private TPSplash getTPSplash(String adUnitId, String data, TPSplashListener listener) {

        Log.i("tradplus", "data = " + data + " mTPSplash = " + mTPSplash + " listener = " + listener);

        ExtraInfo extraInfo = null;
        if (!TextUtils.isEmpty(data)) {
            extraInfo = JSON.parseObject(data, ExtraInfo.class);
        }

        HashMap<String, Object> temp = new HashMap<>();
        TPSplash tpSplash = mTPSplash.get(adUnitId);
        if (tpSplash == null) {
            tpSplash = new TPSplash(getActivity(), adUnitId);
            mTPSplash.put(adUnitId, tpSplash);

            boolean isSimpleListener = extraInfo == null ? false : extraInfo.isSimpleListener();

            tpSplash.setAdListener(new TPSplashAdListener(adUnitId, listener));
            if (!isSimpleListener) {

                tpSplash.setAllAdLoadListener(new TPSplashAllAdListener(adUnitId, listener));
                tpSplash.setDownloadListener(new TPSplashDownloadListener(adUnitId, listener));

            }

        }
//        LogUtil.ownShow("map params = "+params);
//        // 同一个广告位每次load参数不一样，在下面设置
        if (extraInfo != null) {

            if (extraInfo.getLocalParams() != null) {
                temp = (HashMap<String, Object>) extraInfo.getLocalParams();
            }
            if (!TextUtils.isEmpty(extraInfo.getCustomData())) {
                temp.put("custom_data", extraInfo.getCustomData());
            }
            if (!TextUtils.isEmpty(extraInfo.getUserId())) {
                temp.put("user_id", extraInfo.getUserId());
            }

            tpSplash.setCustomParams(temp);

            if (extraInfo.getCustomMap() != null) {
                SegmentUtils.initPlacementCustomMap(adUnitId, extraInfo.getCustomMap());
            }
        }

        return tpSplash;
    }

    private class TPSplashDownloadListener implements DownloadListener {
        private String mAdUnitId;
        private TPSplashListener listener;

        TPSplashDownloadListener(String adUnitId, TPSplashListener listener) {
            mAdUnitId = adUnitId;
            this.listener = listener;
        }

        @Override
        public void onDownloadStart(TPAdInfo tpAdInfo, long l, long l1, String s, String s1) {
            if (listener != null) {
                listener.onDownloadStart(mAdUnitId, JSON.toJSONString(tpAdInfo), l, l1, s, s1);
            }
            Log.v("TradPlusSdk", "onDownloadStart unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onDownloadUpdate(TPAdInfo tpAdInfo, long l, long l1, String s, String s1, int i) {
            if (listener != null) {
                listener.onDownloadUpdate(mAdUnitId, JSON.toJSONString(tpAdInfo), l, l1, s, s1, i);
            }
            Log.v("TradPlusSdk", "onDownloadUpdate unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onDownloadPause(TPAdInfo tpAdInfo, long l, long l1, String s, String s1) {
            if (listener != null) {
                listener.onDownloadPause(mAdUnitId, JSON.toJSONString(tpAdInfo), l, l1, s, s1);
            }
            Log.v("TradPlusSdk", "onDownloadPause unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onDownloadFinish(TPAdInfo tpAdInfo, long l, long l1, String s, String s1) {
            if (listener != null) {
                listener.onDownloadFinish(mAdUnitId, JSON.toJSONString(tpAdInfo), l, l1, s, s1);
            }
            Log.v("TradPlusSdk", "onDownloadFinish unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onDownloadFail(TPAdInfo tpAdInfo, long l, long l1, String s, String s1) {
            if (listener != null) {
                listener.onDownloadFail(mAdUnitId, JSON.toJSONString(tpAdInfo), l, l1, s, s1);
            }
            Log.v("TradPlusSdk", "onDownloadFail unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onInstalled(TPAdInfo tpAdInfo, long l, long l1, String s, String s1) {
            if (listener != null) {
                listener.onInstalled(mAdUnitId, JSON.toJSONString(tpAdInfo), l, l1, s, s1);
            }
            Log.v("TradPlusSdk", "onInstalled unitid=" + mAdUnitId + "=======================");
        }
    }

    public class TPSplashAllAdListener implements LoadAdEveryLayerListener {
        private String mAdUnitId;
        private TPSplashListener listener;

        TPSplashAllAdListener(String adUnitId, TPSplashListener listener) {
            mAdUnitId = adUnitId;
            this.listener = listener;
        }

        @Override
        public void onAdAllLoaded(boolean b) {
            if (listener != null) {

                listener.onAdAllLoaded(mAdUnitId, b);
            }
            Log.v("TradPlusSdk", "onAdAllLoaded unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void oneLayerLoadFailed(TPAdError tpAdError, TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.oneLayerLoadFailed(mAdUnitId, JSON.toJSONString(tpAdError), JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "oneLayerLoadFailed unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void oneLayerLoaded(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.oneLayerLoaded(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "oneLayerLoaded unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdStartLoad(String s) {
            if (listener != null) {
                listener.onAdStartLoad(mAdUnitId);
            }
            Log.v("TradPlusSdk", "onAdStartLoad unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void oneLayerLoadStart(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.oneLayerLoadStart(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "oneLayerLoadStart unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onBiddingStart(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onBiddingStart(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onBiddingStart unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onBiddingEnd(TPAdInfo tpAdInfo, TPAdError tpAdError) {
            if (listener != null) {
                listener.onBiddingEnd(mAdUnitId, JSON.toJSONString(tpAdInfo), JSON.toJSONString(tpAdError));
            }
            Log.v("TradPlusSdk", "onBiddingEnd unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdIsLoading(String s) {
            if (listener != null) {
                listener.onAdIsLoading(mAdUnitId);
            }
            Log.v("TradPlusSdk", "onAdIsLoading unitid=" + mAdUnitId + "=======================");
        }
    }

    private class TPSplashAdListener extends SplashAdListener {
        private String mAdUnitId;
        private TPSplashListener listener;

        TPSplashAdListener(String adUnitId, TPSplashListener listener) {
            mAdUnitId = adUnitId;
            this.listener = listener;
        }

        @Override
        public void onAdLoaded(TPAdInfo tpAdInfo, TPBaseAd tpBaseAd) {
            if (listener != null) {
                listener.onAdLoaded(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "loaded unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdClicked(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onAdClicked(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdClicked unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdClosed(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onAdClosed(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }

            if (TPSplashShowActivity.instance != null) {
                TPSplashShowActivity.instance.finish();
                TPSplashShowActivity.instance = null;
            }
            Log.v("TradPlusSdk", "onAdClosed unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdImpression(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onAdImpression(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdImpression unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdLoadFailed(TPAdError tpAdError) {
            if (listener != null) {
                listener.onAdLoadFailed(mAdUnitId, JSON.toJSONString(tpAdError));
            }
        }

        @Override
        public void onZoomOutEnd(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onZoomOutEnd(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
        }

        @Override
        public void onZoomOutStart(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onZoomOutStart(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
        }

    }
}

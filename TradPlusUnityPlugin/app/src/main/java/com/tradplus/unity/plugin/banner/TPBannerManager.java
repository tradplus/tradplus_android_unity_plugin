package com.tradplus.unity.plugin.banner;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.tradplus.ads.base.bean.TPAdError;
import com.tradplus.ads.base.bean.TPAdInfo;
import com.tradplus.ads.base.common.TPTaskManager;
import com.tradplus.ads.base.util.SegmentUtils;
import com.tradplus.ads.common.serialization.JSON;
import com.tradplus.ads.common.util.ResourceUtils;
import com.tradplus.ads.common.util.ScreenUtil;
import com.tradplus.ads.core.AdCacheManager;
import com.tradplus.ads.core.cache.AdCache;
import com.tradplus.ads.mgr.nativead.TPNativeAdRenderImpl;
import com.tradplus.ads.open.DownloadListener;
import com.tradplus.ads.open.LoadAdEveryLayerListener;
import com.tradplus.ads.open.banner.BannerAdListener;
import com.tradplus.ads.open.banner.TPBanner;
import com.tradplus.unity.plugin.common.BaseUnityPlugin;
import com.tradplus.unity.plugin.common.ExtraInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TPBannerManager extends BaseUnityPlugin {
    private static TPBannerManager sInstance;


    private TPBannerManager() {
    }

    public synchronized static TPBannerManager getInstance() {
        if (sInstance == null) {
            sInstance = new TPBannerManager();
        }
        return sInstance;
    }

    // 保存广告位对象
    private Map<String, TPBanner> mTPBanner = new ConcurrentHashMap<>();

    public void loadAd(String unitId, String sceneId, String data, TPBannerListener listener) {
        TPBanner tpBanner = getTPBanner(unitId, data, listener);

        if (tpBanner != null) {
            tpBanner.loadAd(unitId, sceneId);
        }

    }

    public void closeAutoshow(String unitId) {
        TPBanner tpBanner = getTPBanner(unitId);

        if (tpBanner != null) {
            tpBanner.closeAutoShow();
        }
    }

    public void showAd(String unitId, String sceneId) {
        TPBanner tpBanner = getTPBanner(unitId);

        if (tpBanner != null) {
            tpBanner.showAd();
        }

    }

    public void entryAdScenario(String unitId, String sceneId) {
        TPBanner tpBanner = getTPBanner(unitId);

        if (tpBanner != null) {
            tpBanner.entryAdScenario(sceneId);
        }

    }

    public boolean isReady(String unitId) {
        TPBanner tpBanner = getTPBanner(unitId);

        if (tpBanner != null) {
            return tpBanner.isReady();
        }

        return false;
    }

    public void hideBanner(String adUnitId) {
        TPBanner tpBanner = getTPBanner(adUnitId);

        TPTaskManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (tpBanner != null) {
                    tpBanner.setVisibility(View.GONE);
                }
            }
        });

    }

    public void displayBanner(String adUnitId) {
        TPBanner tpBanner = getTPBanner(adUnitId);
        TPTaskManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (tpBanner != null) {
                    tpBanner.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    public void destroyBanner(String adUnitId) {
        TPBanner tpBanner = getTPBanner(adUnitId);


        TPTaskManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (tpBanner != null) {
                    if (tpBanner.getParent() != null) {
                        ((ViewGroup) tpBanner.getParent()).removeView(tpBanner);
                    }
                    tpBanner.onDestroy();
                    mTPBanner.remove(adUnitId);
                }
            }
        });

    }

    public void setCustomShowData(String adUnitId,String data) {
        TPBanner tpBanner = getTPBanner(adUnitId);

        if (tpBanner != null) {
            tpBanner.setCustomShowData(JSON.parseObject(data));
        }
    }


    private TPBanner getTPBanner(String adUnitId) {
        return mTPBanner.get(adUnitId);
    }

    private TPBanner getTPBanner(String adUnitId, String data, TPBannerListener listener) {

        Log.i("tradplus", "data = " + data + " mTPBanner = " + mTPBanner + " listener = " + listener);

        ExtraInfo extraInfo = null;
        if (!TextUtils.isEmpty(data)) {
            extraInfo = JSON.parseObject(data, ExtraInfo.class);
        }

        HashMap<String, Object> temp = new HashMap<>();


        TPBanner tpBanner = mTPBanner.get(adUnitId);
        if (tpBanner == null) {
            tpBanner = new TPBanner(getActivity());
            mTPBanner.put(adUnitId, tpBanner);

            boolean closeAutoShow = extraInfo == null ? false : extraInfo.isCloseAutoShow();
            boolean isSimpleListener = extraInfo == null ? false : extraInfo.isSimpleListener();
            if (closeAutoShow) {
                tpBanner.closeAutoShow();
            }
            tpBanner.setAdListener(new TPBannerAdListener(adUnitId, listener));
            if (!isSimpleListener) {
                tpBanner.setAllAdLoadListener(new TPBannerAllAdListener(adUnitId, listener));
                tpBanner.setDownloadListener(new TPBannerDownloadListener(adUnitId, listener));
            }

            String className = extraInfo == null ? "" : (TextUtils.isEmpty(extraInfo.getClassName()) ? "" : extraInfo.getClassName());

            if (!TextUtils.isEmpty(className)) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ViewGroup layoutView = (ViewGroup) inflater.inflate(ResourceUtils.getLayoutIdByName(getActivity(), className), null);
                TPNativeAdRenderImpl adRender = new TPNativeAdRenderImpl(getActivity(), layoutView);
                tpBanner.setNativeAdRender(adRender);
            }


            TPBanner finalTpBanner = tpBanner;
            ExtraInfo finalExtraInfo = extraInfo;
            TPTaskManager.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    RelativeLayout mLayout = null;
                    boolean hasPosition = false;
                    int x = 0, y = 0;
                    if (finalExtraInfo != null) {
                        if (finalExtraInfo.getX() != 0 || finalExtraInfo.getY() != 0) {
                            x = (int) finalExtraInfo.getX();
                            y = (int) finalExtraInfo.getY();
                            hasPosition = true;
                        }
                    }
                    mLayout = ScreenUtil.prepLayout(hasPosition ? 0 : (finalExtraInfo == null ? 0 : finalExtraInfo.getAdPosition()), mLayout, getActivity());

                    getActivity().addContentView(mLayout,
                            new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                    mLayout.removeAllViews();

                    mLayout.setVisibility(RelativeLayout.VISIBLE);


                    if (hasPosition) {
                        //设置锚点
                        finalTpBanner.setX(x);
                        finalTpBanner.setY(y);
                    }

                    mLayout.addView(finalTpBanner);

                    RelativeLayout.LayoutParams params =
                            (RelativeLayout.LayoutParams) finalTpBanner.getLayoutParams();

                    float density = ScreenUtil.getScreenDensity(getActivity());

                    if(finalExtraInfo != null) {
                        params.width = (int) (finalExtraInfo.getWidth() * density);
                        params.height = (int) (finalExtraInfo.getHeight() * density);
                    }else{
                        params.width = (int) (320 * density);
                        params.height = (int) (50 * density);
                    }

                    finalTpBanner.setLayoutParams(params);
                }
            });

        }

        if (extraInfo != null) {

            if (extraInfo.getLocalParams() != null) {
                temp = (HashMap<String, Object>) extraInfo.getLocalParams();
            }

            if (extraInfo.getWidth() != 0) {
                temp.put("width", (int) extraInfo.getWidth());
            }
            if (extraInfo.getHeight() != 0) {
                temp.put("height", (int) extraInfo.getHeight());
            }

            Log.v("TradPlusSdk", "setCustomParams unitid=" + adUnitId + "=======================" + JSON.toJSONString(temp));

            tpBanner.setCustomParams(temp);

            if (extraInfo.getCustomMap() != null) {
                SegmentUtils.initPlacementCustomMap(adUnitId, extraInfo.getCustomMap());
            }
        }

        return tpBanner;
    }

    private TPAdInfo getShowAdInfo(String unitId) {
        AdCache cache = AdCacheManager.getInstance().getReadyAd(unitId);
        if (cache == null) return null;
        return new TPAdInfo(unitId, cache.getAdapter());
    }



    private class TPBannerDownloadListener implements DownloadListener {
        private String mAdUnitId;
        private TPBannerListener listener;

        TPBannerDownloadListener(String adUnitId, TPBannerListener listener) {
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

    public class TPBannerAllAdListener implements LoadAdEveryLayerListener {
        private String mAdUnitId;
        private TPBannerListener listener;

        TPBannerAllAdListener(String adUnitId, TPBannerListener listener) {
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


    private class TPBannerAdListener extends BannerAdListener {
        private String mAdUnitId;
        private TPBannerListener listener;

        TPBannerAdListener(String adUnitId, TPBannerListener listener) {
            mAdUnitId = adUnitId;
            this.listener = listener;
        }

        @Override
        public void onAdLoaded(TPAdInfo tpAdInfo) {
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
        public void onAdShowFailed(TPAdError tpAdError, TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onAdShowFailed(mAdUnitId, JSON.toJSONString(tpAdInfo), JSON.toJSONString(tpAdError));
            }
            Log.v("TradPlusSdk", "onAdVideoError unitid=" + mAdUnitId + "=======================");
        }


    }
}

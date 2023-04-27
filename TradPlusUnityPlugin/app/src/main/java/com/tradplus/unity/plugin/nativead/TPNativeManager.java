package com.tradplus.unity.plugin.nativead;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.tradplus.ads.base.bean.TPAdError;
import com.tradplus.ads.base.bean.TPAdInfo;
import com.tradplus.ads.base.bean.TPBaseAd;
import com.tradplus.ads.base.common.TPTaskManager;
import com.tradplus.ads.base.util.SegmentUtils;
import com.tradplus.ads.common.serialization.JSON;
import com.tradplus.ads.common.util.ResourceUtils;
import com.tradplus.ads.common.util.ScreenUtil;
import com.tradplus.ads.core.AdCacheManager;
import com.tradplus.ads.open.DownloadListener;
import com.tradplus.ads.open.LoadAdEveryLayerListener;
import com.tradplus.ads.open.nativead.NativeAdListener;
import com.tradplus.ads.open.nativead.TPNative;
import com.tradplus.unity.plugin.common.BaseUnityPlugin;
import com.tradplus.unity.plugin.common.ExtraInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TPNativeManager extends BaseUnityPlugin {
    private static TPNativeManager sInstance;


    private TPNativeManager() {
    }

    public synchronized static TPNativeManager getInstance() {
        if (sInstance == null) {
            sInstance = new TPNativeManager();
        }
        return sInstance;
    }

    // 保存广告位对象
    private Map<String, TPNativeInfo> mTPNative = new ConcurrentHashMap<>();


    public void loadAd(String unitId, String data, TPNativeListener listener) {
        TPNative tpNative = getTPNative(unitId, data, listener).getTpNative();

        if (tpNative != null) {
            tpNative.loadAd();
        }

    }


    public void showAd(String unitId, String sceneId, String layoutName) {
        if(isReady(unitId)) {
            show(getTPNative(unitId), sceneId, layoutName);
        }

    }

    public void entryAdScenario(String unitId, String sceneId) {
        TPNativeInfo tpNativeInfo = getTPNative(unitId);
        if(tpNativeInfo == null) return;
        TPNative tpNative = tpNativeInfo.getTpNative();

        if (tpNative != null) {
            tpNative.entryAdScenario(sceneId);
        }

    }

    public boolean isReady(String unitId) {
        TPNativeInfo tpNativeInfo = getTPNative(unitId);
        if(tpNativeInfo == null) return false;

        TPNative tpNative = tpNativeInfo.getTpNative();
        if (tpNative != null) {
            return tpNative.isReady();
        }

        return false;
    }


    public void setCustomShowData(String adUnitId,String data){
        TPNativeInfo tpNativeInfo = getTPNative(adUnitId);
        if(tpNativeInfo == null) return;
        TPNative tpNative = tpNativeInfo.getTpNative();

        if(tpNative != null){
            tpNative.setCustomShowData(JSON.parseObject(data));
        }
    }

    private void show(TPNativeInfo info, String sceneId, String layoutName) {

        if (info == null) return;

        TPTaskManager.getInstance().runOnMainThread(new Runnable() {
            public void run() {
                float density = ScreenUtil.getScreenDensity(getActivity());
                int height = 340;
                int width = 320;
                int x = 0, y = 0;
                TPNative tpNative = info.getTpNative();
                RelativeLayout layout = null;
                boolean hasPosition = false;
                ExtraInfo extraInfo = null;
                if (info != null) {
                    extraInfo = info.getExtraInfo();
                    if (extraInfo != null) {
                        if (extraInfo.getWidth() != 0 && extraInfo.getHeight() != 0) {
                            width = (int) extraInfo.getWidth();
                            height = (int) extraInfo.getHeight();
                        }

                        if (extraInfo.getX() != 0 || extraInfo.getY() != 0) {
                            x = (int) extraInfo.getX();
                            y = (int) extraInfo.getY();
                            hasPosition = true;
                        }

                    }
                }

                if (info.getParentView() == null) {
                    layout = ScreenUtil.prepLayout(hasPosition ? 0 : (extraInfo == null ? 0 : extraInfo.getAdPosition()), layout, getActivity());

                    getActivity().addContentView(layout,
                            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                } else {
                    layout = info.getParentView();
                    layout.removeAllViews();
                }
                FrameLayout frameLayout = new FrameLayout(getActivity());
                frameLayout.setLayoutParams(new RelativeLayout.LayoutParams((int) (width * density), (int) (height * density)));

                if (hasPosition) {
                    //设置锚点
                    frameLayout.setX(x);
                    frameLayout.setY(y);
                }
                layout.addView(frameLayout);
                info.setParentView(layout);


                layout.setVisibility(View.VISIBLE);


                if (mTPNative != null)
                    tpNative.showAd(frameLayout, ResourceUtils.getLayoutIdByName(getActivity(), TextUtils.isEmpty(layoutName) ? "tp_native_ad_list_item" : layoutName), sceneId);
            }
        });
    }

    public void hideBanner(String adUnitId) {
        TPNativeInfo tpNativeInfo = getTPNative(adUnitId);

        TPTaskManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (tpNativeInfo != null) {
                    if (tpNativeInfo.getParentView() != null) {
                        tpNativeInfo.getParentView().setVisibility(View.GONE);
                    }
                }
            }
        });

    }

    public void displayBanner(String adUnitId) {
        TPNativeInfo tpNativeInfo = getTPNative(adUnitId);

        TPTaskManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (tpNativeInfo != null) {
                    if (tpNativeInfo.getParentView() != null) {
                        tpNativeInfo.getParentView().setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }


    public void destroyBanner(String adUnitId) {
        TPNativeInfo tpNativeInfo = getTPNative(adUnitId);


        TPTaskManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (tpNativeInfo != null) {
                    if (tpNativeInfo.getParentView() != null) {
                        tpNativeInfo.getParentView().removeAllViews();
                    }

                    if (tpNativeInfo.getTpNative() != null) {
                        tpNativeInfo.getTpNative().onDestroy();
                    }

                    mTPNative.remove(adUnitId);

                }
            }
        });

    }


    private TPNativeInfo getTPNative(String adUnitId) {
        return mTPNative.get(adUnitId);
    }

    private TPNativeInfo getTPNative(String adUnitId, String data, TPNativeListener listener) {

        Log.i("tradplus", "data = " + data + " mTPNative = " + mTPNative + " listener = " + listener);

        ExtraInfo extraInfo = null;
        if (!TextUtils.isEmpty(data)) {
            extraInfo = JSON.parseObject(data, ExtraInfo.class);
        }

        HashMap<String, Object> temp = new HashMap<>();

        TPNativeInfo tpNativeInfo = mTPNative.get(adUnitId);
        TPNative tpNative;
        if (tpNativeInfo == null) {
            tpNativeInfo = new TPNativeInfo();
            mTPNative.put(adUnitId, tpNativeInfo);

            tpNative = new TPNative(getActivity(), adUnitId);


            boolean isSimpleListener = extraInfo == null ? false : extraInfo.isSimpleListener();

            tpNative.setAdListener(new TPNativeAdListener(adUnitId, listener));
            if(!isSimpleListener) {
                tpNative.setAllAdLoadListener(new TPNativeAllAdListener(adUnitId, listener));
                tpNative.setDownloadListener(new TPNativeDownloadListener(adUnitId, listener));
            }


            tpNativeInfo.setTpNative(tpNative);
            tpNativeInfo.setExtraInfo(extraInfo);


        } else {
            tpNative = tpNativeInfo.getTpNative();
        }

        if (extraInfo != null) {

            if (extraInfo.getLocalParams() != null) {
                temp = (HashMap<String, Object>) extraInfo.getLocalParams();
            }

            float width = 0, height = 0;
            if (extraInfo.getWidth() != 0) {
                width = extraInfo.getWidth();
            }
            if (extraInfo.getHeight() != 0) {
                height = extraInfo.getHeight();
            }

            if (width != 0 && height != 0) {
                tpNative.setAdSize((int) width, (int) height);
            }

            tpNative.setCustomParams(temp);

            if (extraInfo.getCustomMap() != null) {
                SegmentUtils.initPlacementCustomMap(adUnitId, extraInfo.getCustomMap());
            }
        }

        return tpNativeInfo;
    }


    private class TPNativeDownloadListener implements DownloadListener {
        private String mAdUnitId;
        private TPNativeListener listener;

        TPNativeDownloadListener(String adUnitId, TPNativeListener listener) {
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

    public class TPNativeAllAdListener implements LoadAdEveryLayerListener {
        private String mAdUnitId;
        private TPNativeListener listener;

        TPNativeAllAdListener(String adUnitId, TPNativeListener listener) {
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

    private class TPNativeAdListener extends NativeAdListener {
        private String mAdUnitId;
        private TPNativeListener listener;

        TPNativeAdListener(String adUnitId, TPNativeListener listener) {
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
            TPNativeInfo tpNativeInfo = getTPNative(mAdUnitId);

            if (tpNativeInfo != null) {
                if (tpNativeInfo.getParentView() != null) {
                    tpNativeInfo.getParentView().removeAllViews();
                }
            }
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

    private class TPNativeInfo {
        private TPNative tpNative;
        private ExtraInfo extraInfo;
        private RelativeLayout parentView;


        public TPNativeInfo() {

        }

        public ExtraInfo getExtraInfo() {
            return extraInfo;
        }

        public void setExtraInfo(ExtraInfo extraInfo) {
            this.extraInfo = extraInfo;
        }

        public TPNative getTpNative() {
            return tpNative;
        }

        public void setTpNative(TPNative tpNative) {
            this.tpNative = tpNative;
        }

        public RelativeLayout getParentView() {
            return parentView;
        }

        public void setParentView(RelativeLayout parentView) {
            this.parentView = parentView;
        }
    }
}

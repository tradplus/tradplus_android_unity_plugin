package com.tradplus.unity.plugin.nativebanner;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.tradplus.ads.base.CommonUtil;
import com.tradplus.ads.base.bean.TPAdError;
import com.tradplus.ads.base.bean.TPAdInfo;
import com.tradplus.ads.base.common.TPTaskManager;
import com.tradplus.ads.base.util.SegmentUtils;
import com.tradplus.ads.common.serialization.JSON;
import com.tradplus.ads.common.util.DeviceUtils;
import com.tradplus.ads.common.util.ResourceUtils;
import com.tradplus.ads.common.util.ScreenUtil;
import com.tradplus.ads.core.AdCacheManager;
import com.tradplus.ads.core.cache.AdCache;
import com.tradplus.ads.mgr.nativead.TPNativeAdRenderImpl;
import com.tradplus.ads.open.DownloadListener;
import com.tradplus.ads.open.LoadAdEveryLayerListener;
import com.tradplus.ads.open.banner.BannerAdListener;
import com.tradplus.ads.open.nativead.TPNativeBanner;
import com.tradplus.unity.plugin.TPUtils;
import com.tradplus.unity.plugin.common.BaseUnityPlugin;
import com.tradplus.unity.plugin.common.ExtraInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TPNativeBannerManager extends BaseUnityPlugin {
    private static TPNativeBannerManager sInstance;


    private TPNativeBannerManager() {
    }

    public synchronized static TPNativeBannerManager getInstance() {
        if (sInstance == null) {
            sInstance = new TPNativeBannerManager();
        }
        return sInstance;
    }

    // 保存广告位对象
    private Map<String, TPNativeBannerInfo> mTPNativeBanner = new ConcurrentHashMap<>();

    public void loadAd(String unitId, String sceneId, String data, TPNativeBannerListener listener) {
        TPNativeBanner tpNativeBanner = getTPNativeBanner(unitId, data, listener).getTpNativeBanner();

        ExtraInfo extraInfo = ExtraInfo.getExtraInfo(data);
        if(extraInfo != null){
            tpNativeBanner.setAutoLoadCallback(extraInfo.isOpenAutoLoadCallback());
        }

        if (tpNativeBanner != null) {
            tpNativeBanner.loadAd(unitId, sceneId,extraInfo == null ? 0f : extraInfo.getMaxWaitTime());
        }

    }

    public void closeAutoshow(String unitId) {
        TPNativeBannerInfo tpNativeBannerInfo = getTPNativeBanner(unitId);
        if(tpNativeBannerInfo == null) return;
        TPNativeBanner tpNativeBanner = tpNativeBannerInfo.getTpNativeBanner();

        if (tpNativeBanner != null) {
            tpNativeBanner.closeAutoShow();
        }
    }

    public void showAd(String unitId, String sceneId) {
        TPNativeBannerInfo tpNativeBannerInfo = getTPNativeBanner(unitId);
        if(tpNativeBannerInfo == null) return;
        TPNativeBanner tpNativeBanner = tpNativeBannerInfo.getTpNativeBanner();


        if (tpNativeBanner != null && isReady(unitId)) {
            TPAdInfo tpAdInfo = getShowAdInfo(unitId);
            tpNativeBanner.showAd(sceneId);
            setShowParam(unitId, tpAdInfo);
        }

    }

    public void entryAdScenario(String unitId, String sceneId) {
        TPNativeBannerInfo tpNativeBannerInfo = getTPNativeBanner(unitId);
        if(tpNativeBannerInfo == null) return;
        TPNativeBanner tpNativeBanner = tpNativeBannerInfo.getTpNativeBanner();
        if (tpNativeBanner != null) {
            tpNativeBanner.entryAdScenario(sceneId);
        }

    }

    public boolean isReady(String unitId) {
        TPNativeBannerInfo tpNativeBannerInfo = getTPNativeBanner(unitId);
        if(tpNativeBannerInfo == null) return false;
        TPNativeBanner tpNativeBanner = tpNativeBannerInfo.getTpNativeBanner();

        if (tpNativeBanner != null) {
            return tpNativeBanner.isReady();
        }

        return false;
    }

    public void hideBanner(String adUnitId) {
        TPNativeBannerInfo tpNativeBannerInfo = getTPNativeBanner(adUnitId);
        if(tpNativeBannerInfo == null) return;
        TPNativeBanner tpNativeBanner = tpNativeBannerInfo.getTpNativeBanner();
        TPTaskManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (tpNativeBanner != null) {
                    tpNativeBanner.setVisibility(View.GONE);
                }
            }
        });

    }

    public void displayBanner(String adUnitId) {
        TPNativeBannerInfo tpNativeBannerInfo = getTPNativeBanner(adUnitId);
        if(tpNativeBannerInfo == null) return;
        TPNativeBanner tpNativeBanner = tpNativeBannerInfo.getTpNativeBanner();
        TPTaskManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (tpNativeBanner != null) {
                    tpNativeBanner.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    public void destroyBanner(String adUnitId) {
        TPNativeBannerInfo tpNativeBannerInfo = getTPNativeBanner(adUnitId);
        if(tpNativeBannerInfo == null) return;
        TPNativeBanner tpNativeBanner = tpNativeBannerInfo.getTpNativeBanner();

        TPTaskManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (tpNativeBanner != null) {
                    if (tpNativeBanner.getParent() != null) {
                        ((ViewGroup) tpNativeBanner.getParent()).removeView(tpNativeBanner);
                    }
                    tpNativeBanner.onDestroy();
                    mTPNativeBanner.remove(adUnitId);
                }
            }
        });

    }


    public void setCustomShowData(String adUnitId, String data) {
        TPNativeBannerInfo tpNativeBannerInfo = getTPNativeBanner(adUnitId);
        if(tpNativeBannerInfo == null) return;
        TPNativeBanner tpNativeBanner = tpNativeBannerInfo.getTpNativeBanner();
        if (tpNativeBanner != null) {
            tpNativeBanner.setCustomShowData(JSON.parseObject(data));
        }
    }


    private TPNativeBannerInfo getTPNativeBanner(String adUnitId) {
        return mTPNativeBanner.get(adUnitId);
    }

    private TPNativeBannerInfo getTPNativeBanner(String adUnitId, String data, TPNativeBannerListener listener) {

        Log.i("tradplus", "data = " + data + " mTPNativeBanner = " + mTPNativeBanner + " listener = " + listener);

        ExtraInfo extraInfo = null;
        if (!TextUtils.isEmpty(data)) {
            extraInfo = JSON.parseObject(data, ExtraInfo.class);
        }

        HashMap<String, Object> temp = new HashMap<>();
        TPNativeBannerInfo tpNativeBannerInfo = mTPNativeBanner.get(adUnitId);
        TPNativeBanner tpNativeBanner = null;
        if (tpNativeBannerInfo == null) {
            tpNativeBannerInfo = new TPNativeBannerInfo();
            mTPNativeBanner.put(adUnitId, tpNativeBannerInfo);
            tpNativeBanner = new TPNativeBanner(getActivity());
            tpNativeBanner.closeAutoShow();
            tpNativeBannerInfo.setTpNativeBanner(tpNativeBanner);


            boolean closeAutoShow = extraInfo == null ? false : extraInfo.isCloseAutoShow();
            if (closeAutoShow) {
                tpNativeBanner.closeAutoShow();
            }

            tpNativeBannerInfo.setCloseAutoShow(closeAutoShow);
            tpNativeBannerInfo.setWidth(extraInfo == null ? DeviceUtils.getScreenWidth(getActivity()) : extraInfo.getWidth());
            tpNativeBannerInfo.setHeight(extraInfo == null ? 50 : extraInfo.getHeight());

            Log.i("nativebanner", "setWidth: " +tpNativeBannerInfo.getWidth());
            String className = extraInfo == null ? "" : (TextUtils.isEmpty(extraInfo.getClassName()) ? "" : extraInfo.getClassName());

            if (!TextUtils.isEmpty(className)) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ViewGroup layoutView = (ViewGroup) inflater.inflate(ResourceUtils.getLayoutIdByName(getActivity(), className), null);
                TPNativeAdRenderImpl adRender = new TPNativeAdRenderImpl(getActivity(), layoutView);
                tpNativeBanner.setNativeAdRender(adRender);
            }

            boolean isSimpleListener = extraInfo == null ? false : extraInfo.isSimpleListener();


            tpNativeBanner.setAdListener(new TPBannerAdListener(adUnitId, listener));
            if (!isSimpleListener) {
                tpNativeBanner.setAllAdLoadListener(new TPBannerAllAdListener(adUnitId, listener));
                tpNativeBanner.setDownloadListener(new TPBannerDownloadListener(adUnitId, listener));
            }


            TPNativeBanner finalTpBanner = tpNativeBanner;
            ExtraInfo finalExtraInfo = extraInfo;
            TPTaskManager.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    final float density = ScreenUtil.getScreenDensity(getActivity());

                    RelativeLayout mRelativeLayout;
                    RelativeLayout mMainRelativeLayout = null;
                    boolean hasPosition = false;
                    int x = 0, y = 0;
                    if (finalExtraInfo != null) {
                        if (finalExtraInfo.getX() != 0 || finalExtraInfo.getY() != 0) {
                            x = (int) finalExtraInfo.getX();
                            y = (int) finalExtraInfo.getY();
                            hasPosition = true;
                        }
                    }

                    mMainRelativeLayout = ScreenUtil.prepLayout(hasPosition ? 0 : (finalExtraInfo == null ? 0 : finalExtraInfo.getAdPosition()), mMainRelativeLayout, getActivity());

                    getActivity().addContentView(mMainRelativeLayout,
                            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    mMainRelativeLayout.removeAllViews();
                    mRelativeLayout = new RelativeLayout(getActivity());

                    int layoutParamsWidth = ViewGroup.LayoutParams.MATCH_PARENT;
                    if (finalExtraInfo != null && finalExtraInfo.getWidth() != 0) {
                        layoutParamsWidth = (int) (finalExtraInfo.getWidth() * density);
                    }
                    Log.i("nativebanner", "layoutParamsWidth: " +layoutParamsWidth);

                    mRelativeLayout.setLayoutParams(new ViewGroup.LayoutParams(layoutParamsWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

                    mRelativeLayout.removeAllViews();
                    if (finalTpBanner.getParent() != null) {
                        ((ViewGroup) finalTpBanner.getParent()).removeView(finalTpBanner);
                    }
                    mRelativeLayout.addView(finalTpBanner);


                    if (hasPosition) {
                        //设置锚点
                        mRelativeLayout.setX(x);
                        mRelativeLayout.setY(y);
                    }

                    mMainRelativeLayout.addView(mRelativeLayout);

                }
            });

        } else {
            tpNativeBanner = tpNativeBannerInfo.getTpNativeBanner();
        }

        if (extraInfo != null) {

            if (extraInfo.getLocalParams() != null) {
                temp = (HashMap<String, Object>) extraInfo.getLocalParams();
            }

            if (extraInfo.getWidth() != 0) {
                temp.put("width", extraInfo.getWidth());
            }
            if (extraInfo.getHeight() != 0) {
                temp.put("height", extraInfo.getHeight());
            }

            tpNativeBanner.setCustomParams(temp);

            if (extraInfo.getCustomMap() != null) {
                SegmentUtils.initPlacementCustomMap(adUnitId, extraInfo.getCustomMap());
            }
        }

        return tpNativeBannerInfo;
    }

    private TPAdInfo getShowAdInfo(String unitId) {
        AdCache cache = AdCacheManager.getInstance().getReadyAd(unitId);
        if (cache == null) return null;
        return new TPAdInfo(unitId, cache.getAdapter());
    }

    private void setShowParam(String unitId, TPAdInfo tpAdInfo) {
        final float density = ScreenUtil.getScreenDensity(getActivity());


        if (tpAdInfo != null && tpAdInfo.height != 0 && "audience-network".equals(tpAdInfo.adSourceName)) {
        } else {
            TPNativeBannerInfo tpNativeBannerInfo = getTPNativeBanner(unitId);
            if(tpNativeBannerInfo == null) return;
            TPNativeBanner tpNativeBanner = tpNativeBannerInfo.getTpNativeBanner();


            if (tpNativeBanner != null) {

                TPTaskManager.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {

                        if (tpNativeBannerInfo == null || tpAdInfo == null) return;

                        float width = tpNativeBannerInfo.getWidth();
                        float height = tpNativeBannerInfo.getHeight();

                        int screenWidth = TPUtils.getScreenWidth(getActivity());

                        // 获取nativebanner对应的布局
                        View viewById = tpNativeBanner.findViewById(CommonUtil.getResId(getActivity(), "tp_ll_nativebanner", "id"));
                        View ad_Choices = tpNativeBanner.findViewById(CommonUtil.getResId(getActivity(), "tp_ll_ad_choices", "id"));

                        if(ad_Choices != null){
                            ViewGroup.LayoutParams paramsChoices = ad_Choices.getLayoutParams();
                            if (width == 0) {
                                paramsChoices.width = screenWidth;
                            } else {
                                paramsChoices.width = (int) (width * density);
                            }
                            Log.i("nativebanner", "paramsChoices.width: " +paramsChoices.width);
                            ad_Choices.setLayoutParams(paramsChoices);
                        }


                        if(viewById != null){
                            ViewGroup.LayoutParams params = viewById.getLayoutParams();

                            if (width == 0) {
                                params.width =  screenWidth;
                            } else {
                                params.width = (int) (width * density);
                            }
                            Log.i("nativebanner", "params.width: " +params.width);
                            if (height == 0) {
                                height = 50;
                            }
                            params.height = (int) (height * density);

                            viewById.setLayoutParams(params);
                        }

                    }
                });

            }
        }
    }




    private class TPBannerDownloadListener implements DownloadListener {
        private String mAdUnitId;
        private TPNativeBannerListener listener;

        TPBannerDownloadListener(String adUnitId, TPNativeBannerListener listener) {
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
        private TPNativeBannerListener listener;

        TPBannerAllAdListener(String adUnitId, TPNativeBannerListener listener) {
            mAdUnitId = adUnitId;
            this.listener = listener;
        }

        @Override
        public void onAdAllLoaded(boolean b) {
            if (listener != null) {
//                Log.v("TradPlusSdk", "onAdAllLoaded1111111 unitid=" + mAdUnitId + "=======================");

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
        private TPNativeBannerListener listener;


        TPBannerAdListener(String adUnitId, TPNativeBannerListener listener) {
            mAdUnitId = adUnitId;
            this.listener = listener;
        }

        @Override
        public void onAdLoaded(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onAdLoaded(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }

            TPNativeBannerInfo tpNativeBannerInfo = getTPNativeBanner(mAdUnitId);
            if(tpNativeBannerInfo == null) return;

            if (!tpNativeBannerInfo.isCloseAutoShow()) {
                TPNativeBanner tpNativeBanner = tpNativeBannerInfo.getTpNativeBanner();
                if (tpNativeBanner != null) {
                    tpNativeBanner.showAd();
                    setShowParam(mAdUnitId, getShowAdInfo(mAdUnitId));
                }
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

    private class TPNativeBannerInfo {
        private TPNativeBanner tpNativeBanner;
        private float width;
        private float height;
        private boolean closeAutoShow;


        public TPNativeBannerInfo() {

        }

        public TPNativeBanner getTpNativeBanner() {
            return tpNativeBanner;
        }

        public void setTpNativeBanner(TPNativeBanner tpNativeBanner) {
            this.tpNativeBanner = tpNativeBanner;
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

        public boolean isCloseAutoShow() {
            return closeAutoShow;
        }

        public void setCloseAutoShow(boolean closeAutoShow) {
            this.closeAutoShow = closeAutoShow;
        }
    }
}

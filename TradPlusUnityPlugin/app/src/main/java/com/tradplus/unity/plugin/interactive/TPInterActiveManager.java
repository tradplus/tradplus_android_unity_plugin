package com.tradplus.unity.plugin.interactive;


import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.tradplus.ads.base.bean.TPAdError;
import com.tradplus.ads.base.bean.TPAdInfo;
import com.tradplus.ads.base.common.TPTaskManager;
import com.tradplus.ads.base.util.SegmentUtils;
import com.tradplus.ads.common.serialization.JSON;
import com.tradplus.ads.common.util.ScreenUtil;
import com.tradplus.ads.core.AdCacheManager;
import com.tradplus.ads.core.cache.AdCache;
import com.tradplus.ads.open.LoadAdEveryLayerListener;
import com.tradplus.ads.open.interactive.InterActiveAdListener;
import com.tradplus.ads.open.interactive.TPInterActive;
import com.tradplus.unity.plugin.common.BaseUnityPlugin;
import com.tradplus.unity.plugin.common.ExtraInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TPInterActiveManager extends BaseUnityPlugin {

    private static final String TAG = "TPInterActiveManager";
    private static TPInterActiveManager sInstance;


    private TPInterActiveManager() {
    }

    public synchronized static TPInterActiveManager getInstance() {
        if (sInstance == null) {
            sInstance = new TPInterActiveManager();
        }
        return sInstance;
    }

    // 保存广告位对象
    private Map<String, TPInterActiveInfo> mTPInterActive = new ConcurrentHashMap<>();


    public void loadAd(String unitId, String data, TPInterActiveListener listener) {
        TPInterActive tpInterActive = getTPInterActive(unitId, data, listener).getTpInterActive();

        if (tpInterActive != null) {
            tpInterActive.loadAd();
        }
    }

    public void showAd(String unitId, String sceneId) {
        if (isReady(unitId)) {
            showInterActive(getTPInterActive(unitId), sceneId);
        }
    }

    private void showInterActive(TPInterActiveInfo tpInterActiveInfo, String sceneId) {
        if (tpInterActiveInfo == null) return;
        Log.i(TAG, "showInterActive: ");

        TPTaskManager.getInstance().runOnMainThread(new Runnable() {

            @Override
            public void run() {
                float density = ScreenUtil.getScreenDensity(getActivity());
                int height = 50;
                int width = 50;
                int x = 0, y = 0;

                TPInterActive tpInterActive = tpInterActiveInfo.getTpInterActive();
                if (tpInterActive == null) return;

                View interActiveAd = tpInterActive.getInterActiveAd();
                if (interActiveAd == null) return;

                Log.i(TAG, "interActiveAd: " + interActiveAd);
                RelativeLayout layout = null;
                boolean hasPosition = false;
                ExtraInfo extraInfo = null;


                extraInfo = tpInterActiveInfo.getExtraInfo();
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


                if (tpInterActiveInfo.getParentView() == null) {
                    layout = ScreenUtil.prepLayout(0, layout, getActivity());
                    getActivity().addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                } else {
                    layout = tpInterActiveInfo.getParentView();
                    layout.removeAllViews();
                }
                Log.i(TAG, "width: " + width + ", height:" + height + ", x:" + x + ", y:" + y);
                FrameLayout frameLayout = new FrameLayout(getActivity());
                frameLayout.setLayoutParams(new RelativeLayout.LayoutParams((int) (width * density), (int) (height * density)));

                if (interActiveAd.getParent() != null) {
                    ((ViewGroup) interActiveAd.getParent()).removeView(interActiveAd);
                }
                frameLayout.addView(interActiveAd);

                if (hasPosition) {
                    //设置锚点
                    frameLayout.setX(x);
                    frameLayout.setY(y);
                }
                layout.addView(frameLayout);
                tpInterActiveInfo.setParentView(layout);

                layout.setVisibility(View.VISIBLE);

                if (mTPInterActive != null) tpInterActive.showAd(sceneId);

            }
        });
    }

    public void hideInterActive(String adUnitId) {
        TPInterActiveInfo tpInterActiveInfo = getTPInterActive(adUnitId);

        TPTaskManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (tpInterActiveInfo != null) {
                    RelativeLayout parentView = tpInterActiveInfo.getParentView();
                    if (parentView != null) {
                        parentView.setVisibility(View.GONE);
                    }
                }
            }
        });

    }

    public void displayInterActive(String adUnitId) {
        TPInterActiveInfo tpInterActiveInfo = getTPInterActive(adUnitId);

        TPTaskManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (tpInterActiveInfo != null) {
                    RelativeLayout parentView = tpInterActiveInfo.getParentView();
                    if (parentView != null) {
                        parentView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    public boolean isReady(String unitId) {
        TPInterActiveInfo tpInterActiveInfo = getTPInterActive(unitId);
        if (tpInterActiveInfo == null) return false;

        TPInterActive tpInterActive = tpInterActiveInfo.getTpInterActive();
        if (tpInterActive != null) {
            return tpInterActive.isReady();
        }

        return false;
    }


    public void setCustomShowData(String adUnitId, String data) {
        TPInterActiveInfo tpInterActiveInfo = getTPInterActive(adUnitId);
        if (tpInterActiveInfo == null) return;

        TPInterActive tpInterActive = tpInterActiveInfo.getTpInterActive();
        if (tpInterActive != null) {
            tpInterActive.setCustomShowData(JSON.parseObject(data));
        }
    }


    public void destroyInterActive(String adUnitId) {
        TPInterActiveInfo tpInterActiveInfo = getTPInterActive(adUnitId);

        TPTaskManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (tpInterActiveInfo != null) {
                    RelativeLayout parentView = tpInterActiveInfo.getParentView();
                    if (parentView != null) {
                        parentView.removeAllViews();
                    }

                    TPInterActive tpInterActive = tpInterActiveInfo.getTpInterActive();

                    if (tpInterActive != null) {
                        tpInterActive.onDestroy();
                    }

                    mTPInterActive.remove(adUnitId);
                }
            }
        });

    }


    private TPInterActiveInfo getTPInterActive(String adUnitId) {
        return mTPInterActive.get(adUnitId);
    }

    private TPInterActiveInfo getTPInterActive(String adUnitId, String data, TPInterActiveListener listener) {

        Log.i("tradplus", "data = " + data + " mTPInterActive = " + mTPInterActive + " listener = " + listener);

        ExtraInfo extraInfo = null;
        if (!TextUtils.isEmpty(data)) {
            extraInfo = JSON.parseObject(data, ExtraInfo.class);
        }

        HashMap<String, Object> temp = new HashMap<>();


        TPInterActiveInfo tpInterActiveInfo = mTPInterActive.get(adUnitId);
        TPInterActive tpInterActive;
        if (tpInterActiveInfo == null) {
            tpInterActiveInfo = new TPInterActiveInfo();
            mTPInterActive.put(adUnitId, tpInterActiveInfo);

            tpInterActive = new TPInterActive(getActivity(), adUnitId);

            boolean isSimpleListener = extraInfo == null ? false : extraInfo.isSimpleListener();

            tpInterActive.setAdListener(new TPInterActiveAdListener(adUnitId, listener));
            if (!isSimpleListener) {
                tpInterActive.setAllAdLoadListener(new TPInterActiveAllAdListener(adUnitId, listener));
            }

            tpInterActiveInfo.setTPInterActive(tpInterActive);
            tpInterActiveInfo.setExtraInfo(extraInfo);
        } else {
            tpInterActive = tpInterActiveInfo.getTpInterActive();
        }

        if (extraInfo != null) {

            if (extraInfo.getLocalParams() != null) {
                temp = (HashMap<String, Object>) extraInfo.getLocalParams();
            }

            // AdFly need to set
            int width = 0;
            int height = 0;
            boolean needClose = true; //default

            if (extraInfo.getWidth() != 0) {
                width = (int)extraInfo.getWidth();
                temp.put("width", width);
            }

            if (extraInfo.getHeight() != 0) {
                height = (int)extraInfo.getHeight();
                temp.put("height", height);
            }

            if (extraInfo.isNeedToClose()) {
                needClose = extraInfo.isNeedToClose();
                temp.put("need_close", needClose);
            }

            tpInterActive.setCustomParams(temp);

            if (extraInfo.getCustomMap() != null) {
                SegmentUtils.initPlacementCustomMap(adUnitId, extraInfo.getCustomMap());
            }
        }

        return tpInterActiveInfo;
    }

    private TPAdInfo getShowAdInfo(String unitId) {
        AdCache cache = AdCacheManager.getInstance().getReadyAd(unitId);
        if (cache == null) return null;
        return new TPAdInfo(unitId, cache.getAdapter());
    }


    public class TPInterActiveAllAdListener implements LoadAdEveryLayerListener {
        private String mAdUnitId;
        private TPInterActiveListener listener;

        TPInterActiveAllAdListener(String adUnitId, TPInterActiveListener listener) {
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


    private class TPInterActiveAdListener implements InterActiveAdListener {
        private String mAdUnitId;
        private TPInterActiveListener listener;

        TPInterActiveAdListener(String adUnitId, TPInterActiveListener listener) {
            mAdUnitId = adUnitId;
            this.listener = listener;
        }

        @Override
        public void onAdLoaded(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onAdLoaded(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v(TAG, "loaded unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdClicked(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onAdClicked(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v(TAG, "onAdClicked unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdClosed(TPAdInfo tpAdInfo) {
            TPInterActiveInfo tpInterActiveInfo = getTPInterActive(mAdUnitId);

            if (tpInterActiveInfo != null) {
                if (tpInterActiveInfo.getParentView() != null) {
                    tpInterActiveInfo.getParentView().removeAllViews();
                }
            }

            if (listener != null) {
                listener.onAdClosed(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v(TAG, "onAdClosed unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdImpression(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onAdImpression(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v(TAG, "onAdImpression unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdFailed(TPAdError tpAdError) {
            if (listener != null) {
                listener.onAdFailed(mAdUnitId, JSON.toJSONString(tpAdError));
            }
        }

        @Override
        public void onAdVideoStart(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onAdVideoStart(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v(TAG, "onAdVideoStart unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdVideoEnd(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onAdVideoEnd(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v(TAG, "onAdVideoEnd unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdVideoError(TPAdInfo tpAdInfo, TPAdError tpAdError) {
            if (listener != null) {
                listener.onAdVideoError(mAdUnitId, JSON.toJSONString(tpAdInfo), JSON.toJSONString(tpAdError));
            }
            Log.v(TAG, "onAdVideoError unitid=" + mAdUnitId + "=======================");
        }

    }

    private class TPInterActiveInfo {
        private TPInterActive tpInterActive;
        private ExtraInfo extraInfo;
        private RelativeLayout parentView;


        public TPInterActiveInfo() {

        }

        public ExtraInfo getExtraInfo() {
            return extraInfo;
        }

        public void setExtraInfo(ExtraInfo extraInfo) {
            this.extraInfo = extraInfo;
        }

        public TPInterActive getTpInterActive() {
            return tpInterActive;
        }

        public void setTPInterActive(TPInterActive tpInterActive) {
            this.tpInterActive = tpInterActive;
        }

        public RelativeLayout getParentView() {
            return parentView;
        }

        public void setParentView(RelativeLayout parentView) {
            this.parentView = parentView;
        }
    }
}

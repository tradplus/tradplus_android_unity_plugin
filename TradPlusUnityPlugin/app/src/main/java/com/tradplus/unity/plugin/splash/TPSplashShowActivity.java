package com.tradplus.unity.plugin.splash;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.FrameLayout;

import com.tradplus.ads.common.util.ResourceUtils;
import com.tradplus.ads.open.splash.TPSplash;

public class TPSplashShowActivity extends Activity {

    private FrameLayout splash_container;
    private String unitId;
    private String sceneId;
    public static TPSplashShowActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ResourceUtils.getLayoutIdByName(this, "tp_splash_activity"));

        splash_container = findViewById(ResourceUtils.getViewIdByName(this,"tp_splash_container"));

        unitId = getIntent().getStringExtra("unitId");
        sceneId = getIntent().getStringExtra("sceneId");

        instance = this;

        if(TextUtils.isEmpty(unitId)){
            finish();
        }

        showSplash();

    }

    private void showSplash(){
        TPSplash tpSplash = TPSplashManager.getInstance().getTPSplash(unitId);
        if(tpSplash == null) return;
        if(!tpSplash.isReady()) return;

        tpSplash.showAd(splash_container,sceneId);
    }
}

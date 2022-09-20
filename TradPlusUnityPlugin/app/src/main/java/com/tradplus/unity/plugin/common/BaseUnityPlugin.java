package com.tradplus.unity.plugin.common;

import android.app.Activity;

import com.unity3d.player.UnityPlayer;

public class BaseUnityPlugin {

    public static Activity getActivity() {
        return UnityPlayer.currentActivity;
    }

}

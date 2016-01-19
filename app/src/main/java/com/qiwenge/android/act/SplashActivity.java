package com.qiwenge.android.act;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.widget.RelativeLayout;

import com.baidu.mobads.SplashAd;
import com.baidu.mobads.SplashAdListener;
import com.liuguangqiang.framework.utils.AppUtils;
import com.liuguangqiang.framework.utils.NetworkUtils;
import com.loopj.android.http.RequestParams;
import com.qiwenge.android.R;
import com.qiwenge.android.base.BaseActivity;
import com.qiwenge.android.constant.Constants;
import com.qiwenge.android.entity.Configures;
import com.qiwenge.android.utils.ApiUtils;
import com.qiwenge.android.utils.http.JHttpClient;
import com.qiwenge.android.utils.http.JsonResponseHandler;

public class SplashActivity extends BaseActivity {

    private static final int mDuration = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        patchAd();
    }

    private void patchAd() {
        RelativeLayout adsParent = (RelativeLayout) this.findViewById(R.id.layout_container);
        SplashAdListener listener = new SplashAdListener() {
            @Override
            public void onAdDismissed() {
                if (NetworkUtils.isAvailable(getApplicationContext())) {
                    connect();
                } else {
                    skipToMain();
                }
            }

            @Override
            public void onAdFailed(String arg0) {
                skipToMain();
            }

            @Override
            public void onAdPresent() {
            }

            @Override
            public void onAdClick() {
                // 设置开屏可接受点击时，该回调可用
            }
        };
        String adPlaceId = "2394443";
        new SplashAd(this, adsParent, listener, adPlaceId, true);
    }

    public boolean canJumpImmediately = false;

    @Override
    protected void onPause() {
        super.onPause();
        canJumpImmediately = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canJumpImmediately) {
            jumpWhenCanClick();
        }
        canJumpImmediately = true;
    }

    private void jumpWhenCanClick() {
        if (canJumpImmediately) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else {
            canJumpImmediately = true;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getScreenSize();
//            if (NetworkUtils.isAvailable(getApplicationContext())) {
//                connect();
//            } else {
//                skipToMain();
//            }
        }
    }

    public void getScreenSize() {
        Display dis = getWindowManager().getDefaultDisplay();
        Point outSize = new Point(0, 0);
        dis.getSize(outSize);
        if (outSize != null) {
            Constants.WIDTH = outSize.x;
            Constants.HEIGHT = outSize.y;
        }
    }

    private void skipToMain() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }, mDuration);
    }

    private void connect() {
        String url = ApiUtils.getConfigures();
        RequestParams params = new RequestParams();
        params.put("version", AppUtils.getVersionName(this));
        params.put("platform", Constants.PLATFORM);
        JHttpClient.get(getApplicationContext(), url, params, new JsonResponseHandler<Configures>(Configures.class, false) {
            @Override
            public void onSuccess(Configures result) {
                if (result != null && result.functions != null) {
                    Constants.openAutoReading = result.functions.autoReading();
                }
            }

            @Override
            public void onFinish() {
                skipToMain();
            }
        });
    }

}

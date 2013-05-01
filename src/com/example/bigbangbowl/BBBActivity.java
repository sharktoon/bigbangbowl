package com.example.bigbangbowl;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.BaseGameActivity;

import android.os.Bundle;

import com.example.bigbangbowl.game.GameScene;
import com.example.bigbangbowl.scenes.SplashScene;

public class BBBActivity extends BaseGameActivity {
    private ZoomCamera mCamera;
    public static final int CAMERA_WIDTH = 800;
    public static final int CAMERA_HEIGHT = 480;

    /** the resolution policy - keeps track of the current width */
    private FixedHeightResolutionPolicy mResolutionPolicy;

    /** currently displayed scene */
    private Scene mCurrentScene;

    @Override
    public EngineOptions onCreateEngineOptions() {
        mCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        FixedHeightResolutionPolicy respol = new FixedHeightResolutionPolicy(CAMERA_HEIGHT, mCamera);
        mResolutionPolicy = respol;
        EngineOptions options = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, respol, mCamera);
        return options;
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        mCamera.set(0, 0, mResolutionPolicy.getCurrentWidth(), CAMERA_HEIGHT);
        mCurrentScene = createSplashScene();
        pOnCreateSceneCallback.onCreateSceneFinished(mCurrentScene);
    }

    /** retrieve currently chosen relative width */
    public int getCurrentWidth() {
        return mResolutionPolicy.getCurrentWidth();
    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
        mEngine.registerUpdateHandler(new TimerHandler(2, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                mEngine.unregisterUpdateHandler(pTimerHandler);
                BBBActivity.this.startGameScene();
            }
        }));
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

    private Scene createSplashScene() {
        SplashScene splash = new SplashScene(this);
        return splash;
    }

    void startGameScene() {
        GameResources.getInstance().init(this);
        
        Scene scene = new GameScene(this, mCamera);
        Scene oldScene = mCurrentScene;
        mEngine.setScene(scene);
        mCurrentScene = scene;
        oldScene.dispose();
    }

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
    }

    @Override
    protected void onDestroy() {
        GameResources.purge();
        super.onDestroy();
    }

}

package com.example.bigbangbowl;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.engine.options.resolutionpolicy.FixedHeightResolutionPolicy;

import com.example.bigbangbowl.scenes.SplashScene;

public class BBBActivity extends BaseGameActivity {
	private Camera mCamera;
	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;
	
	/** the resolution policy - keeps track of the current width */
	FixedHeightResolutionPolicy mResolutionPolicy;
	
	/** currently displayed scene */
	private Scene mCurrentScene;
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		FixedHeightResolutionPolicy respol = new FixedHeightResolutionPolicy(CAMERA_HEIGHT);
		mResolutionPolicy = respol;
		EngineOptions options = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, respol, mCamera);
		return options;
	}
	
	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {
		mCurrentScene = createSplashScene();
		pOnCreateSceneCallback.onCreateSceneFinished(mCurrentScene);
	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		mEngine.registerUpdateHandler(new TimerHandler(2, new ITimerCallback() {
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
//				mEngine.unregisterUpdateHandler(pTimerHandler);
//				BBBActivity.this.startMenuScene();
			}
		}));
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}
	
	private Scene createSplashScene() {
		SplashScene splash = new SplashScene(this);
		return splash;
	}
	
	void startMenuScene() {
		Scene scene = new SplashScene(this);
		scene.setBackground(new Background(.4f, .8f, .6f));
		Scene oldScene = mCurrentScene;
		mEngine.setScene(scene);
		mCurrentScene = scene;
		oldScene.dispose();
		
	}

}

package com.example.bigbangbowl;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.resolutionpolicy.IResolutionPolicy;
import org.andengine.opengl.view.RenderSurfaceView;

import android.view.View.MeasureSpec;

/**
 * resolution policy that covers the entire screen.
 * It preserves screen ration - while ensuring a fixed
 * height value. This means a flexible width.
 * 
 * @author Daniel
 */
public class FixedHeightResolutionPolicy implements IResolutionPolicy {
	
	private final int mHeight;
	private int mCurrentWidth;
	private Camera mCamera;
	
	/**
	 * creates a policy that'll ensure a fixed height, and full screen
	 * coverage while also keeping the original screen ratio.
	 * This results in a flexible width.
	 * 
	 * @param height The fixed height.
	 */
	public FixedHeightResolutionPolicy(int height, Camera camera) {
		this.mHeight = height;
		this.mCamera = camera;
	}

	@Override
	public void onMeasure(RenderSurfaceView pRenderSurfaceView,
			int pWidthMeasureSpec, int pHeightMeasureSpec) {
		
		final int measuredWidth = MeasureSpec.getSize(pWidthMeasureSpec);
		final int measuredHeight = MeasureSpec.getSize(pHeightMeasureSpec);
		
		float scale = 1;
		if(measuredHeight != 0) {
			scale = mHeight / (float)measuredHeight;
		}
		mCurrentWidth = (int)Math.ceil(measuredWidth * scale);
		float xmin = mCamera.getXMin();
//		float xmax = mCamera.getXMax();
		float ymin = mCamera.getYMin();
		float ymax = mCamera.getYMax();
		float curh = ymax - ymin;
		float cameraScale = curh / mHeight;
		mCamera.set(xmin, ymin, xmin + mCurrentWidth * cameraScale, ymax);
		
		pRenderSurfaceView.setMeasuredDimensionProxy(measuredWidth, measuredHeight);
	}

	/**
	 * retrieve the currently calculated width.
	 * @return The current width value.
	 */
	public int getCurrentWidth() {
		return mCurrentWidth;
	}
}

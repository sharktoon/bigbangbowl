package com.example.bigbangbowl.game;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.font.BitmapFont;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.HorizontalAlign;

import com.example.bigbangbowl.BBBActivity;

public class BowlHud extends HUD {
	/** the bitmapfont for the texts */
	private BitmapFont mBitmapFont;
	private VertexBufferObjectManager mVbo;

	public void prepareResources(BBBActivity activity) {
		this.mBitmapFont = new BitmapFont(activity.getTextureManager(),
				activity.getAssets(), "font/BitmapFont.fnt");
		this.mBitmapFont.load();

		mVbo = activity.getVertexBufferObjectManager();
	}

	private Text mMovementDisplay;

	/** show the movement display */
	public void setMovement(int remaining, int total) {
		StringBuffer text = new StringBuffer("mv");
		text.append(remaining);
//		text.append("/");
//		text.append(total);

		if (mMovementDisplay == null) {
			mMovementDisplay = new Text(0, 0, this.mBitmapFont, text,
					new TextOptions(HorizontalAlign.CENTER), mVbo);
			mMovementDisplay.setColor(.5f, .5f, 1);
			this.attachChild(mMovementDisplay);
		} else {
			mMovementDisplay.setText(text);
		}
		mMovementDisplay.setVisible(true);
	}

	/** hide the movement display */
	public void hideMovement() {
		if (mMovementDisplay != null) {
			mMovementDisplay.setVisible(false);
		}
	}
}

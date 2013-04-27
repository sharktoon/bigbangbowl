package com.example.bigbangbowl.game;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;

import com.example.bigbangbowl.BBBActivity;

public class GameScene extends Scene {
	
	/** complete texture atlas */
	private BitmapTextureAtlas mTextureAtlas;
	/** 4x4 fields graphics */
	private ITextureRegion mFieldTexture0;
	private ITextureRegion mFieldTexture1;
	private ITextureRegion mChaosBeastmanTexture;
	private ITextureRegion mChaosWarriorTexture;
	private ITextureRegion mVampireThrallTexture;
	private ITextureRegion mVampireVampireTexture;
	private Sprite mFieldSprites[];
	
	public GameScene(BBBActivity activity) {
		mTextureAtlas = new BitmapTextureAtlas(activity.getTextureManager(), 196 * 4 + 2 * 256, 256);
		mFieldTexture0 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity, "gfx/field_green00.png", 196 * 4, 0);
		mFieldTexture1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity, "gfx/field_green01.png", 196 * 4 + 256, 0);
		
		mChaosBeastmanTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity, "gfx/team_chaos/beastman00.png", 196 * 0, 0);
		mChaosWarriorTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity, "gfx/team_chaos/chaoswarrior00.png", 196 * 1, 0);
		mVampireThrallTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity, "gfx/team_vampire/thrall00.png", 196 * 2, 0);
		mVampireVampireTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity, "gfx/team_vampire/vampire00.png", 196 * 3, 0);
		
		mTextureAtlas.load();
		
		this.setBackground(new Background(0, 0, 0));
		
		final float centerX = activity.getCurrentWidth() / 2;
		final float centerY = BBBActivity.CAMERA_HEIGHT / 2;

		Sprite vampire = new Sprite(centerX - mVampireVampireTexture.getWidth() / 2, centerY - mVampireVampireTexture.getHeight() / 2, mVampireVampireTexture, activity.getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
				return true;
			}
		};
		
		Sprite warrior = new Sprite(0 + mChaosWarriorTexture.getWidth(), 0 + mChaosWarriorTexture.getHeight(), mChaosWarriorTexture, activity.getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
				return true;
			}
		};
		
		
		int size = 10 * 20 / 4;
		mFieldSprites = new Sprite[size];
		
		for(int x = 0; x < 10; x += 2) {
			for(int y = 0; y < 20; y += 2) {
				int index = x / 2 + y * 10 / 4;
				ITextureRegion region = null;
				if(x == 0 || x == 8) region = mFieldTexture1;
				else region = mFieldTexture0;
				mFieldSprites[index] = new Sprite(x * 128, y * 128, region, activity.getVertexBufferObjectManager());
				this.attachChild(mFieldSprites[index]);
			}
		}
		
		this.attachChild(vampire);
		this.registerTouchArea(vampire);
		
		this.attachChild(warrior);
		this.registerTouchArea(warrior);
		
		this.setTouchAreaBindingOnActionDownEnabled(true);
	}

	@Override
	public void dispose() {
		mFieldTexture0 = null;
		mFieldTexture1 = null;
		mChaosBeastmanTexture= null;
		mChaosWarriorTexture = null;
		mVampireThrallTexture = null;
		mVampireVampireTexture = null;
		mTextureAtlas.unload();
		
		super.dispose();
	}

	
	
}

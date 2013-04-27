package com.example.bigbangbowl.game;

import org.andengine.entity.Entity;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;

import android.util.Log;

import com.example.bigbangbowl.BBBActivity;

public class GameScene extends Scene implements IOnSceneTouchListener {
	
	/** number of tiles - width */
	private static final int MAP_WIDTH = 24;
	/** number of tiles - height */
	private static final int MAP_HEIGHT = 10;
	/** pixels per tile */
	public static final int TILE_PIXELS = 128;
	
	/** complete texture atlas */
	private BitmapTextureAtlas mTextureAtlas;
	/** 4x4 fields graphics */
	private ITextureRegion mFieldTexture0;
	private ITextureRegion mFieldTexture1;
	private ITextureRegion mFieldTextureLeft;
	private ITextureRegion mFieldTextureRight;
	private Sprite mFieldSprites[];
	
	/** the entire map display stuff - to be moved and scaled easily */
	private Entity mMapDisplay;
	/** the pitch data */
	private ThePitch mPitch;
	
	public GameScene(BBBActivity activity) {
		mTextureAtlas = new BitmapTextureAtlas(activity.getTextureManager(), 4 * 256, 256);
		mFieldTexture0 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity, "gfx/field_green00.png", 0 * 256, 0);
		mFieldTexture1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity, "gfx/field_green01.png", 1 * 256, 0);
		mFieldTextureLeft = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity, "gfx/field_green02.png", 2*256, 0);
		mFieldTextureRight = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity, "gfx/field_green03.png", 3*256, 0);
		
		mTextureAtlas.load();
		
		mPitch = new ThePitch();
		mPitch.loadResources(activity);
		mPitch.createTeams(activity);
		
		this.setBackground(new Background(0, 0, 0));
		
		final float centerX = activity.getCurrentWidth() / 2;
		final float centerY = BBBActivity.CAMERA_HEIGHT / 2;
		
		Entity map = new Entity(0, 0);
		
		int size = MAP_HEIGHT * MAP_WIDTH / 4;
		mFieldSprites = new Sprite[size];
		
		for(int x = 0; x < MAP_WIDTH; x += 2) {
			for(int y = 0; y < MAP_HEIGHT; y += 2) {
				int index = x / 2 + y * 20 / 4;
				ITextureRegion region = null;
				if(x == 0) region = mFieldTextureLeft;
				else if(x == MAP_WIDTH - 2) region = mFieldTextureRight;
				else if(y == 0 || y == MAP_HEIGHT - 2) region = mFieldTexture1;
				else region = mFieldTexture0;
				mFieldSprites[index] = new Sprite(x * 128, y * 128, region, activity.getVertexBufferObjectManager());
				map.attachChild(mFieldSprites[index]);
			}
		}
		
		float targetWidth = MAP_WIDTH * TILE_PIXELS;
		float targetHeight = MAP_HEIGHT * TILE_PIXELS;
		float scaleX = 1;
		float scaleY = 1;
		if(targetWidth > activity.getCurrentWidth()) {
			scaleX = activity.getCurrentWidth() / targetWidth;
		}
		if(targetHeight > BBBActivity.CAMERA_HEIGHT) {
			scaleY = BBBActivity.CAMERA_HEIGHT / targetHeight;
		}
		float scale = 1;
		if(scaleX < scaleY) scale = scaleX;
		else scale = scaleY;
		
		scale = 2 * scale;
		float posX = (activity.getCurrentWidth() - targetWidth * scale) / 2;
		float posY = (BBBActivity.CAMERA_HEIGHT - targetHeight * scale) / 2;
		
		mMapDisplay = new Entity();
		mMapDisplay.setScale(scale);
		mMapDisplay.setPosition(posX, posY);
		
		mMapDisplay.attachChild(map);
		
		mPitch.placeTeams(mMapDisplay);
		
		this.attachChild(mMapDisplay);
		
		this.setTouchAreaBindingOnActionDownEnabled(true);
		this.setOnSceneTouchListener(this);
	}

	@Override
	public void dispose() {
		mPitch.dispose();
		
		mFieldTexture0 = null;
		mFieldTexture1 = null;
		mFieldTextureLeft = null;
		mFieldTextureRight = null;
		mTextureAtlas.unload();
		
		super.dispose();
	}
	
	/** currently has a touch */
	boolean mHasTouch;
	/** location of touch */
	float mTouchStartX, mTouchStartY;
	/** previous recorded touch location */
	float mTouchLastX, mTouchLastY;

	@Override
	public boolean onSceneTouchEvent(Scene scene, TouchEvent touchEvent) {
		float scale = mMapDisplay.getScaleX();
		float minX = mMapDisplay.getX();
		float maxX = minX + scale * TILE_PIXELS * MAP_WIDTH;
		float minY = mMapDisplay.getY();
		float maxY = minY + scale * TILE_PIXELS * MAP_HEIGHT;
		
		float x = touchEvent.getX();
		float y = touchEvent.getY();
		
		if(minX < x && x < maxX && minY < y && y < maxY) {
			switch(touchEvent.getAction()) {
			case TouchEvent.ACTION_DOWN:
				if(mHasTouch) return false;
				mTouchStartX = x;
				mTouchStartY = y;
				mHasTouch = true;
				mTouchLastX = x;
				mTouchLastY = y;
				break;
			case TouchEvent.ACTION_UP:
				if(!mHasTouch) return false;
				float dx = mTouchStartX - x;
				float dy = mTouchStartY - y;
				float distance = dx * dx + dy * dy;
				if(distance < 256) {
					int tileX = (int)((x - minX) / scale) / TILE_PIXELS;
					int tileY = (int)((y - minY) / scale) / TILE_PIXELS;
					mPitch.clickedTile(tileX, tileY);
//					if(0 <= tileX && tileX < MAP_WIDTH && 0 <= tileY && tileY < MAP_HEIGHT) {
//						Log.w("BBB", "clicked Tile (" + tileX + ";" + tileY + ")");
//					}
				}
				mHasTouch = false;
				break;
			case TouchEvent.ACTION_MOVE:
				if(!mHasTouch) return false;
				float movex = x - mTouchLastX;
				float movey = y - mTouchLastY;
				
				float newX = minX + movex;
				float newY = minY + movey;
				// TODO proper borders
				mMapDisplay.setPosition(newX, newY);

				mTouchLastX = x;
				mTouchLastY = y;
				break;
			}
			return true;
		}
		
		return false;
	}

	
}

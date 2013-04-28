package com.example.bigbangbowl.game;

import java.util.Vector;

import org.andengine.entity.Entity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.example.bigbangbowl.BBBActivity;
import com.example.bigbangbowl.game.dice.Step;

/**
 * Contains all the information what's going on inside the pitch.
 * 
 * Keeps track of all those squares.
 * 
 * @author Daniel
 * 
 */
public class ThePitch {
	/** width/length of the pitch */
	public static final int PITCH_WIDTH = 24;
	/** height/breadth of the pitch */
	public static final int PITCH_HEIGHT = 10;

	/** graphical offset for a player piece */
	public static final int PIECE_OFFSET_X = -32;
	/** graphical offset for a player piece */
	public static final int PIECE_OFFSET_Y = -128;

	/** complete texture atlas */
	private BitmapTextureAtlas mTextureAtlas;
	/** the combatants graphics */
	private ITextureRegion mChaosBeastmanTexture;
	private ITextureRegion mChaosWarriorTexture;
	private ITextureRegion mVampireThrallTexture;
	private ITextureRegion mVampireVampireTexture;
	/** selector images */
	private TextureRegion mSelectorTexture0;
	private TextureRegion mSelectorTexture1;

	/** the highlight thingy */
	private Sprite mSelector;
	private Vector<Step> mSelectedPath = new Vector<Step>();

	/** currently selected piece */
	private PlayerPiece mSelectedPiece;

	/** team 0 */
	private PlayerPiece[] mTeam0;
	/** team 1 */
	private PlayerPiece[] mTeam1;

	/** pitch */
	private PlayerPiece[] mPitch;

	/** to create sprites on the fly */
	private VertexBufferObjectManager mVbo;
	/** the graphical map thingy */
	private Entity mGfxMap;

	/** load resources */
	public void loadResources(BBBActivity activity) {
		mVbo = activity.getVertexBufferObjectManager();
		mTextureAtlas = new BitmapTextureAtlas(activity.getTextureManager(),
				4 * 196 + 2 * 128, 256);

		mChaosBeastmanTexture = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mTextureAtlas, activity,
						"gfx/team_chaos/beastman00.png", 196 * 0, 0);
		mChaosWarriorTexture = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mTextureAtlas, activity,
						"gfx/team_chaos/chaoswarrior00.png", 196 * 1, 0);
		mVampireThrallTexture = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mTextureAtlas, activity,
						"gfx/team_vampire/thrall00.png", 196 * 2, 0);
		mVampireVampireTexture = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mTextureAtlas, activity,
						"gfx/team_vampire/vampire00.png", 196 * 3, 0);

		mSelectorTexture0 = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mTextureAtlas, activity, "gfx/selector00.png",
						196 * 4 + 0 * 128, 0);
		mSelectorTexture1 = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mTextureAtlas, activity, "gfx/selector01.png",
						196 * 4 + 1 * 128, 0);

		mTextureAtlas.load();

		mSelector = new Sprite(0, 0, mSelectorTexture0, mVbo);
	}

	public void createTeams(BBBActivity activity) {
		mTeam0 = new PlayerPiece[11];
		mTeam1 = new PlayerPiece[11];
		mPitch = new PlayerPiece[PITCH_HEIGHT * PITCH_WIDTH];
		for (int i = 0; i < 3; ++i) {
			mTeam0[i] = new PlayerPiece(4, 4, 6, 8);
			Sprite sprite = new Sprite(0, 0, mVampireVampireTexture, mVbo);
			mTeam0[i].setEntity(sprite);
		}
		for (int i = 3; i < 11; ++i) {
			mTeam0[i] = new PlayerPiece(3, 3, 6, 7);
			Sprite sprite = new Sprite(0, 0, mVampireThrallTexture, mVbo);
			mTeam0[i].setEntity(sprite);
		}
		for (int i = 0; i < 3; ++i) {
			mTeam1[i] = new PlayerPiece(4, 3, 5, 9);
			Sprite sprite = new Sprite(0, 0, mChaosWarriorTexture, mVbo);
			mTeam1[i].setEntity(sprite);
		}
		for (int i = 3; i < 11; ++i) {
			mTeam1[i] = new PlayerPiece(3, 3, 6, 8);
			Sprite sprite = new Sprite(0, 0, mChaosBeastmanTexture, mVbo);
			mTeam1[i].setEntity(sprite);
		}
	}

	public void placeTeams(Entity map) {
		mGfxMap = map;
		int places0[] = { 11, 3, 11, 4, 11, 5, 10, 7, 10, 1, 5, 4 };
		int places1[] = { 12, 3, 12, 4, 12, 5, 13, 7, 13, 1, 20, 4 };
		for (int i = 0; i < places0.length / 2; ++i) {
			int x = places0[i * 2];
			int y = places0[i * 2 + 1];
			mTeam0[i].setPosition(x, y);
			mTeam0[i].getEntity().setPosition(x * 128 - 32, y * 128 - 128);
			map.attachChild(mTeam0[i].getEntity());

			mPitch[x + y * PITCH_WIDTH] = mTeam0[i];
		}

		for (int i = 0; i < places1.length / 2; ++i) {
			int x = places1[i * 2];
			int y = places1[i * 2 + 1];
			mTeam1[i].setPosition(x, y);
			mTeam1[i].getEntity().setPosition(x * 128 + PIECE_OFFSET_X,
					y * 128 + PIECE_OFFSET_Y);
			map.attachChild(mTeam1[i].getEntity());

			mPitch[x + y * PITCH_WIDTH] = mTeam1[i];
		}
	}

	public void dispose() {
		mVbo = null;
		mGfxMap = null;

		mChaosBeastmanTexture = null;
		mChaosWarriorTexture = null;
		mVampireThrallTexture = null;
		mVampireVampireTexture = null;

		mSelectorTexture0 = null;
		mSelectorTexture1 = null;

		mTextureAtlas.unload();
	}

	/** informs the pitch that the user chose to tap the given tile */
	public void clickedTile(int tileX, int tileY) {
		if (0 > tileX || tileX >= PITCH_WIDTH || 0 > tileY
				|| tileY >= PITCH_HEIGHT) {
			return;
		}

		if (!mSelector.hasParent()) {
			mGfxMap.attachChild(mSelector);
		}

		int index = tileX + tileY * PITCH_WIDTH;
		if (mPitch[index] != null) {
			for (int i = 0, n = mSelectedPath.size(); i < n; ++i) {
				mSelectedPath.get(i).sprite.detachSelf();
			}
			mSelectedPath.clear();

			mSelectedPiece = mPitch[index];
			mSelector.setPosition(tileX * GameScene.TILE_PIXELS, tileY
					* GameScene.TILE_PIXELS);
		}

		if (mSelectedPiece == null) {
			mSelector.setPosition(tileX * GameScene.TILE_PIXELS, tileY
					* GameScene.TILE_PIXELS);
		} else {
			int lastPosX = mSelectedPiece.getPositionX();
			int lastPosY = mSelectedPiece.getPositionY();

			boolean fail = false;
			boolean showHint = false;
			if (tileX == lastPosX && tileY == lastPosY) {
				fail = true;
				showHint = true;
			}
			for (int i = 0, n = mSelectedPath.size(); i < n; ++i) {
				Step step = mSelectedPath.get(i);
				lastPosX = step.tileX;
				lastPosY = step.tileY;
				if (step.tileX == tileX && step.tileY == tileY) {
					fail = true;
					break;
				}
			}

			if (Math.abs(lastPosX - tileX) > 1
					|| Math.abs(lastPosY - tileY) > 1) {
				fail = true;
				showHint = true;
			}

			if (mSelectedPath.size() >= mSelectedPiece.getMV()) {
				fail = true;
			}

			if (!fail) {
				Sprite sprite = new Sprite(tileX * GameScene.TILE_PIXELS, tileY
						* GameScene.TILE_PIXELS, mSelectorTexture1, mVbo);
				mGfxMap.attachChild(sprite);
				Step step = new Step();
				step.tileX = tileX;
				step.tileY = tileY;
				step.type = Step.TYPE_MOVE;
				step.sprite = sprite;
				mSelectedPath.add(step);
			}

			if (showHint) {
				// TODO
			}
		}

	}

	/** how many steps the currently planned move has */
	public int getCurrentSteps() {
		return mSelectedPath.size();
	}

	/** how many steps the currently selected player may make */
	public int getCurrentMovementLimit() {
		if (mSelectedPiece == null)
			return -1;
		return mSelectedPiece.getMV();
	}

	/** tickwise update */
	public void update(float dt) {
	}
}

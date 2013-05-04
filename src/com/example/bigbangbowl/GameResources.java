package com.example.bigbangbowl;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.font.BitmapFont;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.StrokeFont;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

import android.graphics.Typeface;
import android.util.SparseArray;

/**
 * convenience singleton - provides access to sprite frames for all parts of the
 * game
 * 
 * @author Daniel
 * 
 */
public class GameResources {
    /** the instance */
    private static GameResources sInstance;

    /** singleton constructor */
    private GameResources() {
    }

    /** access the singleton */
    public static GameResources getInstance() {
        if (sInstance == null) {
            sInstance = new GameResources();
        }
        return sInstance;
    }

    /** free up memory claimed */
    public static void purge() {
        if (sInstance != null) sInstance.dispose();
        sInstance = null;
    }

    /** necessary stuff for sprite creation */
    private VertexBufferObjectManager mVbo;
    /** all the neat sprite frames - for the team */
    private BitmapTextureAtlas mTextureAtlas;
    /** sprite frames for the tutorial */
    private BitmapTextureAtlas mTutorialAtlas;

    /** texture regions for all different things */
    private SparseArray<TextureRegion> mTextureRegions;
    /** the standard font */
    private Font mFont;
    /** special font */
    private BitmapFont mBitmapFont;
    
    public static final int FRAME_INVALID = -1;

    public static final int FRAME_CHAOS_BEASTMAN = 0;
    public static final int FRAME_CHAOS_WARRIOR = 1;
    public static final int FRAME_VAMPIRE_THRALL = 2;
    public static final int FRAME_VAMPIRE_VAMPIRE = 3;
    public static final int FRAME_SKELETON = 4;

    public static final int FRAME_FOOT_BLUE = 5;
    public static final int FRAME_FOOT_GREEN = 6;

    public static final int FRAME_SELECTOR0 = 7;
    public static final int FRAME_SELECTOR1 = 8;
    public static final int FRAME_SELECTOR2 = 9;
    public static final int FRAME_HINT = 10;
    public static final int FRAME_BLOOD = 11;

    public static final int FRAME_SIGN_ACCEPT = 12;
    public static final int FRAME_SIGN_DECLINE = 13;
    public static final int FRAME_SIGN_CONTINUE = 14;
    public static final int FRAME_BUTTON_ENDTURN = 15;
    public static final int FRAME_BUTTON_CONFIRM = 16;
    public static final int FRAME_WARNING_TURNOVER = 17;

    public static final int FRAME_MAP_GRID = 18;

    public static final int FRAME_TUTORIAL_TEXTBOX = 19;
    public static final int FRAME_TUTORIAL_CHAR0 = 20;
    public static final int FRAME_TUTORIAL_CHAR1 = 21;

    /**
     * prepares the resources - loads stuff and stores some ... very memory
     * heavy things
     */
    public void init(BBBActivity activity) {
        mTextureRegions = new SparseArray<TextureRegion>();
        mVbo = activity.getVertexBufferObjectManager();

        mTextureAtlas = new BitmapTextureAtlas(activity.getTextureManager(), 2048, 1024);

        // pieces
        mTextureRegions.put(FRAME_CHAOS_BEASTMAN, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas,
                activity, "gfx/team_chaos/beastman00.png", 196 * 0, 0));
        mTextureRegions.put(FRAME_CHAOS_WARRIOR, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas,
                activity, "gfx/team_chaos/chaoswarrior00.png", 196 * 1, 0));
        mTextureRegions.put(FRAME_VAMPIRE_THRALL, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas,
                activity, "gfx/team_vampire/thrall00.png", 196 * 2, 0));
        mTextureRegions.put(FRAME_VAMPIRE_VAMPIRE, BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                mTextureAtlas, activity, "gfx/team_vampire/vampire00.png", 196 * 3, 0));

        mTextureRegions.put(FRAME_SKELETON, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas,
                activity, "gfx/team_undead/skeleton00.png", 196 * 4, 0));

        // feet
        mTextureRegions.put(FRAME_FOOT_BLUE, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas,
                activity, "gfx/foot_blue.png", 196 * 5, 0));
        mTextureRegions.put(FRAME_FOOT_GREEN, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas,
                activity, "gfx/foot_green.png", 196 * 6, 0));

        mTextureRegions.put(FRAME_MAP_GRID, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas,
                activity, "gfx/map_grid.png", 196 * 7, 0));

        // selectors
        mTextureRegions.put(FRAME_SELECTOR0, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas,
                activity, "gfx/selector00.png", 0 * 128, 256));
        mTextureRegions.put(FRAME_SELECTOR1, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas,
                activity, "gfx/selector01.png", 1 * 128, 256));
        mTextureRegions.put(FRAME_SELECTOR2, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas,
                activity, "gfx/selector02.png", 2 * 128, 256));

        // fx
        mTextureRegions.put(FRAME_HINT, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity,
                "gfx/selector03.png", 3 * 128, 256));
        mTextureRegions.put(FRAME_BLOOD, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas,
                activity, "gfx/blood.png", 4 * 128, 256));

        // buttons
        mTextureRegions.put(FRAME_BUTTON_ENDTURN, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas,
                activity, "gfx/buttons/endturn.png", 0, 256 + 128));
        mTextureRegions.put(FRAME_BUTTON_CONFIRM, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas,
                activity, "gfx/buttons/confirm.png", 512, 256 + 128));
        mTextureRegions.put(FRAME_WARNING_TURNOVER, BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                mTextureAtlas, activity, "gfx/buttons/turnover.png", 1024, 256 + 128));
        mTextureRegions.put(FRAME_SIGN_ACCEPT, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas,
                activity, "gfx/sign_accept.png", 0, 256 + 128 + 128));
        mTextureRegions.put(FRAME_SIGN_DECLINE, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas,
                activity, "gfx/sign_decline.png", 128 * 1, 256 + 128 + 128));
        mTextureRegions.put(FRAME_SIGN_CONTINUE, BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas,
                activity, "gfx/sign_continue.png", 128 * 2, 256 + 128 + 128));

        mTextureAtlas.load();

        mTutorialAtlas = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024);

        mTextureRegions.put(FRAME_TUTORIAL_TEXTBOX, BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                mTutorialAtlas, activity, "gfx/characters/textbox.png", 0 * 256, 0 * 512));
        mTextureRegions.put(FRAME_TUTORIAL_CHAR0, BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                mTutorialAtlas, activity, "gfx/characters/norse.png", 1 * 256, 0 * 512));
        mTextureRegions.put(FRAME_TUTORIAL_CHAR1, BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                mTutorialAtlas, activity, "gfx/characters/princess.png", 2 * 256, 0 * 512));
        
        mTutorialAtlas.load();

        // fonts
        final ITexture strokeFontTexture = new BitmapTextureAtlas(activity.getTextureManager(), 256, 256,
                TextureOptions.BILINEAR);
        this.mFont = new StrokeFont(activity.getFontManager(), strokeFontTexture, Typeface.create(
                Typeface.DEFAULT_BOLD, Typeface.BOLD), 32, true, Color.WHITE, 2, Color.BLACK);
        this.mFont.load();

        this.mBitmapFont = new BitmapFont(activity.getTextureManager(), activity.getAssets(), "font/BitmapFont.fnt");
        this.mBitmapFont.load();
    }

    /** clean up */
    private void dispose() {
        mTextureRegions.clear();
        mTextureAtlas.unload();
    }

    /** access the vertex buffer manager */
    public VertexBufferObjectManager getVbo() {
        return mVbo;
    }

    /** creates a sprite with the given frame id */
    public Sprite createSprite(float posX, float posY, int frameId) {
        Sprite sprite = new Sprite(posX, posY, mTextureRegions.get(frameId), mVbo);
        return sprite;
    }

    /** access the texture region for the given frame id */
    public TextureRegion getTextureRegion(int frameId) {
        return mTextureRegions.get(frameId);
    }

    /** access the standard font */
    public Font getFont() {
        return mFont;
    }

    /** access the bitmap font */
    public BitmapFont getBitmapFont() {
        return mBitmapFont;
    }
}

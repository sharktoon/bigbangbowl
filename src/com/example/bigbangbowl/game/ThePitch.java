package com.example.bigbangbowl.game;

import java.util.Random;
import java.util.Vector;

import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.IEntityModifier.IEntityModifierMatcher;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.modifier.IModifier;

import com.example.bigbangbowl.BBBActivity;
import com.example.bigbangbowl.game.dice.IDiceLogReceiver;
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
    /** how long the movement hints are shown */
    private static final float HINT_SHOW_DURATION = 2.0f;
    /** width/length of the pitch */
    public static final int PITCH_WIDTH = 24;
    /** height/breadth of the pitch */
    public static final int PITCH_HEIGHT = 10;

    /** graphical offset for a player piece */
    public static final int PIECE_OFFSET_X = -32;
    /** graphical offset for a player piece */
    public static final int PIECE_OFFSET_Y = -128;
    /** duration of a step */
    private static final float STEP_DURATION = .5f;

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
    /** hint images */
    private TextureRegion mHintTexture0;

    /** the highlight thingy */
    private Sprite mSelector;
    private Vector<Step> mSelectedPath = new Vector<Step>();

    /** flag to block input, while executing plan */
    private boolean mExecutingPlan;
    /** timer during plan execution */
    private float mExecutingTimer;

    /** how long the hint stuff should (still) be visible */
    private float mHintShowTimer;
    /** the hint sprites */
    private Vector<Sprite> mHintSprites;

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
    /** the random number supplier... */
    private Random mRandom;

    /** load resources */
    public void loadResources(BBBActivity activity) {
        mVbo = activity.getVertexBufferObjectManager();
        mTextureAtlas = new BitmapTextureAtlas(activity.getTextureManager(), 4 * 196 + 3 * 128, 256);

        mChaosBeastmanTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity,
                "gfx/team_chaos/beastman00.png", 196 * 0, 0);
        mChaosWarriorTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity,
                "gfx/team_chaos/chaoswarrior00.png", 196 * 1, 0);
        mVampireThrallTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity,
                "gfx/team_vampire/thrall00.png", 196 * 2, 0);
        mVampireVampireTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity,
                "gfx/team_vampire/vampire00.png", 196 * 3, 0);

        mSelectorTexture0 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity,
                "gfx/selector00.png", 196 * 4 + 0 * 128, 0);
        mSelectorTexture1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity,
                "gfx/selector01.png", 196 * 4 + 1 * 128, 0);

        mHintTexture0 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity,
                "gfx/selector03.png", 196 * 4 + 2 * 128, 0);

        mTextureAtlas.load();

        mSelector = new Sprite(0, 0, mSelectorTexture0, mVbo);

        mHintSprites = new Vector<Sprite>(8);
        for (int i = 0; i < 8; ++i) {
            Sprite sprite = new Sprite(0, 0, mHintTexture0, mVbo);
            mHintSprites.add(sprite);
        }

        mRandom = new Random(System.currentTimeMillis());
    }

    public void createTeams(BBBActivity activity) {
        mTeam0 = new PlayerPiece[11];
        mTeam1 = new PlayerPiece[11];
        mPitch = new PlayerPiece[PITCH_HEIGHT * PITCH_WIDTH];
        for (int i = 0; i < 3; ++i) {
            mTeam0[i] = new PlayerPiece(4, 4, 6, 8);
            mTeam0[i].setTeam(0);
            mTeam0[i].setState(PlayerPiece.STATE_STANDING);
            Sprite sprite = new Sprite(0, 0, mVampireVampireTexture, mVbo);
            mTeam0[i].setEntity(sprite);
        }
        for (int i = 3; i < 11; ++i) {
            mTeam0[i] = new PlayerPiece(3, 3, 6, 7);
            mTeam0[i].setTeam(0);
            mTeam0[i].setState(PlayerPiece.STATE_STANDING);
            Sprite sprite = new Sprite(0, 0, mVampireThrallTexture, mVbo);
            mTeam0[i].setEntity(sprite);
        }
        for (int i = 0; i < 3; ++i) {
            mTeam1[i] = new PlayerPiece(4, 3, 5, 9);
            mTeam1[i].setTeam(1);
            mTeam1[i].setState(PlayerPiece.STATE_STANDING);
            Sprite sprite = new Sprite(0, 0, mChaosWarriorTexture, mVbo);
            mTeam1[i].setEntity(sprite);
        }
        for (int i = 3; i < 11; ++i) {
            mTeam1[i] = new PlayerPiece(3, 3, 6, 8);
            mTeam1[i].setTeam(1);
            mTeam1[i].setState(PlayerPiece.STATE_STANDING);
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
            mTeam1[i].getEntity().setPosition(x * 128 + PIECE_OFFSET_X, y * 128 + PIECE_OFFSET_Y);
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
        if (0 > tileX || tileX >= PITCH_WIDTH || 0 > tileY || tileY >= PITCH_HEIGHT) {
            return;
        }

        if (!mSelector.hasParent()) {
            mGfxMap.attachChild(mSelector);
        }

        int index = tileX + tileY * PITCH_WIDTH;
        if (mPitch[index] != null) {
            cancelPlannedMove();

            mSelectedPiece = mPitch[index];
            mSelector.setPosition(tileX * GameScene.TILE_PIXELS, tileY * GameScene.TILE_PIXELS);
            mSelector.setVisible(true);
        }

        if (mSelectedPiece == null) {
            mSelector.setPosition(tileX * GameScene.TILE_PIXELS, tileY * GameScene.TILE_PIXELS);
            mSelector.setVisible(true);
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

            if (Math.abs(lastPosX - tileX) > 1 || Math.abs(lastPosY - tileY) > 1) {
                fail = true;
                showHint = true;
            }

            if (mSelectedPath.size() >= 2 + mSelectedPiece.getMV()) {
                fail = true;
                showHint = false;
            }

            if (!fail) {
                Sprite sprite = new Sprite(tileX * GameScene.TILE_PIXELS, tileY * GameScene.TILE_PIXELS,
                        mSelectorTexture1, mVbo);
                mGfxMap.attachChild(sprite);
                Step step = makeNextStep(tileX, tileY, lastPosX, lastPosY);
                step.sprite = sprite;
                mSelectedPath.add(step);
            }

            if (showHint) {
                triggerShowHint(lastPosX, lastPosY);
            } else {
                hideHints();
            }
        }
    }

    /** get tackle zones targeting given tile - against given team */
    private int getTackleZones(int tileX, int tileY, int team) {
        int count = 0;
        for (int x = tileX - 1; x <= tileX + 1; ++x) {
            if (0 > x || x >= PITCH_WIDTH) continue;
            for (int y = tileY - 1; y <= tileY + 1; ++y) {
                if (0 > y || y >= PITCH_HEIGHT) continue;
                int index = x + y * PITCH_WIDTH;
                if (mPitch[index] != null && mPitch[index].getTeam() != team) {
                    if (mPitch[index].getTackleZone()) {
                        ++count;
                    }
                }
            }
        }
        return count;
    }

    /** adds a step leading to given tile, from last position */
    public Step makeNextStep(int tileX, int tileY, int lastPosX, int lastPosY) {
        Step step = new Step();
        step.tileX = tileX;
        step.tileY = tileY;
        step.type = Step.TYPE_MOVE;
        step.successChance = 1;

        if (getTackleZones(lastPosX, lastPosY, mSelectedPiece.getTeam()) > 0) {
            int tacklemod = getTackleZones(tileX, tileY, mSelectedPiece.getTeam());
            step.dice = true;
            step.dodge = true;
            step.minDodgeRoll = 7 - mSelectedPiece.getAG() - 1 + tacklemod;

            int diceChance = Math.max(2, Math.min(step.minDodgeRoll, 6));
            step.successChance = 1 - (diceChance - 1) / 6.0f;
        }

        if (mSelectedPath.size() >= mSelectedPiece.getMV()) {
            step.gfi = true;
            step.successChance *= 5.0f / 6.0f;
        }

        return step;
    }

    /** how many steps the currently planned move has */
    public int getCurrentSteps() {
        return mSelectedPath.size();
    }

    /** how many steps the currently selected player may make */
    public int getCurrentMovementLimit() {
        if (mSelectedPiece == null) return -1;
        return mSelectedPiece.getMV();
    }

    /** chance to succeed the current move */
    public float getCurrentMoveSuccessChance() {
        if (mSelectedPiece == null) return 1;
        if (mSelectedPath.size() == 0) return 1;

        float successchance = 1;
        for (int i = 0, n = mSelectedPath.size(); i < n; ++i) {
            Step step = mSelectedPath.get(i);
            successchance *= step.successChance;
        }

        return successchance;
    }

    /** shows a movement hint */
    private void triggerShowHint(int tileX, int tileY) {
        if (mHintShowTimer > 0) return;

        mHintShowTimer = HINT_SHOW_DURATION;

        int hintIndex = 0;
        for (int x = tileX - 1; x <= tileX + 1; ++x) {
            if (0 > x || x >= PITCH_WIDTH) continue;
            for (int y = tileY - 1; y <= tileY + 1; ++y) {
                if (0 > y || y >= PITCH_HEIGHT) continue;
                if (x == tileX && y == tileY) continue;

                int index = x + y * PITCH_WIDTH;
                boolean possible = true;
                if (mPitch[index] != null) {
                    possible = false;
                }
                for (int i = 0, n = mSelectedPath.size(); i < n; ++i) {
                    Step step = mSelectedPath.get(i);
                    if (step.tileX == x && step.tileY == y) {
                        possible = false;
                        break;
                    }
                }

                if (possible) {
                    Sprite sprite = mHintSprites.get(hintIndex);
                    ++hintIndex;
                    if (!sprite.hasParent()) mGfxMap.attachChild(sprite);
                    sprite.setAlpha(0);
                    sprite.setVisible(true);
                    sprite.setPosition(x * GameScene.TILE_PIXELS, y * GameScene.TILE_PIXELS);
                }
            }
        }

        // for(int n = mHintSprites.size(); hintIndex < n; ++hintIndex) {
        // Sprite sprite = mHintSprites.get(hintIndex);
        // sprite.setVisible(false);
        // }
    }

    /** hide movement hints */
    private void hideHints() {
        mHintShowTimer = 0;
        for (int hintIndex = 0, n = mHintSprites.size(); hintIndex < n; ++hintIndex) {
            Sprite sprite = mHintSprites.get(hintIndex);
            sprite.setVisible(false);
        }
    }

    /** tickwise update */
    public void update(float dt) {
        if (mHintShowTimer > 0) {
            mHintShowTimer -= dt;

            float alpha = 1;
            if (mHintShowTimer > HINT_SHOW_DURATION - .25f) {
                alpha = (HINT_SHOW_DURATION - mHintShowTimer) * 4;
            }
            if (mHintShowTimer < .25f) {
                alpha = mHintShowTimer * 4;
            }
            for (int hintIndex = 0, n = mHintSprites.size(); hintIndex < n; ++hintIndex) {
                Sprite sprite = mHintSprites.get(hintIndex);
                if (sprite.isVisible()) {
                    sprite.setAlpha(alpha);
                }
            }
            if (mHintShowTimer <= 0) {
                hideHints();
            }
        }

        if (mExecutingPlan) {
            mExecutingTimer -= dt;
            if (mExecutingTimer <= 0) {
                mExecutingTimer += STEP_DURATION;
                executeNextPlannedStep();
            }
        }
    }

    /** the current dice log receiver */
    IDiceLogReceiver mLogger;

    /** execute the current step from the plan */
    private boolean executeNextPlannedStep() {
        boolean failed = false;
        int index = 0;
        Step step = mSelectedPath.get(index);
        if (step.gfi) {
            int logtype = IDiceLogReceiver.LOG_SUCCESS;
            StringBuffer log = new StringBuffer();
            int die = 1 + mRandom.nextInt(6);
            if (die > 1) {
                log.append("SUCCESS");
            } else {
                log.append("FAILURE");
                logtype = IDiceLogReceiver.LOG_FAILURE;
                failed = true;
            }
            log.append(" - GFI 2+; rolled ");
            log.append(die);
            mLogger.showDiceLogLine(logtype, log);
        }

        if (!failed && step.dodge) {
            int logtype = IDiceLogReceiver.LOG_SUCCESS;
            StringBuffer log = new StringBuffer();
            int die = 1 + mRandom.nextInt(6);
            if (die > 1 && die >= step.minDodgeRoll) {
                log.append("SUCCESS - ");
            } else {
                log.append("FAILURE - ");
                failed = true;
                logtype = IDiceLogReceiver.LOG_FAILURE;
            }
            log.append("Dodge ");
            log.append(7 - mSelectedPiece.getAG());
            log.append("+; rolled ");
            log.append(die);
            log.append(" + 1");// (dodge)");
            int tackleZones = getTackleZones(step.tileX, step.tileY, mSelectedPiece.getTeam());
            if (tackleZones > 0) {
                log.append(" - ");
                log.append(tackleZones);
                // log.append("(into tacklezones)");
            }
            log.append(" = ");
            log.append(die + 1 - tackleZones);

            mLogger.showDiceLogLine(logtype, log);
        }

        float currentX = mSelectedPiece.getPositionX() * GameScene.TILE_PIXELS;
        float currentY = mSelectedPiece.getPositionY() * GameScene.TILE_PIXELS;
        float targetX = step.tileX * GameScene.TILE_PIXELS;
        float targetY = step.tileY * GameScene.TILE_PIXELS;
        MoveModifier movemod = new MoveModifier(STEP_DURATION, currentX + PIECE_OFFSET_X, targetX + PIECE_OFFSET_X,
                currentY + PIECE_OFFSET_Y, targetY + PIECE_OFFSET_Y);
        IEntityModifierMatcher matcher = new IEntityModifier.IEntityModifierMatcher() {
            @Override
            public boolean matches(IModifier<IEntity> pObject) {
                return pObject instanceof MoveModifier;
            }
        };
        mSelectedPiece.getEntity().unregisterEntityModifiers(matcher);
        mSelector.unregisterEntityModifiers(matcher);
        MoveModifier selMove = new MoveModifier(STEP_DURATION, currentX, targetX, currentY, targetY);
        mSelector.registerEntityModifier(selMove);
        mSelectedPiece.getEntity().registerEntityModifier(movemod);

        int oldIndex = mSelectedPiece.getPositionX() + mSelectedPiece.getPositionY() * PITCH_WIDTH;
        mPitch[oldIndex] = null;
        mSelectedPiece.setPosition(step.tileX, step.tileY);
        int newIndex = step.tileX + step.tileY * PITCH_WIDTH;
        mPitch[newIndex] = mSelectedPiece;

        if (failed) {
            cancelPlannedMove();
            mExecutingPlan = false;
        } else {
            step.sprite.detachSelf();
            mSelectedPath.remove(index);
            mExecutingPlan = mSelectedPath.size() > 0;
        }

        return !failed;
    }

    /** actually attempt the planned move */
    public void executePlannedMove(IDiceLogReceiver logger) {
        mLogger = logger;
        mLogger.showDiceLogLine(IDiceLogReceiver.LOG_NEUTRAL, "---");
        mExecutingPlan = true;
        mExecutingTimer = 0;

    }

    /** cancel previously planned move */
    public void cancelPlannedMove() {
        for (int i = 0, n = mSelectedPath.size(); i < n; ++i) {
            mSelectedPath.get(i).sprite.detachSelf();
        }
        mSelectedPath.clear();
        mSelectedPiece = null;
        mSelector.setVisible(false);
    }
}

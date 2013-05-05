package com.example.bigbangbowl.game;

import java.util.Random;
import java.util.Vector;

import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.IEntityModifier.IEntityModifierMatcher;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.opengl.font.BitmapFont;
import org.andengine.util.modifier.IModifier;

import com.example.bigbangbowl.BBBActivity;
import com.example.bigbangbowl.GameResources;
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
    public static final int PITCH_WIDTH = 22;
    /** height/breadth of the pitch */
    public static final int PITCH_HEIGHT = 10;

    /** graphical offset for a player piece */
    public static final int PIECE_OFFSET_X = -32;
    /** graphical offset for a player piece */
    public static final int PIECE_OFFSET_Y = -128;
    /** duration of a step */
    private static final float STEP_DURATION = .5f;

    /** the highlight thingy */
    private Sprite mSelector, mAttackSelector;
    private Vector<Step> mSelectedPath = new Vector<Step>();
    /** path has an attack - so must roll first */
    private boolean mPathHasAttack;

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
    /** currently acting piece */
    private PlayerPiece mCurrentActor;

    /** team 0 */
    private PlayerPiece[] mTeam0;
    /** team 1 */
    private PlayerPiece[] mTeam1;

    /** pitch */
    private PlayerPiece[] mPitch;

    /** the graphical map thingy */
    private Entity mGfxMap, mGfxMapBg;
    /** the layer on the map for the player pieces */
    private Entity mPiecesLayer;
    /** the random number supplier... */
    private Random mRandom;
    /** the hud... */
    private BowlHud mHud;

    public static final int TUTORIAL_MOVEMENT = 1;
    public static final int TUTORIAL_GFI = 2;
    public static final int TUTORIAL_BLOCKING = 1000;
    public static final int TUTORIAL_FINISHED = 0xfffff;

    /**
     * tutorial rules setting - may be used to disable certain things usually
     * available
     */
    private int mTutorialRules = TUTORIAL_FINISHED;

    /** team which may move atm */
    private int mCurrentTeam;

    /** load resources */
    public void loadResources(BBBActivity activity, BowlHud hud) {
        mHud = hud;

        GameResources res = GameResources.getInstance();

        mSelector = res.createSprite(0, 0, GameResources.FRAME_SELECTOR0);
        mAttackSelector = res.createSprite(0, 0, GameResources.FRAME_SELECTOR2);

        mHintSprites = new Vector<Sprite>(8);
        for (int i = 0; i < 8; ++i) {
            Sprite sprite = res.createSprite(0, 0, GameResources.FRAME_HINT);
            mHintSprites.add(sprite);
        }

        mRandom = new Random(System.currentTimeMillis());
    }

    public PlayerPiece createPiece(int frame, int ST, int AG, int MA, int AV, int team) {
        GameResources res = GameResources.getInstance();
        BitmapFont font = res.getBitmapFont();

        PlayerPiece piece = new PlayerPiece(ST, AG, MA, AV);
        piece.setTeam(team);
        piece.setState(PlayerPiece.STATE_STANDING);
        Sprite foot;
        if (team == 0) foot = res.createSprite(0, 0, GameResources.FRAME_FOOT_BLUE);
        else if (team == 1) foot = res.createSprite(0, 0, GameResources.FRAME_FOOT_GREEN);
        else foot = res.createSprite(0, 0, GameResources.FRAME_FOOT_YELLOW);

        Sprite sprite = res.createSprite(0, 0, frame);

        Text status = new Text(-PIECE_OFFSET_X, -PIECE_OFFSET_Y, font, " ", res.getVbo());
        foot.attachChild(status);
        foot.attachChild(sprite);
        piece.setEntity(foot, status);

        return piece;
    }

    public void createTeams(BBBActivity activity) {
        mTeam0 = new PlayerPiece[11];
        mTeam1 = new PlayerPiece[11];
        mPitch = new PlayerPiece[PITCH_HEIGHT * PITCH_WIDTH];
        for (int i = 0; i < 3; ++i) {
            mTeam0[i] = createPiece(GameResources.FRAME_VAMPIRE_VAMPIRE, 4, 4, 6, 8, 0);
        }
        for (int i = 3; i < 11; ++i) {
            mTeam0[i] = createPiece(GameResources.FRAME_VAMPIRE_THRALL, 3, 3, 6, 7, 0);
        }
        for (int i = 0; i < 3; ++i) {
            mTeam1[i] = createPiece(GameResources.FRAME_CHAOS_WARRIOR, 4, 3, 5, 9, 1);
        }
        for (int i = 3; i < 11; ++i) {
            mTeam1[i] = createPiece(GameResources.FRAME_CHAOS_BEASTMAN, 3, 3, 6, 8, 1);
        }
    }

    /** set the base map entity on which to place the grid and everything */
    public void setMap(Entity map) {
        mGfxMap = map;
        mGfxMapBg = new Entity();
        mGfxMap.attachChild(mGfxMapBg);
        Entity grid = new Entity();
        mGfxMap.attachChild(grid);
        mPiecesLayer = new Entity();
        mGfxMap.attachChild(mPiecesLayer);

        GameResources res = GameResources.getInstance();
        for (int x = 0; x < PITCH_WIDTH; x += 2) {
            for (int y = 0; y < PITCH_HEIGHT; y += 2) {
                Sprite sprite = res.createSprite(x * GameScene.TILE_PIXELS, y * GameScene.TILE_PIXELS,
                        GameResources.FRAME_MAP_GRID);
                grid.attachChild(sprite);
            }
        }
    }

    public void placeTeams() {
        int places0[] = { 11, 3, 11, 4, 11, 5, 10, 7, 10, 1, 5, 4 };
        int places1[] = { 12, 3, 12, 4, 12, 5, 13, 7, 13, 1, 20, 4 };
        for (int i = 0; i < places0.length / 2; ++i) {
            int x = places0[i * 2];
            int y = places0[i * 2 + 1];

            placePiece(mTeam0[i], x, y);
            // mTeam0[i].setPosition(x, y);
            // mTeam0[i].getEntity().setPosition(x * 128 - 32, y * 128 - 128);
            // mPiecesLayer.attachChild(mTeam0[i].getEntity());
            //
            // mPitch[x + y * PITCH_WIDTH] = mTeam0[i];
        }

        for (int i = 0; i < places1.length / 2; ++i) {
            int x = places1[i * 2];
            int y = places1[i * 2 + 1];
            placePiece(mTeam1[i], x, y);
            // mTeam1[i].setPosition(x, y);
            // mTeam1[i].getEntity().setPosition(x * 128 + PIECE_OFFSET_X, y *
            // 128 + PIECE_OFFSET_Y);
            // mPiecesLayer.attachChild(mTeam1[i].getEntity());
            //
            // mPitch[x + y * PITCH_WIDTH] = mTeam1[i];
        }

        mCurrentTeam = mRandom.nextInt(2);
        switchTeams();
    }

    /**
     * places a single player
     * 
     * @return false if placing failed, b/c square was occupied
     */
    public boolean placePiece(PlayerPiece piece, int tileX, int tileY) {
        int index = tileX + tileY * PITCH_WIDTH;
        if (mPitch[index] != null) return false;

        mPitch[index] = piece;
        piece.setPosition(tileX, tileY);
        if (piece.getEntity() != null) {
            piece.getEntity().setPosition(tileX * GameScene.TILE_PIXELS + PIECE_OFFSET_X,
                    tileY * GameScene.TILE_PIXELS + PIECE_OFFSET_Y);
            mPiecesLayer.attachChild(piece.getEntity());
        }

        return true;
    }

    public void dispose() {
        mGfxMap = null;
        mHud = null;
    }

    public void switchTeams() {
        cancelPlannedMove();

        PlayerPiece[] team;
        if (mCurrentTeam == 0) {
            mCurrentTeam = 1;
            team = mTeam1;
        } else {
            mCurrentTeam = 0;
            team = mTeam0;
        }

        for (int i = 0; i < team.length; ++i) {
            team[i].resetTeamTurn();
        }

        mHud.setCurrentTeam(mCurrentTeam);
    }

    /** informs the pitch that the user chose to tap the given tile */
    public void clickedTile(int tileX, int tileY) {
        if (0 > tileX || tileX >= PITCH_WIDTH || 0 > tileY || tileY >= PITCH_HEIGHT) {
            return;
        }

        if (!mSelector.hasParent()) {
            mGfxMap.attachChild(mSelector);
        }
        if (!mAttackSelector.hasParent()) {
            mGfxMap.attachChild(mAttackSelector);
        }

        int index = tileX + tileY * PITCH_WIDTH;
        boolean teamAllowed = false;
        if (mPitch[index] != null) {
            teamAllowed = mPitch[index].getTeam() == mCurrentTeam || mPitch[index].getTeam() > 1;
        }
        if (mPitch[index] != null && teamAllowed && mPitch[index].canAct()) {
            cancelPlannedMove();

            mSelectedPiece = mPitch[index];
            mSelector.setPosition(tileX * GameScene.TILE_PIXELS, tileY * GameScene.TILE_PIXELS);
            mSelector.setVisible(true);
            if (mSelectedPiece.getState() == PlayerPiece.STATE_DOWN) {
                Step step = makeStandUpStep(tileX, tileY);
                mSelectedPath.add(step);
            }
        }

        if (mSelectedPiece == null) {
            mSelector.setPosition(tileX * GameScene.TILE_PIXELS, tileY * GameScene.TILE_PIXELS);
            mSelector.setVisible(true);
        } else {
            int lastPosX = mSelectedPiece.getPositionX();
            int lastPosY = mSelectedPiece.getPositionY();

            boolean fail = mPathHasAttack;
            boolean showHint = false;
            if (tileX == lastPosX && tileY == lastPosY) {
                fail = true;
                showHint = true;
            }
            if (mPitch[index] != null) {
                fail = true;
                showHint = true;
                // TODO add attack here!
                // TODO add BLITZ check here
                if (mSelectedPath.size() == 0) {
                    if (mTutorialRules >= TUTORIAL_BLOCKING && mPitch[index].getTeam() != mSelectedPiece.getTeam()) {
                        showHint = false;
                        mPathHasAttack = true;
                        mAttackSelector.setVisible(true);
                        mAttackSelector.setPosition(tileX * GameScene.TILE_PIXELS, tileY * GameScene.TILE_PIXELS);

                        Step step = makeBlockStep(tileX, tileY);
                        step.sprite = null;
                        mSelectedPath.add(step);
                    }
                }
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

            int extraMove = 2;
            if(mTutorialRules < TUTORIAL_GFI) extraMove = 0;
            if (mSelectedPath.size() >= extraMove + mSelectedPiece.getRemainingMove()) {
                fail = true;
                showHint = false;
            }

            if (!fail) {
                Sprite sprite = GameResources.getInstance().createSprite(tileX * GameScene.TILE_PIXELS,
                        tileY * GameScene.TILE_PIXELS, GameResources.FRAME_SELECTOR1);
                mGfxMap.attachChild(sprite);
                Step step = makeNextStep(tileX, tileY, lastPosX, lastPosY);
                step.sprite = sprite;
                mSelectedPath.add(step);

                if (mSelectedPath.size() == 1) {
                    mHud.triggerTutorialPlanBegins();
                }
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
        step.movement = 1;

        if (getTackleZones(lastPosX, lastPosY, mSelectedPiece.getTeam()) > 0) {
            int tacklemod = getTackleZones(tileX, tileY, mSelectedPiece.getTeam());
            step.dice = true;
            step.dodge = true;
            step.minDodgeRoll = 7 - mSelectedPiece.getAG() - 1 + tacklemod;

            int diceChance = Math.max(2, Math.min(step.minDodgeRoll, 6));
            step.successChance = 1 - (diceChance - 1) / 6.0f;
        }

        if (getCurrentSteps() >= mSelectedPiece.getRemainingMove()) {
            step.gfi = true;
            step.successChance *= 5.0f / 6.0f;
        }

        return step;
    }

    /** plans to stand up the player! */
    public Step makeStandUpStep(int tileX, int tileY) {
        Step step = new Step();
        step.tileX = tileX;
        step.tileY = tileY;
        step.type = Step.TYPE_STAND_UP;
        step.successChance = 1;
        // TODO dkr: add chance to fail where appropriate!

        step.movement = Math.min(3, mSelectedPiece.getMV());

        return step;
    }

    private int getBlockDice(int tileX, int tileY, PlayerPiece source) {
        int dice = 1;
        PlayerPiece target = null;

        Vector<PlayerPiece> candidates = new Vector<PlayerPiece>();
        for (int x = tileX - 1; x <= tileX + 1; ++x) {
            if (0 > x || x >= PITCH_WIDTH) continue;
            for (int y = tileY - 1; y <= tileY + 1; ++y) {
                if (0 > y || y >= PITCH_HEIGHT) continue;
                int index = x + y * PITCH_WIDTH;
                if (x == tileX && y == tileY) {
                    target = mPitch[index];
                    continue;
                }
                if (mPitch[index] != null && mPitch[index] != source && mPitch[index].getTeam() == source.getTeam()) {
                    candidates.add(mPitch[index]);
                }
            }
        }
        for (int x = source.getPositionX() - 1; x <= source.getPositionX() + 1; ++x) {
            if (0 > x || x >= PITCH_WIDTH) continue;
            for (int y = source.getPositionY() - 1; y <= source.getPositionY() + 1; ++y) {
                if (0 > y || y >= PITCH_HEIGHT) continue;
                int index = x + y * PITCH_WIDTH;
                if (mPitch[index] != null && mPitch[index] != source && mPitch[index] != target
                        && mPitch[index].getTeam() != source.getTeam()) {
                    candidates.add(mPitch[index]);
                }
            }
        }

        int defSt = target.getST();
        int attSt = source.getST();

        for (int i = candidates.size() - 1; i >= 0; --i) {
            PlayerPiece piece = candidates.get(i);
            int baseX = piece.getPositionX();
            int baseY = piece.getPositionY();
            boolean removed = !piece.getTackleZone();
            if (!removed) {
                for (int x = baseX - 1; x <= baseX + 1; ++x) {
                    if (0 > x || x >= PITCH_WIDTH) continue;
                    for (int y = baseY - 1; y <= baseY + 1; ++y) {
                        if (0 > y || y >= PITCH_HEIGHT) continue;
                        int index = x + y * PITCH_WIDTH;
                        if (mPitch[index] == null) continue;
                        if (mPitch[index] == source) continue;
                        if (mPitch[index] == target) continue;
                        if (mPitch[index].getTeam() == piece.getTeam()) continue;
                        if (!mPitch[index].getTackleZone()) continue;

                        candidates.remove(i);
                        removed = true;
                        break;
                    }
                    if (removed) break;
                }
            }
            if (!removed) {
                if (piece.getTeam() == source.getTeam()) ++attSt;
                else ++defSt;
            }
        }

        if (defSt > 2 * attSt) dice = -3;
        else if (defSt > attSt) dice = -2;
        else if (defSt == attSt) dice = 1;
        else if (defSt <= 2 * attSt) dice = 2;
        else dice = 3;

        return dice;
    }

    public Step makeBlockStep(int tileX, int tileY) {
        Step step = new Step();
        step.tileX = tileX;
        step.tileY = tileY;
        step.type = Step.TYPE_BLOCK;
        step.successChance = 1;
        step.movement = 1;

        int dice = getBlockDice(tileX, tileY, mSelectedPiece);
        step.blockDice = dice;

        if (dice > 0) {
            float failchance = 1;
            for (int i = 0; i < dice; ++i) {
                failchance *= 2.0f / 6.0f;
            }
            step.successChance = 1 - failchance;
        } else {
            float successchance = 1;
            for (int i = 0; i > dice; --i) {
                successchance *= 4.0f / 6.0f;
            }
            step.successChance = successchance;
        }

        return step;
    }

    /** how many steps the currently planned move has */
    public int getCurrentSteps() {
        int movement = 0;
        for (int i = 0, n = mSelectedPath.size(); i < n; ++i) {
            movement += mSelectedPath.get(i).movement;
        }
        return movement;
    }

    /** how many steps the currently selected player may make */
    public int getCurrentMovementLimit() {
        if (mSelectedPiece == null) return -1;
        return mSelectedPiece.getRemainingMove();
    }

    /** check if there's currently someone selected */
    public boolean hasSelection() {
        return mSelectedPiece != null;
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
                boolean success = executeNextPlannedStep();
                if (mSelectedPath.size() <= 0) {
                    mHud.triggerTutorialPlanExecuted();
                    mExecutingPlan = false;
                }

                if (!success) {
                    if (mLogger != null) {
                        mLogger.showDiceLogLine(IDiceLogReceiver.LOG_NEUTRAL, "=> TURNOVER");
                    }
                    mHud.showWarningTurnover();
                    switchTeams();
                }
            }
        }
    }

    /** the current dice log receiver */
    IDiceLogReceiver mLogger;

    private boolean executeNextPlannedStep() {
        Step step = mSelectedPath.get(0);
        switch (step.type) {
        case Step.TYPE_MOVE:
            return executeNextMoveStep();
        case Step.TYPE_BLOCK:
            return executeNextBlockStep();
        case Step.TYPE_STAND_UP:
            return executeNextStandUpStep();
        }

        return true;
    }

    /** execute a stand up! */
    private boolean executeNextStandUpStep() {
        mCurrentActor.standUp();

        mSelectedPath.remove(0);
        if (mSelectedPath.size() <= 0) mExecutingPlan = false;
        return true;
    }

    /** execute the current step from the plan */
    private boolean executeNextMoveStep() {
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
            if (die > 1) {
                log.append(" + 1");// (dodge)");
                int tackleZones = getTackleZones(step.tileX, step.tileY, mSelectedPiece.getTeam());
                if (tackleZones > 0) {
                    log.append(" - ");
                    log.append(tackleZones);
                    log.append("(TZ)");
                    // log.append("(into tacklezones)");
                }
                log.append(" = ");
                log.append(die + 1 - tackleZones);
            }

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

        mSelectedPiece.useMovement(1);

        if (failed) {
            mSelectedPiece.setState(PlayerPiece.STATE_DOWN);
            rollHurtDice(mSelectedPiece);
            cancelPlannedMove();
        } else {
            step.sprite.detachSelf();
            mSelectedPath.remove(index);
        }

        return !failed;
    }

    private boolean executeNextBlockStep() {
        boolean failed = false;
        StringBuffer blockLog = new StringBuffer();
        StringBuffer fakeChoiceLog = new StringBuffer();
        final String[] logNames = { "att-down", "bothdown", "push", "push", "def-stumbles", "def-down" };

        Step step = mSelectedPath.get(0);
        int dicecount = Math.abs(step.blockDice);
        Vector<Integer> dicerolls = new Vector<Integer>(dicecount);

        blockLog.append("BLOCK ROLLS ");
        int logType;
        blockLog.append("[");
        blockLog.append(step.blockDice);
        blockLog.append("]: ");
        if (step.blockDice > 0) {
            logType = IDiceLogReceiver.LOG_NEUTRAL;
            // blockLog.append("Attackers choice: ");
        } else {
            logType = IDiceLogReceiver.LOG_FAILURE;
            // blockLog.append("Defenders choice: ");
        }

        // TODO remove fake block dice picking!
        int bestDie = 1;
        int worstDie = 6;
        for (int i = 0; i < dicecount; ++i) {
            int die = 1 + mRandom.nextInt(6);
            dicerolls.add(die);
            if (die > bestDie) bestDie = die;
            if (die < worstDie) worstDie = die;
            blockLog.append(logNames[die - 1]);
            blockLog.append(" ");
        }

        int chosenDie = bestDie;
        if (step.blockDice < 0) {
            chosenDie = worstDie;
        }

        fakeChoiceLog.append("CHOSE: ");
        fakeChoiceLog.append(logNames[chosenDie - 1]);

        if (mLogger != null) {
            mLogger.showDiceLogLine(logType, blockLog);
            if (chosenDie < 3) mLogger.showDiceLogLine(IDiceLogReceiver.LOG_FAILURE, fakeChoiceLog);
            else if (chosenDie > 4) mLogger.showDiceLogLine(IDiceLogReceiver.LOG_SUCCESS, fakeChoiceLog);
            else mLogger.showDiceLogLine(IDiceLogReceiver.LOG_NEUTRAL, fakeChoiceLog);
        }

        int index = step.tileX + step.tileY * PITCH_WIDTH;
        PlayerPiece target = mPitch[index];
        switch (chosenDie) {
        case 1:
            // attacker down
            mSelectedPiece.setState(PlayerPiece.STATE_DOWN);
            rollHurtDice(mSelectedPiece);
            failed = true;
            break;
        case 2:
            // both down
            mSelectedPiece.setState(PlayerPiece.STATE_DOWN);
            target.setState(PlayerPiece.STATE_DOWN);
            rollHurtDice(mSelectedPiece);
            rollHurtDice(target);
            failed = true;
            break;
        case 3:
        case 4:
            // push
            break;
        case 5:
            // defender stumbles
            target.setState(PlayerPiece.STATE_DOWN);
            rollHurtDice(target);
            break;
        case 6:
            // defender down
            target.setState(PlayerPiece.STATE_DOWN);
            rollHurtDice(target);
            break;
        }

        // TODO BLITZ check here!
        mSelectedPiece.endTurn();
        cancelPlannedMove();
        mCurrentActor = null;

        if (mSelectedPath.size() > 0) mSelectedPath.remove(0);
        return !failed;
    }

    /** actually attempt the planned move */
    public void executePlannedMove(IDiceLogReceiver logger) {
        mLogger = logger;
        mExecutingPlan = true;
        mExecutingTimer = 0;

        if (mCurrentActor != null && mCurrentActor != mSelectedPiece) {
            mCurrentActor.endTurn();
            mLogger.showDiceLogLine(IDiceLogReceiver.LOG_NEUTRAL, "---");
        }
        mCurrentActor = mSelectedPiece;
        mCurrentActor.beginTurn();
    }

    /** cancel previously planned move */
    public void cancelPlannedMove() {
        for (int i = 0, n = mSelectedPath.size(); i < n; ++i) {
            Sprite sprite = mSelectedPath.get(i).sprite;
            if (sprite != null) sprite.detachSelf();
        }
        mSelectedPath.clear();
        mSelectedPiece = null;
        mSelector.setVisible(false);
        mAttackSelector.setVisible(false);
        mPathHasAttack = false;
    }

    private void rollHurtDice(PlayerPiece piece) {
        int armorLogType = IDiceLogReceiver.LOG_NEUTRAL;
        int injuryLogType = IDiceLogReceiver.LOG_NEUTRAL;
        int die0 = 1 + mRandom.nextInt(6);
        int die1 = 1 + mRandom.nextInt(6);

        StringBuffer armorHurt = new StringBuffer();
        StringBuffer injuryHurt = null;
        if (die0 + die1 > piece.getAV()) {
            armorHurt.append("ARMOR BROKEN");
            injuryHurt = new StringBuffer();
            int die2 = 1 + mRandom.nextInt(6);
            int die3 = 1 + mRandom.nextInt(6);
            int injuryRoll = die2 + die3;
            if (injuryRoll <= 7) {
                // STUNNED
                injuryHurt.append("STUNNED");
                piece.setState(PlayerPiece.STATE_STUNNED);
            } else if (injuryRoll <= 9) {
                // KNOCKED OUT
                injuryHurt.append("KNOCKED OUT");
                piece.setState(PlayerPiece.STATE_KNOCKEDOUT);
                removePieceFromField(piece, true);
                injuryLogType = IDiceLogReceiver.LOG_SUCCESS;
            } else {
                // INJURY
                injuryHurt.append("CASUALTY");
                piece.setState(PlayerPiece.STATE_INJURED);
                removePieceFromField(piece, true);
                injuryLogType = IDiceLogReceiver.LOG_SUCCESS;
            }
            injuryHurt.append(" - injury roll: ");
            injuryHurt.append(die2);
            injuryHurt.append("+");
            injuryHurt.append(die3);
            injuryHurt.append(" = ");
            injuryHurt.append(injuryRoll);
        } else {
            armorHurt.append("ARMOR HELD");
        }
        armorHurt.append(" - AV");
        armorHurt.append(piece.getAV());
        armorHurt.append(" rolled ");
        armorHurt.append(die0);
        armorHurt.append("+");
        armorHurt.append(die1);
        armorHurt.append(" = ");
        armorHurt.append(die0 + die1);

        if (mLogger != null) {
            mLogger.showDiceLogLine(armorLogType, armorHurt);
            if (injuryHurt != null) mLogger.showDiceLogLine(injuryLogType, injuryHurt);
        }
    }

    public void removePieceFromField(PlayerPiece piece, boolean leaveStain) {
        Entity entity = piece.getEntity();
        entity.clearEntityModifiers();
        entity.detachSelf();

        int index = piece.getPositionX() + piece.getPositionY() * PITCH_WIDTH;
        mPitch[index] = null;

        if (leaveStain) {
            Sprite blood = GameResources.getInstance().createSprite(piece.getPositionX() * GameScene.TILE_PIXELS,
                    piece.getPositionY() * GameScene.TILE_PIXELS, GameResources.FRAME_BLOOD);
            mGfxMapBg.attachChild(blood);
        }
    }

    /** limits the rules to only support certain things */
    public void setTutorialRules(int rules) {
        mTutorialRules = rules;
    }
}

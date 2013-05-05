package com.example.bigbangbowl.tutorial;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.entity.Entity;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.util.color.Color;

import com.example.bigbangbowl.BBBActivity;
import com.example.bigbangbowl.GameResources;
import com.example.bigbangbowl.game.BowlHud;
import com.example.bigbangbowl.game.PlayerPiece;
import com.example.bigbangbowl.game.ThePitch;
import com.example.bigbangbowl.game.BowlHud.IConfirmationCallback;
import com.example.bigbangbowl.game.BowlHud.IEndturnCallback;

public class TutorialScene extends Scene implements IOnSceneTouchListener, IScrollDetectorListener,
        IPinchZoomDetectorListener, IConfirmationCallback, IEndturnCallback, BowlHud.ITutorialCallback {
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
    /** the camera... stored for convenience manipulation */
    private ZoomCamera mZoomCamera;
    // /** the engine */
    // private Engine mEngine;
    private SurfaceScrollDetector mScrollDetector;
    private PinchZoomDetector mPinchZoomDetector;

    /** the hud */
    private BowlHud mHud;

    /** the current tutorial step */
    private int mTutorialStep = 0;

    public TutorialScene(BBBActivity activity, ZoomCamera camera) {
        // mEngine = activity.getEngine();
        mZoomCamera = camera;
        mTextureAtlas = new BitmapTextureAtlas(activity.getTextureManager(), 4 * 256, 256);
        mFieldTexture0 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity,
                "gfx/field_green00.png", 0 * 256, 0);
        mFieldTexture1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity,
                "gfx/field_green01.png", 1 * 256, 0);
        mFieldTextureLeft = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity,
                "gfx/field_green02.png", 2 * 256, 0);
        mFieldTextureRight = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, activity,
                "gfx/field_green03.png", 3 * 256, 0);

        mTextureAtlas.load();

        mHud = new BowlHud();
        mHud.prepareResources(activity);
        mHud.setEndturnCallback(this);
        mZoomCamera.setHUD(mHud);

        mPitch = new ThePitch();
        mPitch.loadResources(activity, mHud);
        mPitch.createTeams(activity);

        this.setBackground(new Background(0, 0, 0));

        Entity map = new Entity(0, 0);

        int size = MAP_HEIGHT * MAP_WIDTH / 4;
        mFieldSprites = new Sprite[size];

        for (int x = 0; x < MAP_WIDTH; x += 2) {
            for (int y = 0; y < MAP_HEIGHT; y += 2) {
                int index = x / 2 + y * 20 / 4;
                ITextureRegion region = null;
                if (x == 0) region = mFieldTextureLeft;
                else if (x == MAP_WIDTH - 2) region = mFieldTextureRight;
                else if (y == 0 || y == MAP_HEIGHT - 2) region = mFieldTexture1;
                else region = mFieldTexture0;
                mFieldSprites[index] = new Sprite(x * TILE_PIXELS - TILE_PIXELS, y * TILE_PIXELS, region,
                        activity.getVertexBufferObjectManager());
                map.attachChild(mFieldSprites[index]);
            }
        }

        GameResources res = GameResources.getInstance();
        Sprite endzone0 = res.createSprite(0, 0, GameResources.FRAME_MAP_ENDZONE);
        endzone0.setScale(-1, -1);
        Sprite endzone1 = res.createSprite(21 * TILE_PIXELS, 0, GameResources.FRAME_MAP_ENDZONE);
        map.attachChild(endzone0);
        map.attachChild(endzone1);

        float targetWidth = MAP_WIDTH * TILE_PIXELS;
        float targetHeight = MAP_HEIGHT * TILE_PIXELS;
        float scaleX = 1;
        float scaleY = 1;
        if (targetWidth > activity.getCurrentWidth()) {
            scaleX = activity.getCurrentWidth() / targetWidth;
        }
        if (targetHeight > BBBActivity.CAMERA_HEIGHT) {
            scaleY = BBBActivity.CAMERA_HEIGHT / targetHeight;
        }
        float scale = 1;
        if (scaleX < scaleY) scale = scaleX;
        else scale = scaleY;

        scale = 2 * scale;
        float posX = (activity.getCurrentWidth() - targetWidth * scale) / 2;
        float posY = (BBBActivity.CAMERA_HEIGHT - targetHeight * scale) / 2;

        mMapDisplay = new Entity();
        mMapDisplay.setScale(scale);
        mMapDisplay.setPosition(posX, posY);

        mMapDisplay.attachChild(map);
        mPitch.setMap(mMapDisplay);
        this.attachChild(mMapDisplay);

        this.setOnAreaTouchTraversalFrontToBack();

        this.mScrollDetector = new SurfaceScrollDetector(this);
        this.mPinchZoomDetector = new PinchZoomDetector(this);

        this.setOnSceneTouchListener(this);
        this.setTouchAreaBindingOnActionDownEnabled(true);

        mHud.registerTutorialObserver(this);
        this.onTutorialMessageContinue();

        // mPitch.placeTeams();
    }

    @Override
    public void dispose() {
        mPitch.dispose();
        mHud.detachSelf();
        mHud.dispose();

        mFieldTexture0 = null;
        mFieldTexture1 = null;
        mFieldTextureLeft = null;
        mFieldTextureRight = null;
        mTextureAtlas.unload();

        // mEngine = null;
        mZoomCamera = null;

        super.dispose();
    }

    /** currently has a touch */
    boolean mHasTouch;
    /** location of touch */
    float mTouchStartX, mTouchStartY;
    /** previous recorded touch location */
    float mTouchDistance;
    private float mPinchZoomStartedCameraZoomFactor;

    @Override
    public boolean onSceneTouchEvent(Scene scene, TouchEvent touchEvent) {
        this.mPinchZoomDetector.onTouchEvent(touchEvent);

        if (this.mPinchZoomDetector.isZooming()) {
            this.mScrollDetector.setEnabled(false);
            return true;
        } else {
            if (touchEvent.isActionDown()) {
                this.mScrollDetector.setEnabled(true);
                mTouchDistance = 0;
            }
            this.mScrollDetector.onTouchEvent(touchEvent);
        }

        float scale = mMapDisplay.getScaleX();
        float minX = mMapDisplay.getX();
        float maxX = minX + scale * TILE_PIXELS * MAP_WIDTH;
        float minY = mMapDisplay.getY();
        float maxY = minY + scale * TILE_PIXELS * MAP_HEIGHT;

        float x = touchEvent.getX();
        float y = touchEvent.getY();

        if (minX < x && x < maxX && minY < y && y < maxY) {
            switch (touchEvent.getAction()) {
            case TouchEvent.ACTION_DOWN:
                // if (mHasTouch) return false;
                mTouchStartX = x;
                mTouchStartY = y;
                mHasTouch = true;
                break;
            case TouchEvent.ACTION_UP:
                if (!mHasTouch) return false;
                float dx = mTouchStartX - x;
                float dy = mTouchStartY - y;
                float distance = dx * dx + dy * dy;
                if (distance < 256) {
                    int tileX = (int) ((x - minX) / scale) / TILE_PIXELS;
                    int tileY = (int) ((y - minY) / scale) / TILE_PIXELS;
                    mPitch.clickedTile(tileX, tileY);

                    int steps = mPitch.getCurrentSteps();
                    int limit = mPitch.getCurrentMovementLimit();
                    if (mPitch.hasSelection()) {
                        mHud.setMovement(limit - steps, limit);
                        mHud.setCurrentSuccessChance(mPitch.getCurrentMoveSuccessChance());
                    } else {
                        mHud.hideMovement();
                    }

                    if (steps > 0) {
                        showConfirmationSigns();
                    } else {
                        hideConfirmationSigns();
                    }
                }
                mHasTouch = false;
                break;
            case TouchEvent.ACTION_MOVE:
                break;
            }
            return true;
        }

        return false;
    }

    @Override
    public void onScrollStarted(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX,
            final float pDistanceY) {
        final float zoomFactor = this.mZoomCamera.getZoomFactor();
        this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
    }

    @Override
    public void onScroll(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX,
            final float pDistanceY) {
        final float zoomFactor = this.mZoomCamera.getZoomFactor();
        this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);

        mTouchDistance += pDistanceX * pDistanceX + pDistanceY * pDistanceY;
        if (mTouchDistance > 250) mHasTouch = false;
    }

    @Override
    public void onScrollFinished(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX,
            final float pDistanceY) {
        final float zoomFactor = this.mZoomCamera.getZoomFactor();
        this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
    }

    @Override
    public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
        this.mPinchZoomStartedCameraZoomFactor = this.mZoomCamera.getZoomFactor();
    }

    @Override
    public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent,
            final float pZoomFactor) {
        this.mZoomCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
    }

    @Override
    public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent,
            final float pZoomFactor) {
        this.mZoomCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
    }

    @Override
    protected void onManagedUpdate(float pSecondsElapsed) {
        mPitch.update(pSecondsElapsed);

        super.onManagedUpdate(pSecondsElapsed);
    }

    /** whether the confirmation stuff is visible, or not */
    private boolean mConfirmationVisible;

    private void showConfirmationSigns() {
        if (mConfirmationVisible) return;

        mConfirmationVisible = true;

        mHud.showConfirmationSigns(this);
    }

    private void hideConfirmationSigns() {
        mConfirmationVisible = false;
        mHud.hideConfirmationSigns();
    }

    @Override
    public void onConfirmationAccept() {
        mPitch.executePlannedMove(mHud);
        hideConfirmationSigns();
    }

    @Override
    public void onConfirmationDecline() {
        mPitch.cancelPlannedMove();
        mHud.hideMovement();
        hideConfirmationSigns();
    }

    @Override
    public void onEndturnSelected() {
        mPitch.switchTeams();
    }

    private void makeTitle(String text) {
        mHud.showTutorialMessage(GameResources.FRAME_INVALID, true, text, Color.RED);
    }

    private void makeHisText(String text) {
        mHud.showTutorialMessage(GameResources.FRAME_TUTORIAL_CHAR0, true, text, Color.CYAN);
    }

    private void makeHerText(String text) {
        mHud.showTutorialMessage(GameResources.FRAME_TUTORIAL_CHAR1, false, text, Color.YELLOW);
    }

    /** ~ the tutorial skeleton! <3 */
    private PlayerPiece mSkeleton;

    /** create the skeleton if necessary */
    private void createSkeleton() {
        if (mSkeleton == null) {
            mSkeleton = mPitch.createPiece(GameResources.FRAME_SKELETON, 3, 2, 5, 7, 3);
        } else {
            mPitch.removePieceFromField(mSkeleton, false);
        }
        mSkeleton.resetTeamTurn();
    }

    private void tutorial1Messages() {
        switch (mTutorialStep) {
        case 1:
            makeTitle("TUTORIAL 1: Scoring!");
            mPitch.setTutorialRules(ThePitch.TUTORIAL_MOVEMENT);
            break;
        case 2:
            makeHisText("Welcome to the Pitch!\nThey told us you want to be a coach.");
            break;
        case 3:
            createSkeleton();
            mPitch.placePiece(mSkeleton, 16, 4);
            makeHerText("We got you a practice player.\nIt’s really good at following orders.");
            break;
        case 4:
            // spawn skeleton
            makeHisText("To score, he needs the ball.\nI'll give it to him.\nYou order him to the endzone.");
            break;
        case 5:
            mSkeleton.setHasBall(true);
            break;
        case 6:
            makeHerText("You're on the right track!\nThose highlights represent a plan.\nOnce sure, hit the green checkmark.");
            break;
        case 7:
            // do nothing
            break;
        case 8:
            // skeleton out of move, didn't reach finish
            makeHisText("He needs to reach the endzone.\nAnd he may only move so many squares.");
            break;
        case 9:
            makeHerText("We better start over.\nMove it diagonally, if you like.");
            mPitch.removePieceFromField(mSkeleton, false);
            break;
        case 10:
            mSkeleton.resetTeamTurn();
            mPitch.placePiece(mSkeleton, 16, 4);
            break;
        case 11:
            makeHisText("Do look for the endzone.\nAnd order the skeleton there.");
            mPitch.removePieceFromField(mSkeleton, false);
            break;
        case 12:
            mSkeleton.resetTeamTurn();
            mPitch.placePiece(mSkeleton, 16, 4);
            break;
        case 13:
            makeHerText("You want to annoy us. We get it.\nSkeleton. Endzone.\nStart over.");
            mPitch.removePieceFromField(mSkeleton, false);
            break;
        case 14:
            mSkeleton.resetTeamTurn();
            mPitch.placePiece(mSkeleton, 16, 4);
            break;
        case 15:
            makeHisText("Concentrate.\nE-N-D-Z-O-N-E.");
            mPitch.removePieceFromField(mSkeleton, false);
            break;
        case 16:
            mSkeleton.resetTeamTurn();
            mPitch.placePiece(mSkeleton, 16, 4);
            mTutorialStep = 12;
            break;
        case 51:
            makeHisText("Well done!\nYour team just scored!");
            break;
        case 52:
            makeHerText("Obviously, when a match is over\nthe team with the higher score wins.");
            mTutorialStep = 100;
            break;
        }
    }

    private void tutorial2Messages() {
        switch (mTutorialStep) {
        case 101:
            makeTitle("TUTORIAL 2: Turns!");
            mPitch.setTutorialRules(ThePitch.TUTORIAL_MOVEMENT);
            createSkeleton();
            break;
        case 102:
            makeHisText("Each player can move only a limited\nnumber of squares each turn.");
            break;
        case 103:
            makeHerText("Each turn.\nA turn means that your whole team gets to act.");
            break;
        case 104:
            makeHisText("Then the opposing team gets to act.\nAnd then it's back to you.");
            mPitch.placePiece(mSkeleton, 16, 4);
            mSkeleton.setHasBall(true);
            break;
        case 105:
            makeHerText("No opposing team here.\nEnd your turn and it's your turn again.");
            mPitch.setTutorialRules(ThePitch.TUTORIAL_TURNS);
            break;
        case 106:
            makeHisText("The skeleton has MA 5.\nMA = Movement Allowance.\nMeans he can move 5 squares.");
            break;
        case 107:
            makeHerText("But by using multiple turns,\nit can reach the endzone far left.");
            break;
        case 108:
            // skeleton can move about freely
            break;
        case 126:
            makeHisText("I really hope it's not too difficult...\n...for you.\nIt will not get easier.");
            break;
        case 127:
            makeHerText("You know. Left.\nIt's really left.\nTrust us.");
            break;
        case 151:
            makeHisText("Skeletons are damn slow!\nHe did arrive, though.");
            break;
        case 152:
            makeHerText("Don't be afraid to bring the ball\ninto your endzone.\nAs long as your team holds it.");
            mTutorialStep = 200;
            break;
        }

    }

    private void tutorial3Messages() {
        switch (mTutorialStep) {
        case 201:
            makeTitle("TUTORIAL 3: Going For It!\nComing soon.");
            break;
        }
    }

    @Override
    public void onTutorialMessageContinue() {
        ++mTutorialStep;
        if (mTutorialStep < 100) tutorial1Messages();
        else if (mTutorialStep < 200) tutorial2Messages();
        else if (mTutorialStep < 300) tutorial3Messages();
    }

    @Override
    public void onTutorialPlanBegins() {
        if (mTutorialStep == 5) {
            onTutorialMessageContinue();
        }
    }

    @Override
    public void onTutorialPlanExecuted() {
        if (mTutorialStep < 50) {
            if (mSkeleton.getRemainingMove() <= 0) {
                if (mSkeleton.getPositionX() == 21) {
                    mTutorialStep = 50;
                }
                onTutorialMessageContinue();
            }
        }
        if (100 < mTutorialStep && mTutorialStep < 150) {
            if (mSkeleton.getPositionX() == 0) {
                mTutorialStep = 150;
                onTutorialMessageContinue();
            } else if (mTutorialStep < 125) {
                ++mTutorialStep;
            } else if (mTutorialStep == 125) {
                onTutorialMessageContinue();
            }
        }
    }

    @Override
    public void onTutorialEndTurn() {
        if (100 < mTutorialStep && mTutorialStep < 150) {
            mSkeleton.resetTeamTurn();
        }
    }
}

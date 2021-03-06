package com.example.bigbangbowl.game;

import java.util.ArrayList;
import java.util.Vector;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.Entity;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

import com.example.bigbangbowl.BBBActivity;
import com.example.bigbangbowl.GameResources;
import com.example.bigbangbowl.TouchSprite;
import com.example.bigbangbowl.TouchSprite.ITouchSpriteCallback;
import com.example.bigbangbowl.game.dice.IDiceLogReceiver;

public class BowlHud extends HUD implements ITouchSpriteCallback, IDiceLogReceiver {
    /** fake names for the teams (for now) */
    static final String[] TEAM_NAMES = { "Capes", "Beasts" };
    static final Color[] TEAM_COLORS = { new Color(119 / 256.f, 33 / 256.f, 116 / 256.f),
            new Color(121 / 256.f, 60 / 256.f, 46 / 256.f) };
    /** duration of the turnover sign being visible */
    static final float TURNOVER_WARNING_DURATION = 3.0f;

    public static interface IConfirmationCallback {
        /** callback when the user ACCEPTs their choice */
        public void onConfirmationAccept();

        /** callback when the user DECLINEs their choice */
        public void onConfirmationDecline();
    }

    /** various callbacks relevant during tutorial! */
    public static interface ITutorialCallback {
        /** tutorial message was just hidden */
        public void onTutorialMessageContinue();

        /** tutorial - path was just begun */
        public void onTutorialPlanBegins();

        /** tutorial - a plan has been fully executed and displayed */
        public void onTutorialPlanExecuted();
        
        /** tutorial - player chose to end turn */
        public void onTutorialEndTurn();
    }

    public static interface IEndturnCallback {
        /** end turn button was tapped */
        public void onEndturnSelected();
    }

    private static class UpdatedEntityContainer {
        /** how long the fade in/out takes */
        private static final float FADE_IN_TIMER = .5f;
        private static final float FADE_OUT_TIMER = 2.f;
        /** how long it's visible */
        private static final float VISIBLE_TIMER = 7.5f;
        /** the entity to fade in and then out */
        private Entity mEntity;
        /** current timer */
        private float mTimer;
        /** which step - 1: fade-in, 2: normal, 3: fade-out */
        private int mStep;

        /** move the contained entity one line upwards */
        public void move(float dy) {
            if (mEntity == null) return;
            float y = mEntity.getY();
            y += dy;
            mEntity.setY(y);
        }

        /** configure this with an entity */
        public void setEntity(Entity entity) {
            mStep = 1;
            mTimer = FADE_IN_TIMER;
            mEntity = entity;
            mEntity.setAlpha(0);
        }

        /** tickwise update */
        public void update(float dt) {
            if (mStep == 0) return;
            mTimer -= dt;
            switch (mStep) {
            case 1:
                if (mTimer > 0) {
                    mEntity.setAlpha(1 - mTimer / FADE_IN_TIMER);
                } else {
                    mStep = 2;
                    mTimer += VISIBLE_TIMER;
                    mEntity.setAlpha(1);
                }
                break;
            case 2:
                if (mTimer <= 0) {
                    mStep = 3;
                    mTimer += FADE_OUT_TIMER;
                }
                break;
            case 3:
                if (mTimer > 0) {
                    mEntity.setAlpha(mTimer / FADE_OUT_TIMER);
                } else {
                    mStep = 0;
                    mEntity.detachSelf();
                    mEntity.dispose();
                    mEntity = null;
                }
                break;
            }
        }

        public boolean isDone() {
            return mStep == 0;
        }

    }

    private TouchSprite mSignAccept;
    private TouchSprite mSignDecline;
    private TouchSprite mSignContinue;
    private TouchSprite mButtonEndturn;
    private TouchSprite mButtonConfirm;
    private Sprite mWarningTurnover;
    private Entity mTutorialChat;
    /**
     * last picture shown - invalid when not immediately displaying tutorial
     * again
     */
    private int mLastTutorialCharFrame;

    /** stored callback - for accept/decline */
    private IConfirmationCallback mConfirmationCallback;
    /** stored callback - for end turn */
    private IEndturnCallback mEndturnCallback;
    /** stored observers - for tutorial stuff */
    private ArrayList<BowlHud.ITutorialCallback> mTutorialCallback = new ArrayList<BowlHud.ITutorialCallback>();
    /** visible dice log stuff */
    private Vector<UpdatedEntityContainer> mLogDisplay;
    /** current displayed team name */
    private Text mTeamName;
    /** how long the turnover thingy is still visible */
    private float mTurnoverTimer;
    
    /** width and height */
    private float mWidth, mHeight;

    public void prepareResources(BBBActivity activity) {
        float scale = 1.0f;
        float posy = BBBActivity.CAMERA_HEIGHT - 64 - 64 * scale;
        float posx = activity.getCurrentWidth() - 64 - 64 * scale;
        
        mWidth = activity.getCurrentWidth();
        mHeight = BBBActivity.CAMERA_HEIGHT;
        
        GameResources res = GameResources.getInstance();
        mSignAccept = new TouchSprite(posx, posy, res.getTextureRegion(GameResources.FRAME_SIGN_ACCEPT), res.getVbo());
        mSignDecline = new TouchSprite(-64 + 64 * scale, posy, res.getTextureRegion(GameResources.FRAME_SIGN_DECLINE),
                res.getVbo());
        mSignAccept.setScale(scale);
        mSignDecline.setScale(scale);
        mSignAccept.setTouchCallback(this);
        mSignDecline.setTouchCallback(this);

        this.attachChild(mSignAccept);
        this.attachChild(mSignDecline);

        TextureRegion endturnframe = res.getTextureRegion(GameResources.FRAME_BUTTON_ENDTURN);
        scale = .6f;
        posx = .5f * (activity.getCurrentWidth() - endturnframe.getWidth());
        posy = (-.5f + .5f * scale) * endturnframe.getHeight();
        mButtonEndturn = new TouchSprite(posx, posy, endturnframe, res.getVbo());
        posy = .5f * (BBBActivity.CAMERA_HEIGHT - endturnframe.getHeight());
        mButtonConfirm = new TouchSprite(posx, posy, res.getTextureRegion(GameResources.FRAME_BUTTON_CONFIRM),
                res.getVbo());
        mButtonEndturn.setScale(scale);
        mButtonConfirm.setScale(scale);

        mButtonEndturn.setTouchCallback(this);
        mButtonConfirm.setTouchCallback(this);
        mButtonConfirm.setVisible(false);
        this.attachChild(mButtonEndturn);
        this.attachChild(mButtonConfirm);

        this.registerTouchArea(mButtonEndturn);
        this.registerTouchArea(mButtonConfirm);
        this.registerTouchArea(mSignAccept);
        this.registerTouchArea(mSignDecline);

        TextureRegion warningframe = res.getTextureRegion(GameResources.FRAME_WARNING_TURNOVER);
        posx = .5f * (activity.getCurrentWidth() - warningframe.getWidth());
        posy = .5f * (BBBActivity.CAMERA_HEIGHT - warningframe.getHeight());
        mWarningTurnover = new Sprite(posx, posy, warningframe, res.getVbo());
        mWarningTurnover.setVisible(false);
        this.attachChild(mWarningTurnover);

        mLogDisplay = new Vector<BowlHud.UpdatedEntityContainer>(10);

        hideConfirmationSigns();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    /** set a callback for the end turn */
    public void setEndturnCallback(IEndturnCallback callback) {
        this.mEndturnCallback = callback;
    }

    private Text mMovementDisplay;
    private Text mChanceDisplay;

    /** show the movement display */
    public void setMovement(int remaining, int total) {
        float r = .5f, g = .5f, b = 1;
        StringBuffer text = new StringBuffer();
        if (remaining >= 0) {
            text.append("mv");
            text.append(remaining);
            if (remaining < 10) text.append(" ");
        } else {
            r = 1;
            g = .5f;
            b = 0;
            text.append("gfi");
            text.append(-remaining);
        }
        // text.append("/");
        // text.append(total);

        if (mMovementDisplay == null) {
            GameResources res = GameResources.getInstance();
            mMovementDisplay = new Text(0, 0, res.getBitmapFont(), text, new TextOptions(HorizontalAlign.LEFT),
                    res.getVbo());
            this.attachChild(mMovementDisplay);
        } else {
            mMovementDisplay.setText(text);
        }
        mMovementDisplay.setColor(r, g, b);
        mMovementDisplay.setVisible(true);
    }

    /** hide the movement display */
    public void hideMovement() {
        if (mMovementDisplay != null) {
            mMovementDisplay.setVisible(false);
        }
        if (mChanceDisplay != null) {
            mChanceDisplay.setVisible(false);
        }
    }

    public void setCurrentSuccessChance(float chance) {
        StringBuffer text = new StringBuffer();
        text.append(Math.round(chance * 1000) / 10.0f);
        text.append("%");
        if (mChanceDisplay == null) {
            GameResources res = GameResources.getInstance();
            mChanceDisplay = new Text(0, 30, res.getBitmapFont(), text, new TextOptions(HorizontalAlign.LEFT),
                    res.getVbo());
            this.attachChild(mChanceDisplay);
        } else {
            mChanceDisplay.setText(text);
        }

        if (chance < .2f) {
            mChanceDisplay.setColor(1, 0, 0);
        } else if (chance < .22f) {
            float g = .5f * (chance - .2f) / .02f;
            mChanceDisplay.setColor(1, g, 0);
        } else if (chance < .4f) {
            mChanceDisplay.setColor(1, .5f, 0);
        } else if (chance < .5f) {
            float factor = (chance - .4f) / .1f;
            mChanceDisplay.setColor(1, .5f + .5f * factor, factor);
        } else {
            mChanceDisplay.setColor(1, 1, 1);
        }

        mChanceDisplay.setVisible(true);
    }

    public void showConfirmationSigns(IConfirmationCallback callback) {
        if (callback == null) return;
        mConfirmationCallback = callback;
        mSignAccept.setVisible(mSignContinue == null);
        mSignDecline.setVisible(mSignContinue == null);
    }

    public void hideConfirmationSigns() {
        mConfirmationCallback = null;
        mSignAccept.setVisible(false);
        mSignDecline.setVisible(false);
    }
    
    /** allows the endturn button to be hidden */
    public void setEndturnVisibility(boolean visible) {
        mButtonEndturn.setVisible(visible);
    }

    @Override
    public boolean onSpriteTouched(TouchSprite sprite, TouchEvent touchEvent, float spriteLocalX, float spriteLocalY) {
        if (sprite == mSignContinue && touchEvent.isActionUp()) {
            if (mTutorialChat != null) {
                this.unregisterTouchArea(mSignContinue);
                mTutorialChat.detachSelf();
                mTutorialChat = null;
                mSignContinue = null;

                for (int i = mTutorialCallback.size() - 1; i >= 0; --i) {
                    mTutorialCallback.get(i).onTutorialMessageContinue();
                }

                if (mTutorialChat == null) {
                    mLastTutorialCharFrame = GameResources.FRAME_INVALID;

                    if (mConfirmationCallback != null) {
                        mSignAccept.setVisible(true);
                        mSignDecline.setVisible(true);
                    }
                }
            }
        }

        if (sprite == mButtonEndturn && touchEvent.isActionUp()) {
            if (mButtonConfirm.isVisible()) {
                mButtonConfirm.setVisible(false);
            } else {
                mButtonConfirm.setVisible(true);
            }
            return true;
        } else if (sprite == mButtonConfirm && touchEvent.isActionUp()) {
            if (mButtonConfirm.isVisible()) {
                if (mEndturnCallback != null) {
                    mEndturnCallback.onEndturnSelected();
                }
                mButtonConfirm.setVisible(false);
                return true;
            }
            return false;
        }
        if (mConfirmationCallback != null && touchEvent.isActionUp()) {
            if (sprite == mSignAccept) {
                mConfirmationCallback.onConfirmationAccept();
            } else if (sprite == mSignDecline) {
                mConfirmationCallback.onConfirmationDecline();
            }
            return true;
        }
        return false;
    }

    /** display a log line */
    @Override
    public void showDiceLogLine(int logtype, CharSequence text) {
        GameResources res = GameResources.getInstance();
        Text line = new Text(20, BBBActivity.CAMERA_HEIGHT - 50, res.getFont(), text, res.getVbo());
        switch (logtype) {
        case IDiceLogReceiver.LOG_FAILURE:
            line.setColor(Color.RED);
            break;
        case IDiceLogReceiver.LOG_SUCCESS:
            line.setColor(Color.YELLOW);
            break;
        case IDiceLogReceiver.LOG_NEUTRAL:
            break;
        }
        this.attachChild(line);

        for (int i = 0, n = mLogDisplay.size(); i < n; ++i) {
            mLogDisplay.get(i).move(-35);
        }

        UpdatedEntityContainer container = new UpdatedEntityContainer();
        container.setEntity(line);
        mLogDisplay.add(container);
    }

    @Override
    protected void onManagedUpdate(float pSecondsElapsed) {
        for (int i = 0, n = mLogDisplay.size(); i < n; ++i) {
            mLogDisplay.get(i).update(pSecondsElapsed);
            if (mLogDisplay.get(i).isDone()) {
                mLogDisplay.remove(i);
                --i;
                --n;
            }
        }

        if (mTurnoverTimer > 0) {
            mTurnoverTimer -= pSecondsElapsed;
            if (mTurnoverTimer <= 0) {
                mWarningTurnover.setVisible(false);
            }
        }

        super.onManagedUpdate(pSecondsElapsed);
    }

    /** set to display the team name for this team */
    public void setCurrentTeam(int team) {
        if (mTeamName != null) {
            mTeamName.detachSelf();
            mTeamName = null;
        }

        if (0 > team || team >= TEAM_NAMES.length) return;

        GameResources res = GameResources.getInstance();
        mTeamName = new Text(0, 0, res.getFont(), TEAM_NAMES[team], res.getVbo());
        float width = mTeamName.getWidth();
        mTeamName.setX(getCamera().getWidth() - width);
        mTeamName.setColor(TEAM_COLORS[team]);
        this.attachChild(mTeamName);
    }

    /** show the turnover warning sign for a short time */
    public void showWarningTurnover() {
        mTurnoverTimer = TURNOVER_WARNING_DURATION;
        mWarningTurnover.setVisible(true);
    }

    /** display a tutorial text - with a character */
    public void showTutorialMessage(int charFrame, boolean left, CharSequence line, Color color) {
        Entity layer = new Entity();
        GameResources res = GameResources.getInstance();
        float scale = .75f;
        float posY = mHeight - 256 - 256 * scale;
        if (charFrame != GameResources.FRAME_INVALID) {
            float posX = 0;
            if (!left) posX = mWidth - 128 - 128 * scale;
            Sprite charsprite = res.createSprite(posX, posY, charFrame);
            layer.attachChild(charsprite);
            charsprite.setScale(scale);

            if (mLastTutorialCharFrame != charFrame) {
                mLastTutorialCharFrame = charFrame;
                float offset;
                if (left) offset = -256;
                else offset = 256;

                charsprite.setPosition(posX + offset, posY);
                MoveModifier move = new MoveModifier(.3f, posX + offset, posX, posY, posY);
                charsprite.registerEntityModifier(move);
            }
        }

        Sprite boxSprite = res.createSprite(.5f * mWidth - 128, posY, GameResources.FRAME_TUTORIAL_TEXTBOX);
        float scaleX = mWidth / boxSprite.getWidth();
        boxSprite.setScale(scaleX, scale);
        layer.attachChild(boxSprite);

        Text text = new Text(4, mHeight - 128, res.getFont(), line, res.getVbo());
        text.setColor(color);
        layer.attachChild(text);

        float posX = mWidth - 128;
        posY = mHeight - 128;
        TouchSprite continueSign = new TouchSprite(posX, posY, res.getTextureRegion(GameResources.FRAME_SIGN_CONTINUE),
                res.getVbo());
        continueSign.setTouchCallback(this);
        layer.attachChild(continueSign);

        mTutorialChat = layer;
        mSignContinue = continueSign;

        this.attachChild(mTutorialChat);
        this.registerTouchArea(mSignContinue);

        mSignAccept.setVisible(false);
        mSignDecline.setVisible(false);
    }

    /** register a new tutorial observer */
    public void registerTutorialObserver(BowlHud.ITutorialCallback observer) {
        for (int i = mTutorialCallback.size() - 1; i >= 0; --i) {
            if (mTutorialCallback.get(i) == observer) return;
        }
        mTutorialCallback.add(observer);
    }

    /** unregister a tutorial observer */
    public void unregisterTutorialObserver(BowlHud.ITutorialCallback observer) {
        for (int i = mTutorialCallback.size(); i >= 0; --i) {
            if (mTutorialCallback.get(i) == observer) {
                mTutorialCallback.remove(i);
                return;
            }
        }
    }

    /** trigger a plan begin tutorial observer call - first step planned */
    public void triggerTutorialPlanBegins() {
        for (int i = mTutorialCallback.size() - 1; i >= 0; --i) {
            mTutorialCallback.get(i).onTutorialPlanBegins();
        }
    }

    /** trigger a plan finished execution tutorial call */
    public void triggerTutorialPlanExecuted() {
        for (int i = mTutorialCallback.size() - 1; i >= 0; --i) {
            mTutorialCallback.get(i).onTutorialPlanExecuted();
        }
    }
    
    /** trigger player chose end turn */
    public void triggerTutorialEndTurn() {
        for (int i = mTutorialCallback.size() - 1; i >= 0; --i) {
            mTutorialCallback.get(i).onTutorialEndTurn();
        }
    }

}

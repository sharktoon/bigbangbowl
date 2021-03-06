package com.example.bigbangbowl.game;

import org.andengine.entity.Entity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;

import com.example.bigbangbowl.GameResources;

/**
 * representation of a player piece - like a human blitzer
 * 
 * @author Daniel
 * 
 */
public class PlayerPiece {
    /** unknown state */
    public static final int STATE_UNKNOW = 0;
    /** on the pitch - standing */
    public static final int STATE_STANDING = 1;
    /** on the pitch - and has the ball */
    public static final int STATE_HAS_BALL = 2;
    /** lying on the pitch */
    public static final int STATE_DOWN = 3;
    /** lying on the pitch for two rounds */
    public static final int STATE_STUNNED = 4;
    /** not on the pitch - but fit */
    public static final int STATE_OUT = 5;
    /** knocked out - needs to roll to be allowed back in */
    public static final int STATE_KNOCKEDOUT = 6;
    /** injured - won't come back this game */
    public static final int STATE_INJURED = 7;
    /** sent off - blocked from playing the rest of this game */
    public static final int STATE_BANNED = 8;

    /** agility value - handling the ball, dodging */
    private int mAgility;
    /** strength - to block */
    private int mStrength;
    /** movement - reach */
    private int mMovementValue;
    /** armor - toughness */
    private int mArmorValue;

    /** the team this one belongs */
    private int mTeam;

    /** current players position on the pitch */
    private int mPositionX, mPositionY;

    /** the state of the player - like injured, or on the pitch */
    private int mState;

    /** the graphical representation of this player */
    private Entity mEntity;
    /** the status display letter */
    private Text mStatusDisplay;
    /** the display of a ball carried by this player */
    private Sprite mBall;

    public PlayerPiece(int ST, int AG, int MV, int AV) {
        this.mAgility = AG;
        this.mStrength = ST;
        this.mMovementValue = MV;
        this.mArmorValue = AV;

        mState = STATE_UNKNOW;
    }

    /** agility of this player */
    public int getAG() {
        return mAgility;
    }

    /** strength */
    public int getST() {
        return mStrength;
    }

    /** allowed movement */
    public int getMV() {
        return mMovementValue;
    }

    /** armor */
    public int getAV() {
        return mArmorValue;
    }

    /** set the position of this player on the pitch */
    public void setPosition(int x, int y) {
        mPositionX = x;
        mPositionY = y;
    }

    /** access position of this player on the pitch */
    public int getPositionX() {
        return mPositionX;
    }

    /** access position of this player on the pitch */
    public int getPositionY() {
        return mPositionY;
    }

    /** set the state of this player - when something happened to them */
    public void setState(int state) {
        this.mState = state;

        if (mStatusDisplay != null) {
            char letter = ' ';
            switch (mState) {
            case STATE_DOWN:
                letter = 'D';
                break;
            case STATE_STUNNED:
                letter = 'S';
                break;
            case STATE_HAS_BALL:
                letter = 'B';
                break;
            }

            StringBuffer buf = new StringBuffer();
            buf.append(letter);
            mStatusDisplay.setText(buf);
        }
    }

    /** retrieve current state of the player - like on the pitch or stuff */
    public int getState() {
        return this.mState;
    }

    /** access the graphical representation */
    public Entity getEntity() {
        return mEntity;
    }

    /** set the graphical representation of this piece */
    public void setEntity(Entity entity, Text letter) {
        mEntity = entity;
        mStatusDisplay = letter;
    }

    /** set to which team this piece belongs */
    public void setTeam(int number) {
        this.mTeam = number;
    }

    /** check to which team this player belongs */
    public int getTeam() {
        return mTeam;
    }

    /**
     * check if this piece has tacklezones atm - potentially change return to
     * int/enum
     */
    public boolean getTackleZone() {
        return mState == STATE_STANDING;
    }

    /** current turn already acted */
    private boolean mTurnDone;
    /** current turn remaining move [-2;MV] */
    private int mRemainingMove;

    /** reset values when teams turn begins */
    public void resetTeamTurn() {
        mRemainingMove = mMovementValue;
        mTurnDone = false;
        if (mState == STATE_STUNNED) {
            setState(STATE_DOWN);
            mTurnDone = true;
        }
        // if (mState == STATE_DOWN) {
        // mRemainingMove -= 3;
        // }
    }

    /** retrieve how much movement is left this turn */
    public int getRemainingMove() {
        return mRemainingMove;
    }

    /** uses up movement */
    public void useMovement(int amount) {
        mRemainingMove -= amount;
    }

    /** stands up the player */
    public void standUp() {
        if (mState == STATE_DOWN) {
            setState(STATE_STANDING);
            if (mMovementValue > 3) useMovement(3);
            else useMovement(mRemainingMove);
        }
    }

    public void beginTurn() {
    }

    /** ends this players turn */
    public void endTurn() {
        mTurnDone = true;
    }

    /** check if this piece can act... still */
    public boolean canAct() {
        if (mTurnDone) return false;
        if (mState == STATE_STANDING || mState == STATE_HAS_BALL) return true;
        if (mState == STATE_DOWN) return true;
        return false;
    }

    /** set the player flag carry ball */
    public void setHasBall(boolean value) {
        if (value) {
            mState = STATE_HAS_BALL;
            if (mBall == null) {
                mBall = GameResources.getInstance().createSprite(-ThePitch.PIECE_OFFSET_X, -ThePitch.PIECE_OFFSET_Y,
                        GameResources.FRAME_BALL_SMALL);
                mEntity.attachChild(mBall);
            }
        } else {
            if (mState == STATE_HAS_BALL) {
                mState = STATE_STANDING;
            }
            if (mBall != null) {
                mBall.detachSelf();
                mBall = null;
            }
        }
    }

    /** check if the player carries the ball */
    public boolean hasBall() {
        return mState == STATE_HAS_BALL;
    }

}

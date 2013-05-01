package com.example.bigbangbowl;

import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class TouchSprite extends Sprite {
    /** no touch */
    private static final int TOUCH_NONE = 0;
    /** touch active - down in area, still fine */
    private static final int TOUCH_ACTIVE = 1;
    /** touch lost - down in area, but moved out */
    private static final int TOUCH_LOST = 2;

    /** locally tracked touch state */
    private int mTouchState = TOUCH_NONE;
    /** tracked touch id */
    private int mTouchId;

    public static interface ITouchSpriteCallback {
        public boolean onSpriteTouched(TouchSprite sprite, TouchEvent touchEvent, float spriteLocalX, float spriteLocalY);
    }

    /** the single callback */
    ITouchSpriteCallback mCallback;

    public TouchSprite(final float pX, final float pY, final ITextureRegion pTextureRegion,
            final VertexBufferObjectManager pVertexBufferObjectManager) {
        super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
    }

    /** sets the only callback for this touch sprite */
    public void setTouchCallback(ITouchSpriteCallback callback) {
        mCallback = callback;
    }

    @Override
    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
        if(!isVisible()) return false;
        
        if (mTouchState == TOUCH_NONE) {
            if (pSceneTouchEvent.isActionDown()) {
                mTouchId = pSceneTouchEvent.getPointerID();
                mTouchState = TOUCH_ACTIVE;
                return true;
            } else {
                return false;
            }
        }

        if (mTouchId != pSceneTouchEvent.getPointerID()) return false;
        if (mTouchState != TOUCH_ACTIVE) return false;

        if (pSceneTouchEvent.isActionOutside()) {
            mTouchState = TOUCH_LOST;
            return false;
        }

        if (pSceneTouchEvent.isActionUp() && mTouchState == TOUCH_ACTIVE) {
            if (mCallback != null) {
                mCallback.onSpriteTouched(this, pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
            mTouchState = TOUCH_NONE;
        }
        return true;
    }

}

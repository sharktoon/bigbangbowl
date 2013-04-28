package com.example.bigbangbowl;

import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class TouchSprite extends Sprite {
    
    public static interface ITouchSpriteCallback {
        public boolean onSpriteTouched(TouchSprite sprite, TouchEvent touchEvent, float spriteLocalX, float spriteLocalY);
    }
    
    /** the single callback */
    ITouchSpriteCallback mCallback;
    
    public TouchSprite(final float pX, final float pY, final ITextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager) {
        super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
    }
    
    /** sets the only callback for this touch sprite */
    public void setTouchCallback(ITouchSpriteCallback callback) {
        mCallback = callback;
    }

    @Override
    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
        if(mCallback != null) {
            return mCallback.onSpriteTouched(this, pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
        }
        return false;
    }
    
    
}

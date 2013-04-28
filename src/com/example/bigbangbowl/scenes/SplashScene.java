package com.example.bigbangbowl.scenes;

import java.io.IOException;
import java.io.InputStream;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.util.adt.io.in.IInputStreamOpener;

import com.example.bigbangbowl.BBBActivity;

public class SplashScene extends Scene {

    /** texture atlas */
    private ITexture mSplashTexture;
    /** special texture region in atlas */
    private ITextureRegion mSplashRegion;

    /** the splash sprite */
    private Sprite mSplash;

    /** creates a splash scene and loads required stuff */
    public SplashScene(final BBBActivity activity) {
        super();

        try {
            mSplashTexture = new BitmapTexture(activity.getTextureManager(), new IInputStreamOpener() {
                @Override
                public InputStream open() throws IOException {
                    return activity.getAssets().open("gfx/splash.jpg");
                }
            });
            mSplashTexture.load();
            mSplashRegion = TextureRegionFactory.extractFromTexture(mSplashTexture);
        } catch (IOException ioe) {
        }

        mSplash = new Sprite(0, 0, mSplashRegion, activity.getVertexBufferObjectManager());
        // {
        // @Override
        // protected void preDraw(GLState pGLState, Camera pCamera) {
        // super.preDraw(pGLState, pCamera);
        // pGLState.enableDither();
        // }
        // };
        float currentWidth = activity.getCurrentWidth();
        float scaleY = currentWidth / BBBActivity.CAMERA_WIDTH;
        mSplash.setScale(1, scaleY);
        mSplash.setPosition(0, 0);
        attachChild(mSplash);
    }

    /** release all claimed resources */
    public void dispose() {
        mSplash.detachSelf();
        mSplash.dispose();
        mSplashTexture.unload();
        mSplashRegion = null;

        super.dispose();
    }

}

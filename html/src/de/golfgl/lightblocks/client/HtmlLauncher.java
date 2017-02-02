package de.golfgl.lightblocks.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

import de.golfgl.lightblocks.LightBlocksGame;

public class HtmlLauncher extends GwtApplication {

    @Override
    public GwtApplicationConfiguration getConfig() {
        return new GwtApplicationConfiguration(LightBlocksGame.nativeGameWidth, LightBlocksGame.nativeGameHeight);
    }

    @Override
    public ApplicationListener createApplicationListener() {
        return new LightBlocksGame();
    }
}
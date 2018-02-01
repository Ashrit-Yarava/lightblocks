package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 22.01.2018.
 */

public class ScaledLabel extends Label {
    public ScaledLabel(CharSequence text, Skin skin, String labelStyle) {
        this(text, skin, labelStyle, LightBlocksGame.LABEL_SCALING);
    }

    public ScaledLabel(CharSequence text, Skin skin, String labelStyle, float scaling) {
        super(text, skin, labelStyle);

        setFontScale(scaling);
        pack();
    }
}

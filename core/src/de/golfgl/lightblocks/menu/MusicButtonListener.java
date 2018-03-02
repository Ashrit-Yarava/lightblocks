package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 22.03.2017.
 */

public class MusicButtonListener extends ChangeListener {

    private final static int NO_SOUND = 0;
    private final static int NO_MUSIC = 1;
    private final static int SOUND_MUSIC = 2;

    private int state;
    private LightBlocksGame app;
    private boolean useLabel;

    public MusicButtonListener(LightBlocksGame app, boolean useLabel, IMusicButton actor) {
        this.app = app;
        this.useLabel = useLabel;
        this.state = (app.isPlayMusic() ? SOUND_MUSIC : app.isPlaySounds() ? NO_MUSIC : NO_SOUND);
        setIconAndLabelFromState(actor);
    }

    public void changed(ChangeEvent event, Actor actor) {
        state--;
        if (state < 0)
            state = SOUND_MUSIC;

        app.setPlayMusic(state == SOUND_MUSIC);
        app.setPlaySounds(state > NO_SOUND);
        setIconAndLabelFromState(((IMusicButton) actor));
    }

    protected void setIconAndLabelFromState(IMusicButton actor) {
        switch (state) {
            case SOUND_MUSIC:
                actor.setFaText(FontAwesome.SETTINGS_MUSIC);
                if (useLabel)
                    actor.setText(app.TEXTS.get("menuPlayMusic"));
                break;
            case NO_MUSIC:
                actor.setFaText(FontAwesome.SETTINGS_SPEAKER_ON);
                if (useLabel)
                    actor.setText(app.TEXTS.get("menuPlaySounds"));
                break;
            default:
                actor.setFaText(FontAwesome.SETTINGS_SPEAKER_OFF);
                if (useLabel)
                    actor.setText(app.TEXTS.get("menuNoSounds"));
        }
    }

    public interface IMusicButton {
        void setFaText(String text);

        void setText(String text);
    }
}

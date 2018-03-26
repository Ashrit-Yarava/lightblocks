package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.lightblocks.menu.ITouchActionButton;
import de.golfgl.lightblocks.screen.PlayScreenInput;

/**
 * Created by Benjamin Schulte on 14.01.2018.
 */

public class MyStage extends ControllerMenuStage {
    private static final float TOUCH_INTERVAL = 1.5f;
    private static boolean touchActionActivated = false;
    private float timeSinceTouch;

    public MyStage(Viewport viewport) {
        super(viewport);

        // Falls unter Android keine Touchscreen-Eingabe vorhanden, dann TouchAction sofort aktivieren
        if (Gdx.app.getType().equals(Application.ApplicationType.Android) &&
                !PlayScreenInput.isInputTypeAvailable(PlayScreenInput.KEY_TOUCHSCREEN))
            touchActionActivated = true;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        timeSinceTouch += delta;

        if (timeSinceTouch > TOUCH_INTERVAL) {
            timeSinceTouch = 0;

            touchActionActivated = touchActionActivated || Controllers.getControllers().size > 0;

            if (touchActionActivated && getFocusedActor() instanceof ITouchActionButton) {
                ITouchActionButton actor = (ITouchActionButton) getFocusedActor();

                actor.touchAction();
            }
        }
    }

    @Override
    protected void onFocusGained(Actor focussedActor, Actor oldFocussed) {
        super.onFocusGained(focussedActor, oldFocussed);

        if (touchActionActivated && focussedActor instanceof ITouchActionButton) {
            ((ITouchActionButton) focussedActor).touchAction();
            timeSinceTouch = 0;
        }
    }

    @Override
    public boolean keyDown(int keyCode) {
        boolean handled = super.keyDown(keyCode);

        if (handled && keyCode != Input.Keys.BACK &&
                keyCode != Input.Keys.VOLUME_DOWN && keyCode != Input.Keys.VOLUME_UP)
            touchActionActivated = true;

        return handled;
    }

    @Override
    public boolean isDefaultActionKeyCode(int keyCode) {
        return super.isDefaultActionKeyCode(keyCode) || keyCode == Input.Keys.SPACE || keyCode == Input.Keys.ALT_LEFT;
    }
}
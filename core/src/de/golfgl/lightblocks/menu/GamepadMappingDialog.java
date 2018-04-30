package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonWriter;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.gdx.controllers.mapping.ControllerMappings;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.GlowLabelButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.state.MyControllerMapping;

/**
 * Created by Benjamin Schulte on 05.11.2017.
 */

public class GamepadMappingDialog extends ControllerMenuDialog {
    private static final int NUM_STEPS = (5 + 1) * 2;
    private final LightBlocksGame app;
    private final ControllerMappings mappings;
    private final Controller controller;
    private final GlowLabelButton skipButton;
    private final Label instructionLabel;
    private final String instructionIntro;
    private int currentStep = 0;
    private float timeSinceLastRecord = 0;
    private int inputToRecord = -1;

    public GamepadMappingDialog(LightBlocksGame app, Controller controller) {
        super("", app.skin);

        this.app = app;
        this.mappings = app.controllerMappings;
        this.controller = controller;

        instructionIntro = app.TEXTS.get("configGamepadStepIntro") + "\n";
        instructionLabel = new ScaledLabel(instructionIntro, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        instructionLabel.setWrap(true);

        String name = controller.getName();
        if (name.length() > 25)
            name = name.substring(0, 23) + "...";

        getContentTable().pad(20, 20, 0, 20);
        getContentTable().add(new ScaledLabel(name, app.skin, LightBlocksGame.SKIN_FONT_TITLE, .75f)).colspan(2);
        getContentTable().row();
        getContentTable().add(instructionLabel).fill().minWidth(LightBlocksGame.nativeGameWidth * .7f)
                .minHeight(instructionLabel.getPrefHeight() * 3f).colspan(2);
        instructionLabel.setAlignment(Align.center);

        getButtonTable().pad(20);
        getButtonTable().defaults().expandX();

        skipButton = new GlowLabelButton(app.TEXTS.get("configGamepadSkip"), app.skin, GlowLabelButton
                .FONT_SCALE_SUBMENU, 1f);
        skipButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (inputToRecord >= 0) {
                    currentStep = currentStep + 2 - (currentStep % 2);
                    switchStep();
                } else
                    hide();
            }
        });

        getButtonTable().add(skipButton);
        addFocusableActor(skipButton);

        Button restartButton = new GlowLabelButton(app.TEXTS.get("configGamepadRestart"), app.skin, GlowLabelButton
                .FONT_SCALE_SUBMENU, 1f);
        restartButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                currentStep = 0;
                switchStep();
            }
        });

        getButtonTable().add(restartButton);
        addFocusableActor(restartButton);

        switchStep();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        timeSinceLastRecord += delta;

        if (timeSinceLastRecord > .25f && inputToRecord >= 0) {
            timeSinceLastRecord = 0;
            ControllerMappings.RecordResult recordResult = mappings.recordMapping(controller, inputToRecord);

            switch (recordResult) {
                case need_second_button:
                    currentStep++;
                    switchStep();
                    break;
                case recorded:
                    currentStep = currentStep + 2 - (currentStep % 2);
                    switchStep();
                    break;
                default:
                    //nix zu tun, wir warten ab
            }
        }
    }

    private void switchStep() {
        switch (currentStep) {
            case 0:
                mappings.resetMappings(controller);
                instructionLabel.setText(instructionIntro + app.TEXTS.get("configGamepadStep0"));
                inputToRecord = MyControllerMapping.AXIS_HORIZONTAL;
                break;
            case 1:
                instructionLabel.setText(instructionIntro + app.TEXTS.get("configGamepadStep1"));
                inputToRecord = MyControllerMapping.AXIS_HORIZONTAL;
                break;
            case 2:
                instructionLabel.setText(instructionIntro + app.TEXTS.get("configGamepadStep2"));
                inputToRecord = MyControllerMapping.AXIS_VERTICAL;
                break;
            case 3:
                instructionLabel.setText(instructionIntro + app.TEXTS.get("configGamepadStep3"));
                inputToRecord = MyControllerMapping.AXIS_VERTICAL;
                break;
            case 4:
            case 5:
                instructionLabel.setText(instructionIntro + app.TEXTS.get("configGamepadStep4"));
                inputToRecord = MyControllerMapping.BUTTON_ROTATE_CLOCKWISE;
                break;
            case 6:
            case 7:
                instructionLabel.setText(instructionIntro + app.TEXTS.get("configGamepadStep6"));
                inputToRecord = MyControllerMapping.BUTTON_ROTATE_COUNTERCLOCK;
                break;
            case 8:
            case 9:
                instructionLabel.setText(instructionIntro + app.TEXTS.get("configGamepadStep8"));
                inputToRecord = MyControllerMapping.BUTTON_START;
                break;
            case 10:
            case 11:
                instructionLabel.setText(instructionIntro + app.TEXTS.get("configGamepadStep10"));
                inputToRecord = MyControllerMapping.BUTTON_CANCEL;
                break;
            default:
                instructionLabel.setText(app.TEXTS.get("configGamepadDone"));
                app.localPrefs.saveControllerMappings(mappings.toJson().toJson(JsonWriter.OutputType.json));
                skipButton.setText("OK");

                inputToRecord = -1;
        }
    }
}

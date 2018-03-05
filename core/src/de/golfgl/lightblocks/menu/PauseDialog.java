package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.GlowLabelButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.PlayScreen;

/**
 * Created by Benjamin Schulte on 21.03.2017.
 */

public class PauseDialog extends Dialog {
    protected final Color EMPHASIZE_COLOR = new Color(1, .3f, .3f, 1);
    protected final Color NORMAL_COLOR = new Color(1, 1, 1, 1);
    private final Label titleLabel;
    private final Label textLabel;
    private final Label inputMsgLabel;
    private boolean emphasizeInputMsg;

    public PauseDialog(LightBlocksGame app, final PlayScreen playScreen) {
        super("", app.skin, LightBlocksGame.SKIN_WINDOW_OVERLAY);


        Table table = getContentTable();

        table.defaults();
        table.row();
        titleLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_TITLE, .8f);
        table.add(titleLabel).pad(20, 20, 10, 20);

        table.row();
        textLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_BIG, .75f);
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.center);
        table.add(textLabel).prefWidth
                (LightBlocksGame.nativeGameWidth * .75f).pad(10);

        table.row();
        inputMsgLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_BIG, .85f);
        inputMsgLabel.setWrap(true);
        inputMsgLabel.setAlignment(Align.center);
        table.add(inputMsgLabel).prefWidth
                (LightBlocksGame.nativeGameWidth * .75f).pad(10, 10, 30, 10);

        getButtonTable();
        getButtonTable().defaults().uniform().padBottom(20).minWidth(80).fill();
        button(new FaButton(FontAwesome.MISC_CROSS, app.skin),
                new Runnable() {
                    @Override
                    public void run() {
                        playScreen.goBackToMenu();
                    }
                });
        button(new GlowLabelButton("", "?", app.skin, GlowLabelButton.SMALL_SCALE_MENU),
                new Runnable() {
                    @Override
                    public void run() {
                        playScreen.showInputHelp();
                    }
                });
        FaButton musicButton = new FaButton("", app.skin);
        musicButton.addListener(new MusicButtonListener(app, false, musicButton));
        button(musicButton);
//        button(new TextButton(FontAwesome.BIG_FORWARD, app.skin, FontAwesome.SKIN_FONT_FA),
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        if (playScreen.isPaused())
//                            playScreen.switchPause(false);
//                    }
//                });

        // Modal wird ausgeschaltet, da sonst alle InputEvents weggeklaut werden
        setModal(false);
    }

    public boolean isEmphasizeInputMsg() {
        return emphasizeInputMsg;
    }

    public void setEmphasizeInputMsg(boolean emphasizeInputMsg) {
        if (this.emphasizeInputMsg != emphasizeInputMsg) {
            this.emphasizeInputMsg = emphasizeInputMsg;

            if (!emphasizeInputMsg) {
                inputMsgLabel.clearActions();
                inputMsgLabel.setColor(NORMAL_COLOR);
            } else
                inputMsgLabel.addAction(Actions.forever(Actions.sequence(Actions.color(EMPHASIZE_COLOR, 1.5f),
                        Actions.color(NORMAL_COLOR, 1.5f))));
        }
    }

    public Label getInputMsgLabel() {
        return inputMsgLabel;
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setText(String text) {
        textLabel.setText(text);
    }

    @Override
    public Dialog show(Stage stage) {
        show(stage, null);
        getColor().a = 1;
        setPosition((stage.getWidth() - getWidth()) / 2, (stage.getHeight() - getHeight()) / 2);
        return this;
    }

    @Override
    protected void result(Object object) {
        // PauseDialog nicht verstecken - das macht die Logik im Playscreen
        cancel();

        if (object instanceof Runnable)
            ((Runnable) object).run();
    }
}

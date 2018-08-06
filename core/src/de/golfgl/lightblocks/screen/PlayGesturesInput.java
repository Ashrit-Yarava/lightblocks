package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.TutorialModel;
import de.golfgl.lightblocks.scene2d.BlockActor;

/**
 * Jegliche Touchscreen-Kontrollen verarbeiten
 * <p>
 * Created by Benjamin Schulte on 25.01.2017.
 */
public class PlayGesturesInput extends PlayScreenInput {
    public static final int SWIPEUP_DONOTHING = 0;
    public static final int SWIPEUP_PAUSE = 1;
    public static final int SWIPEUP_HARDDROP = 2;
    private static final float TOUCHPAD_DEAD_RADIUS = .5f;
    private static final float MAX_SOFTDROPBEGINNING_INTERVAL = .3f;

    private static final float SCREEN_BORDER_PERCENTAGE = 0.1f;
    // Flipped Mode: Tap löst horizontale Bewegung aus, Swipe rotiert
    private static final boolean flippedMode = false;

    public Color rotateRightColor = new Color(.1f, 1, .3f, .8f);
    public Color rotateLeftColor = new Color(.2f, .8f, 1, .8f);
    boolean touchDownValid = false;
    boolean beganHorizontalMove;
    boolean beganSoftDrop;
    boolean didSomething;
    Group touchPanel;
    Vector2 touchCoordinates;
    private int screenX;
    private int screenY;
    private float timeSinceTouchDown;
    private int dragThreshold;
    private Label toTheRight;
    private Label toTheLeft;
    private Label toDrop;
    private Label rotationLabel;
    private boolean didHardDrop;

    private LandscapeOnScreenButtons landscapeOnScreenControls;
    private PortraitOnScreenButtons portraitOnScreenControls;
    private boolean tutorialMode;

    @Override
    public String getInputHelpText() {
        return playScreen.app.TEXTS.get(isUsingOnScreenButtons() ? "inputOnScreenButtonHelp" : "inputGesturesHelp");
    }

    @Override
    public String getTutorialContinueText() {
        return playScreen.app.TEXTS.get(isUsingOnScreenButtons() ? "tutorialContinueGamepad" :
                "tutorialContinueGestures");
    }

    @Override
    public void setPlayScreen(PlayScreen playScreen) {
        super.setPlayScreen(playScreen);

        dragThreshold = playScreen.app.localPrefs.getTouchPanelSize(playScreen.app.getDisplayDensityRatio());
        tutorialMode = playScreen.gameModel instanceof TutorialModel;

        if (playScreen.app.localPrefs.getShowTouchPanel() || tutorialMode)
            initializeTouchPanel(playScreen, dragThreshold);

        if (isUsingOnScreenButtons()) {
            initLandscapeOnScreenControls();
            initPortraitOnScreenControls();
        }

    }

    @Override
    public void doPoll(float delta) {
        if (isUsingOnScreenButtons()) {
            landscapeOnScreenControls.setVisible(playScreen.isLandscape() && !isPaused());
            portraitOnScreenControls.setVisible(!playScreen.isLandscape() && !isPaused());
        }

        if (touchDownValid)
            timeSinceTouchDown += delta;

        if (toDrop != null)
            toDrop.setVisible(beganSoftDrop || timeSinceTouchDown <= MAX_SOFTDROPBEGINNING_INTERVAL);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        if (button == Input.Buttons.RIGHT) {
            playScreen.goBackToMenu();
            return true;
        } else if (pointer != 0 || button != Input.Buttons.LEFT)
            return false;

        touchDownValid = screenY > Gdx.graphics.getHeight() * SCREEN_BORDER_PERCENTAGE * (isPaused() ? 2 : 1)
                && (!isUsingOnScreenButtons() || isPaused());

        if (!touchDownValid)
            return false;

        this.screenX = screenX;
        this.screenY = screenY;
        this.timeSinceTouchDown = 0;
        didHardDrop = false;

        if (isPaused()) {
            playScreen.switchPause(false);
            didSomething = true;
        } else {
            if (!flippedMode)
                playScreen.gameModel.setInputFreezeInterval(.1f);

            setTouchPanel(screenX, screenY);
            didSomething = false;
        }

        return true;
    }

    protected boolean isUsingOnScreenButtons() {
        return playScreen.app.localPrefs.useOnScreenControlsInLandscape() && !tutorialMode;
    }

    public Group initializeTouchPanel(AbstractScreen playScreen, int dragTrashold) {
        if (touchPanel != null)
            touchPanel.remove();

        touchPanel = new Group();
        touchPanel.setTransform(false);
        Vector2 touchCoordinates1;

        touchCoordinates1 = new Vector2(0, 0);
        touchCoordinates = new Vector2(dragTrashold, 0);

        playScreen.stage.getViewport().unproject(touchCoordinates);
        playScreen.stage.getViewport().unproject(touchCoordinates1);

        float screenDragTreshold = Math.abs(touchCoordinates.x - touchCoordinates1.x);

        touchPanel.setVisible(false);
        rotationLabel = new Label(FontAwesome.ROTATE_RIGHT, playScreen.app.skin, FontAwesome.SKIN_FONT_FA);

        float scale = Math.min(1, screenDragTreshold * 1.9f / rotationLabel.getPrefWidth());
        rotationLabel.setFontScale(scale);

        rotationLabel.setPosition(-rotationLabel.getPrefWidth() / 2, -rotationLabel.getPrefHeight() / 2);

        toTheRight = new Label(FontAwesome.CIRCLE_RIGHT, playScreen.app.skin, FontAwesome.SKIN_FONT_FA);
        toTheRight.setFontScale(scale);
        toTheRight.setPosition(screenDragTreshold, -toTheRight.getPrefHeight() / 2);
        toTheLeft = new Label(FontAwesome.CIRCLE_LEFT, playScreen.app.skin, FontAwesome.SKIN_FONT_FA);
        toTheLeft.setFontScale(scale);
        toTheLeft.setPosition(-screenDragTreshold - toTheRight.getPrefWidth(), -toTheRight.getPrefHeight() / 2);

        toDrop = new Label(FontAwesome.CIRCLE_DOWN, playScreen.app.skin, FontAwesome.SKIN_FONT_FA);
        toDrop.setFontScale(scale);
        toDrop.setPosition(-toDrop.getPrefWidth() / 2, -screenDragTreshold - toDrop.getPrefHeight());

        touchPanel.addActor(toTheLeft);
        touchPanel.addActor(toTheRight);
        touchPanel.addActor(toDrop);
        touchPanel.addActor(rotationLabel);

        playScreen.stage.addActor(touchPanel);

        return touchPanel;
    }

    private void initPortraitOnScreenControls() {
        if (portraitOnScreenControls == null) {
            portraitOnScreenControls = new PortraitOnScreenButtons();
            portraitOnScreenControls.setVisible(false);
            playScreen.stage.addActor(portraitOnScreenControls);
        }
        portraitOnScreenControls.resize();
    }

    private void initLandscapeOnScreenControls() {
        if (landscapeOnScreenControls == null) {
            landscapeOnScreenControls = new LandscapeOnScreenButtons();
            landscapeOnScreenControls.setVisible(false);
            playScreen.stage.addActor(landscapeOnScreenControls);
        }
        landscapeOnScreenControls.resize();
    }

    public void setTouchPanel(int screenX, int screenY) {

        if (touchPanel == null || flippedMode)
            return;

        touchCoordinates.set(screenX, screenY);
        playScreen.stage.getViewport().unproject(touchCoordinates);
        touchPanel.setPosition(touchCoordinates.x, touchCoordinates.y);

        if (this.screenX >= Gdx.graphics.getWidth() / 2) {
            setTouchPanelColor(rotateRightColor);
            rotationLabel.setText(FontAwesome.ROTATE_RIGHT);
        } else {
            setTouchPanelColor(rotateLeftColor);
            rotationLabel.setText(FontAwesome.ROTATE_LEFT);
        }

        touchPanel.setZIndex(Integer.MAX_VALUE);
        touchPanel.setVisible(true);
    }

    public void setTouchPanelColor(Color c) {
        if (touchPanel == null)
            return;

        toTheRight.setColor(c);
        toTheLeft.setColor(c);
        toDrop.setColor(c);
        rotationLabel.setColor(c);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Bei mehr als dragThreshold Pixeln erkennen wir eine Bewegung an...
        if (pointer != 0 || !touchDownValid)
            return false;

        // Horizontale Bewegung  bemerken - aber nur wenn kein Hard Drop eingeleitet!
        if (!didHardDrop) {
            if (!flippedMode) {
                if ((!beganHorizontalMove) && (Math.abs(screenX - this.screenX) > dragThreshold)) {
                    beganHorizontalMove = true;
                    playScreen.gameModel.startMoveHorizontal(screenX - this.screenX < 0);
                }
                if ((beganHorizontalMove) && (Math.abs(screenX - this.screenX) < dragThreshold)) {
                    playScreen.gameModel.endMoveHorizontal(true);
                    playScreen.gameModel.endMoveHorizontal(false);
                    beganHorizontalMove = false;
                }
            }
            if (flippedMode) {
                if (Math.abs(screenX - this.screenX) > dragThreshold) {
                    playScreen.gameModel.setRotate(screenX - this.screenX > 0);
                    touchDownValid = false;
                }
            }
        }

        if (!beganHorizontalMove && screenY - this.screenY > dragThreshold && !beganSoftDrop
                && timeSinceTouchDown <= MAX_SOFTDROPBEGINNING_INTERVAL) {
            beganSoftDrop = true;
            playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_SOFT_DROP);
        }
        // Soft Drop sofort beenden, wenn horizontal bewegt oder wieder hochgezogen
        if ((beganHorizontalMove || screenY - this.screenY < dragThreshold) && beganSoftDrop) {
            beganSoftDrop = false;
            playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_NO_DROP);
        }

        int swipeUpType = playScreen.app.localPrefs.getSwipeUpType();
        int swipeUpTresholdFactor = swipeUpType == SWIPEUP_HARDDROP ? 3 : 4;
        if (screenY - this.screenY < -swipeUpTresholdFactor * dragThreshold && swipeUpType != SWIPEUP_DONOTHING) {
            if (swipeUpType == SWIPEUP_PAUSE && !isPaused())
                playScreen.switchPause(false);
            else if (swipeUpType == SWIPEUP_HARDDROP && !didHardDrop && !beganHorizontalMove) {
                playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_HARD_DROP);
                didHardDrop = true;
            }
        }

        // rotate vermeiden
        if (!didSomething && (Math.abs(screenX - this.screenX) > dragThreshold
                || Math.abs(screenY - this.screenY) > dragThreshold)) {
            playScreen.gameModel.setInputFreezeInterval(0);
            didSomething = true;
        }

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer != 0 || !touchDownValid || button != Input.Buttons.LEFT)
            return false;

        playScreen.gameModel.setInputFreezeInterval(0);

        if (touchPanel != null)
            touchPanel.setVisible(false);

        if (!didSomething) {
            if (!flippedMode)
                playScreen.gameModel.setRotate(screenX >= Gdx.graphics.getWidth() / 2);
            else
                playScreen.gameModel.doOneHorizontalMove(screenX <= Gdx.graphics.getWidth() / 2);

        } else
            playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_NO_DROP);

        if (beganHorizontalMove) {
            playScreen.gameModel.endMoveHorizontal(true);
            playScreen.gameModel.endMoveHorizontal(false);
            beganHorizontalMove = false;
        }

        return true;
    }

    @Override
    public String getAnalyticsKey() {
        return "gestures";
    }

    private class TouchpadChangeListener extends ChangeListener {
        boolean upPressed;
        boolean downPressed;
        boolean rightPressed;
        boolean leftPressed;

        @Override
        public void changed(ChangeListener.ChangeEvent event, Actor actor) {
            if (!(actor instanceof Touchpad))
                return;

            Touchpad touchpad = (Touchpad) actor;

            boolean upNowPressed = touchpad.getKnobPercentY() > TOUCHPAD_DEAD_RADIUS;
            boolean downNowPressed = touchpad.getKnobPercentY() < -TOUCHPAD_DEAD_RADIUS;
            boolean rightNowPressed = touchpad.getKnobPercentX() > TOUCHPAD_DEAD_RADIUS;
            boolean leftNowPressed = touchpad.getKnobPercentX() < -TOUCHPAD_DEAD_RADIUS;

            // zwei Richtungen gleichzeitig: entscheiden welcher wichtiger ist
            if ((upNowPressed || downNowPressed) && (leftNowPressed || rightNowPressed)) {
                if (Math.abs(touchpad.getKnobPercentY()) >= Math.abs(touchpad.getKnobPercentX())) {
                    rightNowPressed = false;
                    leftNowPressed = false;
                } else {
                    upNowPressed = false;
                    downNowPressed = false;
                }

            }

            if (upPressed != upNowPressed) {
                upPressed = upNowPressed;

                // nix zu tun
            }

            if (downPressed != downNowPressed) {
                downPressed = downNowPressed;
                if (downPressed)
                    playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_SOFT_DROP);
                else
                    playScreen.gameModel.setSoftDropFactor(0);
            }

            if (rightPressed != rightNowPressed) {
                rightPressed = rightNowPressed;
                if (rightPressed)
                    playScreen.gameModel.startMoveHorizontal(false);
                else
                    playScreen.gameModel.endMoveHorizontal(false);
            }

            if (leftPressed != leftNowPressed) {
                leftPressed = leftNowPressed;
                if (leftPressed)
                    playScreen.gameModel.startMoveHorizontal(true);
                else
                    playScreen.gameModel.endMoveHorizontal(true);
            }

        }
    }

    private class LandscapeOnScreenButtons extends Group {
        private final Touchpad touchpad;
        private final Button rotateRightButton;
        private final Button rotateLeftButton;
        private final Button hardDropButton;

        public LandscapeOnScreenButtons() {
            touchpad = new Touchpad(0, playScreen.app.skin);
            touchpad.addListener(new TouchpadChangeListener());
            addActor(touchpad);

            rotateRightButton = new ImageButton(playScreen.app.skin, "rotateright");
            rotateRightButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    playScreen.gameModel.setRotate(true);
                }
            });
            addActor(rotateRightButton);

            rotateLeftButton = new ImageButton(playScreen.app.skin, "rotateleft");
            rotateLeftButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    playScreen.gameModel.setRotate(false);
                }
            });
            addActor(rotateLeftButton);

            hardDropButton = new ImageButton(playScreen.app.skin, "harddrop");
            hardDropButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_HARD_DROP);
                }
            });
            addActor(hardDropButton);

        }

        public void resize() {
            float size = Math.min(playScreen.stage.getHeight() * .5f, playScreen.centerGroup.getX());
            touchpad.setSize(size, size);
            touchpad.setPosition(0, 0);
            rotateRightButton.setSize(size * .4f, size * .4f);
            rotateLeftButton.setSize(size * .4f, size * .4f);
            hardDropButton.setSize(size * .4f, size * .4f);
            rotateRightButton.setPosition(playScreen.stage.getWidth() - size * .5f, size - rotateRightButton
                    .getHeight());
            rotateLeftButton.setPosition(rotateRightButton.getX() - size * .45f, (rotateRightButton.getY() -
                    rotateRightButton.getHeight()) / 2);
            hardDropButton.setPosition(rotateRightButton.getX() - size * .55f, rotateRightButton.getY());
        }
    }

    private class PortraitOnScreenButtons extends Group {
        private static final int PADDING = 8;
        private final Button rotateRight;
        private final Button rotateLeft;
        private final Button moveRight;
        private final Button moveLeft;
        private boolean rightPressed = false;
        private boolean leftPressed = false;
        private boolean didSoftDrop = false;

        public PortraitOnScreenButtons() {
            rotateRight = new PortraitButton(FontAwesome.ROTATE_RIGHT, Align.top);
            rotateRight.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    rightPressed = true;
                    checkSoftDrop();
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    rightPressed = false;
                    playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_NO_DROP);
                    if (!didSoftDrop)
                        playScreen.gameModel.setRotate(true);
                }
            });
            addActor(rotateRight);

            rotateLeft = new PortraitButton(FontAwesome.ROTATE_LEFT, Align.top);
            rotateLeft.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    leftPressed = true;
                    checkSoftDrop();
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    leftPressed = false;
                    playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_NO_DROP);
                    if (!didSoftDrop)
                        playScreen.gameModel.setRotate(false);
                }
            });
            addActor(rotateLeft);

            moveRight = new PortraitButton(FontAwesome.RIGHT_CHEVRON, Align.bottom);
            moveRight.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    playScreen.gameModel.startMoveHorizontal(false);
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    playScreen.gameModel.endMoveHorizontal(false);
                }
            });
            addActor(moveRight);

            moveLeft = new PortraitButton(FontAwesome.LEFT_CHEVRON, Align.bottom);
            moveLeft.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    playScreen.gameModel.startMoveHorizontal(true);
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    playScreen.gameModel.endMoveHorizontal(true);
                }
            });
            addActor(moveLeft);

        }

        private void checkSoftDrop() {
            didSoftDrop = rightPressed && leftPressed;
            if (didSoftDrop)
                playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_SOFT_DROP);
        }


        public void resize() {
            float gameboardWidth = BlockActor.blockWidth * Gameboard.GAMEBOARD_COLUMNS;

            rotateRight.setPosition(playScreen.stage.getWidth() / 2 + gameboardWidth / 2 + 2 * PADDING,
                    PADDING * 3 + playScreen.centerGroup.getY());
            rotateRight.setSize(playScreen.stage.getWidth() - rotateRight.getX() - PADDING,
                    playScreen.stage.getHeight() * .4f - rotateRight.getY() - PADDING);

            rotateLeft.setPosition(PADDING, rotateRight.getY());
            rotateLeft.setSize(rotateRight.getWidth(), rotateRight.getHeight());

            moveRight.setPosition(rotateRight.getX(), rotateRight.getY() + rotateRight.getHeight() + PADDING);
            moveRight.setSize(rotateRight.getWidth(), rotateRight.getHeight());

            moveLeft.setPosition(rotateLeft.getX(), moveRight.getY());
            moveLeft.setSize(moveRight.getWidth(), moveRight.getHeight());
        }

        private class PortraitButton extends Button {
            public PortraitButton(String label, int alignment) {
                super(playScreen.app.skin, "smoke");
                Label buttonLabel = new Label(label, playScreen.app.skin, FontAwesome.SKIN_FONT_FA);
                buttonLabel.setAlignment(alignment);
                buttonLabel.setFontScale(.8f);
                add(buttonLabel).fill().expand();
            }
        }
    }
}

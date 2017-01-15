package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scenes.BlockActor;
import jdk.nashorn.internal.ir.Block;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by Benjamin Schulte on 15.01.2017.
 */
public class MainMenuScreen implements Screen {
    final LightBlocksGame app;
    private final CheckBox menuMusicButton;
    Stage stage;
    private Sound switchSound;
    ParticleEffect effect = new ParticleEffect();

    public MainMenuScreen(LightBlocksGame lightBlocksGame) {
        this.app = lightBlocksGame;

        stage = new Stage(new FitViewport(LightBlocksGame.nativeGameWidth, LightBlocksGame.nativeGameHeight));
        Gdx.input.setInputProcessor(stage);
        switchSound = app.assetManager.get("sound/switchon.ogg", Sound.class);

        // Create a table that fills the screen. Everything else will go inside this table.
        final Table table = new Table();
        table.setFillParent(true);

        stage.addActor(table);

        // Die Blockgroup nimmt die Steinanimation auf
        final Group blockGroup = new Group();
        blockGroup.setTransform(false);

        table.add(blockGroup).colspan(2).center();

        // Der Titel
        table.row();

        final Label gameTitle = new Label(app.TEXTS.get("gameTitle").toUpperCase(), app.skin);
        gameTitle.setFontScaleX(1.4f);
        gameTitle.setFontScaleY(1.2f);
        table.add(gameTitle).colspan(2).spaceTop(LightBlocksGame.nativeGameWidth / 12);
        table.row();
        final Label gameAuthor = new Label(app.TEXTS.get("gameAuthor"), app.skin);
        gameAuthor.setFontScale(0.8f);
        table.add(gameAuthor).colspan(2).
                spaceBottom(LightBlocksGame.nativeGameWidth / 12).top();


        //nun die Buttons zum bedienen
        table.row();

        // Create a button with the "default" TextButtonStyle. A 3rd parameter can be used to specify a name other than "default".
        final TextButton button = new TextButton(app.TEXTS.get("menuPlayButton"), app.skin);
        button.addListener(new ChangeListener() {
                               public void changed (ChangeEvent event, Actor actor) {

                                   // falls die anfangsanimation noch läuft, unterbrechen
                                   for (Actor block : blockGroup.getChildren()) {
                                       block.clearActions();
                                   }

                                   table.addAction(Actions.moveTo(-LightBlocksGame.nativeGameWidth, table.getY(), .5f));
                               }}
                               );

        table.add(button).minWidth(LightBlocksGame.nativeGameWidth / 2).colspan(2);

        table.row();
        table.add(new Label(app.TEXTS.get("menuPlayerNameField") + ":", app.skin));
        table.add(new TextField(null, app.skin));

        table.row();
        table.add();
        menuMusicButton = new CheckBox(app.TEXTS.get("menuMusicButton"), app.skin);
        menuMusicButton.setChecked(app.prefs.getBoolean("musicPlayback", true));
        table.add(menuMusicButton);

        table.setColor(Color.CLEAR);

        table.addAction(Actions.fadeIn(2));

        constructBlockAnimation(blockGroup);

    }

    /**
     * This method constructs the blocks animation when starting.
     * It is then played via Actions
     */
    private void constructBlockAnimation(final Group blockGroup) {
        for (int i=0; i<4; i++) {
            BlockActor block = new BlockActor(app.assetManager);
            blockGroup.addActor(block);
            block.setY(500);
            block.setEnlightened(true);
            if (i!=3) block.setX(-BlockActor.blockWidth);

            block.setColor(Color.RED);
            block.addAction(Actions.sequence(Actions.delay(2 * i),
                    Actions.moveTo(block.getX(), (i == 3 ? 0 : i * BlockActor.blockWidth ), 0.5f),
                    run(new Runnable() {
                        @Override
                        public void run() {
                            switchSound.play();
                        }
                    }),
                    Actions.delay(0.1f),
                    run(block.dislightenAction)));
        }

        stage.addAction(sequence(delay(10), forever(sequence(run(new Runnable() {
            @Override
            public void run() {
                BlockActor block = (BlockActor) blockGroup.getChildren().get(MathUtils.random(0, 3));
                block.setEnlightened(!block.isEnlightened());
            }
        }), delay(5)))));
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        // See below for what true means.
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}

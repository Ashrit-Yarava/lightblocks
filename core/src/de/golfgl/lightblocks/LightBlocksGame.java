package de.golfgl.lightblocks;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.minlog.Log;

import java.util.HashMap;
import java.util.List;

import de.golfgl.lightblocks.gpgs.IGpgsClient;
import de.golfgl.lightblocks.gpgs.IGpgsListener;
import de.golfgl.lightblocks.model.Mission;
import de.golfgl.lightblocks.model.TutorialModel;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.INsdHelper;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.MainMenuScreen;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.screen.PlayerAccountMenuScreen;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.GameStateHandler;
import de.golfgl.lightblocks.state.GamepadConfig;
import de.golfgl.lightblocks.state.Player;

import static com.badlogic.gdx.Gdx.app;

public class LightBlocksGame extends Game implements IGpgsListener {
    public static final int nativeGameWidth = 480;
    public static final int nativeGameHeight = 800;
    public static final String GAME_URL_SHORT = "http://bit.ly/2lrP1zq";
    public static final String GAME_URL = "http://www.golfgl.de/lightblocks/";
    public static final String GAME_STOREURL = "http://play.google.com/store/apps/details?id=de.golfgl.lightblocks";
    // An den gleichen Eintrag im AndroidManifest denken!!!
    public static final String GAME_VERSIONSTRING = "0.70.052";
    // Abstand für Git
    public static final boolean GAME_DEVMODE = true;

    public static final String SKIN_FONT_TITLE = "bigbigoutline";
    public static final String SKIN_FONT_BIG = "big";

    public Skin skin;
    public AssetManager assetManager;
    public I18NBundle TEXTS;
    public Preferences prefs;
    public GameStateHandler savegame;
    // these resources are used in the whole game... so we are loading them here
    public TextureRegion trBlock;
    public TextureRegion trBlockEnlightened;
    public TextureRegion trGlowingLine;
    public Sound dropSound;
    public Sound rotateSound;
    public Sound removeSound;
    public Sound gameOverSound;
    public Sound cleanSpecialSound;
    public Sound unlockedSound;
    public Sound swoshSound;
    public Sound garbageSound;
    public ShareHandler share;
    public AbstractMultiplayerRoom multiRoom;
    public Player player;
    public IGpgsClient gpgsClient;
    // Android Modellname des Geräts
    public String modelNameRunningOn;
    public MainMenuScreen mainMenuScreen;
    public INsdHelper nsdHelper;
    // der AccountScreen um An-/Abmeldung dort anzuzeien
    PlayerAccountMenuScreen accountScreen;
    private FPSLogger fpsLogger;
    private Boolean playMusic;
    private Boolean showTouchPanel;
    private Boolean gpgsAutoLogin;
    private Boolean dontAskForRating;
    private GamepadConfig gamepadConfig;
    private List<Mission> missionList;
    private HashMap<String, Mission> missionMap;

    public Boolean getGpgsAutoLogin() {
        if (gpgsAutoLogin == null)
            gpgsAutoLogin = prefs.getBoolean("gpgsAutoLogin", true);

        return gpgsAutoLogin;
    }

    public void setGpgsAutoLogin(Boolean gpgsAutoLogin) {
        if (gpgsAutoLogin != this.gpgsAutoLogin) {
            prefs.putBoolean("gpgsAutoLogin", gpgsAutoLogin);
            prefs.flush();
        }
        this.gpgsAutoLogin = gpgsAutoLogin;
    }

    public void setAccountScreen(PlayerAccountMenuScreen accountScreen) {
        this.accountScreen = accountScreen;
    }

    @Override
    public void create() {
        if (GAME_DEVMODE)
            fpsLogger = new FPSLogger();
        else {
            Log.set(Log.LEVEL_WARN);
            Gdx.app.setLogLevel(Application.LOG_ERROR);
        }

        prefs = app.getPreferences("lightblocks");

        if (share == null)
            share = new ShareHandler();

        // GPGS: Wenn beim letzten Mal angemeldet, dann wieder anmelden
        if (player == null) {
            player = new Player();
            player.setGamerId(modelNameRunningOn);
        }

        savegame = new GameStateHandler(this);

        if (getGpgsAutoLogin() && gpgsClient != null)
            gpgsClient.connect(true);

        I18NBundle.setSimpleFormatter(true);

        skin = new Skin(Gdx.files.internal("skin/neon-ui.json"));

        ObjectMap<String, BitmapFont> objectMap = skin.getAll(BitmapFont.class);

        //Sicherstellen dass alle Fonts optimal gerendet werden
        for (BitmapFont font : objectMap.values()) {
            font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        assetManager = new AssetManager();
        assetManager.load("i18n/strings", I18NBundle.class);
        assetManager.load("raw/block-deactivated.png", Texture.class);
        assetManager.load("raw/block-light.png", Texture.class);
        assetManager.load("raw/lineglow.png", Texture.class);
        assetManager.load("sound/switchon.ogg", Sound.class);
        assetManager.load("sound/switchflip.ogg", Sound.class);
        assetManager.load("sound/glow05.ogg", Sound.class);
        assetManager.load("sound/gameover.ogg", Sound.class);
        assetManager.load("sound/cleanspecial.ogg", Sound.class);
        assetManager.load("sound/unlocked.ogg", Sound.class);
        assetManager.load("sound/swosh.ogg", Sound.class);
        assetManager.load("sound/garbage.ogg", Sound.class);
        assetManager.finishLoading();

        TEXTS = assetManager.get("i18n/strings", I18NBundle.class);
        trBlock = new TextureRegion(assetManager.get("raw/block-deactivated.png", Texture.class));
        trBlock.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        trBlockEnlightened = new TextureRegion(assetManager.get("raw/block-light.png", Texture.class));
        trBlockEnlightened.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        trGlowingLine = new TextureRegion(assetManager.get("raw/lineglow.png", Texture.class));
        trGlowingLine.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        dropSound = assetManager.get("sound/switchon.ogg", Sound.class);
        rotateSound = assetManager.get("sound/switchflip.ogg", Sound.class);
        removeSound = assetManager.get("sound/glow05.ogg", Sound.class);
        gameOverSound = assetManager.get("sound/gameover.ogg", Sound.class);
        unlockedSound = assetManager.get("sound/unlocked.ogg", Sound.class);
        garbageSound = assetManager.get("sound/garbage.ogg", Sound.class);
        cleanSpecialSound = assetManager.get("sound/cleanspecial.ogg", Sound.class);
        swoshSound = assetManager.get("sound/swosh.ogg", Sound.class);

        mainMenuScreen = new MainMenuScreen(this);

        if (savegame.hasGameState())
            this.setScreen(mainMenuScreen);
        else {
            // beim ersten Mal ins Tutorial!
            try {
                PlayScreen ps = PlayScreen.gotoPlayScreen(mainMenuScreen, TutorialModel.getTutorialInitParams());
                ps.setShowScoresWhenGameOver(false);
                ps.setBackScreen(mainMenuScreen);
            } catch (VetoException e) {
                this.setScreen(mainMenuScreen);
            }

        }
    }

    @Override
    public void render() {
        super.render(); //important!
        if (GAME_DEVMODE && fpsLogger != null)
            fpsLogger.log();
    }

    @Override
    public void pause() {
        super.pause();

        if (gpgsClient != null)
            gpgsClient.disconnect(true);
    }

    @Override
    public void resume() {
        super.resume();

        if (getGpgsAutoLogin() && gpgsClient != null && !gpgsClient.isConnected())
            gpgsClient.connect(true);
    }

    @Override
    public void dispose() {
        if (multiRoom != null)
            try {
                multiRoom.leaveRoom(true);
            } catch (VetoException e) {
                e.printStackTrace();
            }

        mainMenuScreen.dispose();
        skin.dispose();
    }

    public boolean isPlayMusic() {
        if (playMusic == null)
            playMusic = prefs.getBoolean("musicPlayback", true);

        return playMusic;
    }

    public void setPlayMusic(boolean playMusic) {
        if (this.playMusic != playMusic) {
            this.playMusic = playMusic;
            prefs.putBoolean("musicPlayback", playMusic);
            prefs.flush();
        }
    }

    public boolean getShowTouchPanel() {
        if (showTouchPanel == null)
            showTouchPanel = prefs.getBoolean("showTouchPanel", true);

        return showTouchPanel;
    }

    public void setShowTouchPanel(boolean showTouchPanel) {
        if (this.showTouchPanel != showTouchPanel) {
            this.showTouchPanel = showTouchPanel;

            prefs.putBoolean("showTouchPanel", showTouchPanel);
            prefs.flush();
        }
    }

    public int getTouchPanelSize() {
        return prefs.getInteger("touchPanelSize", 50);
    }

    public void setTouchPanelSize(int touchPanelSize) {
        prefs.putInteger("touchPanelSize", touchPanelSize);
        prefs.flush();
    }

    @Override
    public void gpgsConnected() {
        player.setGamerId(gpgsClient.getPlayerDisplayName());
        setGpgsAutoLogin(true);
        handleAccountChanged();
    }

    private void handleAccountChanged() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                mainMenuScreen.refreshAccountInfo();
                if (accountScreen != null)
                    accountScreen.refreshAccountChanged();
                //beim ersten Connect Spielstand laden (wenn vorhanden)
                // War zuerst in GpgsConnect, es wurde aber der allerste Login nicht mehr automatisch gesetzt.
                // (obwohl das Willkommen... Schild kam)
                // Nun in UI Thread verlagert
                if (!savegame.isAlreadyLoadedFromCloud() && gpgsClient.isConnected())
                    gpgsClient.loadGameState(false);

            }
        });
    }

    @Override
    public void gpgsDisconnected() {
        player.setGamerId(modelNameRunningOn);
        handleAccountChanged();
    }

    @Override
    public void gpgsErrorMsg(final String msg) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                Screen currentScreen = getScreen();
                if (currentScreen instanceof AbstractScreen)
                    ((AbstractScreen) currentScreen).showDialog(msg);

            }
        });
    }

    @Override
    public void gpgsGameStateLoaded(byte[] gameState) {
        savegame.gpgsLoadGameState(gameState);
    }

    public GamepadConfig getGamepadConfig() {
        if (gamepadConfig == null) {
            gamepadConfig = new GamepadConfig();

            gamepadConfig.pauseButton = prefs.getInteger("gpButtonPause", GamepadConfig.GC_BUTTON_START);
            gamepadConfig.verticalAxis = prefs.getInteger("gpAxisVertical", GamepadConfig.GC_AXIS_VERTICAL_ANDROID);
            gamepadConfig.rotateClockwiseButton = prefs.getInteger("gpButtonClockwise", GamepadConfig.GC_BUTTON_START);
        }

        return gamepadConfig;
    }

    public void setGamepadConfig(GamepadConfig gamepadConfig) {
        this.gamepadConfig = gamepadConfig;

        prefs.putInteger("gpButtonPause", gamepadConfig.pauseButton);
        prefs.putInteger("gpAxisVertical", gamepadConfig.verticalAxis);
        prefs.putInteger("gpButtonClockwise", gamepadConfig.rotateClockwiseButton);

        prefs.flush();
    }

    public List<Mission> getMissionList() {
        if (missionList == null) {
            missionList = Mission.getMissionList();

            // Hashmap aufbauen
            missionMap = new HashMap<String, Mission>(missionList.size());
            for (Mission mission : missionList)
                missionMap.put(mission.getUniqueId(), mission);
        }

        return missionList;
    }

    public Mission getMissionFromUid(String uid) {
        if (missionMap == null)
            getMissionList();

        return missionMap.get(uid);
    }

    public Boolean getDontAskForRating() {
        if (dontAskForRating == null)
            dontAskForRating = prefs.getBoolean("dontAskForRating", false);

        return dontAskForRating;
    }

    public void setDontAskForRating(Boolean dontAskForRating) {
        this.dontAskForRating = dontAskForRating;
        prefs.putBoolean("dontAskForRating", dontAskForRating);
        prefs.flush();
    }
}

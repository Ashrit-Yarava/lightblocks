package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Übernimmt die Steuerung der Music (an/aus) sowie den gerade angenommenen Zustand siehe MusicState)
 */
public class PlayMusic {
    public static final float TIME_TO_CHANGE = 0.3f;
    private final LightBlocksGame app;
    private Music slowMusic;
    private Music fastMusic;
    private boolean isPlayMusic;
    private MusicState state = MusicState.playingSlowly;
    private boolean shouldPlayFast;
    private float fadingVolume;

    public PlayMusic(LightBlocksGame app) {
        this.app = app;
    }

    public void act(float delta) {
        // slow to fast geht von 0 bis 1, wobei das direkt die Lautstärke von fastMusic ist
        // und slowMusic das umgekehrt hat

        if (isPlayMusic && state.equals(MusicState.transitioning)) {
            float newVolChange = (delta / TIME_TO_CHANGE) * (shouldPlayFast ? 1 : -1);
            fadingVolume = fadingVolume + newVolChange;

            fastMusic.setVolume(MathUtils.clamp(fadingVolume, 0, 1));
            slowMusic.setVolume(MathUtils.clamp(1 - fadingVolume, 0, 1));

            if (fadingVolume >= 1f) {
                state = MusicState.playingFast;
                slowMusic.stop();
            } else if (fadingVolume <= 0) {
                state = MusicState.playingSlowly;
                fastMusic.stop();
            }
        }

    }

    public void setPlayingMusic(boolean playMusic) {
        if (playMusic && !isPlayMusic) {
            state = shouldPlayFast ? MusicState.playingFast : MusicState.playingSlowly;
            slowMusic = Gdx.audio.newMusic(Gdx.files.internal(app.getSoundAssetFilename("gameSlow")));
            slowMusic.setLooping(true);
            fastMusic = Gdx.audio.newMusic(Gdx.files.internal(app.getSoundAssetFilename("gameFast")));
            fastMusic.setLooping(true);

        } else if (!playMusic && isPlayMusic) {
            slowMusic.dispose();
            slowMusic = null;
            fastMusic.dispose();
            fastMusic = null;
        }
        isPlayMusic = playMusic;
    }

    public void play() {
        if (isPlayMusic)
            switch (state) {
                case playingSlowly:
                    slowMusic.play();
                    break;
                case transitioning:
                    slowMusic.play();
                    fastMusic.play();
                    break;
                case playingFast:
                    fastMusic.play();
                    break;
            }
    }

    public void pause() {
        if (isPlayMusic)
            switch (state) {
                case playingSlowly:
                    slowMusic.pause();
                    break;
                case transitioning:
                    slowMusic.pause();
                    fastMusic.pause();
                    break;
                case playingFast:
                    fastMusic.pause();
                    break;
            }
    }

    public void stop() {
        if (isPlayMusic)
            switch (state) {
                case playingSlowly:
                    slowMusic.stop();
                    break;
                case transitioning:
                    slowMusic.stop();
                    fastMusic.stop();
                    break;
                case playingFast:
                    fastMusic.stop();
                    break;
            }
    }

    public void dispose() {
        setPlayingMusic(false);
    }

    public void setFastPlay(boolean fastPlay) {
        if (this.shouldPlayFast != fastPlay) {

            switch (state) {
                case playingFast:
                case playingSlowly:
                    state = MusicState.transitioning;
                    play();
                    break;
            }

            this.shouldPlayFast = fastPlay;
        }
    }

    private enum MusicState {playingSlowly, transitioning, playingFast}
}

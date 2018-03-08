package de.golfgl.lightblocks;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import de.golfgl.lightblocks.gpgs.GpgsMultiPlayerClient;
import de.golfgl.lightblocks.multiplayer.NsdAdapter;

public class AndroidLauncher extends AndroidApplication {
    public static final int RC_SELECT_PLAYERS = 10000;
    public final static int RC_INVITATION_INBOX = 10001;

    // Network Service detection
    NsdAdapter nsdAdapter;
    //Google Play Games
    GpgsMultiPlayerClient gpgsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // auch hiermit lassen sich Fehler vermeiden wenn über anderen Launcher gestartet
        // launchMode: singleTask im Manifest tut aber das gleiche, ist nur evtl. zu viel
//        if (!isTaskRoot()) {
//            finish();
//            return;
//        }

        // Create the Google Api Client with access to the Play Games services
        gpgsClient = new GpgsMultiPlayerClient();
        gpgsClient.initialize(this, true);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.hideStatusBar = true;
        config.useAccelerometer = true;
        config.useCompass = false;
        config.useGyroscope = false;
        config.useWakelock = true;
        //immersive Mode leider immer noch nicht möglich, weil es zwei Probleme gibt:
        // nach Wechsel der Anwendung bleibt der Bereich manchmal schwarz, außerdem beim Runterziehen der Notifications
        // mehrmaliges Resize 

        LightBlocksGame game = new LightBlocksGame() {
            @Override
            public void lockOrientation(Input.Orientation orientation) {
                if (orientation == null)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                else if (orientation.equals(Input.Orientation.Landscape))
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                else
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            }

            @Override
            public void unlockOrientation() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        };

        // Sharing is caring - daher mit diesem Handler den nativen Android-Dialog aufrufen
        game.share = new AndroidShareHandler();

        game.gpgsClient = gpgsClient;
        gpgsClient.setListener(game);

        // Gerätemodell wird für den Spielernamen benötigt
        game.modelNameRunningOn = Build.MODEL;

        // Network Serice Discovery
        this.nsdAdapter = new NsdAdapter(this);
        game.nsdHelper = nsdAdapter;

        initialize(game, config);
    }

    @Override
    protected void onPause() {
        if (nsdAdapter != null)
            nsdAdapter.stopDiscovery();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (nsdAdapter != null) {
            nsdAdapter.unregisterService();
            nsdAdapter.stopDiscovery();
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        gpgsClient.onGpgsActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SELECT_PLAYERS)
            gpgsClient.getMultiPlayerRoom().selectPlayersResult(resultCode, data);

        else if (requestCode == RC_INVITATION_INBOX)
            gpgsClient.getMultiPlayerRoom().selectInvitationResult(resultCode, data);

    }

    public class AndroidShareHandler extends ShareHandler {

        @Override
        public void shareText(String message, String title) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setType("text/plain");
            Intent chooser = Intent.createChooser(intent, "Share");

            startActivity(chooser);
        }
    }
}

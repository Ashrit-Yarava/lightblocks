package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;

import org.robovm.apple.gamekit.GKInvite;
import org.robovm.apple.gamekit.GKLocalPlayer;
import org.robovm.apple.gamekit.GKLocalPlayerListenerAdapter;
import org.robovm.apple.gamekit.GKPlayer;
import org.robovm.apple.uikit.UIViewController;

import de.golfgl.gdxgamesvcs.IGameServiceListener;
import de.golfgl.lightblocks.MyGameCenterClient;
import de.golfgl.lightblocks.gpgs.IMultiplayerGsClient;

public class GameCenterMultiplayerClient extends MyGameCenterClient implements IMultiplayerGsClient {

    private final UIViewController viewController;
    private GcMultiplayerRoom gcMultiplayerRoom;
    private GKInvite invitation;
    private IGameServiceListener gsListener;

    public GameCenterMultiplayerClient(UIViewController viewController) {
        super(viewController);
        this.viewController = viewController;
        GKLocalPlayer.getLocalPlayer().registerListener(new GKLocalPlayerListenerAdapter() {

            @Override
            public void didAcceptInvite(GKPlayer player, GKInvite invite) {
                invitation = invite;
                // nochmal listener aufrufen um die Invitation-Prüfung auszulösen
                if (gsListener != null)
                    gsListener.gsOnSessionActive();
            }
        });
    }

    @Override
    public AbstractMultiplayerRoom createMultiPlayerRoom() {
        if (gcMultiplayerRoom == null)
            gcMultiplayerRoom = new GcMultiplayerRoom(this, viewController);

        return gcMultiplayerRoom;
    }

    @Override
    public boolean hasPendingInvitation() {
        return invitation != null;
    }

    @Override
    public void acceptPendingInvitation() {
        if (invitation != null) {
            createMultiPlayerRoom();
            gcMultiplayerRoom.acceptInvitation(invitation);
            invitation = null;
        }
    }

    @Override
    public void setListener(IGameServiceListener gsListener) {
        this.gsListener = gsListener;
        super.setListener(gsListener);
    }

    protected IGameServiceListener getGsListener() {
        return gsListener;
    }
}

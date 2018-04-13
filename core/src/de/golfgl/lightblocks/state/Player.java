package de.golfgl.lightblocks.state;

import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;

/**
 * This class represents the player
 * <p>
 * Created by Benjamin Schulte on 26.02.2017.
 */

public class Player {

    private String gamerId;

    public String getName() {
        if (gamerId == null || gamerId.isEmpty())
            return "John";
        else
            return gamerId;
    }

    public String getGamerId() {
        return gamerId;
    }

    public void setGamerId(String gamerId) {
        if (gamerId == null)
            gamerId = "";

        this.gamerId = gamerId;
    }

    public MultiPlayerObjects.Player toKryoPlayer() {
        MultiPlayerObjects.Player kp = new MultiPlayerObjects.Player();
        kp.name = getName();

        return kp;
    }

}

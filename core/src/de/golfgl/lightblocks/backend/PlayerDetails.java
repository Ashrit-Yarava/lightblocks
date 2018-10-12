package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Detailinformationen zu einem Spieler
 */

public class PlayerDetails implements IPlayerInfo {
    public final String uuid;
    public final String nickName;
    public final long lastActivity;
    public final long memberSince;
    public final long countTotalBlocks;
    public final int experience;
    public final String publicContact;
    public final List<ScoreListEntry> highscores;
    public final String passwordEmail;

    PlayerDetails(JsonValue fromJson) {
        uuid = fromJson.getString("id");
        nickName = fromJson.getString("nickName");

        // diese in Übersichtsliste nicht enthalten und daher mit Defaults vorzubelegen
        lastActivity = fromJson.getLong("lastActivity", 0);
        memberSince = fromJson.getLong("memberSince", 0);
        countTotalBlocks = fromJson.getLong("countTotalBlocks", 0);
        experience = fromJson.getInt("experience", 0);
        publicContact = fromJson.getString("publicContact", null);
        passwordEmail = fromJson.getString("passwordEmail", null);

        // TODO Highscores
        highscores = new ArrayList<ScoreListEntry>();
    }

    @Override
    public String getUserId() {
        return uuid;
    }

    @Override
    public String getUserNickName() {
        return nickName;
    }

    @Override
    public String getUserDecoration() {
        //TODO
        return null;
    }
}

package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.utils.JsonValue;

/**
 * Created by Benjamin Schulte on 28.09.2018.
 */

public class ScoreListEntry {
    public final long scoreValue;
    public final String userId;
    public final String nickName;
    public final String country;
    public final String platform;
    public final String inputType;
    public final String params;
    public final String decoration;
    public final int drawnBlocks;
    public final long scoreGainedTime;

    public ScoreListEntry(JsonValue fromJson) {
        scoreValue = fromJson.getLong("scoreValue");
        userId = fromJson.getString("userId");
        nickName = fromJson.getString("nickName");
        country = fromJson.getString("country", "");
        platform = fromJson.getString("platform", "");
        inputType = fromJson.getString("inputType", "");
        params = fromJson.getString("params", "");
        drawnBlocks = fromJson.getInt("drawnBlocks");
        scoreGainedTime = fromJson.getLong("scoreGainedTime");
        decoration = fromJson.getString("decoration", null);
    }
}


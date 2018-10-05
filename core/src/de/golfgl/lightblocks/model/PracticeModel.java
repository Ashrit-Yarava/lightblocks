package de.golfgl.lightblocks.model;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import de.golfgl.lightblocks.state.BestScore;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Practice: Wird nicht schneller und der Bestscore wird ausschließlich über Line Clears definiert
 * <p>
 * Created by Benjamin Schulte on 24.05.2018.
 */

public class PracticeModel extends GameModel {
    public static final String MODEL_PRACTICE_ID = "practice";

    @Override
    public String getIdentifier() {
        return MODEL_PRACTICE_ID;
    }

    @Override
    public String getGoalDescription() {
        return "goalModelPractice";
    }

    @Override
    public InitGameParameters getInitParameters() {
        InitGameParameters retVal = new InitGameParameters();

        retVal.setBeginningLevel(getScore().getStartingLevel());
        retVal.setInputKey(inputTypeKey);
        retVal.setGameMode(InitGameParameters.GameMode.Practice);

        return retVal;
    }

    @Override
    protected void submitGameEnded(boolean success) {
        // Practice endet immer mit vollem Board. Also definieren wir success hier so, ob 100 Lines geschafft wurden
        super.submitGameEnded(getScore().getClearedLines() >= 100);
    }

    @Override
    protected void initGameScore(int beginningLevel) {
        super.initGameScore(beginningLevel);
        // Scoring auf PRACTICE-Mode stellen (Level für Score unerheblich, und es wird nicht schneller)
        getScore().setScoringType(GameScore.TYPE_PRACTICE);
    }

    @Override
    public boolean showBlocksScore() {
        return true;
    }

    @Override
    public void setBestScore(BestScore bestScore) {
        super.setBestScore(bestScore);
        // bei Practice sind die meisten abgelegten Blöcke maßgeblich
        bestScore.setComparisonMethod(BestScore.ComparisonMethod.blocks);
    }

    @Override
    public String getScoreboardParameters() {
        JsonValue root = new JsonValue(JsonValue.ValueType.object);
        root.addChild("startLevel", new JsonValue(getScore().getStartingLevel()));
        return root.toJson(JsonWriter.OutputType.json);
    }
}

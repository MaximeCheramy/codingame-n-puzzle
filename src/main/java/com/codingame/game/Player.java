package com.codingame.game;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codingame.gameengine.core.AbstractSoloPlayer;

public class Player extends AbstractSoloPlayer {
    private int expectedOutputLines = 1;

    @Override
    public int getExpectedOutputLines() {
        return expectedOutputLines;
    }

    public void setExpectedOutputLines(int expectedOutputLines) {
        this.expectedOutputLines = expectedOutputLines;
    }
    
    private static final Pattern PLAYER_ACTION_PATTERN = Pattern
            .compile("(?<row>-?[0-9]{1})\\s+(?<col>-?[0-9]{1})");

    public Coord getAction() throws TimeoutException, InvalidAction {
        try {
            String playerAction = getOutputs().get(0);
            Matcher match = PLAYER_ACTION_PATTERN.matcher(playerAction);

            if (match.matches()) {
                return new Coord(Integer.parseInt(match.group("row")), Integer.parseInt(match.group("col")));
            } else {
                throw new InvalidAction("Invalid output.");
            }
        } catch (TimeoutException | InvalidAction e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidAction("Invalid output.");
        }
    }
}

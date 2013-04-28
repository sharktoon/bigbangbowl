package com.example.bigbangbowl.game.dice;

public interface IDiceLogReceiver {
    public static final int LOG_NEUTRAL = 0;
    public static final int LOG_SUCCESS = 1;
    public static final int LOG_FAILURE = 2;
    /** used to display a line of the dice log */
    public void showDiceLogLine(int logtype, CharSequence text);

}

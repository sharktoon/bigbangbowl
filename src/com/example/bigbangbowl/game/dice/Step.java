package com.example.bigbangbowl.game.dice;

import org.andengine.entity.sprite.Sprite;

/**
 * more or less a convenience struct to store data for each step of an action
 */
public class Step {
    /** movement */
    public static final int TYPE_MOVE = 0;
    /** block */
    public static final int TYPE_BLOCK = 1;
    /** pass */
    public static final int TYPE_PASS = 2;

    /** target of this step */
    public int tileX, tileY;
    /** type of this step */
    public int type;
    /** graphical display */
    public Sprite sprite;

    /** whether or not this involves rolling the dice */
    public boolean dice = false;
    /** dodge required */
    public boolean dodge = false;
    /** required dice roll result */
    public int minDodgeRoll;
    /** gfi requrired */
    public boolean gfi = false;
    
    /** block */
    public int blockDice = 0;

    /** chance to fail - in percent [0;1] */
    public float successChance;
}
package com.fern.pipo_ballus;

public class ActionContainer {
    public final static int ACTION_NONE = 0;
    public final static int ACTION_ROTATE_CW = 1;
    public final static int ACTION_ROTATE_CCW = 2;
    public final static int ACTION_JUMP = 3;

    public int action = 0;

    public int actionFrame;

    ActionContainer() {
        this.actionFrame = 0;
    }

}

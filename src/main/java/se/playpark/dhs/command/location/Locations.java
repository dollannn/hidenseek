package se.playpark.dhs.command.location;

/**
 * @author bobby29831
 */
public enum Locations {

    GAME,
    LOBBY,
    EXIT,
    SEEKER;

    public String message() {
        return this + "_SPAWN";
    }

}
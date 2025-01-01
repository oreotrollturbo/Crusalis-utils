package io.github.pingisfun.hitboxplus.datatracking;

public class DataTracking {

    public static int locationPings = 0;
    public static int positionPings = 0;
    public static int kills = 0;
    public static int deaths = 0;
    public static int rallyPoints = 0;
    public static int openedConfig = 0;
    public static int flagsDetected = 0;
    //public static int flagsPlaced = 0;
    public static int joinedCrusalis = 0;

    public static void wipeVariables(){
        locationPings = 0;
        positionPings = 0;
        kills = 0;
        deaths = 0;
        rallyPoints = 0;
        openedConfig = 0;
        flagsDetected = 0;
        //flagsPlaced = 0;
        joinedCrusalis = 0;
    }
}

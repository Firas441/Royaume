package sample;

import java.util.ArrayList;

public class Player {
    private String name;
    private ArrayList<Castle> castles;
    public Player(String name){
        castles = new ArrayList<Castle>();
        this.name = name;
    }
    public void addCastle(Castle c){
        castles.add(c);
    }

    public String getName() {
        return name;
    }

    public ArrayList<Castle> getCastles(){
        return castles;
    }
}

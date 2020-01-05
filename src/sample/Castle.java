package sample;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class Castle extends Sprite {
    private boolean owner;
    private Player player;
    private int tresor;
    private List<Soldier> troops = new ArrayList<Soldier>();
    private boolean on_attack = false, selected = false;
    private List<Integer> soldiers_production;

    public Castle(Pane layer,Image image ,double x, double y, boolean owner, Player player) {
        super(layer, image, x, y);
        this.owner = owner;
        this.player = player;
        if (this.owner)
        	this.tresor = Settings.CASTLE_TRESOR;
        else 
        	this.tresor = Settings.CASTLE_NEUTRAL_TRESOR;
        soldiers_production = new ArrayList<Integer>();
    }
    
    public List<Soldier> getTroops(){
    	return troops;
    }

    public List<Integer> getSoldiersProduction(){
        return soldiers_production;
    }
    
    public void initializeTroops(Pane layer, Image soldierImage) {
    	for (int i =0 ; i < Settings.NB_SOLDIERS_INITIAL; i++)
    		troops.add( new Soldier(layer, soldierImage, this.getX(), this.getY(), this) );
    }
    
    public void removeTroop(Soldier s) {
    	troops.remove(s);
    }

    public void addTresor(int t){
        tresor += t;
    }

	@Override
	public String toString() {
		if(this.player!=null)
			return "Castle of " + player.getName() + ", number of troops: " + troops.size() + ", Treasure: " + tresor;
		return "Castle of nobody, number of troops: " + troops.size() + ", Treasure: " + tresor;
	}

	public boolean canAttack(int nb_troops){
        return !(nb_troops < 1 || troops.size() < nb_troops);
    }

    public void setOn_attack(boolean a){
        on_attack = a;
    }

    public boolean isSelected(){
        return selected;
    }

    public void setSelected(boolean b){
        selected = b;
    }

    public Player getPlayer(){
        return player;
    }

    public void setPlayer(Player p){
        player = p;
    }

    public int freeSoldiers() {
        int cpt = 0;
        for (Soldier s : troops){
            if (!s.isOut())
                cpt++;
        }
        return cpt;
    }

    public void addToProduction(int i){
        soldiers_production.add(i);
    }

    public void removeFromProduction() {
        soldiers_production.remove(0);
    }

    public int nextProduction(){
        if(soldiers_production.size() != 0)
            return soldiers_production.get(0);
        return -1;
    }
}

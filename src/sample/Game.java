package sample;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;

public class Game {
	private Sprite[][] gameland;
	private Player[] players;
	private List<Castle> castles = new ArrayList<Castle>();
	int nbPlayers;
	private int nbNeutral;
	private boolean[][] castleAttacks;
	private Direction paths[][][];
	private int[][] path_index;
	
	public Game(Player[] players, int nbPlayers, Pane layer, Image freeLand) {
		this.gameland = new Sprite[Settings.GAME_WIDTH][Settings.GAME_HEIGHT];
		this.nbPlayers = nbPlayers;
		this.players = players;
		for (int i=0 ; i < Settings.GAME_WIDTH ; i++) {
			for(int j =0 ; j < Settings.GAME_HEIGHT ; j++) {
				gameland[i][j] = new FreeLand(layer, freeLand,Settings.CASE_WIDTH * i, Settings.CASE_HEIGHT * j);
			}
		}
	}

	public List<Castle> getCastles(){
		return castles;
	}

	public boolean verify_case(Sprite s) {
		return s.getClass() == FreeLand.class;
	}
	
	public void random_case(int[] pos) {
		int x =(int) (Math.random() * (double)Settings.GAME_WIDTH);
		int y =(int) (Math.random() * (double)Settings.GAME_HEIGHT);
		boolean cond = true;
		while(!cond) {
			if(gameland[x][y] instanceof FreeLand ) {
				//left
				if(cond && x>0 && !verify_case(gameland[x-1][y])) { cond = false; }
				//right 
				else if( cond && x<Settings.GAME_WIDTH && !verify_case(gameland[x+1][y])) { cond = false; }
				//up
				else if( cond && y>0 && !verify_case(gameland[x][y-1])) { cond = false; }
				//down
				else if(y<Settings.GAME_HEIGHT && !verify_case(gameland[x][y+1])) { cond = false; }
				//other 4
				else if(!verify_case(gameland[x-1][y-1]) && !verify_case(gameland[x+1][y-1]) 
						&& !verify_case(gameland[x+1][y-1]) &&  !verify_case(gameland[x+1][y+1])) {
					cond = false;
				}
			}
		}
		pos[0]=x;
		pos[1]=y;
	}
	
	public void generateInitialGameLand(Pane layer, Image castleImage, Image castleNImage, Image soldierImage) {
		for (int i = 0; i < nbPlayers; i++) {
			int[] pos = new int[2];
			random_case(pos);
			gameland[pos[0]][pos[1]] = new Castle(layer, castleImage,pos[0] * Settings.CASTLE_WIDTH, pos[1] * Settings.CASTLE_HEIGHT, true, players[i]);
			Castle c =(Castle) gameland[pos[0]][pos[1]];
			c.initializeTroops(layer, soldierImage);
			players[i].addCastle(c);
			castles.add(c);
		}

		nbNeutral = nbPlayers+1 ;
		for (int i=0 ; i < nbNeutral ; i++) {
			int[] pos = new int[2];
			random_case(pos);
			gameland[pos[0]][pos[1]] = new Castle(layer,castleNImage,pos[0] * Settings.CASTLE_WIDTH, pos[1] * Settings.CASTLE_HEIGHT, false, null);
			Castle c =(Castle) gameland[pos[0]][pos[1]];
			c.initializeTroops(layer, soldierImage);
			castles.add(c);
		}

        castleAttacks = new boolean[ castles.size() ][ castles.size() ];
		paths = new Direction[ castles.size() ][castles.size()][];
        path_index = new int[ castles.size() ][castles.size()];
		for ( int i = 0 ; i < castles.size() ; i++ ){
            for(int j =0 ; j < castles.size() ; j++)
                castleAttacks[i][j] = false;
        }

		for ( int i = 0 ; i < castles.size() ; i++ ){
            for( int j =0 ; j < castles.size() ; j++ )
                path_index[i][j] = 0;
        }
	}
	
	public void updateSoldiersGame() {
    	for(Castle c : castles){
    		for (int i =0 ; i < c.getTroops().size() ; i++) {
    			c.getTroops().get(i).updateSoldierVisibility();
    		}
		}
	}

	List<Direction> path(double attacker_posX, double attacker_posY, double target_posX, double target_posY){
		List<Direction> path = new ArrayList<Direction>();
		if ( attacker_posX == target_posX && attacker_posY == target_posY )
			return path;

		double aux = target_posY == attacker_posY ? Settings.CASE_WIDTH : 0;
		while( attacker_posX > target_posX + aux ) {
			path.add(Direction.LEFT);
			attacker_posX -= Settings.CASE_WIDTH;
		}
		while( attacker_posX < target_posX - aux ) {
			path.add(Direction.RIGHT);
            attacker_posX += Settings.CASE_WIDTH;
		}

		while( attacker_posY > target_posY + Settings.CASE_HEIGHT) {
			path.add(Direction.UP);
            attacker_posY -= Settings.CASE_HEIGHT;
		}
		while( attacker_posY < target_posY - Settings.CASE_HEIGHT) {
			path.add(Direction.DOWN);
            attacker_posY += Settings.CASE_HEIGHT;
		}

		return path;
	}

    public void attack(Castle attacker, Castle target, int nb_troops){
	    if (attacker.freeSoldiers() < nb_troops || target == attacker)
	        return;
	    castleAttacks[ castles.indexOf(attacker) ][ castles.indexOf(target) ] = true;
	    attacker.setOn_attack(true);
	    path_index[ castles.indexOf(attacker) ][ castles.indexOf(target) ] = 0;
	    List<Direction> p = path(attacker.getX(), attacker.getY(), target.getX(), target.getY());
	    paths[ castles.indexOf(attacker) ][ castles.indexOf(target) ] = new Direction[ p.size() ];
	    for (int i = 0 ; i < p.size() ; i++){
            paths[ castles.indexOf(attacker) ][ castles.indexOf(target) ][i] = p.get(i);
        }
	    int n = nb_troops;
	    for (int i = attacker.getTroops().size() - attacker.freeSoldiers() ; i < attacker.getTroops().size() && n > 0 ; i++){
                attacker.getTroops().get(i).setDestination(target.getX(), target.getY());
                n--;
        }
    }

    public void castlesTick(Pane layer, Image soldierImage){
	    for (Castle c : castles){
	        c.addTresor(10);
	        if(c.nextProduction() != -1){
				for(int i = 0; i < c.getSoldiersProduction().size() ; i++){
					if(c.nextProduction() == 0){
						c.getTroops().add(new Soldier(layer, soldierImage, c.getX(), c.getY(), c));
						c.getSoldiersProduction().remove(0);
						i--;
					}
					else {
						for(Integer p : c.getSoldiersProduction())
						p--;
					}
				}
	        }
        }
    }

    public void attacksTick(){
        for(int i = 0; i < castles.size() ; i++ ){
            for (int j = 0; j < castles.size() ; j++){
                if(i != j && castleAttacks[i][j]){
                    for (int s = 0; s < castles.get(i).getTroops().size() ; s++) {
						if (castles.get(i).getTroops().get(s).isOut() && castles.get(i).getTroops().get(s).getDestination()[0] == castles.get(j).getX() && castles.get(i).getTroops().get(s).getDestination()[1] == castles.get(j).getY()) {
							if (!castles.get(i).getTroops().get(s).isArrivedToDestination())
								castles.get(i).getTroops().get(s).move(paths[i][j][path_index[i][j]]);
							else {
								castles.get(i).getTroops().get(s).decreaseDamage();
								if(castles.get(i).getTroops().get(s).isDead()){
									castles.get(i).getTroops().get(s).removeFromLayer();
									castles.get(i).getTroops().remove(s);
									s--;
								}
								if(castles.get(j).getTroops().size() > 0){
									castles.get(j).getTroops().get(0).decreaseHealth();
									if(castles.get(j).getTroops().get(0).isDead()){
										castles.get(j).getTroops().get(0).removeFromLayer();
										castles.get(j).getTroops().remove(0);
									}
								}

								if (castles.get(j).getTroops().size() == 0) {
									castles.get(j).setPlayer(castles.get(i).getPlayer());
									castleAttacks[i][j] = false;
								}
							}
						}
					}
					path_index[i][j]++;
					if ( path_index[i][j] >= paths[i][j].length )
						path_index[i][j] = 0;
                }
            }
        }

		for(int i = 0; i < castles.size() ; i++ ){
			for (int j = 0; j < castles.size() ; j++){
				if(i != j && castleAttacks[i][j]){
					for (int s = 0; s < castles.get(i).getTroops().size() ; s++) {
						if(castles.get(i).getTroops().get(s).getDestination()[0] == castles.get(j).getX() && castles.get(i).getTroops().get(s).getDestination()[1] == castles.get(j).getY())
							return;
					}
					castleAttacks[i][j] = false;
				}
			}
		}

    }

    public void productionTick(){

	}

	public void tick(Pane layer, Image image){
		castlesTick(layer, image);
        attacksTick();
        productionTick();
	}

	public boolean gameOver(){
		String p = castles.get(0).getPlayer().getName();
		for (int i = 1 ; i < castles.size() ; i++){
			if(castles.get(i).getPlayer().getName() != p)
				return false;
		}
		return true;
	}

}

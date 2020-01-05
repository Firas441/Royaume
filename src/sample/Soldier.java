package sample;

import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import sample.Castle;
import sample.Sprite;

public class Soldier extends Sprite {
    private int speed, health, damage;
    private Castle castle;
    private boolean out_of_castle;
    private double[] destination;

    public Soldier(Pane layer, Image image, double x, double y, Castle castle) {
        super(layer, image, x, y);
        this.speed = Settings.SOLDIER_SPEED;
        this.health = Settings.SOLDIER_HEALTH;
        this.damage = Settings.SOLDIER_DAMAGE;
        this.castle = castle;
        out_of_castle = false;
        destination = new double[2];
    }
    
    public void updateSoldierVisibility() {
        if(health <= 0 || damage <= 0)
            this.removeFromLayer();
        else{
            if(this.getX() == castle.getX() && this.getY() == castle.getY())
                this.removeFromLayer();
            else {
                this.removeFromLayer();
                this.addToLayer();
            }
        }
    }

    boolean move(Direction d){
        switch (d){
            case UP: if( this.y == 0 )
                         return false;
                     this.y -= Settings.CASE_HEIGHT;
                     break;
            case LEFT: if( this.x == 0 )
                           return false;
                       this.x -= Settings.CASE_WIDTH;
                       break;
            case DOWN: if( this.y == Settings.CASE_HEIGHT * (Settings.GAME_HEIGHT - 1) )
                           return false;
                       this.y += Settings.CASE_HEIGHT;
                       break;
            case RIGHT: if(this.x == Settings.CASE_WIDTH * (Settings.GAME_WIDTH - 1))
                            return false;
                        this.x += Settings.CASE_WIDTH;
                        break;
        }
        return true;
    }

    public boolean isOut(){
        return out_of_castle;
    }

    public void setDestination(double X, double Y){
        destination[0] = X;
        destination[1] = Y;
        out_of_castle = true;
    }

    public double[] getDestination(){
        return destination;
    }

    public boolean isArrivedToDestination(){
        return (destination[0] == this.x && ( destination[1] + Settings.CASE_HEIGHT == this.y || destination[1] - Settings.CASE_HEIGHT == this.y ) )
                || (destination[1] == this.y && ( destination[0] + Settings.CASE_WIDTH == this.x || destination[0] - Settings.CASE_WIDTH == this.x ) );
    }

    public boolean isDead(){
        return (health <= 0 || damage <= 0);
    }

    public void decreaseHealth(){
        health--;
    }

    public void decreaseDamage(){
        damage--;
    }

}

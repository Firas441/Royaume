package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;

public class Main extends Application {
    private Group root;
    private Scene scene;
    private Text message, timer;
    private Pane layer;
    private Player[] players = new Player[2];
    private Player myturn;
    private int pos = 0;
    private Image castleImage, castleNeutralImage, soldierImage, landImage;
    private HBox statusBar;
    private AnimationTimer gameloop;
    private Input input;
    private Castle selected;
    private Popup popup;
    private int popup_nb_troops;
    private Text popup_nb_troopsText;

    @Override
    public void start(Stage primaryStage) {
        root = new Group();
        scene = new Scene(root, Settings.SCENE_WIDTH, Settings.SCENE_HEIGHT + Settings.STATUS_BAR_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        primaryStage.setTitle("Empire");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        layer = new Pane();
        root.getChildren().add(layer);
        
		castleImage = new Image(getClass().getResource("/images/castle.png").toExternalForm(), Settings.CASTLE_WIDTH, Settings.CASTLE_HEIGHT, true, true);
		castleNeutralImage = new Image(getClass().getResource("/images/castleNeutral.png").toExternalForm(), Settings.CASTLE_WIDTH, Settings.CASTLE_HEIGHT, true, true);
		soldierImage = new Image(getClass().getResource("/images/knight.png").toExternalForm(), Settings.SOLDIER_WIDTH, Settings.SOLDIER_HEIGHT, true, true);
		landImage = new Image(getClass().getResource("/images/grass.png").toExternalForm(), Settings.CASE_WIDTH, Settings.CASE_HEIGHT, true, true);
		
        players[0] = new Player("Firas");
        players[1] = new Player("Athman");
        myturn = players[0];
        Game g = new Game(players, 2, layer, landImage);
        g.generateInitialGameLand(layer, castleImage, castleNeutralImage, soldierImage);
        g.updateSoldiersGame();

        input = new Input(scene);
        input.addListeners();
        message = new Text(); timer = new Text();

        createStatusBar();
        listenOnCastles(g, primaryStage);
        gameloop = new AnimationTimer() {
             private int i =0;
             private long lastCurrentTimePerOne = System.currentTimeMillis();
             @Override
             public void handle(long now) {
                 processInput();
                 if( !input.isPaused() && (now - lastCurrentTimePerOne) / 1000000000 >= 1){
                    lastCurrentTimePerOne = now;
                    updateStatusBar(i);
                    i++;
                    if (i == Settings.SECONDS_PER_TURN + 1){
                        pos = (pos + 1) % players.length;
                        myturn = players[pos];
                        i = 0;
                        g.tick(layer, soldierImage);
                        g.updateSoldiersGame();
                        for(Castle c : g.getCastles()) {
                            if (c.isSelected()) {
                                updateStatusBar(c);
                                break;
                            }
                        }
                    }
                    if(g.gameOver())
                        System.exit(0);
                }
             }
        };
        gameloop.start();
    }

    public void createStatusBar() {
        statusBar = new HBox(50);
        statusBar.setAlignment(Pos.CENTER);
        message.setText("                                                                      ");
        timer.setText("");
        statusBar.getChildren().addAll(message,timer);
        statusBar.getStyleClass().add("statusBar");
        statusBar.relocate(0, Settings.SCENE_HEIGHT);
        statusBar.setPrefSize(Settings.SCENE_WIDTH, Settings.STATUS_BAR_HEIGHT);
        root.getChildren().add(statusBar);
    }

    public void listenOnCastles(Game g, Stage stage) {
        for(Castle c : g.getCastles()) {
            c.getView().setOnMouseClicked(e -> {
                if( e.getButton() == MouseButton.PRIMARY ){
                    updateStatusBar(c);
                    initializeSelectedVariable(g);
                    c.setSelected(true);
                    selected = c;
                }
            });

            c.getView().setOnContextMenuRequested(e -> {
                ContextMenu contextMenu = new ContextMenu();
                MenuItem attack, addSoldier;

                if(selected.getPlayer() == myturn && c.getPlayer() != myturn){
                    attack = new MenuItem("Attack");
                    attack.setOnAction(evt -> setPopup(g, selected, c, stage));
                }
                else attack = new MenuItem();

                if(myturn == c.getPlayer()){
                    addSoldier = new MenuItem("Add Soldier");
                    addSoldier.setOnAction(evt -> {
                        c.addToProduction(Settings.SOLDIER_TIME);
                        updateStatusBar(c);
                    });
                }
                else addSoldier = new MenuItem();

                MenuItem info = new MenuItem("Info");
                info.setOnAction(evt -> updateStatusBar(c));

                contextMenu.getItems().addAll(attack, addSoldier, info);
                contextMenu.show(c.getView(), e.getScreenX(), e.getScreenY());
            });
        }
    }

    public void setPopup(Game g,Castle attacker, Castle target, Stage stage){
        popup = new Popup();
        popup.setWidth(400);
        popup.setHeight(400);

        Button plus = new Button("+"), minus = new Button("-"), confirm = new Button("confirm");
        plus.setOnAction(e -> {
            if (popup_nb_troops < attacker.getTroops().size()) {
                popup_nb_troops++;
                popup_nb_troopsText.setText(String.valueOf(popup_nb_troops));
            }
        });
        minus.setOnAction(e -> {
            if (popup_nb_troops > 0) {
                popup_nb_troops--;
                popup_nb_troopsText.setText(String.valueOf(popup_nb_troops));
            }
        });
        confirm.setOnAction(e -> {
            g.attack(attacker, target,popup_nb_troops);
            popup.hide();
        });

        popup_nb_troops = attacker.getTroops().size();
        popup_nb_troopsText = new Text(String.valueOf(popup_nb_troops));

        HBox layout = new HBox(20);
        layout.setStyle("-fx-background-color: cornsilk; -fx-padding: 10;");
        layout.getChildren().addAll(plus, popup_nb_troopsText, minus, confirm);
        popup.getContent().addAll(layout);
        popup.show(layer, stage.getX() + target.getX(), stage.getY() + target.getY());
    }

    public void updateStatusBar(Castle c) {
        statusBar.getChildren().removeAll(message, timer);
        message.setText(c.toString());
        statusBar.getChildren().addAll(message,timer);
    }

    public void updateStatusBar(int seconds) {
        statusBar.getChildren().removeAll(message,timer);
        timer.setText(Settings.SECONDS_PER_TURN - seconds + " seconds left (" + myturn.getName() + "'s turn)");
        statusBar.getChildren().addAll(message, timer);
    }

    public void processInput(){
        if (input.isExit()) {
            Platform.exit();
            System.exit(0);
        }
    }

    public void initializeSelectedVariable(Game g){
        for(Castle c: g.getCastles()){
            c.setSelected(false);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

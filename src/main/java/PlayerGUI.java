




import java.util.List;
import java.util.concurrent.CountDownLatch;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;


/* This class is the GUI for the game. Implements GUIInterface and extends Application */

/**
 *
 * @author jed_lechner
 */
public class PlayerGUI extends Application {
    
    private BorderPane root; // the root node of the board
    private GridPane grid; // the grid where the game will be implemented
        
    /* Players in the Game. Right now they are represented by circles */
    private FPController player; 
    
    private String move = null;
    
    private StackPane p1;
    private StackPane p2;
    private StackPane p3;
    private StackPane p4;
    
    
    // on off latch
    public static final CountDownLatch latch = new CountDownLatch(1);
    
    // instantiation of class
    public static PlayerGUI gui = null;
    
    /**
     * Constructor
     */
    public PlayerGUI() {
        guiStartUpTest(this);
        player = new FPController(2);
    }
    
    // waiting for gui to set up
    public static PlayerGUI waitForGUIStartUpTest()  {
        try {
            latch.await();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        return gui;

    }
    
    /**
     * 
     * @param g: Passing in parameter to initialize gui
     */
    public static void guiStartUpTest(PlayerGUI g) {
        gui = g;
        latch.countDown();
    }
    
    /**
     * 
     * @param c: The player object to set up with
     * Used for four player game
     */
    public void setPlayer(FPController c) {
        player = c;
    }
    
    /**
     *  closes the application upon call
     */
    public void stopApplication() {
        try {
            Platform.exit(); 
        } catch(Exception e) {
            e.printStackTrace();
        }
       
    }
    
    
    /**
     * 
     * @param stage: Main stage to work off
     * Entry point for javaFX Applications
     */
    @Override
    public void start(Stage stage) {
        grid = drawGrid(2);
        Scene scene = new Scene(grid, 300, 300, Color.BLUE);
        stage.setTitle("Player Board");
        
        stage.setScene(scene);
        stage.show();
        //stage.setFullScreen(true);
        
    }

    /**
     * @param move: The move to update the board.
     * Calls buildWall or movePlayer based on the message passed in
     */
    public void update(String move) {
        move = move.toLowerCase();
        int column = 0;
        int row = 0;
        String[] s = move.split(" ");
        if(move.contains("h") || move.contains("v")) {
            column = parseToInt(s[0]);
            row = parseToInt(s[1]);
            buildWall(column, row, s[2]);
        } else {
            column = parseToInt(s[0]);
            row = parseToInt(s[1]);
            movePlayer(column, row);
        }

    }
    
    /**
     * 
     * @returns the move string generated by user click
     */
    public String getMove() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                TRUMPwall();
            }
        });
        
        return move;
        
            
    }

    public void setMove(){
        move = null;
    }


    /**
     * 
     * @param column: the column to move the player
     * @param row: the column to move the player
     * builds a wall based on the c, r, and direction
     */
    private void buildWall(int column, int row, String direction) {
        final int c = revert(column);
        final int r = revert(row);
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("Building wall");
                if(direction.equals("v")) {
                    grid.add(new Rectangle(5.0, 20, Color.WHITE), c + 1, r);
                    grid.add(new Rectangle(5.0, 20, Color.WHITE), c + 1, r + 2);
                } else {
                    grid.add(new Rectangle(20, 5.0, Color.WHITE), c, r + 1);
                    grid.add(new Rectangle(20, 5.0, Color.WHITE), c+2, r +1);
                }
                player.setPlayerTurn();
            }
        });
    }


    /**
     * 
     * @param col: the column to move the player
     * @param nrow: the row to move the player
     * moves the player to correct grid
     */
    private void movePlayer(int col, int nrow) {
        final int c = revert(col);
        final int r = revert(nrow);
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int turn = player.getPlayerTurn();
                grid.getChildren().remove(player.getPlayerNode(turn));
                grid.add(returnPlayer(turn), c, r);
                player.setPlayerPosition(turn, col, nrow);
                player.setPlayerTurn();
            }
        });
          
    }

    /** 
     * @param num: The player to kick.
     * Kicks the player passed in.
     */
    public void removePlayer(int num) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                grid.getChildren().remove(returnPlayer(num));
                player.removePlayer(num);                
                player.setPlayerTurn();
            }
        });
        
    }
    
    /**
     * 
     * @param turn: Player number turn
     * @returns The player object of the player.
     */
    private StackPane returnPlayer(int turn) {
        switch(turn) {
            case 1:
                return p1;
            case 2:
                return p2;
            case 3:
                return p3;
            case 4:
                return p4;
        }
        return null;
    }
    
    // pre: none
    // post: sets a wall in the area clicked. It will cover two rows or two columns
    // depending on where you click. As of right now the click is between the squares
    private void TRUMPwall() {
        List <Node> childrens = grid.getChildren();
        
        // still need valid wall placement check
        
        // loop through and add the event to each node
        childrens.stream().forEach((node) -> {
            node.setOnMousePressed((MouseEvent event) -> {
                int row = GridPane.getRowIndex(node);
                int column = GridPane.getColumnIndex(node);
                                
                if(row % 2 == 0 && column % 2 != 0 && row != 16) { // vertical wall
                    move = convert(column) + " " +  convert(row - 1) + " v";
                    grid.add(new Rectangle(5.0, 30.0, Color.BLUE), column, row);
                    row+=2;
                    grid.add(new Rectangle(5.0, 30.0, Color.BLUE), column, row);
                    player.setPlayerTurn();
                } else if (row % 2 != 0 && column % 2 == 0 && column != 16) { // horizontal wall
                    move = convert(column) + " " + convert(row - 1) + " h";
                    grid.add(new Rectangle(30, 5.0, Color.BLUE), column, row);
                    column += 2;
                    grid.add(new Rectangle(30, 5.0, Color.BLUE), column, row);
                    player.setPlayerTurn();
                } else {
                    player.setPlayerTurn();
                    move = convert(column) + " " + convert(row);

                }
                System.out.println(move);
            });
        });
    }
    


    // method to parse an integer from a string    
    private int parseToInt(String s) {
        try { 
            return Integer.parseInt(s);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    

    /**
     * 
     * @param n: row or column to revert to 17x17 grid
     * @return correct row or column
     */
    private int revert(int n) {
        return n*2;
    }
    
    /**
     * 
     * @param column: the column number to convert
     * @return the converted column based on the 8x8 board
     */
    private int convert(int n) {
        if(n % 2 == 0) {
            return n / 2;
        } else {
            return n / 2 + 1;
        }
    }
    
    
    /**
     * 
     * @param numOfPlayers: The number of players in the game 2 or 4
     * @return The board with a 16 x 16 grid
     */
    private GridPane drawGrid(int numOfPlayers) {
        player = new FPController(numOfPlayers);
        GridPane gp = new GridPane();
        // loop through and add rectangles to create the board
        for(int i = 0; i < 17; i++) {
            for(int j = 0; j < 17; j++) {
                if(i % 2 == 0 && j%2==0) {  
                    gp.add(new Rectangle(30.0, 30.0, Color.BROWN), i , j);
                    //gp.add(new Text("(" + i + ", " + j + ")"), i, j);
                } else if(i % 2 != 0 && j % 2 == 0) { // vertical rectangles
                    gp.add(new Rectangle(5.0, 30, Color.BLACK), i, j);
                }else if(i % 2 == 0 && j % 2 != 0) { // horizontal rectangles
                    gp.add(new Rectangle(30, 5.0, Color.BLACK), i, j);
                } 
                
            }
        } 
        
        // default is 2 players, add the other two if there are more
        p1 = player.getPlayerNode(1);
        p2 = player.getPlayerNode(2);
        gp.add(p1, 8, 0);
        gp.add(p2, 8, 16);
        if(numOfPlayers == 4) {
            p3 = player.getPlayerNode(3);
            p4 = player.getPlayerNode(4);
            gp.add(p3, 0, 8);
            gp.add(p4, 16, 8);
        }
        
        
        gp.setAlignment(Pos.CENTER);
//        gp.getStylesheets().addAll(this.getClass().getResource("Layout.css").toExternalForm());
        gp.setId("board"); // set the css id of the gridpane
        
        return gp;
    }
    
    /**
     * Aligns all of the nodes to the center
     */
    private void centerAlignNodes() {
        List <Node> childrens = grid.getChildren();
        
        for(Node c : childrens) {
            GridPane.setHalignment(c, HPos.CENTER);
            GridPane.setValignment(c, VPos.CENTER);
        }
    }
    
    public static void main(String[] args) {
        Application.launch(args);
    }
    
}
   

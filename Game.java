/**
 * Class to actually run the game
 * @author Rupin Mittal and Brandon Wang
 * @version May 29, 2019
 */

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class Game extends Application
{
    //object variables
    //MainMenu mainMenu;                        //the mainmenu object
    //PauseMenu pauseMenu;                      //the pause menu
    private Environment currentEnvironment;     //the current environment being used
    private Environment gameEnvironment;        //the game environment
    private IntroEnvironment introEnvironment;  //the intro environment
    private Player player;                      //the player
    private NormalWall nWall;                   //the normal wall
    private BlueWall bWall;                     //the blue wall
    private RedWall rWall;                      //the red wall
    private GreenWall gWall;                    //the green wall
    private AnimationTimer animationTimer;      //the animation timer to run everything
    private Wall colliderWall;                  //the wall that the user is colliding into in collisions
    private Rectangle2D viewport;               //the rectangle to have offset in the game

    //variables for movement
    private boolean up, down, right, left;      //the variables for the players movement
    private boolean escape;                     //for escaping the game
    private double cameraOffset;                //the variable to offset the screen for scrolling
    private double futureXVel;                  //the future horizontal velocity
    private double futureYVel;                  //the future vertical velocity
    private double futureX;                     //future horizontal position
    private double futureY;                     //future vertical position
    private int hDirection;                     //the horizontal direction
    private int vDirection;                     //the vertical direction

    //constants
    private final double Y_ACC = 7, X_ACC = 10, FRICT_ACC = 5, GRAV_ACC = 5, JUMP_ACC = 5, MAX_VEL = 50; //the constants for movement
    private final int TILE_SIZE = 32;    //the tile size

    //variables for the actual display of the game
    private ImageView environment;           //the environment being displayed
    private Group root;                      //the Group
    private Scene scene;                     //the scene

    //methods to run class
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        initializeVariables();      //initialize all the variables
        //initialize the display
        root.getChildren().add(environment);
        root.getChildren().add(player.getImageView());
        player.setXPos(100);
        player.setYPos(448 - player.getHeight());
        primaryStage.setTitle("RGB");
        primaryStage.setScene(scene);
        primaryStage.show();

        //run controls
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {            //on keys pressed
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case UP:    up = true; break;
                    case LEFT:  left = true; break;
                    case RIGHT: right = true; break;
                    case DOWN:  down = true; break;
                    case ESCAPE:  escape = true; break;
                }
            }
        });

        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {           //on keys released
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case UP:    up = false; break;
                    case LEFT:  left = false; break;
                    case RIGHT: right = false; break;
                    case DOWN:  down = false; break;
                    case ESCAPE:  escape = false; break;
                }
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();

        //AnimationTimer to run game
        AnimationTimer timer = new AnimationTimer()
        {
            @Override
            public void handle(long now)
            {
                if(player.isAlive())
                {
                    //initialize instance variables
                    futureXVel = player.getXVel();              //get the future horizontal velocity
                    futureYVel = player.getYVel();              //get the future vertical velocity
                    futureX = player.getXPos();                 //get the future x position
                    futureY = player.getYPos();                 //get the future y position

                    //make changes to the velocity with the constants
                    futureXVel -= Math.signum(player.getXVel()) * FRICT_ACC/30;     //apply friction
                    futureYVel += GRAV_ACC/30;                                      //apply gravity
                    if(Math.abs(futureXVel) > MAX_VEL)                              //if the velocity is more than the max
                        futureXVel = MAX_VEL * Math.signum(futureXVel);             //then limit the velocity
                    //Stopping player if velocity passes 0 (friction)
                    if((int)Math.signum(futureXVel) == -1 * (int)Math.signum(player.getXVel())
                        && !left && !right)
                        futureXVel = 0;

                    //keypresses
                    if(up && (currentEnvironment.isCollision(player.getXPos(), futureY + player.getHeight() + 1) 
                            || currentEnvironment.isCollision(player.getXPos() + player.getWidth(), futureY + player.getHeight() + 1)))
                        futureYVel = -1 * JUMP_ACC;
                    if(left)
                        futureXVel -= X_ACC / 30;
                    if(right)
                        futureXVel += X_ACC / 30;

                    //update the player's future position
                    futureX += futureXVel;
                    futureY += futureYVel;

                    //collision checks
                    boolean horizontalCollision = false;
                    boolean verticalCollision = false;
                    
                    //top left corner of player
                    if(currentEnvironment.isCollision(futureX, futureY))
                    {
                        colliderWall = getColliderWall(futureX, futureY);
                        
                        //top left collision with ceiling
                        if(currentEnvironment.isCollision(player.getXPos(), futureY))
                        {
                            //wall matches
                            if(currentEnvironment.getTypeNumber(player.getXPos(), futureY) % 4 == 4)
                                colliderWall.interactCeiling(futureY);
                            else //normal wall
                                nWall.interactCeiling(futureY);
                            verticalCollision = true;
                        }
                        //top left collision with left wall
                        if(currentEnvironment.isCollision(futureX, player.getYPos()))
                        {
                            //wall mathces
                            if(currentEnvironment.getTypeNumber(futureX, player.getYPos()) % 4 == 1)
                                colliderWall.interactRight(futureX);
                            else //normal wall
                                nWall.interactRight(futureX);
                            horizontalCollision = true;
                        }
                    }
                    
                    //top right corner of player
                    if(currentEnvironment.isCollision(futureX + player.getWidth(), futureY))
                    {
                        colliderWall = getColliderWall(futureX + player.getWidth(), futureY);
                        
                        //top right collision with ceiling
                        if(currentEnvironment.isCollision(player.getXPos() + player.getWidth(), futureY))
                        {
                            //wall matches
                            if(currentEnvironment.getTypeNumber(player.getXPos() + player.getWidth(), futureY) % 4 == 4)
                                colliderWall.interactCeiling(futureY);
                            else //normal wall
                                nWall.interactCeiling(futureY);
                            verticalCollision = true;
                        }
                        //top right collision with right wall
                        if(currentEnvironment.isCollision(futureX + player.getWidth(), player.getYPos()))
                        {
                            //wall matches
                            if(currentEnvironment.getTypeNumber(futureX + player.getWidth(), player.getYPos()) % 4 == 2)
                                colliderWall.interactLeft(futureX + player.getWidth());
                            else //normal wall
                                nWall.interactLeft(futureX + player.getWidth());
                            horizontalCollision = true;
                        }
                    }
                    
                    //bottom left corner of player
                    if(currentEnvironment.isCollision(futureX, futureY + player.getHeight()))
                    {
                        colliderWall = getColliderWall(futureX, futureY + player.getHeight());
                        
                        //bottom left collision with floor
                        if(currentEnvironment.isCollision(player.getXPos(), futureY + player.getHeight()))
                        {
                            //wall matches
                            if(currentEnvironment.getTypeNumber(player.getXPos(), futureY + player.getHeight()) % 4 == 3)
                                colliderWall.interactFloor(futureY + player.getHeight());
                            else //normal wall
                                nWall.interactFloor(futureY + player.getHeight());
                            verticalCollision = true;
                        }
                        //bottom left collision with left wall
                        if(currentEnvironment.isCollision(futureX, player.getYPos() + player.getHeight() - 0.05))
                        {
                            //wall matches
                            if(currentEnvironment.getTypeNumber(futureX, player.getYPos() + player.getHeight() - 0.05) % 4 == 1)
                                colliderWall.interactRight(futureX);
                            else //normal wall
                                nWall.interactRight(futureX);
                            horizontalCollision = true;
                        }
                    }
                    
                    if(currentEnvironment.isCollision(futureX + player.getWidth(), futureY + player.getHeight()))  //bottom right corner of player
                    {
                        colliderWall = getColliderWall(futureX + player.getWidth(), futureY + player.getHeight());
                        
                        //bottom right collision with floor
                        if(currentEnvironment.isCollision(player.getXPos() + player.getWidth(), futureY + player.getHeight()))
                        {
                            //wall matches
                            if(currentEnvironment.getTypeNumber(player.getXPos() + player.getWidth(), futureY + player.getHeight()) % 4 == 3)
                                colliderWall.interactFloor(futureY + player.getHeight());
                            else //normal wall
                                nWall.interactFloor(futureY + player.getHeight());
                            verticalCollision = true;
                        }
                        //bottom right collision with right wall
                        if(currentEnvironment.isCollision(futureX + player.getWidth(), player.getYPos() + player.getHeight() - 0.05))
                        {
                            //wall matches
                            if(currentEnvironment.getTypeNumber(futureX + player.getWidth(), player.getYPos() + player.getHeight() - 0.05) % 4 == 2)
                                colliderWall.interactLeft(futureX + player.getWidth());
                            else //normal wall
                                nWall.interactLeft(futureX + player.getWidth());
                            horizontalCollision = true;
                        }
                    }
                    
                    if(currentEnvironment.isCollision(futureX, futureY + player.getHeight() / 2))  //left edge of player
                    {
                        colliderWall = getColliderWall(futureX, futureY + player.getHeight() / 2);
                        
                        //left collision with left wall
                        if(currentEnvironment.isCollision(futureX, player.getYPos() + player.getHeight() / 2))
                        {
                            //wall matches
                            if(currentEnvironment.getTypeNumber(futureX, player.getYPos() + player.getHeight() / 2) % 4 == 1)
                                colliderWall.interactRight(futureX);
                            else //normal wall
                                nWall.interactRight(futureX);
                            horizontalCollision = true;
                        }
                    }
                    
                    if(currentEnvironment.isCollision(futureX + player.getWidth(), futureY + player.getHeight() / 2))  //right edge of player
                    {
                        colliderWall = getColliderWall(futureX + player.getWidth(), futureY + player.getHeight());
                        
                        //right collision with right wall
                        if(currentEnvironment.isCollision(futureX + player.getWidth(), player.getYPos() + player.getHeight() / 2))
                        {
                            //wall matches
                            if(currentEnvironment.getTypeNumber(futureX + player.getWidth(), player.getYPos() + player.getHeight() / 2) % 4 == 2)
                                colliderWall.interactLeft(futureX + player.getWidth());
                            else //normal wall
                                nWall.interactLeft(futureX + player.getWidth());
                            horizontalCollision = true;
                        }
                    }
                    
                    if(!horizontalCollision)
                    {
                        player.setXPos(futureX);
                        player.setXVel(futureXVel);
                    }
                    if(!verticalCollision)
                    {
                        player.setYPos(futureY);
                        player.setYVel(futureYVel);
                    }

                    if(player.isAlive())
                    {
                        //update the animation that is being run
                        player.updateAnimation();

                        //check out of bounds movement
                        //if(player.getYPos() > environment.getFitHeight())   //if player is out of screen vertically
                            //player.setAliveStatus(false);                                  //kill the player
                        //if(player.getXPos() > environment.getFitWidth())    //if player is to left of sector
                        //move to next sector

                        //check if interacting with enemies
                        //if player and enemy's position is the same, kill the player

                        //do offset
                        //if(character.getX() > cameraOffset)     //if character is out of offsetrange
                            //environment.setViewport(new Rectangle2D(character.getX() - character.getFitWidth(), 0, 200, 200));  //scroll screen
                    }
                    //else
                        //show game over screen
                        //reset to sector 1
                }
                //else
                    //show game over screen
                    //reset to sector 1
            }
        };
        timer.start();
    }

    //methods
    /*
     * Method to initialize the variables in the Game class
     */
    private void initializeVariables()
    {
        //initialize main menu
        //initialize pause menu
        gameEnvironment = new Environment("test.txt", "Test.png");   //create first game environment
        //introEnvironment = new IntroEnvironment("IntroCollisionsData.txt", "IntroMap.png", "IntroForeground.png", "IntoBackground.png");    //create intro environment
        currentEnvironment = gameEnvironment;
        //player = mainMenu.getPlayer;              //initialize player
        player = new Player(new Image("player.png", 0, 50, true, false), new Image("player.png", 0, 50, true, false), new Image("player.png", 0, 50, true, false), new Image("player.png", 0, 50, true, false));
        nWall = new NormalWall(player, TILE_SIZE);  //initialize the wall variables
        bWall = new BlueWall(player, TILE_SIZE);
        gWall = new GreenWall(player, TILE_SIZE);
        rWall = new RedWall(player);
        environment = gameEnvironment.getMapImageView();                            //get environment imageview
        root = new Group();                                                         //the Group
        scene = new Scene(root);                                                    //the scene
        viewport = new Rectangle2D(0, 0, 200, 200);       //the rectangle to have offset in the game
        //environment.setViewport(viewport);                                          //set imageview to have the rectangle
        //cameraOffset = viewport.getWidth() - ((viewport.getWidth() - character.getFitWidth())/2);   //the amount to offset camera by for scrolling
    }

    /*
     * Method to get the object of the type of wall by color
     * @param nextX the next horizontal position being moved to
     * @param nextY the next vertical position being moved to
     */
    private Wall getColliderWall(double nextX, double nextY)
    {
        Wall wall;                                                //the wall object that will be returned
        int typeNumber = currentEnvironment.getTypeNumber(nextX, nextY); //get the wall number

        if((typeNumber >= 1) && (typeNumber <= 4))      //if the wall number is 1, 2, 3, 4
            wall = nWall;
        else
            if((typeNumber >= 5) && (typeNumber <= 8))      //if the wall number is 5, 6, 7, 8
                wall = rWall;
            else
                if((typeNumber >= 9) && (typeNumber <= 12))      //if the wall number is 9, 10, 11, 12
                    wall = gWall;
                else
                    wall = bWall;                                //else blue wall

        return wall;
    }

    /*
     * Method to reset the player to the start of sector 1 after they die
     */
    private void resetToSectorStart()
    {
        player.setAliveStatus(true);  //revive player
        //reset players position to start of sector 1
        //display everything
    }
}
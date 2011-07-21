package com.jefflunt.pedestrians;

import java.awt.Point;

import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.gui.AbstractComponent;
import org.newdawn.slick.gui.ComponentListener;
import org.newdawn.slick.gui.MouseOverArea;

import com.jefflunt.pedestrians.pathfinding.PedestrianPathFinder;
import com.jefflunt.pedestrians.pathfinding.PedestrianTileBasedMap;

/** The Pedestrian Simulation that handles the initial simulation state, logic, and rendering. */
public class PedestrianSim extends BasicGame implements ComponentListener {
  
  private Pedestrian[] peds;
  private PedestrianPathFinder pathFinder;
  private PedestrianTileBasedMap tileMap;
  private long nextTileMapSaveTime;
  private static Image[] images;
  
  private MouseOverArea playButton;
  private MouseOverArea pauseButton;
  
  /** Creates a new simulation.
   * 
   * @param title the title given to the application window.
   */
  public PedestrianSim(String title) {
    super(title);
    
    nextTileMapSaveTime = System.currentTimeMillis() + ConfigValues.millisBetweenTilemapSaves;
  }
  
  /** Gets the tile map used by this instance of the simulation.
   * 
   * @return a tile map that is global to this simulation.
   */
  public PedestrianTileBasedMap getGlobalMap() {
    return tileMap;
  }
  
  public void initUI(GameContainer container) {
    playButton = new MouseOverArea (container, images[0], 5, container.getHeight()-ConfigValues.HEIGHT_OF_CONTROL_PANEL+5);
    pauseButton = new MouseOverArea(container, images[1], 5, container.getHeight()-ConfigValues.HEIGHT_OF_CONTROL_PANEL+5);
    
    playButton.addListener(this);
    playButton.setAcceptingInput(false);
    
    pauseButton.addListener(this);
  }
  
  /** Restores the game state from disk, or creates a new game with default values.
   * 
   * @param container the game container
   */
  public void initGameState(GameContainer container) {
    if ((tileMap = PedestrianTileBasedMap.loadTileMap("default.tilemap")) == null) {
      tileMap = new PedestrianTileBasedMap(500, 500);
      tileMap.randomizeObstacles();
    }
    
    Pedestrian.setGlobalTileMap(tileMap);
    pathFinder = new PedestrianPathFinder(tileMap, ConfigValues.pathFindingMaxSearchDepth, true);
    
    regenerateAllPedestrians(container);
  }
  
  @Override
  public void init(GameContainer container) throws SlickException {
    container.setShowFPS(false);
    container.setMinimumLogicUpdateInterval(10);
    
    initGameState(container);
  }
  
  /** Randomly places Pedestrians around the map.
   * 
   * @param container the game container, used to pass on to the individual Pedestrians, which is then used by the Pedestrian's draw method
   */
  public void regenerateAllPedestrians(GameContainer container) {
    peds = new Pedestrian[1000];
    for (int i = 0; i < peds.length; i++) {
      Point randomOpenTile = tileMap.getRandomOpenTile();
      peds[i] = new Pedestrian((randomOpenTile.x*ConfigValues.TILE_SIZE) + (ConfigValues.TILE_SIZE/2),
                               (randomOpenTile.y*ConfigValues.TILE_SIZE) + (ConfigValues.TILE_SIZE/2),
                               container);
    }
    
    tileMap.resetAllCongestionValues();
  }

  @Override
  public void componentActivated(AbstractComponent source) {
    if (source == playButton) {
      ConfigValues.simPaused = false;
    } else if (source == pauseButton) {
      ConfigValues.simPaused = true;
    }
    
    playButton.setAcceptingInput(!playButton.isAcceptingInput());
    pauseButton.setAcceptingInput(!playButton.isAcceptingInput());
  }
  
  @Override
  public void update(GameContainer gc, int delta) throws SlickException {
    if (!ConfigValues.simPaused) {
      // if logic updates drop below 33 FPS, this will effectively slow down movement so coliision detection is still kept intact
      if (delta > 33)
        delta = 33;
      
      processInput(gc);
      movePedestrians(delta);
      saveTileMapIfNecessary();
    }
  }
  
  /** Checks to see if the tileMap has been marked as changes, and if so, saves an updated copy to disk. */
  private void saveTileMapIfNecessary() {
    if (System.currentTimeMillis() > nextTileMapSaveTime) {
      if (tileMap.isDirty()) {
        tileMap.save("default.tilemap");
        tileMap.setDirty(false);
      }
      
      nextTileMapSaveTime = System.currentTimeMillis() + ConfigValues.millisBetweenTilemapSaves;
    }
  }
  
  /** Tells the Pedestrians to do their movement.
   * 
   * @param delta the amount of time that has elapsed, in milliseconds
   */
  private void movePedestrians(int delta) {
    for (Pedestrian ped : peds) {
      if (!ped.isOnAPathSomewhere()) {
        int randX;
        int randY;
        double distancetoWanderTarget;
        
        do {
          randX = (int) (Math.random() * tileMap.getWidthInTiles());
          randY = (int) (Math.random() * tileMap.getHeightInTiles());
          distancetoWanderTarget = Math.hypot((ped.getCenterX()/ConfigValues.TILE_SIZE)-randX, (ped.getCenterY()/ConfigValues.TILE_SIZE)-randY);
        } while (distancetoWanderTarget > 40);
        
        ped.headAlongPath(pathFinder.findPath(ped, 
                                              (int) ped.getCenterX()/ConfigValues.TILE_SIZE,
                                              (int) ped.getCenterY()/ConfigValues.TILE_SIZE, 
                                              randX, 
                                              randY), 
                                              Pedestrian.WALKING_SPEED, 
                                              true);
      }
      
      ped.move(delta);
    }
  }
  
  /** Processes input from the keyboard and mouse.
   * 
   * @param gc the game container, from which you can get the Input object
   * @throws SlickException in case something goes terribly wrong, such as the Input object not being available. Maybe will likely ensue if this happens.
   */
  private void processInput(GameContainer gc) throws SlickException {
    Input input  = gc.getInput();
    
    if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
      int blockX = (input.getMouseX()+ConfigValues.viewportX) / ConfigValues.TILE_SIZE;
      int blockY = (input.getMouseY()+ConfigValues.viewportY) / ConfigValues.TILE_SIZE;
      
      if (input.getMouseY() < gc.getHeight()-ConfigValues.HEIGHT_OF_CONTROL_PANEL) {
        tileMap.permanentlyBlock(blockX, blockY);
        tileMap.setDirty(true);
      }
    }
    
    if (input.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON)) {
      int blockX = (input.getMouseX()+ConfigValues.viewportX) / ConfigValues.TILE_SIZE;
      int blockY = (input.getMouseY()+ConfigValues.viewportY) / ConfigValues.TILE_SIZE;
      
      if (input.getMouseY() < gc.getHeight()-ConfigValues.HEIGHT_OF_CONTROL_PANEL) {
        tileMap.permanentlyOpen(blockX, blockY);
        tileMap.setDirty(true);
      }
    }
    
    if (input.isKeyDown(Input.KEY_F1)) {
      if (input.isKeyDown(Input.KEY_LSHIFT) || input.isKeyDown(Input.KEY_RSHIFT)) {
        gc.setShowFPS(false);
        ConfigValues.renderSystemInfo = false;
      } else {
        gc.setShowFPS(true);
        ConfigValues.renderSystemInfo = true;
      } 
    }
    
    if (input.isKeyDown(Input.KEY_1)) {
      if (input.isKeyDown(Input.KEY_LSHIFT) || input.isKeyDown(Input.KEY_RSHIFT)) {
        regenerateAllPedestrians(gc);
        for (int x = 0; x < tileMap.getWidthInTiles(); x++) {
          for (int y = 0; y < tileMap.getHeightInTiles(); y++) {
            tileMap.getTileStateAt(x, y).clearRegisteredPedestrians();
          }
        }
      } 
    }
    
    if (input.isKeyDown(Input.KEY_C)) {
      if (input.isKeyDown(Input.KEY_LSHIFT) || input.isKeyDown(Input.KEY_RSHIFT)) {
        ConfigValues.renderCongestion = false;
      } else {
        ConfigValues.renderCongestion = true;
      } 
    }
    
    if (input.isKeyDown(Input.KEY_X)) {
      if (input.isKeyDown(Input.KEY_LSHIFT) || input.isKeyDown(Input.KEY_RSHIFT)) {
        ConfigValues.renderXRay = false;
      } else {
        ConfigValues.renderXRay = true;
      } 
    }
    
    if (input.isKeyDown(Input.KEY_N)) {
      if (input.isKeyDown(Input.KEY_LSHIFT) || input.isKeyDown(Input.KEY_RSHIFT)) {
        ConfigValues.renderPedNames = false;
      } else {
        ConfigValues.renderPedNames = true;
      } 
    }
    
    if (input.isKeyDown(Input.KEY_S)) {
      if (input.isKeyDown(Input.KEY_LSHIFT) || input.isKeyDown(Input.KEY_RSHIFT)) {
        ConfigValues.renderTurnSensors = false;
      } else {
        ConfigValues.renderTurnSensors = true;
      } 
    }
    
    if (input.isKeyDown(Input.KEY_P)) {
      if (input.isKeyDown(Input.KEY_LSHIFT) || input.isKeyDown(Input.KEY_RSHIFT)) {
        ConfigValues.renderPaths = false;
      } else {
        ConfigValues.renderPaths = true;
      }
    }
    
    if (input.isKeyDown(Input.KEY_UP)) {
      if (ConfigValues.viewportY > 0)
        ConfigValues.viewportY -= 10; 
    }
    
    if (input.isKeyDown(Input.KEY_DOWN)) {
      if (ConfigValues.viewportY < (tileMap.getHeightInTiles()*ConfigValues.TILE_SIZE)-gc.getHeight()+ConfigValues.HEIGHT_OF_CONTROL_PANEL)
        ConfigValues.viewportY += 10; 
    }
    
    if (input.isKeyDown(Input.KEY_LEFT)) {
      if (ConfigValues.viewportX > 0)
      ConfigValues.viewportX -= 10; 
    }
    
    if (input.isKeyDown(Input.KEY_RIGHT)) {
      if (ConfigValues.viewportX < (tileMap.getHeightInTiles()*ConfigValues.TILE_SIZE)-gc.getWidth())
      ConfigValues.viewportX += 10; 
    }
  }
  
  @Override
  public void render(GameContainer container, Graphics g) throws SlickException {
    if (images == null) {
      loadImageResources();
      initUI(container);
    }
    
    int startX = (ConfigValues.viewportX/ConfigValues.TILE_SIZE) - 1;
    int startY = (ConfigValues.viewportY/ConfigValues.TILE_SIZE) - 1;
    
    int stopX = startX + (container.getWidth()/ConfigValues.TILE_SIZE)  + 2;
    int stopY = startY + (container.getHeight()/ConfigValues.TILE_SIZE) + 2;
    
    for (int x = startX; x < stopX; x++) {
      for (int y = startY; y < stopY; y++) {
        if (tileMap.blocked(pathFinder, x, y)) {
          g.drawImage(images[2], (x*ConfigValues.TILE_SIZE)-ConfigValues.viewportX, (y*ConfigValues.TILE_SIZE)-ConfigValues.viewportY);
        } 
      }
    }
    
    for (Pedestrian ped : peds) {
      if ((ped.getCenterX() >= ConfigValues.viewportX) && 
          (ped.getCenterY() >= ConfigValues.viewportY) &&
          (ped.getCenterX() <= ConfigValues.viewportX+container.getWidth()) &&
          (ped.getCenterY() <= ConfigValues.viewportY+container.getHeight())) {
       
        ped.draw(ped.getCenterX()-ConfigValues.viewportX, ped.getCenterY()-ConfigValues.viewportY);
      }
    }
    
    if (ConfigValues.renderCongestion) {
      for (int x = startX; x < stopX; x++) {
        for (int y = startY; y < stopY; y++) {
           if (!tileMap.blocked(pathFinder, x, y)) {
            float congestion = tileMap.getTileStateAt(x, y).getCongestion();
            
            if (congestion > 1) {
              g.setColor(new Color(0, (int) (20*congestion), 0, 200));
              g.fillRect((x*ConfigValues.TILE_SIZE)-ConfigValues.viewportX, (y*ConfigValues.TILE_SIZE)-ConfigValues.viewportY, ConfigValues.TILE_SIZE, ConfigValues.TILE_SIZE);
            }
          }
        }
      }
    }
    
    if (ConfigValues.renderSystemInfo) {
      g.setColor(new Color(50, 50, 50, 180));
      g.fillRect(0, 0, 300, 70);
      g.setColor(Color.white);
      g.drawString("MEM total(used):   " + (Runtime.getRuntime().totalMemory()/1000000) + "(" + ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1000000) + ") MB", 10, 25);
      g.drawString("Ped. history size: " + (peds.length*peds[0].getMovementHistory().size()) + " nodes", 10, 40);
    }
    
    if (tileMap.isDirty()) {
      g.setColor(Color.blue);
      g.fillRect(0, 0, 200, 20);
      g.setColor(Color.white);
      g.drawString("Tile map changed..." , 3, 0);
    }
    
    playButton.render(container, g);
    if (pauseButton.isAcceptingInput()) {
      pauseButton.render(container, g);
    }
  }
  
  /** Gets an image at the specified resource.
   * 
   * @param index the index of the image desired
   * @return the Image requested
   */
  public static Image getImageResource(int index) {
    return images[index];
  }
  
  /** Loads the image resources needed for the game.
   * 
   * @throws SlickException if there is a problem loading one of the images
   */
  public void loadImageResources() throws SlickException {
    images = new Image[4];
    
    images[0] = new Image("images/controls/play.png");
    images[1] = new Image("images/controls/pause.png");
    images[2] = new Image("images/tiles/stone.png");
    images[3] = new Image("images/peds/ped.png");
  }

}

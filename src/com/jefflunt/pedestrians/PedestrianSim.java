package com.jefflunt.pedestrians;

import java.awt.Point;
import java.util.LinkedList;

import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

import com.jefflunt.pedestrians.pathfinding.PedestrianPathFinder;
import com.jefflunt.pedestrians.pathfinding.PedestrianTileBasedMap;

/** The Pedestrian Simulation that handles the initial simulation state, logic, and rendering. */
public class PedestrianSim extends BasicGame {
  
  private Pedestrian[] peds;
  private PedestrianPathFinder pathFinder;
  private PedestrianTileBasedMap tileMap;
  private long nextTileMapSaveTime;
  private static Image[] images;
  
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
  
  @Override
  public void init(GameContainer container) throws SlickException {
    container.setShowFPS(ConfigValues.renderSystemInfo);
    container.setMinimumLogicUpdateInterval(10);
    
    if ((tileMap = PedestrianTileBasedMap.loadTileMap("default.tilemap")) == null) {
      tileMap = new PedestrianTileBasedMap(container);
      tileMap.randomizeObstacles();
    }
    
    Pedestrian.setGlobalTileMap(tileMap);
    pathFinder = new PedestrianPathFinder(tileMap, ConfigValues.pathFindingMaxSearchDepth, true);
    
    regenerateAllPedestrians(container);
  }
  
  public void regenerateAllPedestrians(GameContainer container) {
    peds = new Pedestrian[2000];
    for (int i = 0; i < peds.length; i++) {
      Point randomOpenTile = tileMap.getRandomOpenTile();
      peds[i] = new Pedestrian((randomOpenTile.x*ConfigValues.TILE_SIZE) + (ConfigValues.TILE_SIZE/2),
                               (randomOpenTile.y*ConfigValues.TILE_SIZE) + (ConfigValues.TILE_SIZE/2),
                               container);
    }
    
    tileMap.resetAllCongestionValues();
  }

  @Override
  public void update(GameContainer gc, int delta) throws SlickException {
    // if logic updates drop below 33 FPS, this will effectively slow down movement so coliision detection is still kept intact
    if (delta > 33)
      delta = 33;
    
    Input input  = gc.getInput();
    
    if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
      int blockX = input.getMouseX() / ConfigValues.TILE_SIZE;
      int blockY = input.getMouseY() / ConfigValues.TILE_SIZE;
      
      tileMap.permanentlyBlock(blockX, blockY);
      tileMap.setDirty(true);
    }
    
    if (input.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON)) {
      int blockX = input.getMouseX() / ConfigValues.TILE_SIZE;
      int blockY = input.getMouseY() / ConfigValues.TILE_SIZE;
      
      tileMap.permanentlyOpen(blockX, blockY);
      tileMap.setDirty(true);
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
    
    if (System.currentTimeMillis() > nextTileMapSaveTime) {
      if (tileMap.isDirty()) {
        tileMap.save("default.tilemap");
        tileMap.setDirty(false);
      }
      
      nextTileMapSaveTime = System.currentTimeMillis() + ConfigValues.millisBetweenTilemapSaves;
    }
  }
  
  @Override
  public void render(GameContainer container, Graphics g) throws SlickException {
    if (images == null) {
      loadImageResources();
    }
    
    LinkedList<Pedestrian> pedsToRender = null;
    
    for (int x = 0; x < tileMap.getWidthInTiles(); x++) {
      for (int y = 0; y < tileMap.getHeightInTiles(); y++) {
        if (tileMap.blocked(pathFinder, x, y)) {
          g.drawImage(images[1], x*ConfigValues.TILE_SIZE, y*ConfigValues.TILE_SIZE-5);
        }
        
        pedsToRender = tileMap.getTileStateAt(x, y).getRegisteredPedestrians();
        
        for (Pedestrian ped : pedsToRender) {
          ped.draw(ped.getCenterX(), ped.getCenterY());
        }
        
        
      }
    }
    
    for (int x = 0; x < tileMap.getWidthInTiles(); x++) {
      for (int y = 0; y < tileMap.getHeightInTiles(); y++) {
         if (!tileMap.blocked(pathFinder, x, y)) {
          if (ConfigValues.renderCongestion) {
            float congestion = tileMap.getTileStateAt(x, y).getCongestion();
            
            if (congestion > 1) {
              g.setColor(new Color(0, (int) (20*congestion), 0, 200));
              g.fillRect(x*ConfigValues.TILE_SIZE, y*ConfigValues.TILE_SIZE, ConfigValues.TILE_SIZE, ConfigValues.TILE_SIZE);
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
    
    g.setColor(Color.darkGray);
    g.fillRect(0, container.getHeight()-ConfigValues.HEIGHT_OF_CONTROL_PANEL, container.getWidth(), ConfigValues.HEIGHT_OF_CONTROL_PANEL);
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
    images = new Image[3];
    
    images[0] = new Image("images/controls/gear.png");
    images[1] = new Image("images/tiles/stone.png");
    images[2] = new Image("images/peds/ped.png");
  }

}

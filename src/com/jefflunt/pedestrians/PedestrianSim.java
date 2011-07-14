package com.jefflunt.pedestrians;

import java.awt.Point;

import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

import com.jefflunt.pedestrians.pathfinding.PedestrianPathFinder;
import com.jefflunt.pedestrians.pathfinding.PedestrianTileBasedMap;

/** The Pedestrian Simulation that handles the initial simulation state, logic, and rendering. */
public class PedestrianSim extends BasicGame {
  
  private Pedestrian[] peds;
  private PedestrianPathFinder pathFinder;
  private static PedestrianTileBasedMap tileMap;
  
  /** Creates a new simulation.
   * 
   * @param title the title given to the application window.
   */
  public PedestrianSim(String title) {
    super(title);
  }
  
  /** Gets the tile map used by this instance of the simulation.
   * 
   * @return a tile map that is global to this simulation.
   */
  public static PedestrianTileBasedMap getGlobalMap() {
    return tileMap;
  }
  
  @Override
  public void init(GameContainer container) throws SlickException {
    container.setShowFPS(ConfigValues.renderSystemInfo);
    tileMap = new PedestrianTileBasedMap(container);
    Pedestrian.setGlobalTileMap(tileMap);
    pathFinder = new PedestrianPathFinder(tileMap, ConfigValues.MAX_SEARCH_DEPTH, true);
    
    tileMap.randomizeObstacles();
    
    peds = new Pedestrian[100];
    for (int i = 0; i < peds.length; i++) {
      Point randomOpenTile = tileMap.getRandomOpenTile();
      peds[i] = new Pedestrian((randomOpenTile.x*ConfigValues.TILE_SIZE) + (ConfigValues.TILE_SIZE/2),
                               (randomOpenTile.y*ConfigValues.TILE_SIZE) + (ConfigValues.TILE_SIZE/2),
                               container);
    }
  }

  @Override
  public void update(GameContainer gc, int delta) throws SlickException {
    Input input  = gc.getInput();
    
    // TODO: Add a key in here to turn congestion rendering on/off
    
    if (input.isKeyDown(Input.KEY_F1)) {
      if (input.isKeyDown(Input.KEY_LSHIFT) || input.isKeyDown(Input.KEY_RSHIFT)) {
        gc.setShowFPS(false);
        ConfigValues.renderSystemInfo = false;
      } else {
        gc.setShowFPS(true);
        ConfigValues.renderSystemInfo = true;
      } 
    }
    
    if (input.isKeyDown(Input.KEY_C)) {
      if (input.isKeyDown(Input.KEY_LSHIFT) || input.isKeyDown(Input.KEY_RSHIFT)) {
        ConfigValues.renderCongestion = false;
      } else {
        ConfigValues.renderCongestion = true;
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
    
    if (input.isKeyDown(Input.KEY_O)) {
      int blockX = input.getMouseX() / ConfigValues.TILE_SIZE;
      int blockY = input.getMouseY() / ConfigValues.TILE_SIZE;
      
      if (input.isKeyDown(Input.KEY_LSHIFT) || input.isKeyDown(Input.KEY_RSHIFT)) {
        tileMap.permanentlyBlock(blockX, blockY);
      } else {
        tileMap.permanentlyOpen(blockX, blockY);
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
  }
  
  @Override
  public void render(GameContainer container, Graphics g) throws SlickException {
    for (int x = 0; x < tileMap.getWidthInTiles(); x++) {
      for (int y = 0; y < tileMap.getHeightInTiles(); y++) {
        if (tileMap.blocked(pathFinder, x, y)) {
          g.setColor(Color.red);
          g.fillRect(x*ConfigValues.TILE_SIZE, y*ConfigValues.TILE_SIZE, ConfigValues.TILE_SIZE, ConfigValues.TILE_SIZE);
        } else {
          if (ConfigValues.renderCongestion) {
            float congestion = tileMap.getTileStateAt(x, y).getCongestion();
            
            if (congestion > 1) {
              g.setColor(new Color(0, (int) (20*congestion), 0));
              g.fillRect(x*ConfigValues.TILE_SIZE, y*ConfigValues.TILE_SIZE, ConfigValues.TILE_SIZE, ConfigValues.TILE_SIZE);
            }
          }
        }
      }
    }
    
    for (Pedestrian ped : peds) {
      ped.draw(ped.getCenterX(), ped.getCenterY());
    }
    
    if (ConfigValues.renderSystemInfo) {
      g.setColor(Color.white);
      g.drawString("MEM total(used): " + (Runtime.getRuntime().totalMemory()/1000000) + "(" + ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1000000) + ") MB", 10, 25);
    }
  }

}

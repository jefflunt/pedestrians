package com.jefflunt.pedestrians;

import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.pathfinding.PathFindingContext;

import com.jefflunt.pedestrians.pathfinding.PedestrianPathFinder;
import com.jefflunt.pedestrians.pathfinding.PedestrianTileBasedMap;

/** The Pedestrian Simulation that handles logic, rendering, etc. */
public class PedestrianSim extends BasicGame implements PedestrianTileBasedMap {
  
  private Pedestrian[] peds;
  private boolean blockingMap[][];
  private PedestrianPathFinder pathFinder;
  
  private static PedestrianTileBasedMap tileMap;
  
  public PedestrianSim(String title) {
    super(title);
    tileMap = this;
  }
  
  public static PedestrianTileBasedMap getGlobalMap() {
    return tileMap;
  }
  
  @Override
  public void init(GameContainer container) throws SlickException {
    blockingMap = new boolean[container.getWidth()/ConfigValues.TILE_SIZE][container.getHeight()/ConfigValues.TILE_SIZE];
    pathFinder = new PedestrianPathFinder(this, ConfigValues.MAX_SEARCH_DEPTH, true);
    randomizeObstacles();
    peds = new Pedestrian[100];
    int randomBlockX;
    int randomBlockY;
    for (int i = 0; i < peds.length; i++) {
      do {
        randomBlockX = (int)(Math.random()*tileMap.getWidthInTiles());
        randomBlockY = (int)(Math.random()*tileMap.getHeightInTiles());
        peds[i] = new Pedestrian((randomBlockX*ConfigValues.TILE_SIZE) + (ConfigValues.TILE_SIZE/2),
                                 (randomBlockY*ConfigValues.TILE_SIZE) + (ConfigValues.TILE_SIZE/2),
                                 container);
      } while (blockingMap[randomBlockX][randomBlockY] == true);
    }
  }

  @Override
  public void update(GameContainer gc, int delta) throws SlickException {
    Input input  = gc.getInput();
    if (input.isKeyDown(Input.KEY_P)) {
      for (Pedestrian ped : peds) {
        ped.pause();
      }
    } else if (input.isKeyDown(Input.KEY_R)) {
      randomizeObstacles();
    } else if (input.isKeyDown(Input.KEY_SPACE)) {
      for (Pedestrian ped : peds) {
        ped.resume(Pedestrian.WALKING_SPEED);
      }
    } else if (input.isKeyDown(Input.KEY_O)) {
      int blockX = input.getMouseX() / ConfigValues.TILE_SIZE;
      int blockY = input.getMouseY() / ConfigValues.TILE_SIZE;
      blockingMap[blockX][blockY] = true;
    } else if (input.isKeyDown(Input.KEY_C)) {
      int blockX = input.getMouseX() / ConfigValues.TILE_SIZE;
      int blockY = input.getMouseY() / ConfigValues.TILE_SIZE;
      blockingMap[blockX][blockY] = false;
    }
    
    for (Pedestrian ped : peds) {
      if (!ped.isOnAPathSomewhere()) {
        int randX;
        int randY;
        double distancetoWanderTarget;
        
        do {
          randX = (int) (Math.random() * getWidthInTiles());
          randY = (int) (Math.random() * getHeightInTiles());
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
    g.setColor(Color.red);
    for (int x = 0; x < blockingMap.length; x++) {
      for (int y = 0; y < blockingMap[0].length; y++) {
        if (blockingMap[x][y] == true)
          g.fillRect(x*ConfigValues.TILE_SIZE, y*ConfigValues.TILE_SIZE, ConfigValues.TILE_SIZE, ConfigValues.TILE_SIZE);
      }
    }
    
    for (Pedestrian ped : peds) {
      ped.draw(ped.getCenterX(), ped.getCenterY());
    }
      
    g.drawString("MEM total(used): " + (Runtime.getRuntime().totalMemory()/1000000) + "(" + ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1000000) + ") MB", 10, 25);
  }
  
  public void randomizeObstacles() {
    blockingMap = new boolean[getWidthInTiles()][getHeightInTiles()];
    for (int x = 4; x < blockingMap.length-4; x++) {
      for (int y = 4; y < blockingMap[0].length-4; y++) {
        int blockSize = ((int) (Math.random() * 20));
        switch (blockSize) {
          case 1:
            blockingMap[x][y] = true;
            break;
          case 2:
            blockingMap[x][y] = true;
            blockingMap[x+1][y] = true;
            blockingMap[x][y+1] = true;
            blockingMap[x+1][y+1] = true;
            break;
          case 3:
            blockingMap[x][y] = true;
            blockingMap[x+1][y] = true;
            blockingMap[x][y+1] = true;
            blockingMap[x+1][y+1] = true;
            blockingMap[x+2][y] = true;
            blockingMap[x+2][y+1] = true;
            blockingMap[x+2][y+2] = true;
            blockingMap[x+1][y+2] = true;
            blockingMap[x][y+2] = true;
            break;
        }
      }
    }
  }
  
  @Override
  public boolean blocked(PathFindingContext context, int x, int y) {
    if ((x < 0) || (y < 0) || (x >= getWidthInTiles()) || (y >= getHeightInTiles()))
      return true;
    else
      return blockingMap[x][y];
  }
  
  @Override
  public boolean diagonallyBlocked(PathFindingContext context, int currentX, int currentY, int evalX, int evalY) {
    return (blocked(context, currentX, evalY) && blocked(context, evalX, currentY));
  }

  @Override
  public float getCost(PathFindingContext context, int x, int y) {
    return 1;
  }

  @Override
  public int getHeightInTiles() {
    return blockingMap[0].length;
  }

  @Override
  public int getWidthInTiles() {
    return blockingMap.length;
  }

  @Override
  public void pathFinderVisited(int arg0, int arg1) {
    // Used for debugging new heuristics
  }

}

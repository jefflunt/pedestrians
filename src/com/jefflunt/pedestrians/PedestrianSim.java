package com.jefflunt.pedestrians;

import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

/** The Pedestrian Simulation that handles logic, rendering, etc. */
public class PedestrianSim extends BasicGame implements TileBasedMap {
  
  private Pedestrian simon;
  private boolean blockingMap[][];
  private AStarPathFinder pathFinder;
  
  public PedestrianSim(String title) {
    super(title);
  }
  
  @Override
  public void init(GameContainer container) throws SlickException {
    simon = new Pedestrian(400, 300, container);
    blockingMap = new boolean[container.getWidth()/ConfigValues.TILE_SIZE][container.getHeight()/ConfigValues.TILE_SIZE];
    pathFinder = new AStarPathFinder(this, ConfigValues.MAX_SEARCH_DEPTH, true);
  }

  @Override
  public void update(GameContainer gc, int delta) throws SlickException {
    Input input  = gc.getInput();
    if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
      if ((input.isKeyDown(Input.KEY_LALT)) || input.isKeyDown(Input.KEY_RALT)) {
        Path newPath = pathFinder.findPath(simon, (int)simon.getCenterX()/ConfigValues.TILE_SIZE, (int)simon.getCenterY()/ConfigValues.TILE_SIZE, input.getMouseX()/ConfigValues.TILE_SIZE,  input.getMouseY()/ConfigValues.TILE_SIZE);
        simon.headAlongPath(newPath, Pedestrian.WALKING_SPEED, true);
      } else {
        simon.addStepToPath(input.getMouseX(), input.getMouseY());
      }
      
      if (simon.getSpeed() == Pedestrian.STOPPED)
        simon.changeSpeedTo(Pedestrian.WALKING_SPEED);
    } else if (input.isKeyDown(Input.KEY_P)) {
      simon.pause();
    } else if (input.isKeyDown(Input.KEY_R)) {
      randomizeObstacles();
    } else if (input.isKeyDown(Input.KEY_S)) {
      simon.stop();
    } else if (input.isKeyDown(Input.KEY_SPACE)) {
      simon.resume(Pedestrian.WALKING_SPEED);
    } else if (input.isKeyDown(Input.KEY_LSHIFT) || input.isKeyDown(Input.KEY_RSHIFT)) {
      simon.changeSpeedTo(Pedestrian.RUNNING_SPEED);
    } else if (input.isKeyDown(Input.KEY_O)) {
      int blockX = input.getMouseX() / ConfigValues.TILE_SIZE;
      int blockY = input.getMouseY() / ConfigValues.TILE_SIZE;
      
      blockingMap[blockX][blockY] = true;
    } else if (input.isKeyDown(Input.KEY_C)) {
      int blockX = input.getMouseX() / ConfigValues.TILE_SIZE;
      int blockY = input.getMouseY() / ConfigValues.TILE_SIZE;
      
      blockingMap[blockX][blockY] = false;
    }
    
    simon.move(delta);
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
    
    simon.draw(simon.getCenterX(), simon.getCenterY());
    
    g.drawString("p: " + (int)simon.getCenterX() + ", " + (int)simon.getCenterY() + 
                 " -> " + (int)simon.getTargetX() + ", " + (int)simon.getTargetY(), 10, 30); 
    g.drawString("@ " + simon.getSpeed() + " px/sec", 10, 45);
    g.drawString("direction: " + simon.getDirection(), 10, 60);
    
    if (simon.getTargetPath() == null)
      g.drawString("Path segment/length: 0/0", 10, 75);
    else
      g.drawString("Path segment/length: " + simon.getTargetPathIndex() + "/" + simon.getTargetPath().getLength(), 10, 75);
    
    g.drawString("MEM total(used): " + (Runtime.getRuntime().totalMemory()/1000000) + "(" + ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1000000) + ") MB", 10, 90);
  }
  
  public void randomizeObstacles() {
    for (int x = 0; x < blockingMap.length; x++) {
      for (int y = 0; y < blockingMap[0].length; y++) {
        blockingMap[x][y] = ((int) (Math.random() * 2)) == 1;
      }
    }
  }

  @Override
  public boolean blocked(PathFindingContext context, int x, int y) {
    if ((x < 0) || (y < 0) || (x > getWidthInTiles()) || (y > getHeightInTiles()))
      return true;
    else
      return blockingMap[x][y];
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

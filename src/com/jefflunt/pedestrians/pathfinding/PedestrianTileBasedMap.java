package com.jefflunt.pedestrians.pathfinding;

import java.awt.Point;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.util.pathfinding.PathFindingContext;

import com.jefflunt.pedestrians.ConfigValues;

public class PedestrianTileBasedMap implements ExtendedTileBasedMap {
  
  /** The array of integers that store the current blocking state of this tile map. */
  private TileState blockingMap[][];

  /** Constructs a new PedestrianTileBasedMap with no obstacles, and no Pedestrians.
   * 
   * @param container the GameContainer used by this map. This is used to set the number of tiles, in the map.
   */
  public PedestrianTileBasedMap(GameContainer container) {
    this(container.getWidth()/ConfigValues.TILE_SIZE, container.getHeight()/ConfigValues.TILE_SIZE);
  }
  
  public PedestrianTileBasedMap(int widthInTiles, int heightInTiles) {
    blockingMap = new TileState[widthInTiles][heightInTiles];
    for (int x = 0; x < blockingMap.length; x++) {
      for (int y = 0; y < blockingMap[0].length; y++) {
        blockingMap[x][y] = new TileState();
      }
    }
  }
  
  /** Gets the coordinates of the center of the tile at (x, y).
   * 
   * @param x the x-coordinate of the tile in question
   * @param y the y-coordinate of the tile in question
   * @return a Point containing the (x, y) coordinates of the center point of the block in question
   */
  public Point getCenterOfTileAt(int x, int y) {
    return (new Point((x*ConfigValues.TILE_SIZE) + (ConfigValues.TILE_SIZE/2), (y*ConfigValues.TILE_SIZE) + (ConfigValues.TILE_SIZE/2)));
  }
  
  /** Gets the TileState object of the tile at (x, y).
   * 
   * @param x the x-coordinate of the tile in question
   * @param y the y-coordinate of the tile in question
   * @return the TileState object of the tile at (x, y)
   */
  public TileState getTileStateAt(int x, int y) {
    if ((x < 0) || (y < 0) || (x >= getWidthInTiles()) || (y >= getHeightInTiles())) {
      return (new TileState());
    } else {
      return blockingMap[x][y];
    }
  }
  
  /** Gets a randomly chosen open tile.
   * NOTE: If the tile map is almost completely closed, this method can take a long time to return.
   * 
   * @return the (x, y) coordinates of a an open (non-blocked) tile.
   */
  public Point getRandomOpenTile() {
    int randomBlockX;
    int randomBlockY;
    do {
      randomBlockX = (int)(Math.random()*getWidthInTiles());
      randomBlockY = (int)(Math.random()*getHeightInTiles());
    } while (blockingMap[randomBlockX][randomBlockY].isBlocked());
    
    return (new Point(randomBlockX, randomBlockY));
  }

  @Override
  public boolean blocked(PathFindingContext context, int x, int y) {
    if ((x < 0) || (y < 0) || (x >= getWidthInTiles()) || (y >= getHeightInTiles()))
      return true;
    else
      return (blockingMap[x][y].isBlocked());
  }
  
  @Override
  public boolean diagonallyBlocked(PathFindingContext context, int currentX, int currentY, int evalX, int evalY) {
    return (blocked(context, currentX, evalY) && blocked(context, evalX, currentY));
  }
  
  /** Allows you to permanently block a given tile (i.e. add an obstacle).
   * 
   * @param blockX the x-coordinate of the block you want to claim
   * @param blockY the y-coordinate of the block you want to claim
   * @return true if this block was already permanently blocked, false otherwise
   */
  public boolean permanentlyBlock(int x, int y) {
    boolean alreadyBlocked = blockingMap[x][y].isBlocked();
    
    if (!alreadyBlocked) {
      blockingMap[x][y].setIsBlocked(true);
    }
    
    return alreadyBlocked;
  }
  
  /** Allows you to permanently open a given tile (i.e. to clear any obstacle).
   * 
   * @param blockX the x-coordinate of the block you want to open
   * @param blockY the y-coordinate of the block you want to open
   * @return true if the specified block was already open, false otherwise
   */
  public boolean permanentlyOpen(int x, int y) {
    boolean alreadyOpen = !blockingMap[x][y].isBlocked();
    
    if (!alreadyOpen) {
      blockingMap[x][y].setIsBlocked(false);
    }
    
    return alreadyOpen;
  }
  
  public void randomizeObstacles() {
    for (int x = 0; x < blockingMap.length; x++) {
      for (int y = 0; y < blockingMap[0].length; y++) {
        blockingMap[x][y].setIsBlocked(false);
      }
    }
    
    for (int x = 4; x < blockingMap.length-4; x++) {
      for (int y = 4; y < blockingMap[0].length-4; y++) {
        int blockSize = ((int) (Math.random() * 20));
        switch (blockSize) {
          case 1:
            blockingMap[x][y].setIsBlocked(true);
            break;
          case 2:
            blockingMap[x][y].setIsBlocked(true);
            blockingMap[x+1][y].setIsBlocked(true);
            blockingMap[x][y+1].setIsBlocked(true);
            blockingMap[x+1][y+1].setIsBlocked(true);
            break;
          case 3:
            blockingMap[x][y].setIsBlocked(true);
            blockingMap[x+1][y].setIsBlocked(true);
            blockingMap[x][y+1].setIsBlocked(true);
            blockingMap[x+1][y+1].setIsBlocked(true);
            blockingMap[x+2][y].setIsBlocked(true);
            blockingMap[x+2][y+1].setIsBlocked(true);
            blockingMap[x+2][y+2].setIsBlocked(true);
            blockingMap[x+1][y+2].setIsBlocked(true);
            blockingMap[x][y+2].setIsBlocked(true);
            break;
        }
      }
    }
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
  public float getCost(PathFindingContext context, int x, int y) {
    //return 1;
    return getTileStateAt(x, y).getCongestion();
  }
  
  @Override
  public void pathFinderVisited(int arg0, int arg1) {
    // Used for debugging new heuristics
  }

}

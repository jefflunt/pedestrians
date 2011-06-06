package com.jefflunt.pedestrians;

import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

public class PathfindingMap implements TileBasedMap {

  public static final int BLOCK_WIDTH = 10;
  public static final int BLOCK_HEIGHT = 10;
  
  boolean[][] blockMap;
  
  public PathfindingMap(int width, int height) throws RuntimeException {
    if ((width % BLOCK_WIDTH != 0) || (height % BLOCK_HEIGHT != 0)) {
      throw new RuntimeException("The width and height of the game canvas must be a multiple of the block size.");
    }
    
    blockMap = new boolean[width/BLOCK_WIDTH][height/BLOCK_HEIGHT];
  }
  
  // TODO Figure out the strategy for registering Pedestrians with the block map
  
  @Override
  public boolean blocked(PathFindingContext arg0, int arg1, int arg2) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public float getCost(PathFindingContext arg0, int arg1, int arg2) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getHeightInTiles() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getWidthInTiles() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void pathFinderVisited(int arg0, int arg1) {
    // TODO Auto-generated method stub
    
  }

}

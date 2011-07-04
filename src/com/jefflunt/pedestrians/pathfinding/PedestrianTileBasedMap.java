package com.jefflunt.pedestrians.pathfinding;

import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

import com.jefflunt.pedestrians.Pedestrian;

public interface PedestrianTileBasedMap extends TileBasedMap {

  public boolean diagonallyBlocked(PathFindingContext context, int currentX, int currentY, int evalX, int evalY);
  public boolean temporarilyBlock(Pedestrian ped, int blockX, int blockY);
  
}

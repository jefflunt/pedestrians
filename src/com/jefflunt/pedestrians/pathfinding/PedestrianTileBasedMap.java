package com.jefflunt.pedestrians.pathfinding;

import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

public interface PedestrianTileBasedMap extends TileBasedMap {

  public boolean diagonallyBlocked(PathFindingContext context, int currentX, int currentY, int evalX, int evalY);
  
}

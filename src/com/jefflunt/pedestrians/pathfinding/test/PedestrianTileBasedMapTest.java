package com.jefflunt.pedestrians.pathfinding.test;

import static org.junit.Assert.*;

import java.awt.Point;

import org.junit.Before;
import org.junit.Test;

import com.jefflunt.pedestrians.pathfinding.PedestrianTileBasedMap;

public class PedestrianTileBasedMapTest {

  private PedestrianTileBasedMap pedMap;
  
  @Before
  public void setUp() {
    pedMap = new PedestrianTileBasedMap(100, 100);
  }
  
  @Test
  public void theGetCenterOfTileAtMethodReturnsTheProperCenterPoint() {
    Point centerOfTile = pedMap.getCenterOfTileAt(5, 6);
    
    assertEquals(110, centerOfTile.x);
    assertEquals(130, centerOfTile.y);
  }
  
}

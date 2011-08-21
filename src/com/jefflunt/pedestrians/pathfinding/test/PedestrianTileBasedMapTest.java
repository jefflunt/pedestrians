package com.jefflunt.pedestrians.pathfinding.test;

import static org.junit.Assert.*;

import java.awt.Point;
import java.io.File;

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
  
  @Test
  public void savingAMapToDiskWorks() {
    File tileMapFile = new File("test.tilemap");
    assertTrue(pedMap.save(tileMapFile.getName()));
    
    tileMapFile.delete();
  }
  
  @Test
  public void loadingAMapFromDiskWorks() {
    File tileMapFile = new File("test.tilemap");
    pedMap.save(tileMapFile.getName());
    
    pedMap = PedestrianTileBasedMap.loadTileMap("test.tilemap");
    assertNotNull(pedMap);
    
    tileMapFile.delete();
  }
  
}

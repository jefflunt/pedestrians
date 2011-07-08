package com.jefflunt.pedestrians.pathfinding.test;

import static org.junit.Assert.*;

import java.awt.Point;

import org.junit.Before;
import org.junit.Test;

import com.jefflunt.pedestrians.ConfigValues;
import com.jefflunt.pedestrians.Pedestrian;
import com.jefflunt.pedestrians.pathfinding.PedestrianTileBasedMap;
import com.jefflunt.pedestrians.physics.Vector;


public class PedestrianTileBasedMapTest {

  private PedestrianTileBasedMap pedMap;
  
  @Before
  public void setUp() {
    pedMap = new PedestrianTileBasedMap(100, 100);
  }
  
  @Test
  public void maginitudeProximityCalculationGetsTheProperValue() {
    assertEquals(32.7680, pedMap.getTilePushMagnitude(7), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(2.6700, pedMap.getPedestrianPushMagnitude(5), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void theGetCenterOfTileAtMethodReturnsTheProperCenterPoint() {
    Point centerOfTile = pedMap.getCenterOfTileAt(5, 6);
    
    assertEquals(55, centerOfTile.x);
    assertEquals(65, centerOfTile.y);
  }
  
  @Test
  public void tilePushVectorsAreCalculatedCorrectly() {
    // This is a fictitious test, because it places the Pedestrian inside of a blocked tile, but it's fine for demonstration of the calculation
    Pedestrian simon = new Pedestrian(55, 50, null);
    
    pedMap.permanentlyBlock(5, 5);
    // The Pedestrian is within tile (5, 5), and it is blocked, meaning it should push back on the pedestrian.
    Vector tilePushVector = pedMap.pushVectorFromTile(simon, 5, 5);
    
    assertEquals(204.8000, tilePushVector.getMagnitude(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals((3*Math.PI)/2, tilePushVector.getDirection(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
}

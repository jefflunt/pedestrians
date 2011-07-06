package com.jefflunt.pedestrians.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.newdawn.slick.util.pathfinding.Path;

import com.jefflunt.pedestrians.ConfigValues;
import com.jefflunt.pedestrians.Pedestrian;

public class PedestrianTest {

  private Pedestrian simon;
  
  @Before
  public void setUp() {
    simon = new Pedestrian(0, 0, null);
  }
  
  @Test
  public void aPedestrianKnowsTheBasicDirectionInWhichTheyAreHeading() {
    simon.headDirection(ConfigValues.UP, Pedestrian.WALKING_SPEED);
    assertEquals(ConfigValues.UP, simon.getPrimaryDirection());
    
    simon.headDirection(ConfigValues.DOWN, Pedestrian.WALKING_SPEED);
    assertEquals(ConfigValues.DOWN, simon.getPrimaryDirection());
    
    simon.headDirection(ConfigValues.LEFT, Pedestrian.WALKING_SPEED);
    assertEquals(ConfigValues.LEFT, simon.getPrimaryDirection());
    
    simon.headDirection(ConfigValues.RIGHT, Pedestrian.WALKING_SPEED);
    assertEquals(ConfigValues.RIGHT, simon.getPrimaryDirection());
    
    simon.headDirection(ConfigValues.UP_LEFT, Pedestrian.WALKING_SPEED);
    assertEquals(ConfigValues.UP_LEFT, simon.getPrimaryDirection());
    
    simon.headDirection(ConfigValues.UP_RIGHT, Pedestrian.WALKING_SPEED);
    assertEquals(ConfigValues.UP_RIGHT, simon.getPrimaryDirection());
    
    simon.headDirection(ConfigValues.DOWN_LEFT, Pedestrian.WALKING_SPEED);
    assertEquals(ConfigValues.DOWN_LEFT, simon.getPrimaryDirection());
    
    simon.headDirection(ConfigValues.DOWN_RIGHT, Pedestrian.WALKING_SPEED);
    assertEquals(ConfigValues.DOWN_RIGHT, simon.getPrimaryDirection());
  }
  
  @Test
  public void expandingThePathThatAPedestrianFollowsWorksAsExpected() {
    Path compressedPath = new Path();
    compressedPath.appendStep(1, 1);
    simon.headAlongPath(compressedPath, Pedestrian.WALKING_SPEED, true);
    assertEquals(15, simon.getTargetX(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(15, simon.getTargetY(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void addingAStepToThePathWorks() {
    assertFalse(simon.isOnAPathSomewhere());
    simon.addStepToPath(100, 0);  // distance of 100 to end of path
    assertTrue(simon.isOnAPathSomewhere());
    assertEquals(100, simon.distanceToEndOfPath(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(1, simon.getNumberOfPointsInPath());
    
    simon.addStepToPath(100, 75); // distance of 175 (total) to end of path
    assertEquals(2, simon.getNumberOfPointsInPath());
    assertEquals(175, simon.distanceToEndOfPath(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void tellingAPedestrianToHeadAlongAPathCorrectlySetsUpTheirFirstStepAlongThePath() {
    Path simonsPath = new Path();
    simonsPath.appendStep(100, 50);
    simonsPath.appendStep(200, 250);
    
    simon.headAlongPath(simonsPath, Pedestrian.WALKING_SPEED);
    assertEquals(100, simon.getTargetX(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(50,  simon.getTargetY(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void aPedestrianCanCalculateTheirCurrentEstimatedDistanceFromWhereTheyAreToTheEndOfTheirPath() {
    Path simonsPath = new Path();
    simonsPath.appendStep(100, 0);    // distance of 100, along the x-axis
    simonsPath.appendStep(100, 75);   // distance of 75, along the y-axis
        
    simon.headAlongPath(simonsPath, ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(2, simon.getNumberOfPointsInPath());
    assertEquals(175, simon.distanceToEndOfPath(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void aPedestrianKnowsIfTheyAreOnAPath() {
    assertFalse(simon.isOnAPathSomewhere());
    assertTrue(simon.hasReachedDestination());
    
    Path simonsPath = new Path();
    simonsPath.appendStep(100, 100);
    simon.headAlongPath(simonsPath, Pedestrian.WALKING_SPEED);
    
    assertTrue(simon.isOnAPathSomewhere());
    assertFalse(simon.hasReachedDestination());
  }
  
  @Test
  public void tellingAPedestrianToChangeSpeedWhenTheyHaveAlreadyArrivedAtTheirDestinationHasNoEffectOnTheirSpeed() {
    simon.changeSpeedTo(Pedestrian.WALKING_SPEED);
    assertTrue(simon.hasReachedDestination());
    assertEquals(0, simon.getSpeed(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
    
  @Test public void aPedestrianCanAccuratelyCalculateTheirDistanceToAGivenPointAsTheBirdFlies() {
    assertEquals(50, simon.distanceToPoint(50, 0), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
    
  @Test public void tellingAPedestrianToChangeSpeedWorks() {
    simon.headToward(100, 100, Pedestrian.WALKING_SPEED);
    assertEquals(Pedestrian.WALKING_SPEED, simon.getSpeed(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    
    simon.changeSpeedTo(Pedestrian.RUNNING_SPEED);
    assertEquals(Pedestrian.RUNNING_SPEED, simon.getSpeed(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test public void tellingAPedestrianToStopWillSetTheirSpeedToZeroAndMakesThemBelieveThatTheyHaveArrivedAtTheirDestination() {
    simon.headToward(50, 50, Pedestrian.WALKING_SPEED);
    simon.stop();
    assertEquals(0, simon.getSpeed(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertTrue(simon.hasReachedDestination());
  }
  
  @Test
  public void tellingAPedestrianToHeadTowardAParticularDestinationCalculatesTheDirectionAndDestinationCorrectly() {
    // 45-degree angle
    simon.headToward(50, 50, Pedestrian.WALKING_SPEED);
    assertEquals(50, simon.getTargetX(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(50,  simon.getTargetY(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0.7853, simon.getDirection(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    
    // 315-degree angle (the opposite direction of 45 degrees)
    simon.headToward(-50, -50, Pedestrian.WALKING_SPEED);
    assertEquals(-50, simon.getTargetX(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(-50, simon.getTargetY(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(3.9269, simon.getDirection(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    
    // RIGHT
    simon.headToward(50, 0, Pedestrian.WALKING_SPEED);
    assertEquals(50, simon.getTargetX(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0,  simon.getTargetY(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0, simon.getDirection(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
     
    // UP
    simon.headToward(0, -50, Pedestrian.WALKING_SPEED);
    assertEquals(0,   simon.getTargetX(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(-50, simon.getTargetY(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(4.7124, simon.getDirection(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
}

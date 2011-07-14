package com.jefflunt.pedestrians.test;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;

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
  public void calcualtionOfRelativePointsFromCenterWorksAsExcpected() {
    Point2D.Float relativePoint = simon.getRelativePointFromCenter(1, 1);
    assertEquals( 1, relativePoint.x, ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals( 1, relativePoint.y, ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void expandingThePathThatAPedestrianFollowsWorksAsExpected() {
    Path compressedPath = new Path();
    compressedPath.appendStep(1, 1);
    simon.headAlongPath(compressedPath, Pedestrian.WALKING_SPEED, true);
    assertEquals(30, simon.getTargetX(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(30, simon.getTargetY(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
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
  
}

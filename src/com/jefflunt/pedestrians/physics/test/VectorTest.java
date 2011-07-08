package com.jefflunt.pedestrians.physics.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.jefflunt.pedestrians.ConfigValues;
import com.jefflunt.pedestrians.physics.Vector;

public class VectorTest {

  private Vector testVector;
  
  @Before
  public void setUp() {
    testVector = new Vector(0, 100);
  }
  
  @Test
  public void vectorsInitializedWithADirectionAndDistanceCalculateTheCorrectDirectionalComponents() {
    assertEquals(100, testVector.getXComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0,   testVector.getYComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void vectorsInitializedWithZerosForBothTheirDirectionAndMagnitudeWorkCorrectly() {
    Vector testVector = new Vector(0, 0);
    
    assertEquals(0, testVector.getDirection(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0, testVector.getMagnitude(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0, testVector.getXComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0, testVector.getYComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void setXComponentWorksAsExpected() {
    testVector.setXComponent(75);
    
    assertEquals(75,  testVector.getXComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0,   testVector.getYComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(75, testVector.getMagnitude(),  ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0.0,  testVector.getDirection(),  ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void setYComponentWorksAsExpected() {
    testVector.setYComponent(75);
    
    assertEquals(100, testVector.getXComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(75,  testVector.getYComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(125, testVector.getMagnitude(),  ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0.6435,  testVector.getDirection(),  ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void addingTwoExistingVectorsResultsInAThirdVectorOfTheProperProportions() {
    Vector firstVector = new Vector(0, 100);                    // 100 units in the +x direction
    Vector secondVector = new Vector((float) (Math.PI/2), 75);  // 75  units in the +y direction
    
    Vector thirdVector = Vector.addTwoVectors(firstVector, secondVector);
    assertEquals(100,     thirdVector.getXComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(75,      thirdVector.getYComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0.64350, thirdVector.getDirection(),  ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(125,     thirdVector.getMagnitude(),  ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void addingOneVectorToAnotherExistingVectorWorksCorrectly() {
    Vector firstVector = new Vector(0, 100);                    // 100 units in the +x direction
    Vector secondVector = new Vector((float) (Math.PI/2), 75);  // 75  units in the +y direction
    firstVector.add(secondVector);
    
    assertEquals(100,     firstVector.getXComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(75,      firstVector.getYComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0.64350, firstVector.getDirection(),  ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(125,     firstVector.getMagnitude(),  ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void scalingVectorsWorksCorrectly() {
    testVector.scaleMagnitudeByPercentage(1.5f);
    
    assertEquals(150, testVector.getXComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0,   testVector.getYComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(150, testVector.getMagnitude(),  ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0,   testVector.getDirection(),  ConfigValues.MAX_FLOATING_POINT_PRECISION);
    
    testVector.scaleMagnitudeByPercentage(0);
    
    assertEquals(0, testVector.getXComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0, testVector.getYComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0, testVector.getMagnitude(),  ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0, testVector.getDirection(),  ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void buildingVectorsFromXAndYComponentsWorksAsExpected() {
    Vector vectorFromComponents = Vector.getVectorFromComponents(75, 75);
    
    assertEquals(75, vectorFromComponents.getXComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(75, vectorFromComponents.getYComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals((float) (Math.PI/4), vectorFromComponents.getDirection(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals((float) (Math.hypot(75, 75)), vectorFromComponents.getMagnitude(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void buildingVectorsFromComponentsWithZeroForTheHeightWorksAsExpected() {
    Vector vectorFromComponents = Vector.getVectorFromComponents(75, 0);
    
    assertEquals(75, vectorFromComponents.getXComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0, vectorFromComponents.getYComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals((float) 0, vectorFromComponents.getDirection(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals((float) 75, vectorFromComponents.getMagnitude(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void buildingVectorsFromComponentsThatAreBothZeroWorksAndSetsTheDirectionAndMagnitudeToZero() {
    Vector vectorFromComponents = Vector.getVectorFromComponents(0, 0);
    
    assertEquals(0, vectorFromComponents.getXComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0, vectorFromComponents.getYComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals((float) 0, vectorFromComponents.getDirection(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals((float) 0, vectorFromComponents.getMagnitude(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void buildingVectorsFromDirectionAndMagnitudesThatAreBothZeroWorksAndSetsTheComponentsToZero() {
    Vector vectorFromComponents = new Vector(0, 0);
    
    assertEquals(0, vectorFromComponents.getXComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(0, vectorFromComponents.getYComponent(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals((float) 0, vectorFromComponents.getDirection(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals((float) 0, vectorFromComponents.getMagnitude(), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
  @Test
  public void gettingTheDirectionWithDeltasReturnsTheCorrectDirection() {
    assertEquals(0,             Vector.getDirectionFromDeltas(100, 0),  ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(Math.PI/2,     Vector.getDirectionFromDeltas(0, 100),  ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(Math.PI,       Vector.getDirectionFromDeltas(-100, 0), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(3*(Math.PI/2), Vector.getDirectionFromDeltas(0, -100), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    
    assertEquals(Math.PI/4,     Vector.getDirectionFromDeltas( 100,  100), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(3*(Math.PI/4), Vector.getDirectionFromDeltas(-100,  100), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(5*(Math.PI/4), Vector.getDirectionFromDeltas(-100, -100), ConfigValues.MAX_FLOATING_POINT_PRECISION);
    assertEquals(7*(Math.PI/4), Vector.getDirectionFromDeltas( 100, -100), ConfigValues.MAX_FLOATING_POINT_PRECISION);
  }
  
}

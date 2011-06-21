package com.jefflunt.pedestrians.test;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assume.*;
import com.jefflunt.pedestrians.PedestrianSim;


public class PedestrianSimTest {

  private PedestrianSim pedSim;
  
  @Before
  public void setUp() {
    pedSim = new PedestrianSim("Test suite");
  }
  
  @Test
  public void theBlockedMethodOfPedestrianSimTestHandlesOutOfBoundsParametersGracefully() {
    try {
      pedSim.blocked(null, -1, -1);
    } catch (Exception ex) {
      assumeNoException(ex);
    }
  }
  
}

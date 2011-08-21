package com.jefflunt.pedestrians.world.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.jefflunt.pedestrians.world.Clock;

/** Tests the functionality of a clock. */
public class ClockTest {

  /** The clock used for testing. */
  private Clock clock;
  
  @Before
  public void setUp() {
    clock = new Clock(0);
  }
  
  @Test
  public void theClockTicksProperly() {
    assertEquals(0, clock.getCurrentTime());
    clock.tickTock();
    assertEquals(1, clock.getCurrentTime());
  }
  
  @Test
  public void settingTheClockToASpecificTimeWorks() {
    assertEquals(0, clock.getCurrentTime());
    clock.setCurrentTime(123456);
    assertEquals(123456, clock.getCurrentTime());
  }
  
  @Test
  public void gettingDayHourMinuteAndDateTimeStringWorkProperly() {
    assertEquals(1, clock.getDay());
    assertEquals(0, clock.getHour());
    assertEquals(0, clock.getMinute());
    assertEquals("Day 1, 00:00", clock.getDateTimeString());
    
    clock.setCurrentTime(12345678);
    
    assertEquals(29, clock.getDay());
    assertEquals(13, clock.getHour());
    assertEquals(52, clock.getMinute());
    assertEquals("Day 29, 13:52", clock.getDateTimeString());
  }
  
}

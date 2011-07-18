package com.jefflunt.pedestrians.test;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jefflunt.pedestrians.ConfigValues;

public class ConfigValuesTest {

  private File configFile;
  
  @Before
  public void setUp() {
    configFile = new File("test.conf");
  }
  
  @After
  public void cleanUp() {
    configFile.delete();
  }
  
  @Test
  public void configValuesLoadAsExpected() {
    ConfigValues.save("test.conf");
    
    assertTrue(ConfigValues.load("test.conf"));
    
    File config = new File("test.conf");
    config.delete();
  }
  
  @Test
  public void configValuesSaveAsExpected() {
    assertTrue(ConfigValues.save("test.conf"));
  }
  
}

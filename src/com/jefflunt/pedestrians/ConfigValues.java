package com.jefflunt.pedestrians;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/** Various configuration options for Pedestrians.
 */
public class ConfigValues {

  // These values are constants, not to be changed without developer intervention.
  
  /** The size (both width and height) of path finding tiles. */
  public static final int TILE_SIZE = 20;
  /** The radius of a Pedestrian. Used for rendering, etc. */
  public static final float PEDESTRIAN_RADIUS = 3;
  /** The maximum precision by which floating point movement, acceleration, etc. are measured. */
  public static final float MAX_FLOATING_POINT_PRECISION = 0.001f;
  /** The height, in pixels, of the control panel along the bottom of the screen. */
  public static final int HEIGHT_OF_CONTROL_PANEL = 20;
  
  /** Constant value representing up, like on a computer screen. */
  public static final int UP = 0;
  /** Constant value representing down, like on a computer screen. */
  public static final int DOWN = 1;
  /** Constant value representing left, like on a computer screen. */
  public static final int LEFT = 2;
  /** Constant value representing right, like on a computer screen. */
  public static final int RIGHT = 3;
  /** Constant value representing up and to the left, like on a computer screen. */
  public static final int UP_LEFT = 4;
  /** Constant value representing up and to the right, like on a computer screen. */
  public static final int UP_RIGHT = 5;
  /** Constant value representing down and to the left, like on a computer screen. */
  public static final int DOWN_LEFT = 6;
  /** Constant value representing down and to the right, like on a computer screen. */
  public static final int DOWN_RIGHT = 7;
  
  
  // These values are intended to be changed by the simulation, at will. These are the default values.
  // Loading/saving is available to override the defaults.
  
  /** The number of radians/second a Pedestrian will turn, at most. */
  public static float pedestrianTurnRate = (float) (3*Math.PI);
  /** The maximum length of a path found by the path finder. */
  public static int pathFindingMaxSearchDepth = 1000;
  /** The number of movement history records the Pedestrians will keep. */
  public static int pedestrianMovementHistoryDepth = 1800;
  
  /** The number of milliseconds between congestion calculations. */
  public static long millisBetweenCongestionCalculations = 50;
  /** The number of milliseconds between saves of the tile map state. */
  public static long millisBetweenTilemapSaves = 1000;
  
  /** A flag that specifies whether or not to render pedestrian path finding paths. */
  public static boolean renderPaths = false;
  /** A flag that specifies whether or not to render system info, such as current JVM memory usage, frames-per-second rendering, etc. */
  public static boolean renderSystemInfo = false;
  /** A flag that specifies whether or not to render Pedestrian names, next to the Pedestrians on teh obstacle field. */
  public static boolean renderPedNames = false;
  /** A flag that specifies whether or not to render Pedestrian turn sensors. */
  public static boolean renderTurnSensors = false;
  /** A flag that specifies whether or not to render tile congestion values. */
  public static boolean renderCongestion = false;
  /** A flag that specifies whether or not to render in x-ray mode (Pedestrians with circles, and a direction, rather than a sprite). */
  public static boolean renderXRay = false;
  
  /** The x-coordinate of the upper-left corner of the view port. */
  public static int viewportX = 0;
  /** The y-coordinate of the upper-left corner of the view port. */
  public static int viewportY = 0;
  
  /** Saves the values in this class to disk.
   * 
   * @param filename the name of the file in which to store the values.
   */
  public static boolean save(String filename) {
    boolean savedSuccessfully = true;
    
    try {
      ObjectOutputStream fileOut = new ObjectOutputStream(new FileOutputStream(filename));
      
      fileOut.writeFloat(pedestrianTurnRate);
      fileOut.writeInt(pathFindingMaxSearchDepth);
      fileOut.writeInt(pedestrianMovementHistoryDepth);
      
      fileOut.writeLong(millisBetweenCongestionCalculations);
      fileOut.writeLong(millisBetweenTilemapSaves);
      
      fileOut.writeBoolean(renderPaths);
      fileOut.writeBoolean(renderSystemInfo);
      fileOut.writeBoolean(renderPedNames);
      fileOut.writeBoolean(renderTurnSensors);
      fileOut.writeBoolean(renderCongestion);
      
      fileOut.writeInt(viewportX);
      fileOut.writeInt(viewportY);
      
      fileOut.flush();
      fileOut.close();
    } catch (IOException ioEx) {
      savedSuccessfully = false;
    }
    
    return savedSuccessfully;
  }
  
  /** Loads the values in this class from disk.
   * 
   * @param filename the name of the file from which to retrieve the values.
   */
  public static boolean load(String filename) {
    boolean loadedSuccessfully = true;
    
    try {
      ObjectInputStream fileIn = new ObjectInputStream(new FileInputStream(filename));
      
      pedestrianTurnRate              = fileIn.readFloat();
      pathFindingMaxSearchDepth       = fileIn.readInt();
      pedestrianMovementHistoryDepth  = fileIn.readInt();
      
      millisBetweenCongestionCalculations = fileIn.readLong();
      millisBetweenTilemapSaves = fileIn.readLong();
      
      renderPaths       = fileIn.readBoolean();
      renderSystemInfo  = fileIn.readBoolean();
      renderPedNames    = fileIn.readBoolean();
      renderTurnSensors = fileIn.readBoolean();
      renderCongestion  = fileIn.readBoolean();
      
      viewportX = fileIn.readInt();
      viewportY = fileIn.readInt();
      
      fileIn.close();
    } catch (IOException ioEx) {
      loadedSuccessfully = false;
    }
    
    return loadedSuccessfully;
  }
  
  /** Random names for Pedestrians. This is a basic list pulled from US data on popular baby names for the year 2010. */
  public static final String[] randomNames = {
    "Jacob",      "Isabella", "Ethan",  "Sophia",   "Michael", 
    "Emma",       "Jayden",   "Olivia", "William",  "Ava", 
    "Alexander",  "Emily",    "Noah",   "Abigail",  "Daniel", 
    "Madison",    "Aiden",    "Chloe",  "Anthony",  "Mia"
  };
  
}

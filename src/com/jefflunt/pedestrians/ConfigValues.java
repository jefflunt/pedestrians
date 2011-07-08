package com.jefflunt.pedestrians;

/** Various config options for movers.
 */
public class ConfigValues {

  /** The maximum precision by which floating point movement, acceleration, etc. are measured. */
  public static final float MAX_FLOATING_POINT_PRECISION = 0.001f;
  
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

  /** The size (both width and height) of path finding tiles. */
  public static final int TILE_SIZE = 10;
  /** The maximum distance from which blocked tiles will push on a Pedestrian. */
  public static final float TILE_PUSH_RADIUS = 10;
  /** The maximum length of a path found by the path finder. */
  public static final int MAX_SEARCH_DEPTH = 1000;
  
  /** The radius of a Pedestrian. Used for rendering, etc. */
  public static final float PEDESTRIAN_RADIUS = 3;
  /** The maximum distance from which Pedestrians will push against each other for the purpose of collision avoidance. */
  public static final float PEDESTRIAN_PUSH_RADIUS = 9;
  
  /** A flag that specifies whether or not to render pedestrian path finding paths. */
  public static boolean renderPaths = false;
  /** A flag that specifies whether or not to render system info, such as current JVM memory usage, frames-per-second rendering, etc. */
  public static boolean renderSystemInfo = false;
  /** A flag that specifies whether or not to render Pedestrian names, next to the Pedestrians on teh obstacle field. */
  public static boolean renderPedNames = false;
  
  /** Random names for Pedestrians. This is a basic list pulled from US data on popular baby names for the year 2010. */
  public static final String[] randomNames = {
    "Jacob",      "Isabella", "Ethan",  "Sophia",   "Michael", 
    "Emma",       "Jayden",   "Olivia", "William",  "Ava", 
    "Alexander",  "Emily",    "Noah",   "Abigail",  "Daniel", 
    "Madison",    "Aiden",    "Chloe",  "Anthony",  "Mia"
  };
  
}

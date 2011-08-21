package com.jefflunt.pedestrians.world;

/** A time-keeping device, that keeps track of the current 'time' in the world. */
public class Clock {

  /** The number of hours in a day. */
  public static final int HOURS_PER_DAY = 24;
  /** The number of minutes in an hour. */
  public static final int MINUTES_PER_HOUR = 60;
  
  /** The number of ticks that represent 1 minute. */
  public static final int TICKS_PER_MINUTE = 300;
  /** The number of ticks in 1 hour. */
  public static final int TICKS_PER_HOUR = TICKS_PER_MINUTE*MINUTES_PER_HOUR;
  /** The number of ticks in 1 day. */
  public static final int TICKS_PER_DAY  = TICKS_PER_HOUR*HOURS_PER_DAY;
  
  /** The number of ticks of the clock that have passed. */
  private long currentTime;
  
  /** Creates a new clock with the specified current time (in ticks), and the specified number of ticks in a day. */
  public Clock(long currentTime) {
    this.currentTime = currentTime;
  }
  
  /** Increments the time of this clock by 1 tick. */
  public void tickTock() {
    currentTime++;
  }
  
  /** Gets the current time of this clock, in ticks. */
  public long getCurrentTime() {
    return currentTime;
  }
  
  /** Sets the time of the clock to the one specified, in ticks of the clock. */
  public void setCurrentTime(long currentTimeInTicks) {
    currentTime = currentTimeInTicks;
  }
  
  /** Gets the current day of the clock. */
  public long getDay() {
    return 1 + (currentTime / TICKS_PER_DAY);
  }
  
  /** Gets the current hour of the clock. */
  public long getHour() {
    return ((currentTime / TICKS_PER_HOUR) % HOURS_PER_DAY);
  }
  
  /** Gets the current minute of the clock. */
  public long getMinute() {
    return ((currentTime / TICKS_PER_MINUTE) % MINUTES_PER_HOUR);
  }
  
  /** Gets a string that represents the current date and time of day. */
  public String getDateTimeString() {
    StringBuffer dateTime = new StringBuffer();
    
    dateTime.append("Day ");
    dateTime.append(getDay() + ", ");
    dateTime.append(getTwoDigitStringVersion(getHour()) + ":");
    dateTime.append(getTwoDigitStringVersion(getMinute()) + "");
    
    return dateTime.toString();
  }
  
  /** Converts the incoming value into a String with a minimum of two digits. */
  private String getTwoDigitStringVersion(long value) {
    String strVersion = (new Long(value)).toString();
    
    while (strVersion.length() < 2)
      strVersion = "0" + strVersion;
    
    return strVersion;
  }
  
}

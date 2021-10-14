/*
 * Copyright © 2016 Mark Pope
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify, merge, 
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons 
 * to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or 
 * substantial portions of the Software. 
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.pope.sprinkler.ejb.timers;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.pope.sprinkler.googleapi.GoogleCalendarReader;
import com.pope.sprinkler.googleapi.GoogleSpreadSheetWriter;
import com.pope.sprinkler.timer.SprinklerJob;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;

@Singleton
public class CalendarTimerSessionBean {
  
  public CalendarTimerSessionBean() {
  }

  private static final Set<Timer> timers = new HashSet<>();

  

  @Schedule(dayOfWeek = "*", month = "*", hour = "*", dayOfMonth = "*", year = "*", minute = "15", second = "0", persistent = false)
  public void scheduleTimers() {
    try {
      GoogleSpreadSheetWriter.getInstance().log("0", "Reading Sprinkler Calendar");
      GoogleCalendarReader cal = new GoogleCalendarReader();

      List<Event> events = cal.getTodaysSchedules();
      //events.forEach(event -> scheduleSprinkler(event));

      if (events.isEmpty()) {
        //Log no daily activity
      } else {
      //events.forEach(event -> scheduleSprinkler(event));
        for (Event event : events) {
          scheduleSprinkler(event);
        }
      }

    } catch (Exception e) {
      GoogleSpreadSheetWriter.getInstance().log("-99", e.getMessage());
      Logger.getLogger(CalendarTimerSessionBean.class.getName()).log(Level.SEVERE, null, e);
      //Send email
    }
  }

  /**
   * Create a new timer to start at the start time of the Calendar event
   * 
   * @param event
   * @throws Exception 
   */
  private void scheduleSprinkler(Event event)  {
    try {
    Timer timer = new Timer(true);

    DateTime start = event.getStart().getDateTime();
    GoogleSpreadSheetWriter.getInstance().log(event.getSummary() + "[" + event.getDescription() + "]", "Scheduled to start at " + start);
 
    timer.schedule(new SprinklerJob(event), new Date(start.getValue()));
    timers.add(timer);
    } catch(Exception ex) {
       Logger.getLogger(CalendarTimerSessionBean.class.getName()).log(Level.SEVERE, null, ex);
      //Send email
    }

  }

 /**
  * Public accessor to existing timers
  * @return 
  */ 
  public Set<Timer> getTimers() {
    return timers;
  }
}

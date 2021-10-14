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
package com.pope.sprinkler.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.pope.sprinkler.ejb.timers.CalendarTimerSessionBean;
import com.pope.sprinkler.googleapi.GoogleSpreadSheetWriter;
import com.pope.sprinkler.timer.SprinklerJob;
import java.util.Iterator;
import java.util.Timer;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 *  REST methods for HTML page manual control.
 *  1. http://host:port/sprinkler/v1/1/on/5  runs sprinkler 1 for 5 minutes
 *  2. http://host:port/sprinkler/v1/1/off turns off sprinkler 1
 *  3. http://host:port/sprinkler/reset re-reads Calendar and creates new schedules.
 */
@Stateless
@Path("/")
public class RESTSessionBean {

  @EJB
  CalendarTimerSessionBean calTimers;
  

  @GET
  @Path(value = "reset")
  public String reset(@PathParam("sprinkler") int sprinkler) throws Exception {
    GoogleSpreadSheetWriter.getInstance().log("0", "Resetting scheduled timers");

    for (Iterator timerz = calTimers.getTimers().iterator(); timerz.hasNext();) {
      ((Timer) timerz.next()).cancel();
    }
    calTimers.scheduleTimers();
    return buildResponse("Reset Complete");
  }

  @GET
  @Path("{sprinkler}/on/{duration}")
  public String on(@PathParam("sprinkler") int sprinkler, @PathParam("duration") int duration) {

    try {
      Event event = new Event();
      event.setDescription("" + sprinkler);

      EventDateTime start = new EventDateTime();
      start.setDateTime(new DateTime(0));
      event.setStart(start);

      EventDateTime end = new EventDateTime();
      end.setDateTime(new DateTime(duration * 1000));

      event.setEnd(end);
      SprinklerJob job = new SprinklerJob(event);
      job.run();
    } catch (Exception e) {
      e.printStackTrace();
      GoogleSpreadSheetWriter.getInstance().log("-99", e.getMessage());
    }
    return buildResponse("Started sprinkler " + sprinkler + " for " + duration + " minutes.");
  }

  @GET
  @Path("{sprinkler}/off")
  public String off(@PathParam("sprinkler") int sprinkler) {

    Event event = new Event();
    event.setDescription("" + sprinkler);

    EventDateTime start = new EventDateTime();
    start.setDateTime(new DateTime(0));
    event.setStart(start);

    EventDateTime end = new EventDateTime();
    end.setDateTime(new DateTime(0));
    event.setEnd(end);

    SprinklerJob job = new SprinklerJob(event);
    job.stop();
    return buildResponse("Stopped sprinkler " + sprinkler);
  }

  /**
   * Convenience method to build an HTML response.
   * 
   * @param message
   * @return 
   */
  private String buildResponse(String message) {
    return "<html>"
            + "<head>"
            + "<meta http-equiv=\"refresh\" content=\"3;url=/sprinkler/index.html\" />"
            + "</head>"
            + "<body>"
            + "<h1>" + message + "</h1>"
            + "</body>"
            + "</html>";
  }
}

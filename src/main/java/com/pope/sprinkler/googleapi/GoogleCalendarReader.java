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
package com.pope.sprinkler.googleapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoogleCalendarReader extends BaseGoogleApi {

	/**
	 * Wrapper class that reads my Google account Calendar named Sprinkler. You must have a valid Google account
	 * with a Calendar named Sprinkler. Any event in this Calendar must have the sprinkler number in the event title
	 * and a duration.
	 */
	public GoogleCalendarReader() {

	}
	/**
	 * Application name.
	 */
	private final String APPLICATION_NAME = "Google Calendar API";

	private final String SPRINKLER_CAL_NAME = "Sprinklers";

	/**
	 * Build and return an authorized Calendar client service.
	 *
	 * @return an authorized Calendar client service
	 * @throws IOException
	 */
	public com.google.api.services.calendar.Calendar getCalendarService() throws IOException {

		Credential credential = authorize();

		com.google.api.services.calendar.Calendar retVal = new com.google.api.services.calendar.Calendar.Builder(
		    HTTP_TRANSPORT, JSON_FACTORY, credential)
		    .setApplicationName(APPLICATION_NAME)
		    .build();
		return retVal;
	}

	/**
	 * Get all sprinkler events for the day that have start times greater than current time.
	 *
	 * @return Sprinkler events for the day
	 * @throws IOException
	 */
	public List<Event> getTodaysSchedules() throws IOException {

		List<Event> retVal = new ArrayList<>();

		if (false) {
			return testData();
		} else {

			com.google.api.services.calendar.Calendar service = getCalendarService();
			String calendarId = null; 

			calendarId = getSprinklerCalendar(service);
			System.out.println("CAL ID " + calendarId);

			Events feed = service.events().list(calendarId).execute();
			java.util.Calendar endOfDay = java.util.Calendar.getInstance();
			endOfDay.set(java.util.Calendar.HOUR, 0);
			endOfDay.set(java.util.Calendar.MINUTE, 0);
			endOfDay.set(java.util.Calendar.DAY_OF_YEAR, endOfDay.get(java.util.Calendar.DAY_OF_YEAR) + 1);
			long beginRange = new Date().getTime(); //current time
			long endRange = endOfDay.getTimeInMillis(); //midnight

			for (Event event : feed.getItems()) {
				try {
					Integer.parseInt(event.getDescription());
				} catch (NumberFormatException e) {
					Logger.getLogger(GoogleSpreadSheetWriter.class.getName()).log(Level.INFO, e.getMessage(), e);
				}

				long start = event.getStart().getDateTime().getValue();

				if ((beginRange < start) && (start < endRange)) {
					retVal.add(event);
				}
			}
			return retVal;
		}
	}

	public List<Event> testData() {

		List<Event> list = new ArrayList<>();
		Date baseDate = new Date();

		list.add(createTestEvent(baseDate, "1", 10, 15));
		list.add(createTestEvent(baseDate, "2", 15, 20));
		list.add(createTestEvent(baseDate, "3", 20, 55));
		list.add(createTestEvent(baseDate, "4", 25, 30));
		list.add(createTestEvent(baseDate, "5", 30, 35));
		list.add(createTestEvent(baseDate, "6", 35, 40));
		return list;
	}

	private Event createTestEvent(Date baseDate, String sprinkler, int startMin, int endMin) {
		Event event = new Event();
		EventDateTime start = new EventDateTime();
		EventDateTime end = new EventDateTime();
		start.setDateTime(new DateTime(baseDate.getTime() + startMin * 1000));
		end.setDateTime(new DateTime(baseDate.getTime() + endMin * 1000));
		event.setStart(start);
		event.setEnd(end);
		event.setSummary("" + sprinkler);
		return event;
	}

	private String getSprinklerCalendar(com.google.api.services.calendar.Calendar service) throws IOException {
		String pageToken = null;
		do {
			CalendarList calendarList = service.calendarList().list().setPageToken(null).execute();
			java.util.List<CalendarListEntry> items = calendarList.getItems();

			for (CalendarListEntry calendarListEntry : items) {
				if (calendarListEntry.getSummary().equals(SPRINKLER_CAL_NAME)) {
					return calendarListEntry.getId();
				}
				pageToken = calendarList.getNextPageToken();
			}
		} while (pageToken != null);

		com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
		calendar.setSummary(SPRINKLER_CAL_NAME);
		calendar.setTimeZone("America/Los_Angeles");

		com.google.api.services.calendar.model.Calendar createdCalendar = service.calendars().insert(calendar).execute();
		return createdCalendar.getId();
	}
}

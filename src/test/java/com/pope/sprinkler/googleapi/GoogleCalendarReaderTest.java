package com.pope.sprinkler.googleapi;

import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import java.util.Iterator;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mpope
 */
public class GoogleCalendarReaderTest {

	public GoogleCalendarReaderTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

//	@Test
	public void getTodaysEvents() throws Exception {
		com.pope.sprinkler.googleapi.GoogleCalendarReader reader = new com.pope.sprinkler.googleapi.GoogleCalendarReader();
		List<Event> list = reader.getTodaysSchedules();
		for (Iterator<Event> iterator = list.iterator(); iterator.hasNext();) {
			Event next = iterator.next();
			System.out.println("Summary " + next.getSummary());
			
		}
	}
	public void testGetCal() throws Exception {
		com.pope.sprinkler.googleapi.GoogleCalendarReader reader = new com.pope.sprinkler.googleapi.GoogleCalendarReader();
		com.google.api.services.calendar.Calendar service = reader.getCalendarService();

		String pageToken = null;
		do {
			CalendarList calendarList = service.calendarList().list().setPageToken(null).execute();
			java.util.List<CalendarListEntry> items = calendarList.getItems();

			for (CalendarListEntry calendarListEntry : items) {
				if (calendarListEntry.getSummary().equals("XXXXX")) {
					System.out.println(calendarListEntry.getId());
					return;
				}
				pageToken = calendarList.getNextPageToken();
			}
		} while (pageToken != null);

		com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
		calendar.setSummary("XXXXX");
		calendar.setTimeZone("America/Los_Angeles");

		com.google.api.services.calendar.model.Calendar createdCalendar = service.calendars().insert(calendar).execute();
		System.out.println(createdCalendar.getId());

	}

}

/*
 * Copyright Ã‚Â© 2016 Mark Pope
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

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;
import com.pope.sprinkler.ejb.timers.CalendarTimerSessionBean;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Use a Google Spreadsheet as a server log.
 *
 * If ambitious move to a custom Log Handler and store in lib/ext.
 */
public class GoogleSpreadSheetWriter extends BaseGoogleApi {

	private static GoogleSpreadSheetWriter writer = null;
	private final String  SPRINKLER_SPREADSHEET_NAME = "Sprinkler";

	/**
	 * Maintain a single instances.
	 */
	private GoogleSpreadSheetWriter() {

	}

	public static GoogleSpreadSheetWriter getInstance() {
		if (writer == null) {
			writer = new GoogleSpreadSheetWriter();
		}
		return writer;
	}

	/**
	 * Application name.
	 */
	private static final String APPLICATION_NAME = "Google Spreadsheet API";

	private static SpreadsheetService service;

	/**
	 * Build and return an authorized Calendar client service.
	 *
	 * @return an authorized Calendar client service
	 * @throws IOException
	 */
	public SpreadsheetService getSpreadsheetService() throws IOException {

		Credential credential = authorize();

		SpreadsheetService retVal = new SpreadsheetService(SPRINKLER_SPREADSHEET_NAME);
		retVal.setOAuth2Credentials(credential);
		return retVal;
	}

	public Drive getDriveService() throws IOException {
		Credential credential = authorize();
		return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
	}

	public SpreadsheetEntry getSpreadsheet() throws IOException, ServiceException {

		URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");

		service = getSpreadsheetService();
		SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
		List<SpreadsheetEntry> spreadsheets = feed.getEntries();

		// Look for existing spreadsheet named 'Sprinkler'
		boolean found = false;
		for (SpreadsheetEntry entity : spreadsheets) {

			if (entity.getTitle().getPlainText().equals(SPRINKLER_SPREADSHEET_NAME)) {
				found = true;
				return entity;
			}
		}
		if (!found) {

			Logger.getLogger(GoogleSpreadSheetWriter.class.getName()).log(Level.INFO, "Creating 'Sprinkler' spreadsheet");
			Drive drive = getDriveService();

			File metaFile = new File();
			metaFile.setTitle(SPRINKLER_SPREADSHEET_NAME);
			metaFile.setKind(SpreadsheetEntry.KIND);
			metaFile.setMimeType("application/vnd.google-apps.spreadsheet");

			drive.files().insert(metaFile).setFields("id").execute();

			SpreadsheetEntry spreadsheet = writer.getSpreadsheet();
			WorksheetEntry worksheet = spreadsheet.getWorksheets().get(0);

			// Write header line into Spreadsheet
			URL cellFeedUrl = worksheet.getCellFeedUrl();
			CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);

			CellEntry cellEntry = new CellEntry(1, 1, "TimeStamp");
			cellFeed.insert(cellEntry);
			cellEntry = new CellEntry(1, 2, "Sprinkler");
			cellFeed.insert(cellEntry);
			cellEntry = new CellEntry(1, 3, "Description");
			cellFeed.insert(cellEntry);

			return spreadsheet;

		}
		return null;
	}

	/**
	 * Our log method. Log the dateTime, sprinkler number, and message. If logging errors use a non-positive integer
	 * for the sprinkler number.
	 *
	 * @param sprinkler
	 * @param message
	 */
	public void log(String sprinkler, String message) {
		try {
			SpreadsheetEntry spreadsheet = writer.getSpreadsheet();
			WorksheetEntry worksheet = spreadsheet.getWorksheets().get(0);
			URL listFeedURL = worksheet.getListFeedUrl();

			ListEntry entry = createEntry(sprinkler, message);

			service.insert(listFeedURL, entry);

		} catch (Exception e) {
			Logger.getLogger(GoogleSpreadSheetWriter.class.getName()).log(Level.SEVERE, e.getMessage(), e);
		}

	}

	private static ListEntry createEntry(String sprinkler, String message) {
		Map<String, Object> rowValues = new HashMap<String, Object>();
		rowValues.put("TimeStamp", new Date().toString());
		rowValues.put("Sprinkler", sprinkler);
		rowValues.put("Description", message);
		return createRow(rowValues);
	}

	private static ListEntry createHeaderEntry() {
		Map<String, Object> rowValues = new HashMap<String, Object>();
		rowValues.put("A1", "TimeStamp");
		rowValues.put("B1", "Sprinkler");
		rowValues.put("C1", "Description");
		return createRow(rowValues);
	}

	private static ListEntry createRow(Map<String, Object> rowValues) {
		ListEntry row = new ListEntry();
		for (String columnName : rowValues.keySet()) {
			Object value = rowValues.get(columnName);
			row.getCustomElements().setValueLocal(columnName,
			    String.valueOf(value));
		}
		return row;
	}

	private void setHeaders(WorksheetEntry worksheet) throws IOException, ServiceException {
		URL cellFeedUrl = worksheet.getCellFeedUrl();
		CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);

		// Iterate through each cell, updating its value if necessary.
		// TODO: Update cell values more intelligently.
		for (CellEntry cell : cellFeed.getEntries()) {
			if (cell.getTitle().getPlainText().equals("A1")) {
				cell.changeInputValueLocal("TimeStamp");
				cell.update();
			} else if (cell.getTitle().getPlainText().equals("B1")) {
				cell.changeInputValueLocal("Sprinkler");
				cell.update();
			} else if (cell.getTitle().getPlainText().equals("C1")) {
				cell.changeInputValueLocal("Description");
				cell.update();
			}
		}
	}

}

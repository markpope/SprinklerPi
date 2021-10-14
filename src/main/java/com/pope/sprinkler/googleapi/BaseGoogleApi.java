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
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

/**
 * Perform authentication for both Google APIs here.
 */
class BaseGoogleApi {

	protected static final File DATA_STORE_DIR = new File("/opt/sprinklerpi");

	protected static FileDataStoreFactory DATA_STORE_FACTORY;

	protected static HttpTransport HTTP_TRANSPORT;

	protected static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	protected static final List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR_READONLY, "https://www.googleapis.com/auth/drive.file", "https://www.googleapis.com/auth/userinfo.email", "https://www.googleapis.com/auth/userinfo.profile", "https://docs.google.com/feeds", "https://spreadsheets.google.com/feeds","https://www.googleapis.com/auth/calendar");

	private Credential credential = null;

	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (GeneralSecurityException | IOException t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Creates an authorized Credential object. Check expiration and recreate if near death
	 *
	 * @return an authorized Credential object.
	 * @throws IOException
	 */
	public Credential authorize() throws IOException {

		if ((credential == null) || (credential.getExpiresInSeconds() < 15)) {
			// Load client secrets.
//			InputStream in = GoogleSpreadSheetWriter.class.getResourceAsStream("/opt/sprinklerpi/client_secret.json");
			FileInputStream googCred = new FileInputStream(new File("/opt/sprinklerpi/client_secret.json"));
			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(GoogleSpreadSheetWriter.JSON_FACTORY, new InputStreamReader(googCred));

			// Build flow and trigger user authorization request.
			GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(GoogleSpreadSheetWriter.HTTP_TRANSPORT, GoogleSpreadSheetWriter.JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(GoogleSpreadSheetWriter.DATA_STORE_FACTORY).setAccessType("offline").build();
			credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver.Builder().setPort(8080).build()).authorize("user");
		}

		return credential;
	}

}

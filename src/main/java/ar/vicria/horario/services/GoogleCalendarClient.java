package ar.vicria.horario.services;

import ar.vicria.horario.properties.GoogleCalendarProperties;
import ar.vicria.horario.services.util.DateUtil;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GoogleCalendarClient {

    private static final String APPLICATION_NAME = "free time bot";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final GoogleCalendarProperties properties;

    private Credential credential;

    public List<Event> getMySchedule() throws Exception {
        Credential credential = authorize();
        Calendar service = new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        String calendarId = "primary"; // Идентификатор вашего календаря, например, "primary"

        DateTime startOfWeek = DateUtil.startOfThisWeek();
        DateTime endOfWeek = DateUtil.endOfThisWeek();
        com.google.api.client.util.DateTime start = new com.google.api.client.util.DateTime(startOfWeek.toDate());
        com.google.api.client.util.DateTime end = new com.google.api.client.util.DateTime(endOfWeek.toDate());
        Events events = service.events().list(calendarId)
                .setMaxResults(50)
                .setTimeMin(start)
                .setTimeMax(end)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        return events.getItems();
    }

    private Credential authorize() throws IOException, GeneralSecurityException {
        if (credential == null) {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            GoogleClientSecrets clientSecrets = loadClientSecrets();

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                    .build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(51767).build();

            credential = new AuthorizationCodeInstalledApp(
                    flow, receiver).authorize("1021593323500-srtefgf540baufsrv2oplrbehchr3hak.apps.googleusercontent.com");
        }
//        String authorizationCode = ""; // Здесь укажите код авторизации, полученный после авторизации пользователя
//        TokenResponse tokenResponse = flow.newTokenRequest(authorizationCode).setRedirectUri("http://localhost:51767/Callback").execute();
//        String accessToken = tokenResponse.getAccessToken();
//        String refreshToken = tokenResponse.getRefreshToken();


        return credential;
    }

    private GoogleClientSecrets loadClientSecrets()
            throws IOException {
        String initialString = properties.toString();
        InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        return GoogleClientSecrets.load(
                JSON_FACTORY,
                new InputStreamReader(targetStream, StandardCharsets.UTF_8)
        );
    }

}

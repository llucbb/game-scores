package com.king.gamescores.server;

import com.king.gamescores.handler.HighScoreHandler;
import com.king.gamescores.handler.LoginHandler;
import com.king.gamescores.handler.ScoreHandler;
import com.king.gamescores.log.ScoresLogger;
import com.king.gamescores.service.ScoresService;
import com.king.gamescores.service.SessionKeyService;
import com.king.gamescores.util.Strings;
import org.junit.After;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.Assert.*;

public class ScoresServerTest {

    private static final Logger LOG = Logger.getLogger(ScoresServerTest.class.getName());

    static {
        try {
            ScoresLogger.setup();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private enum Method {
        GET, POST
    }

    private static final int PORT = 8080;
    private static final String BASE_URL = String.format("http://localhost:%d", PORT);
    private static final int EXPECTED_LEVEL = 1;
    private static final int EXPECTED_USER_ID = 3;
    private static final String EXPECTED_TOKEN = "token";
    private static final int EXPECTED_SCORE = 1500;
    private static final String EXPECTED_HIGH_SCORE = "1=1500";

    private ScoresServer scoresServer;

    // Mock SessionKeyService
    private final SessionKeyService sessionKeyService = new SessionKeyService() {
        public String generateSessionKey(int userId) {
            return EXPECTED_TOKEN;
        }

        public int getUserIdFromSessionKey(String sessionKey) {
            return EXPECTED_USER_ID;
        }

        public boolean isSessionKeyValid(String sessionKey) {
            return true;
        }
    };

    // Mock ScoresService
    private final ScoresService scoresService = new ScoresService() {
        @Override
        public void registerScore(int level, int userId, int score) {
        }

        @Override
        public String getHighScoresForLevel(int level) {
            return EXPECTED_HIGH_SCORE;
        }
    };

    @Test
    public void rootContextShouldFail() throws IOException {
        scoresServer = ScoresServer.start(PORT, 1);
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(Method.GET.name());

        conn.connect();
        int responseCode = conn.getResponseCode();
        String response = getResponse(conn);

        assertEquals(HTTP_BAD_REQUEST, responseCode);
        assertFalse(Strings.isNotEmpty(response));
    }

    @Test
    public void loginNonNumericUserIdShouldFail() throws IOException {
        scoresServer = ScoresServer.start(PORT, 1).loginHandler(new LoginHandler(sessionKeyService));
        URL url = new URL(String.format(BASE_URL.concat("/%s/login"), "nonNumeric"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(Method.GET.name());

        conn.connect();
        int responseCode = conn.getResponseCode();
        String token = getResponse(conn);

        assertEquals(HTTP_BAD_REQUEST, responseCode);
        assertFalse(Strings.isNotEmpty(token));
    }

    @Test
    public void loginIncorrectPathShouldFail() throws IOException {
        scoresServer = ScoresServer.start(PORT, 1).loginHandler(new LoginHandler(sessionKeyService));
        URL url = new URL(String.format(BASE_URL.concat("/%d/log"), EXPECTED_USER_ID));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(Method.GET.name());

        conn.connect();
        int responseCode = conn.getResponseCode();
        String token = getResponse(conn);

        assertEquals(HTTP_BAD_REQUEST, responseCode);
        assertFalse(Strings.isNotEmpty(token));
    }

    @Test
    public void loginSuccessful() throws IOException {
        scoresServer = ScoresServer.start(PORT, 1).loginHandler(new LoginHandler(sessionKeyService));
        URL url = new URL(String.format(BASE_URL.concat("/%d/login"), EXPECTED_USER_ID));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(Method.GET.name());

        conn.connect();
        int responseCode = conn.getResponseCode();
        String token = getResponse(conn);

        assertEquals(HTTP_OK, responseCode);
        assertTrue(Strings.isNotEmpty(token));
        assertEquals(EXPECTED_TOKEN, token);
    }

    @Test
    public void scoreZeroLevelShouldFail() throws IOException {
        scoresServer = ScoresServer.start(PORT, 1).scoreHandler(new ScoreHandler(sessionKeyService));
        URL url = new URL(String.format(BASE_URL.concat("/%d/score?sessionkey=%s"), 0, EXPECTED_TOKEN));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(Method.POST.name());
        setRequestBody(conn, String.valueOf(EXPECTED_SCORE));

        conn.connect();
        int responseCode = conn.getResponseCode();
        String response = getResponse(conn);

        assertEquals(HTTP_BAD_REQUEST, responseCode);
        assertFalse(Strings.isNotEmpty(response));
    }

    @Test
    public void scoreMissingSessionKeyShouldFail() throws IOException {
        scoresServer = ScoresServer.start(PORT, 1).scoreHandler(new ScoreHandler(sessionKeyService));
        URL url = new URL(String.format(BASE_URL.concat("/%d/score?sessionkey="), EXPECTED_LEVEL));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(Method.POST.name());
        setRequestBody(conn, String.valueOf(EXPECTED_SCORE));

        conn.connect();
        int responseCode = conn.getResponseCode();
        String response = getResponse(conn);

        assertEquals(HTTP_BAD_REQUEST, responseCode);
        assertFalse(Strings.isNotEmpty(response));
    }

    @Test
    public void scoreWrongSessionKeyParamNameShouldFail() throws IOException {
        scoresServer = ScoresServer.start(PORT, 1).scoreHandler(new ScoreHandler(sessionKeyService));
        URL url = new URL(String.format(BASE_URL.concat("/%d/score?sessionkeyWrong=%s"), EXPECTED_LEVEL,
                EXPECTED_TOKEN));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(Method.POST.name());
        setRequestBody(conn, String.valueOf(EXPECTED_SCORE));

        conn.connect();
        int responseCode = conn.getResponseCode();
        String response = getResponse(conn);

        assertEquals(HTTP_BAD_REQUEST, responseCode);
        assertFalse(Strings.isNotEmpty(response));
    }

    @Test
    public void scoreIncorrectPathShouldFail() throws IOException {
        scoresServer = ScoresServer.start(PORT, 1).scoreHandler(new ScoreHandler(sessionKeyService));
        URL url = new URL(String.format(BASE_URL.concat("/%d/sco?sessionkey=%s"), EXPECTED_LEVEL, EXPECTED_TOKEN));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(Method.POST.name());
        setRequestBody(conn, String.valueOf(EXPECTED_SCORE));

        conn.connect();
        int responseCode = conn.getResponseCode();
        String response = getResponse(conn);

        assertEquals(HTTP_BAD_REQUEST, responseCode);
        assertFalse(Strings.isNotEmpty(response));
    }

    @Test
    public void scoreSuccessful() throws IOException {
        scoresServer = ScoresServer.start(PORT, 1).scoreHandler(new ScoreHandler(sessionKeyService));
        URL url = new URL(String.format(BASE_URL.concat("/%d/score?sessionkey=%s"), EXPECTED_LEVEL, EXPECTED_TOKEN));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(Method.POST.name());
        setRequestBody(conn, String.valueOf(EXPECTED_SCORE));

        conn.connect();
        int responseCode = conn.getResponseCode();
        String response = getResponse(conn);

        assertEquals(HTTP_OK, responseCode);
        assertFalse(Strings.isNotEmpty(response));
    }

    @Test
    public void highScoreListSignedLevelShouldFail() throws IOException {
        scoresServer = ScoresServer.start(PORT, 1).highScoreHandler(new HighScoreHandler(scoresService));
        URL url = new URL(String.format(BASE_URL.concat("/%s/highscorelist"), "-2"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(Method.GET.name());

        conn.connect();
        int responseCode = conn.getResponseCode();
        String response = getResponse(conn);

        assertEquals(HTTP_BAD_REQUEST, responseCode);
        assertFalse(Strings.isNotEmpty(response));
    }

    @Test
    public void highScoreListIncorrectPathShouldFail() throws IOException {
        scoresServer = ScoresServer.start(PORT, 1).highScoreHandler(new HighScoreHandler(scoresService));
        URL url = new URL(String.format(BASE_URL.concat("/%d/highsco"), EXPECTED_LEVEL));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(Method.GET.name());

        conn.connect();
        int responseCode = conn.getResponseCode();
        String response = getResponse(conn);

        assertEquals(HTTP_BAD_REQUEST, responseCode);
        assertFalse(Strings.isNotEmpty(response));
    }

    @Test
    public void highScoreListSuccessful() throws IOException {
        scoresServer = ScoresServer.start(PORT, 1).highScoreHandler(new HighScoreHandler(scoresService));
        URL url = new URL(String.format(BASE_URL.concat("/%d/highscorelist"), EXPECTED_LEVEL));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(Method.GET.name());

        conn.connect();
        int responseCode = conn.getResponseCode();
        String response = getResponse(conn);

        assertEquals(HTTP_OK, responseCode);
        assertTrue(Strings.isNotEmpty(response));
        assertEquals(EXPECTED_HIGH_SCORE, response);
    }

    private void setRequestBody(HttpURLConnection conn, String body) throws IOException {
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    private String getResponse(HttpURLConnection conn) {
        StringBuilder response = new StringBuilder();
        InputStream is;
        try {
            is = conn.getInputStream();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                boolean keepGoing = true;
                while (keepGoing) {
                    String currentLine = br.readLine();
                    if (currentLine == null) {
                        keepGoing = false;
                    } else {
                        response.append(currentLine);
                    }
                }
            }
        } catch (IOException e) {
            //Do nothing
        }
        return response.toString();
    }

    @After
    public void tierDown() {
        scoresServer.stopServer(0);
    }
}

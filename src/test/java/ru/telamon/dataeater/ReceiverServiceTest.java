package ru.telamon.dataeater;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ru.yandex.qatools.embed.postgresql.distribution.Version.V10_3;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReceiverServiceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.username}")
    private String userName;

    @Value("${spring.datasource.password}")
    private String password;

    private EmbeddedPostgres embeddedPostgres;

    @Before
    public void startEmbeddDatabase() throws IOException {
        embeddedPostgres = new EmbeddedPostgres(V10_3);
        final String url = embeddedPostgres.start("localhost", 8081, "message", userName, password);
        jdbcTemplate.execute(readFile("src/sql/create.sql"));
    }

    @Test
    public void sendMessage() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final String message = "{\"version\": 1, \"text\":\"Hello world\"}";
        final HttpEntity<String> entity = new HttpEntity<>(message, headers);
        final long deviceTime = System.currentTimeMillis();
        String deviceId = "ROCKET239";
        final ResponseEntity response = restTemplate.exchange("/message/" + deviceId + "/" + deviceTime, HttpMethod.POST,
                entity, String.class);
        assertEquals("HTTP status check", HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Check that exactly one row was inserted",
                1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "t_message"));
        jdbcTemplate.query("select * from t_message", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                assertEquals("check device time", deviceTime, rs.getTimestamp("device_time").getTime());
                long dbTime =  rs.getTimestamp("db_time").getTime();
                assertTrue("check device time not after db time", deviceTime <= dbTime);
                System.out.println(dbTime - deviceTime);
                assertTrue("check that record inserted in less that 1 second", deviceTime + 1000 > dbTime);
                assertEquals("Check device id in database", deviceId, rs.getString("device_id"));
                assertEquals("Check message in database", message, rs.getString("message"));
            }
        });
    }

    @After
    public void stopPostgres() {
        embeddedPostgres.stop();
    }

    private static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded,  StandardCharsets.UTF_8);
    }
}

package ru.telamon.dataeater;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageDao {

    public static final String INSERT_MESSAGE_SQL = "insert into t_message (device_id, device_time, message) values (?,?,?)";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public MessageDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addMessage(final String deviceId, final long deviceTime, final String message) {
        jdbcTemplate.update(INSERT_MESSAGE_SQL, deviceId, new java.sql.Timestamp(deviceTime), message);
    }
}

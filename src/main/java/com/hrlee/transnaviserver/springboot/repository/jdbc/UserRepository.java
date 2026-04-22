package com.hrlee.transnaviserver.springboot.repository.jdbc;

import com.hrlee.transnaviserver.springboot.entity.User;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SimplePropertyRowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public User getUser(@NonNull String usrId) {
        List<User> usrFound = jdbcTemplate.query("SELECT * FROM user WHERE id=\"" + usrId + "\"", new SimplePropertyRowMapper<User>(User.class));

        if(usrFound.size() != 1)
            return null;

        return usrFound.get(0);
    }
}

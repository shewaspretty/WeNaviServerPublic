package com.hrlee.transnaviserver.springboot.jpa;

import com.hrlee.transnaviserver.springboot.entity.jpa.UserRegistered;
import com.hrlee.transnaviserver.springboot.repository.jpa.UserRepositoryDeprecated;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepositoryDeprecated userRepositoryDeprecated;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserRegistered usrFound = userRepositoryDeprecated.getUserById(username);
        if(usrFound == null)
            throw new UsernameNotFoundException("user name not found");

        return usrFound;
    }
}

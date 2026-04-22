package com.hrlee.transnaviserver.springboot.entity.jpa;

import com.hrlee.transnaviserver.springboot.security.AuthorityHolder;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collection;
import java.util.List;

@Deprecated
@Entity
@Getter
public class UserRegistered implements UserDetails {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nick_name", nullable = false)
    private String nickName;


    protected UserRegistered() {}

    public UserRegistered(String id, String nickName, String password) {
        this.id = id;
        this.nickName = nickName;

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();;
        this.password = encoder.encode(password);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(AuthorityHolder.DEFAULT_USER_AUTHORITY));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return id;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

package com.green.university.security;

import com.green.university.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;  // 생성자로 받는 필드

    public CustomUserDetails(User user) {  // 생성자 추가
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ROLE_STUDENT, ROLE_PROFESSOR, ROLE_STAFF 형태로 맞춰줌
        String role = "ROLE_" + user.getUserRole().toUpperCase();
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // 우리는 userId를 PK로 쓰니까 여기선 id 문자열 반환
        return String.valueOf(user.getId());
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

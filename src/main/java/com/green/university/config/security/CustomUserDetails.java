package com.green.university.config.security;

import com.green.university.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;  // 로그인한 User 엔티티

    public CustomUserDetails(User user) {  // 생성자 추가
        this.user = user;
    }

    // 컨트롤러, 서비스에서 편하게 쓰기위해 추가 -> 이거 안쓰면 더 번거로워지나? 보기
    public Long getId() {
        return user.getId();
    }

    public String getUserRole() {
        return user.getUserRole();
    }

    public User getUser() {
        return user;
    }

    // ================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
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
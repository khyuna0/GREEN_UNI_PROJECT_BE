package com.green.university.config.security;

import com.green.university.entity.User;
import com.green.university.repository.interfaces.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Long id;
        try {
            id = Long.valueOf(username);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("잘못된 ID 형식입니다: ");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        return new CustomUserDetails(user);
    }
}

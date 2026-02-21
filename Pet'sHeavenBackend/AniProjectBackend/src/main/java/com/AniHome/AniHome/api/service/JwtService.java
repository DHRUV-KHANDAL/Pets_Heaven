package com.AniHome.AniHome.api.service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.AniHome.AniHome.api.dao.UserDao;
import com.AniHome.AniHome.api.entity.JwtRequest;
import com.AniHome.AniHome.api.entity.JwtResponse;
import com.AniHome.AniHome.api.entity.User;
import com.AniHome.AniHome.api.util.JwtUtil;

@Service
public class JwtService implements UserDetailsService {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDao userDao;

    @Autowired
    private AuthenticationManager authenticationManager;

    public JwtResponse createJwtToken(JwtRequest jwtRequest) throws Exception {
        if (jwtRequest == null) {
            throw new IllegalArgumentException("JWT request must not be null");
        }

        String userName = jwtRequest.getUserName();
        if (userName == null) {
            throw new IllegalArgumentException("Username must not be null");
        }

        String userPassword = jwtRequest.getUserPassword();
        if (userPassword == null) {
            throw new IllegalArgumentException("Password must not be null");
        }

        authenticate(userName, userPassword);

        UserDetails userDetails = loadUserByUsername(userName);
        String newGeneratedToken = jwtUtil.generateToken(userDetails);
        System.out.println("Yogesh TEST =" + newGeneratedToken);

        User user = userDao.findById(userName).orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        return new JwtResponse(user, newGeneratedToken);
    }

    @SuppressWarnings("unchecked")
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null) {
            throw new UsernameNotFoundException("User not found!");
        }
        User user = userDao.findById(username).orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        return new org.springframework.security.core.userdetails.User(user.getUserName(), user.getUserPassword(),
                getAuthority(user));

    }

    @SuppressWarnings("rawtypes")
    private Set getAuthority(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        user.getRole().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
        });
        return authorities;
    }

    private void authenticate(String userName, String userPassword) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, userPassword));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            System.out.println("Invalid Credentials");
            throw new BadCredentialsException("INVALID_CREDENTIALS", e);
        }
    }

}

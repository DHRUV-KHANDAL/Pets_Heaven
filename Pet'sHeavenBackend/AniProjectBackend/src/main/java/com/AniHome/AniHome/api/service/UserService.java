package com.AniHome.AniHome.api.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.AniHome.AniHome.api.dao.RoleDao;
import com.AniHome.AniHome.api.dao.UserDao;
import com.AniHome.AniHome.api.entity.Role;
import com.AniHome.AniHome.api.entity.User;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void initRoleAndUser() {

        Role userRole = new Role();
        userRole.setRoleName("User");
        userRole.setRoleDescription("Default user role");
        roleDao.save(userRole);

        Role rescuerRole = new Role();
        rescuerRole.setRoleName("Rescuer");
        rescuerRole.setRoleDescription("Rescuer role who will rescue the animal");
        roleDao.save(rescuerRole);

        Role adminRole = new Role();
        adminRole.setRoleName("Admin");
        adminRole.setRoleDescription("Admin role");
        roleDao.save(adminRole);

    }

    public User registerNewUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        // Ensure roles exist
        try {
            if (!roleDao.existsById("User")) {
                Role userRole = new Role();
                userRole.setRoleName("User");
                userRole.setRoleDescription("Default user role");
                roleDao.save(userRole);
            }

            if (!roleDao.existsById("Rescuer")) {
                Role rescuerRole = new Role();
                rescuerRole.setRoleName("Rescuer");
                rescuerRole.setRoleDescription("Rescuer role who will rescue the animal");
                roleDao.save(rescuerRole);
            }

            if (!roleDao.existsById("Admin")) {
                Role adminRole = new Role();
                adminRole.setRoleName("Admin");
                adminRole.setRoleDescription("Admin role");
                roleDao.save(adminRole);
            }
        } catch (Exception e) {
            // Roles might already exist
        }

        // Check if user already exists
        String userName = user.getUserName();
        if (userName == null) {
            throw new IllegalArgumentException("Username must not be null");
        }

        if (userDao.existsById(userName)) {
            throw new DataIntegrityViolationException("Username Already Exist!");
        }

        // Set role based on userRole field
        Set<Role> userRoles = new HashSet<>();
        Role role;

        if ("rescuer".equalsIgnoreCase(user.getUserRole())) {
            role = roleDao.findById("Rescuer").orElse(null);
        } else if ("admin".equalsIgnoreCase(user.getUserRole())) {
            role = roleDao.findById("Admin").orElse(null);
        } else {
            role = roleDao.findById("User").orElse(null);
        }

        if (role != null) {
            userRoles.add(role);
        }

        user.setRole(userRoles);
        user.setUserPassword(getEncodedPassword(user.getUserPassword()));

        return userDao.save(user);
    }

    public User updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        String userName = user.getUserName();
        if (userName == null) {
            throw new IllegalArgumentException("Username must not be null");
        }

        User u = userDao.findById(userName)
                .orElseThrow(() -> new DataIntegrityViolationException("User does not exist"));
        u.setUserName(userName);
        u.setUserFirstName(user.getUserFirstName());
        u.setUserLastName(user.getUserLastName());
        u.setUserEmail(user.getUserEmail());
        u.setUserPhone(user.getUserPhone());
        u.setCity(user.getCity());
        u.setRole(u.getRole());

        return userDao.save(u);
    }

    public String getEncodedPassword(String password) {
        return passwordEncoder.encode(password);
    }
}

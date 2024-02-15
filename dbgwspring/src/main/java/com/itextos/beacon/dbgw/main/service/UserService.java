package com.itextos.beacon.dbgw.main.service;

import java.util.Arrays;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.itextos.beacon.dbgw.main.config.MySingletonObject;
import com.itextos.beacon.dbgw.main.model.Role;
import com.itextos.beacon.dbgw.main.model.User;
import com.itextos.beacon.dbgw.main.repository.RoleRepository;
import com.itextos.beacon.dbgw.main.repository.UserRepository;

@Service
public class UserService {
	
	@Autowired 
	private MySingletonObject mySingletonObject;
	
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	public User findUserByUsername(String username) {
		
		return mySingletonObject.getUsersWithUsername().get(username.toLowerCase());
	}

	public User saveUser(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		Role userRole = roleRepository.findByRole("ROLE_USER");
		user.setRoles(new HashSet<>(Arrays.asList(userRole)));
		
        return user = userRepository.save(user);
	}
	
	public User saveAdmin(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		Role adminRole = roleRepository.findByRole("ROLE_ADMIN");
		user.setRoles(new HashSet<>(Arrays.asList(adminRole)));
		
        return user = userRepository.save(user);
	}

}

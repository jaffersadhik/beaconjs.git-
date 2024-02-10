package com.itextos.beacon.dbgw.main.service;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.itextos.beacon.dbgw.main.model.User;

@Service
public class MyUserDetailsService implements UserDetailsService {
	
	@Autowired
	UserService userService;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userService.findUserByUsername(username.toLowerCase());

		if(user == null)
			throw new UsernameNotFoundException("User Not Found");

		return MyUserDetails.build(user);
	}

}

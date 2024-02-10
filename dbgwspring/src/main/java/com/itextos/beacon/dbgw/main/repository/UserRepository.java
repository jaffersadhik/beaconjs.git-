package com.itextos.beacon.dbgw.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.itextos.beacon.dbgw.main.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	Boolean existsByUsernameIgnoreCase(String username);
	
	User findByUsernameIgnoreCase(String username);
	
}
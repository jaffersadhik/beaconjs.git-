package com.itextos.beacon.dbgw.main.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.itextos.beacon.dbgw.main.model.User;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
	
	
	
}
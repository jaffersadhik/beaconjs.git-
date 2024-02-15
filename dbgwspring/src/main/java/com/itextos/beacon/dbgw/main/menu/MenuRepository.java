package com.itextos.beacon.dbgw.main.menu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    // Define custom query methods if needed
	
	List<Menu> findByUser_Id(Long userId);
}


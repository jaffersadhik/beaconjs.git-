package com.itextos.beacon.dbgw.main.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmenuRepository extends JpaRepository<Submenu, Long> {
    // Define custom query methods if needed
}


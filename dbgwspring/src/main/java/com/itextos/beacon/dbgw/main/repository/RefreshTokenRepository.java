package com.itextos.beacon.dbgw.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.itextos.beacon.dbgw.main.model.RefreshToken;
import com.itextos.beacon.dbgw.main.model.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	RefreshToken findByRefreshToken(String token);

	@Modifying
	int deleteByUser(User user);

}

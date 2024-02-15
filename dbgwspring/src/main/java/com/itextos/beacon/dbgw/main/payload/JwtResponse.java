package com.itextos.beacon.dbgw.main.payload;

import java.util.Set;

import com.itextos.beacon.dbgw.main.menu.Menu;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {

	private String type;
	private String token;
	private String refreshToken;
	private Long userid;
	private String fullname;
	private String username;
	private Set<String> roles;
	private Set<Menu> menus;

	public JwtResponse(String type, String token, String refreshToken, Long userid, String fullname, String username, Set<String> roles,Set<Menu> menus) {
		this.type = type;
		this.token = token;
		this.refreshToken = refreshToken;
		this.userid = userid;
		this.fullname = fullname;
		this.username = username;
		this.roles = roles;
		this.menus = menus;
	}
}

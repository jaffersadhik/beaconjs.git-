package com.itextos.beacon.dbgw.main.config;

import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.itextos.beacon.dbgw.main.model.User;

import lombok.Getter;
import lombok.Setter;

@Component
@Scope("singleton")
@Getter
@Setter
public class MySingletonObject {

	private Map<Long,User> usersWithUserid;
	
	private Map<String,User> usersWithUsername;

}


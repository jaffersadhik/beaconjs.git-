package com.itextos.beacon.dbgw.main.config;



import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextos.beacon.dbgw.main.model.User;

@Service
public class MySingletonService {
	
    private final MySingletonObject mySingletonObject;

    @Autowired
    public MySingletonService(MySingletonObject mySingletonObject) {
        this.mySingletonObject = mySingletonObject;
    }

    public void setUsersInMemory(Map<Long,User> usermap) {
    	
    	mySingletonObject.setUsersWithUserid(usermap);
    
    	mySingletonObject.setUsersWithUsername(getUsersWithUsername(usermap));
    }

	private Map<String, User> getUsersWithUsername(Map<Long, User> usermap) {
		
		Map<String, User> result=new HashMap<String,User>(); 
		
		usermap.forEach((k,v)->{
			
			result.put(v.getUsername().toLowerCase(), v);
		});
		
		return result;
	}
}

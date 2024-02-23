package com.itextos.beacon.dbgw.main.config;



import java.util.HashMap;
import java.util.List;
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

    public void setUsersInMemory(List<User> userlist) {
    	
    	mySingletonObject.setUsersWithUserid(getUsersWithUserId(userlist));
    
    	mySingletonObject.setUsersWithUsername(getUsersWithUsername(userlist));
    }

	private Map<String, User> getUsersWithUsername(List<User> userlist) {
		
		Map<String, User> result=new HashMap<String,User>(); 
		
		userlist.forEach((u)->{
			
			result.put(u.getUsername().toLowerCase(), u);
		});
		
		return result;
	}
	
	private Map<Long, User> getUsersWithUserId(List<User> userlist) {
		
		Map<Long, User> result=new HashMap<Long,User>(); 
		
		userlist.forEach((u)->{
			
			result.put(u.getId(), u);
		});
		
		return result;
	}
}

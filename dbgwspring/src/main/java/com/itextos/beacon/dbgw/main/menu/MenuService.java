package com.itextos.beacon.dbgw.main.menu;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextos.beacon.dbgw.main.config.MySingletonObject;

@Service
public class MenuService {
   
	@Autowired 
	private MySingletonObject mySingletonObject;

    public Set<Menu> getAllMenus(Long userId) {
       // return mySingletonObject.getUsersWithUserid().get(userId).getMenus();
    	return null;
    }
}


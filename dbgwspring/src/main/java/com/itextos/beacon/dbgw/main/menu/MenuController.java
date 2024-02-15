package com.itextos.beacon.dbgw.main.menu;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menus")
public class MenuController {
    @Autowired
    private MenuService uiMenuItemService;

    @PostMapping
    public List<Menu> getAllUIMenus(@RequestParam("userid") Long userid) {
        return uiMenuItemService.getAllMenus(userid);
    }
}


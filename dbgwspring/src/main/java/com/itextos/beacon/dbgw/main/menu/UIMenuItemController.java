package com.itextos.beacon.dbgw.main.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/ui-menus")
public class UIMenuItemController {
    @Autowired
    private UIMenuItemService uiMenuItemService;

    @GetMapping
    public List<Menu> getAllUIMenus() {
        return uiMenuItemService.getAllUIMenus();
    }
}


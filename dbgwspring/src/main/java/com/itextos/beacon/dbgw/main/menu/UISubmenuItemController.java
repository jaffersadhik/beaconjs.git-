package com.itextos.beacon.dbgw.main.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/ui-submenus")
public class UISubmenuItemController {
    @Autowired
    private UISubmenuItemService uiSubmenuItemService;

    @GetMapping
    public List<Submenu> getAllUISubmenus() {
        return uiSubmenuItemService.getAllUISubmenus();
    }
}

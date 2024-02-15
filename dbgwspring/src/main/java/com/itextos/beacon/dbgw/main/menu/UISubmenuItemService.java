package com.itextos.beacon.dbgw.main.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UISubmenuItemService {
    @Autowired
    private SubmenuRepository uiSubmenuItemRepository;

    public List<Submenu> getAllUISubmenus() {
        return uiSubmenuItemRepository.findAll();
    }
}


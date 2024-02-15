package com.itextos.beacon.dbgw.main.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MenuService {
    @Autowired
    private MenuRepository uiMenuItemRepository;

    public List<Menu> getAllMenus(Long userId) {
        return uiMenuItemRepository.findByUser_Id(userId);
    }
}


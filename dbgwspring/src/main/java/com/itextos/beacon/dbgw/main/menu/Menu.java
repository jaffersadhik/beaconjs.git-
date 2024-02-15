package com.itextos.beacon.dbgw.main.menu;

import javax.persistence.*;

import com.itextos.beacon.dbgw.main.model.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "menus")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Menu {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "menu_id")
    private Long id;

    private String name;
    private String url;
    private String icon;

    @OneToMany(mappedBy = "parentMenu", cascade = CascadeType.ALL)
    private List<Submenu> submenus;

    // Getters and setters
    // Constructor
}

package com.itextos.beacon.dbgw.main.menu;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @OneToMany(mappedBy = "parentMenu", fetch = FetchType.EAGER)
    private Set<Submenu> submenus;
    
    
	

    // Getters and setters
    // Constructor
}

package com.itextos.beacon.dbgw.main.menu;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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

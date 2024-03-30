package com.unitia.ejbclient;

import java.util.ArrayList;
import java.util.List;

import com.unitia.util.misc.Prop;

public class UnitiaHandoverSingleTon {

	private static  List<UnitiaHandover> handoverList=new ArrayList<UnitiaHandover>();

	private static int POINTER=0;
	
	private static UnitiaHandoverSingleTon obj=new UnitiaHandoverSingleTon();
	
	
	private UnitiaHandoverSingleTon() {
		
		init();
	}
	
	private void init() {
		
		List<String> gwlist=Prop.getInstance().getEJBServerList();
		
		gwlist.forEach((ipport)->{
			
			for(int i=0;i<3;i++) {
				handoverList.add(new UnitiaHandover(ipport) );
			}
		});
		
	}

	public static UnitiaHandoverSingleTon getInstance() {
		
		if(obj==null) {
			
			obj=new UnitiaHandoverSingleTon();
		}
		
		return obj;
	}
	
	public UnitiaHandover getUnitiaHandover() {
		
		if(POINTER>=handoverList.size()) {
			
			POINTER=0;
		}
		
		UnitiaHandover obj=handoverList.get(POINTER);
		
		POINTER++;
		
		return obj;
		
	}
}

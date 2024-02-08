package demo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Test {

	static String format="yyyy-MM-dd HH:mm:ss.SSS";
	
	static String recv_time=" 2024-02-07 10:56:50.673";
	static String cs="2024-02-07 10:57:05.557";
	public static void main(String[] args) throws ParseException {
		
		System.out.println("Hai");
		
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false);

       System.out.println(recv_time); 

       System.out.println(sdf.parse(recv_time).getTime()); ;

       System.out.println(sdf.format(new Date(sdf.parse(recv_time).getTime()))); ;
       
       
       System.out.println(cs); 

       System.out.println(sdf.parse(cs).getTime()); ;

       System.out.println(sdf.format(new Date(sdf.parse(cs).getTime()))); 
       
       System.out.println(sdf.parse(cs).getTime()-sdf.parse(recv_time).getTime()); ;

       

	}

}

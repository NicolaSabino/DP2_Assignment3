package it.polito.dp2.RNS.sol3.admClient;

import java.util.Calendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class CalendarConverter {
	
	public static XMLGregorianCalendar toXMLGregorianCalendar(Calendar cal){
		DatatypeFactory dtf;
		try {
			dtf = DatatypeFactory.newInstance();
			XMLGregorianCalendar xgc = dtf.newXMLGregorianCalendar(); 
			xgc.setYear(cal.get(Calendar.YEAR));
			xgc.setDay(cal.get(Calendar.DAY_OF_MONTH));
			xgc.setMonth(cal.get(Calendar.MONTH)+ 1);
			xgc.setHour(cal.get(Calendar.HOUR_OF_DAY));
			xgc.setMinute(cal.get(Calendar.MINUTE));
			xgc.setSecond(cal.get(Calendar.SECOND));
			xgc.setMillisecond(cal.get(Calendar.MILLISECOND));
			// Calendar ZONE_OFFSET and DST_OFFSET fields are in milliseconds.
			int offsetInMinutes = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 1000);
			xgc.setTimezone(offsetInMinutes); 
			return xgc;
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Calendar toCalendar(XMLGregorianCalendar xmlCalendar){
	    TimeZone timeZone = xmlCalendar.getTimeZone(xmlCalendar.getTimezone());        
	    Calendar calendar = Calendar.getInstance(timeZone);
	    calendar.set(Calendar.YEAR,xmlCalendar.getYear());
	    calendar.set(Calendar.MONTH,xmlCalendar.getMonth()-1);
	    calendar.set(Calendar.DATE,xmlCalendar.getDay());
	    calendar.set(Calendar.HOUR_OF_DAY,xmlCalendar.getHour());
	    calendar.set(Calendar.MINUTE,xmlCalendar.getMinute());
	    calendar.set(Calendar.SECOND,xmlCalendar.getSecond());  
	    return calendar;
	}

}

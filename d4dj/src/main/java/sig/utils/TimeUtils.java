package sig.utils;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;


public class TimeUtils {
	public static String GetTimeDifferenceFromCurrentDate(Date pastDate) {
		long totalseconds = (Calendar.getInstance().getTimeInMillis()-pastDate.getTime())/1000;
		//System.out.println("Total Seconds: "+totalseconds);
		long seconds = (long)(totalseconds);
		long minutes = (long)(seconds/60);
		long hours = (long)(minutes/60);
		long days = (long)(hours/24);
		StringBuilder string = new StringBuilder();
		DecimalFormat df = new DecimalFormat("00");
		if (days>0) {
			string.append(days);
		}
		if (hours>0) {
			string.append(((string.length()>0)?":":"")+(hours%24));
		}
		if (minutes>0) {
			string.append(((string.length()>0)?":":"")+df.format((minutes%60)));
		}
		if (seconds>0) {
			string.append(((string.length()>0)?":":"")+df.format((seconds%60)));
		}
		return string.toString();
	}
}

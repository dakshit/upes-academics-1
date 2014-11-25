/*
 * Copyright (c) 2014 Shaleen Jain <shaleen.jain95@gmail.com>
 *
 * This file is part of UPES Academics.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shalzz.attendance;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.shalzz.attendance.model.ListFooter;
import com.shalzz.attendance.model.ListHeader;
import com.shalzz.attendance.model.Period;
import com.shalzz.attendance.model.Subject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class DataAssembler {

	private static String mTag = "Data Assembler";

    public static void parseStudentDetails(String response,Context mContext) {
        Document doc = Jsoup.parse(response);

        Elements tddata = doc.select("td");

        if(doc.getElementsByTag("title").size()==0 || doc.getElementsByTag("title").get(0).text().equals("UPES - Home"))
        {
            // TODO: relogin
            String msg ="It seems your session has expired.\nPlease Login again.";
            if(!mContext.getClass().getName().equals("com.shalzz.attendance.wrapper.MyVolley"))
                Crouton.makeText((Activity) mContext, msg, Style.ALERT).show();
            Log.e(mTag,"Login Session Expired");
        }
        else if (tddata != null && tddata.size() > 0) {
            int i = 0;
            ListHeader header = new ListHeader();
            for (Element element : tddata) {
                if (i == 5)
                    header.setName(element.text());
                else if (i == 8)
                    header.setFatherName(element.text());
                else if (i == 11)
                    header.setCourse(element.text());
                else if (i == 14)
                    header.setSection(element.text());
                else if (i == 17)
                    header.setRollNo(element.text());
                else if (i == 20)
                    header.setSAPId(Integer.parseInt(element.text()));
                ++i;
            }

            DatabaseHandler db = new DatabaseHandler(mContext);
            db.addOrUpdateListHeader(header);
            db.close();
        }
    }
	
	/**
	 * Extracts Attendance details from the HTML code.
	 * @param response HTML page string
	 */
	public static void  parseAttendance(String response,Context mContext) {

		ArrayList<Float> claHeld = new ArrayList<Float>();
		ArrayList<Float> claAttended = new ArrayList<Float>();
		ArrayList<String> abDates = new ArrayList<String>();
		ArrayList<String> projPer = new ArrayList<String>();
		ArrayList<String> subjectName = new ArrayList<String>();
		ArrayList<Float> percentage = new ArrayList<Float>();

		Log.i(mTag, "Parsing response...");
		Document doc = Jsoup.parse(response);

		Elements tddata = doc.select("td");

		if(doc.getElementsByTag("title").size()==0 || doc.getElementsByTag("title").get(0).text().equals("UPES - Home"))
		{
			// TODO: relogin
			String msg ="It seems your session has expired.\nPlease Login again.";
			if(!mContext.getClass().getName().equals("com.shalzz.attendance.wrapper.MyVolley"))
				Crouton.makeText((Activity) mContext, msg, Style.ALERT).show();
			Log.e(mTag,"Login Session Expired");
		}
		else if (tddata != null && tddata.size() > 0)
		{
			int i=0;
			for(Element element : tddata)
			{
			    if(i>29)
				{
					// for subjects
					if ((i - 30) % 7 == 0) {
						subjectName.add(element.text());
					}
					// for Classes Held
					else if ((i - 31) % 7 == 0) {
						claHeld.add(Float.parseFloat(element.text()));
					}
					// for Classes attended
					else if ((i - 32) % 7 == 0) {
						claAttended.add(Float.parseFloat(element.text()));
					}
					// for Dates Absent
					else if ((i - 33) % 7 == 0) {
						abDates.add(element.text());
					}
					// for attendance percentage
					else if ((i - 34) % 7 == 0) {
						percentage.add(Float.parseFloat(element.text()));
					}
					// for projected percentage
					else if ((i - 35) % 7 == 0) {
						projPer.add(element.text());
					}
				}
				++i;
			}

			Elements total = doc.select("th");
			ListFooter footer = new ListFooter();
			footer.setAttended(Float.parseFloat(total.get(10).text()));
			footer.setHeld(Float.parseFloat(total.get(9).text()));
			footer.setPercentage(Float.parseFloat(total.get(12).text()));
			DatabaseHandler db = new DatabaseHandler(mContext);
			db.addOrUpdateListFooter(footer);

			Log.i(mTag, "Response parsing complete.");

			for(i=0;i<claHeld.size();i++)
			{
				Subject subject = new Subject(i+1, 
						subjectName.get(i),
						claHeld.get(i),
						claAttended.get(i),
						abDates.get(i),
						percentage.get(i),
						projPer.get(i));
				db.addOrUpdateSubject(subject);
			}
			db.close();
		}
	}

	public static int parseTimeTable(String response,Context mContext) {

		Document doc = Jsoup.parse(response);
		Elements thdata = doc.select("th");

		ArrayList<String> time = new ArrayList<String>();
		ArrayList<String> mon = new ArrayList<String>();
		ArrayList<String> tue = new ArrayList<String>();
		ArrayList<String> wed = new ArrayList<String>();
		ArrayList<String> thur = new ArrayList<String>();
		ArrayList<String> fri = new ArrayList<String>();
		ArrayList<String> sat = new ArrayList<String>();
		String dayNames[] = {"mon","tue","wed","thur","fri","sat"};
		ArrayList<ArrayList<String>> days = new ArrayList<ArrayList<String>>();
		days.add(mon);
		days.add(tue);
		days.add(wed);
		days.add(thur);
		days.add(fri);
		days.add(sat);

		if(doc.getElementsByTag("title").size()==0 || doc.getElementsByTag("title").get(0).text().equals("UPES - Home"))
		{
			String msg ="It seems your session has expired.\nPlease Login again.";
			if(!mContext.getClass().getName().equals("com.shalzz.attendance.wrapper.MyVolley"))
				Crouton.makeText((Activity) mContext, msg, Style.ALERT).show();
			Log.e(mTag,"Login Session Expired");
            return -1;
		}
        else if(doc.getElementsByClass("infomessage").text().equals("No reports found for the given criteria.")) {
            if(!mContext.getClass().getName().equals("com.shalzz.attendance.wrapper.MyVolley"))
                Crouton.makeText((Activity) mContext, "No TimeTable available at this time", Style.ALERT).show();
            Log.e(mTag,"No TimeTable");
            return -2;
        }
		else if (thdata != null && thdata.size() > 0)
		{
			int i=0;
			for(Element element : thdata)
			{
				if(i>8)
				{
					// get time
					if ((i - 9) % 7 == 0) {
						time.add(element.html());
					}
					// periods on mon
					if ((i - 10) % 7 == 0) {
						mon.add(element.html());
					}
					// periods on tue
					if ((i - 11) % 7 == 0) {
						tue.add(element.html());
					}
					// periods on wed
					if ((i - 12) % 7 == 0) {
						wed.add(element.html());
					}
					// periods on thur
					if ((i - 13) % 7 == 0) {
						thur.add(element.html());
					}
					// periods on fri
					if ((i - 14) % 7 == 0) {
						fri.add(element.html());
					}
					// periods on sat
					if ((i - 15) % 7 == 0) {
						sat.add(element.html());
					}
				}
				++i;
			}
            DatabaseHandler db = new DatabaseHandler(mContext);
			for(int j=0;j<days.size();j++)
			{
                ArrayList<String> dayofweek = days.get(j);
				for(i=0;i<time.size();i++)
				{
                    String[] parts = dayofweek.get(i).split("<br />");
                    int index = time.get(i).indexOf("-");
                    String start = time.get(i).substring(0,index);
                    String end = time.get(i).substring(index+1);
                    while(i+1<time.size() && dayofweek.get(i).equals(dayofweek.get(i + 1))) {
                        index = time.get(i+1).indexOf("-");
                        end = time.get(i+1).substring(index+1);
                        i++;
                    }
                    Period period = new Period();
                    Period period1 = new Period();
                    if(!dayofweek.get(i).equals("***")) {
                        if(parts.length==7) {
                            String batch = parts[0].substring(parts[0].indexOf('-'));
                            period.setTeacher(parts[1]);
                            period.setSubjectName(parts[2].replaceAll("&amp;", "&")+" "+batch);
                            period.setRoom(parts[3].split("<hr")[0]);
                            batch = parts[3].split("<hr")[1].substring(parts[3].split("<hr")[1].indexOf('-'));
                            period1.setTeacher(parts[4]);
                            period1.setSubjectName(parts[5].replaceAll("&amp;", "&")+" "+batch);
                            period1.setRoom(parts[6]);
                        }
                        else if (!parts[0].isEmpty()) {
                            System.out.println(parts[0]);
                            String batch = parts[0].substring(parts[0].indexOf('-'));
                            period.setTeacher(parts[1]);
                            period.setSubjectName(parts[2].replaceAll("&amp;", "&")+" "+batch);
                            period.setRoom(parts[3]);
                        }
                        else {
                            period.setTeacher(parts[1]);
                            period.setSubjectName(parts[2].replaceAll("&amp;", "&"));
                            period.setRoom(parts[3]);
                        }
                    }
                    period.setDay(dayNames[j]);
                    period.setTime(start,end);
                    if(!period.getSubjectName().isEmpty())
                        db.addOrUpdatePeriod(period);
                    if(parts.length==7) {
                        period1.setDay(dayNames[j]);
                        period1.setTime(start,end);
                        db.addOrUpdatePeriod(period1);
                    }
				}
			}
            db.close();
		}
        return 0;
	}
}

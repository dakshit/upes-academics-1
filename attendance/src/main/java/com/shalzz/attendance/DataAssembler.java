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

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.bugsnag.android.Bugsnag;
import com.shalzz.attendance.model.ListFooter;
import com.shalzz.attendance.model.ListHeader;
import com.shalzz.attendance.model.Period;
import com.shalzz.attendance.model.Subject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class DataAssembler {

    // TODO: do in background
    private static String mTag = "Data Assembler";
    private static String sync_class = "com.shalzz.attendance.wrapper.MyVolley";

    public static int parseStudentDetails(String response,Context mContext) {
        Resources resources = mContext.getResources();
        String session_error = resources.getString(R.string.session_error);
        String session_error_identifier = resources.getString(R.string.session_error_identifier);
        String http_tag_title = resources.getString(R.string.http_tag_title);

        Document doc = Jsoup.parse(response);
        Elements tddata = doc.select("td");
        Log.i(mTag, "Parsing student details...");

        if(doc.getElementsByTag(http_tag_title).size()==0 || doc.getElementsByTag(http_tag_title).text().equals(session_error_identifier))
        {
            if(!mContext.getClass().getName().equals(sync_class))
                Miscellaneous.showMultilineSnackBar(mContext, session_error);
            Bugsnag.leaveBreadcrumb("Login Session Expired");
            return -1;
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

            Log.i(mTag, "Got student details.");
            DatabaseHandler db = new DatabaseHandler(mContext);
            db.addOrUpdateListHeader(header);
            db.close();
        }
        return 0;
    }

    /**
     * Extracts Attendance details from the HTML code.
     * @param response HTML page string
     */
    public static int  parseAttendance(String response,Context mContext) {

        Resources resources = mContext.getResources();
        String session_error = resources.getString(R.string.session_error);
        String session_error_identifier = resources.getString(R.string.session_error_identifier);
        String unavailable_data = resources.getString(R.string.unavailable_data);
        String unavailable_data_identifier = resources.getString(R.string.unavailable_data_identifier);
        String http_tag_title = resources.getString(R.string.http_tag_title);
        String http_tag_div = resources.getString(R.string.http_tag_div);

        DatabaseHandler db = new DatabaseHandler(mContext);
        db.deleteAllSubjects();

        ArrayList<Float> claHeld = new ArrayList<>();
        ArrayList<Float> claAttended = new ArrayList<>();
        ArrayList<String> abDates = new ArrayList<>();
        ArrayList<String> projPer = new ArrayList<>();
        ArrayList<String> subjectName = new ArrayList<>();
        ArrayList<Float> percentage = new ArrayList<>();

        Log.i(mTag, "Parsing response...");
        Document doc = Jsoup.parse(response);

        Elements tddata = doc.select("td");

        if(doc.getElementsByTag(http_tag_title).size()==0 || doc.getElementsByTag(http_tag_title).text().equals(session_error_identifier))
        {
            if(!mContext.getClass().getName().equals(sync_class))
                Miscellaneous.showMultilineSnackBar(mContext, session_error);
            Bugsnag.leaveBreadcrumb("Login Session Expired");
            return -1;
        }
        else if(doc.getElementsByClass(http_tag_div).text().equals(unavailable_data_identifier)) {
            if(!mContext.getClass().getName().equals(sync_class))
                Miscellaneous.showMultilineSnackBar(mContext, unavailable_data);
            Bugsnag.leaveBreadcrumb("Data not available");
            return -2;
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
                db.addSubject(subject);
            }
            db.close();
        }
        return 0;
    }

    public static int parseTimeTable(String response,Context mContext) {

        Resources resources = mContext.getResources();
        String session_error = resources.getString(R.string.session_error);
        String session_error_identifier = resources.getString(R.string.session_error_identifier);
        String unavailable_timetable = resources.getString(R.string.unavailable_timetable);
        String unavailable_timetable_identifier = resources.getString(R.string.unavailable_timetable_identifier);
        String http_tag_title = resources.getString(R.string.http_tag_title);
        String http_tag_div = resources.getString(R.string.http_tag_div);

        DatabaseHandler db = new DatabaseHandler(mContext);
        db.deleteAllPeriods();

        Document doc = Jsoup.parse(response);
        Elements thdata = doc.select("th");

        ArrayList<String> time = new ArrayList<>();
        ArrayList<String> mon = new ArrayList<>();
        ArrayList<String> tue = new ArrayList<>();
        ArrayList<String> wed = new ArrayList<>();
        ArrayList<String> thur = new ArrayList<>();
        ArrayList<String> fri = new ArrayList<>();
        ArrayList<String> sat = new ArrayList<>();
        String dayNames[] = {"mon","tue","wed","thur","fri","sat"};
        ArrayList<ArrayList<String>> days = new ArrayList<>();
        days.add(mon);
        days.add(tue);
        days.add(wed);
        days.add(thur);
        days.add(fri);
        days.add(sat);

        if(doc.getElementsByTag(http_tag_title).size()==0 || doc.getElementsByTag(http_tag_title).text().equals(session_error_identifier))
        {
            if(!mContext.getClass().getName().equals(sync_class))
                Miscellaneous.showMultilineSnackBar(mContext, session_error);
            Bugsnag.leaveBreadcrumb("Login Session Expired");
            return -1;
        }
        else if(doc.getElementsByClass(http_tag_div).text().equals(unavailable_timetable_identifier)) {
            if(!mContext.getClass().getName().equals(sync_class))
                Miscellaneous.showSnackBar(mContext, unavailable_timetable);
            Bugsnag.leaveBreadcrumb("No TimeTable");
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
                            String batch = parts[0].substring(parts[0].indexOf('-')+1);
                            period.setTeacher(parts[1]);
                            period.setSubjectName(parts[2].replaceAll("&amp;", "&"));
                            period.setRoom(parts[3].split("<hr")[0]);
                            period.setBatch(batch);
                            batch = parts[3].split("<hr")[1].substring(parts[3].split("<hr")[1].indexOf('-')+1);
                            period1.setTeacher(parts[4]);
                            period1.setSubjectName(parts[5].replaceAll("&amp;", "&"));
                            period1.setRoom(parts[6]);
                            period1.setBatch(batch);
                        }
                        else if (!parts[0].isEmpty()) {
                            System.out.println(parts[0]);
                            String batch = parts[0].substring(parts[0].indexOf('-')+1);
                            period.setTeacher(parts[1]);
                            period.setSubjectName(parts[2].replaceAll("&amp;", "&"));
                            period.setRoom(parts[3]);
                            period.setBatch(batch);
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
                        db.addPeriod(period);
                    if(parts.length==7) {
                        period1.setDay(dayNames[j]);
                        period1.setTime(start,end);
                        db.addPeriod(period1);
                    }
                }
            }
            db.close();
        }
        return 0;
    }
}

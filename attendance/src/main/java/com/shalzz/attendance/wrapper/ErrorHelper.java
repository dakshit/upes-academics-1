package com.shalzz.attendance.wrapper;

import android.content.Context;
import android.content.res.Resources;

import com.shalzz.attendance.Miscellaneous;
import com.shalzz.attendance.R;

public class ErrorHelper {

    public static void showSnackbar(int result, Context mContext) {

        Resources resources = mContext.getResources();
        String session_error = resources.getString(R.string.session_error);
        String unavailable_data = resources.getString(R.string.unavailable_data);
        String unavailable_timetable = resources.getString(R.string.unavailable_timetable);

        switch (result) {
            case -1:
                Miscellaneous.showMultilineSnackBar(mContext, session_error);
                break;
            case -2:
                Miscellaneous.showSnackBar(mContext, unavailable_data);
                break;
            case -3:
                Miscellaneous.showSnackBar(mContext, unavailable_timetable);
                break;
            default:
                break;
        }
    }
}

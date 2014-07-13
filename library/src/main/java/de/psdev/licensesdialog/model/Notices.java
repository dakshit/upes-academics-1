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

package de.psdev.licensesdialog.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Notices implements Parcelable {

    private final List<Notice> mNotices;

    public Notices() {
        mNotices = new ArrayList<Notice>();
    }

    // Setter / Getter

    public void addNotice(final Notice notice) {
        mNotices.add(notice);
    }

    public List<Notice> getNotices() {
        return mNotices;
    }

    // Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeList(this.mNotices);
    }

    protected Notices(final Parcel in) {
        mNotices = new ArrayList<Notice>();
        in.readList(this.mNotices, Notice.class.getClassLoader());
    }

    public static Creator<Notices> CREATOR = new Creator<Notices>() {
        public Notices createFromParcel(final Parcel source) {
            return new Notices(source);
        }

        public Notices[] newArray(final int size) {
            return new Notices[size];
        }
    };
}

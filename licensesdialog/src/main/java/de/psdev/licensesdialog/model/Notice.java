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

import de.psdev.licensesdialog.licenses.License;

public class Notice implements Parcelable {

    private String mName;
    private String mUrl;
    private String mCopyright;
    private License mLicense;

    //

    public Notice() {
    }

    public Notice(final String name, final String url, final String copyright, final License license) {
        mName = name;
        mUrl = url;
        mCopyright = copyright;
        mLicense = license;
    }

    // Setter / Getter

    public void setName(final String name) {
        mName = name;
    }

    public void setUrl(final String url) {
        mUrl = url;
    }

    public void setCopyright(final String copyright) {
        mCopyright = copyright;
    }

    public void setLicense(final License license) {
        mLicense = license;
    }

    public String getName() {
        return mName;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getCopyright() {
        return mCopyright;
    }

    public License getLicense() {
        return mLicense;
    }

    // Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(mName);
        dest.writeString(mUrl);
        dest.writeString(mCopyright);
        dest.writeSerializable(mLicense);
    }

    private Notice(final Parcel in) {
        mName = in.readString();
        mUrl = in.readString();
        mCopyright = in.readString();
        mLicense = (License) in.readSerializable();
    }

    public static Creator<Notice> CREATOR = new Creator<Notice>() {
        public Notice createFromParcel(final Parcel source) {
            return new Notice(source);
        }

        public Notice[] newArray(final int size) {
            return new Notice[size];
        }
    };
}

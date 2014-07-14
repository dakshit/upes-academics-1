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

package de.psdev.licensesdialog;/*
 * Copyright 2013 Philip Schiffer
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/de.psdev.licensesdialog.licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import android.content.Context;

import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

public final class NoticesHtmlBuilder {

    private final Context mContext;
    private Notices mNotices;
    private Notice mNotice;
    private String mStyle;
    private boolean mShowFullLicenseText;

    public static NoticesHtmlBuilder create(final Context context) {
        return new NoticesHtmlBuilder(context);
    }

    private NoticesHtmlBuilder(final Context context) {
        mContext = context;
        mStyle = context.getResources().getString(R.string.notices_default_style);
        mShowFullLicenseText = false;
    }

    public NoticesHtmlBuilder setNotices(final Notices notices) {
        mNotices = notices;
        mNotice = null;
        return this;
    }

    public NoticesHtmlBuilder setNotice(final Notice notice) {
        mNotice = notice;
        mNotices = null;
        return this;
    }

    public NoticesHtmlBuilder setStyle(final String style) {
        mStyle = style;
        return this;
    }

    public NoticesHtmlBuilder setShowFullLicenseText(final boolean showFullLicenseText) {
        mShowFullLicenseText = showFullLicenseText;
        return this;
    }

    public String build() {
        final StringBuilder noticesHtmlBuilder = new StringBuilder(500);
        appendNoticesContainerStart(noticesHtmlBuilder);
        if (mNotice != null) {
            appendNoticeBlock(noticesHtmlBuilder, mNotice);
        } else if (mNotices != null) {
            for (final Notice notice : mNotices.getNotices()) {
                appendNoticeBlock(noticesHtmlBuilder, notice);
            }
        } else {
            throw new IllegalStateException("no notice(s) set");
        }
        appendNoticesContainerEnd(noticesHtmlBuilder);
        return noticesHtmlBuilder.toString();
    }

    //

    private void appendNoticesContainerStart(final StringBuilder noticesHtmlBuilder) {
        noticesHtmlBuilder.append("<!DOCTYPE html><html><head>")
                .append("<style type=\"text/css\">").append(mStyle).append("</style>")
                .append("</head><body>");
    }

    private void appendNoticeBlock(final StringBuilder noticesHtmlBuilder, final Notice notice) {
        noticesHtmlBuilder.append("<ul><li>").append(notice.getName());
        final String currentNoticeUrl = notice.getUrl();
        if (currentNoticeUrl != null && currentNoticeUrl.length() > 0) {
            noticesHtmlBuilder.append(" (<a href=\"").append(currentNoticeUrl).append("\">").append(currentNoticeUrl).append("</a>)");
        }
        noticesHtmlBuilder.append("</li></ul>");
        noticesHtmlBuilder.append("<pre>");
        final String copyright = notice.getCopyright();
        if (copyright != null) {
            noticesHtmlBuilder.append(copyright).append("<br/><br/>");
        }
        noticesHtmlBuilder.append(getLicenseText(notice.getLicense())).append("</pre>");
    }

    private void appendNoticesContainerEnd(final StringBuilder noticesHtmlBuilder) {
        noticesHtmlBuilder.append("</body></html>");
    }

    private String getLicenseText(final License license) {
        if (license != null) {
            return mShowFullLicenseText ? license.getFullText(mContext) : license.getSummaryText(mContext);
        }
        return "";
    }
}

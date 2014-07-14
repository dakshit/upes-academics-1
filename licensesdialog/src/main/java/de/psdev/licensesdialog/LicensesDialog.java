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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.webkit.WebView;

import java.util.List;

import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

public class LicensesDialog {
    public static final Notice LICENSES_DIALOG_NOTICE = new Notice("de.psdev.licensesdialog.LicensesDialog", "http://psdev.de/de.psdev.licensesdialog.LicensesDialog", "Copyright 2013 Philip Schiffer",
        new ApacheSoftwareLicense20());

    private final Context mContext;
    private final String mTitleText;
    private final String mLicensesText;
    private final String mCloseText;

    //
    private DialogInterface.OnDismissListener mOnDismissListener;

    public LicensesDialog(final Context context, final int titleResourceId, final int rawNoticesResourceId, final int closeResourceId, final boolean showFullLicenseText, final boolean includeOwnLicense) {
        mContext = context;
        // Load defaults
        final String style = context.getString(R.string.notices_default_style);
        mTitleText = context.getString(titleResourceId);
        try {
            final Resources resources = context.getResources();
            if ("raw".equals(resources.getResourceTypeName(rawNoticesResourceId))) {
                final Notices notices = NoticesXmlParser.parse(resources.openRawResource(rawNoticesResourceId));
                if (includeOwnLicense) {
                    final List<Notice> noticeList = notices.getNotices();
                    noticeList.add(LICENSES_DIALOG_NOTICE);
                }
                mLicensesText = NoticesHtmlBuilder.create(mContext).setShowFullLicenseText(showFullLicenseText).setNotices(notices).setStyle(style).build();
            } else {
                throw new IllegalStateException("not a raw resource");
            }
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
        mCloseText = context.getString(closeResourceId);
    }

    public LicensesDialog(final Context context, final int rawNoticesResourceId, final boolean showFullLicenseText, final boolean includeOwnLicense) {
        this(context, R.string.notices_title, rawNoticesResourceId, R.string.notices_close, showFullLicenseText, includeOwnLicense);
    }

    public LicensesDialog(final Context context, final Notices notices, final boolean showFullLicenseText, final boolean includeOwnLicense) {
        mContext = context;
        // Load defaults
        final String style = context.getString(R.string.notices_default_style);
        mTitleText = context.getString(R.string.notices_title);
        try {
            if (includeOwnLicense) {
                final List<Notice> noticeList = notices.getNotices();
                noticeList.add(LICENSES_DIALOG_NOTICE);
            }
            mLicensesText = NoticesHtmlBuilder.create(mContext).setShowFullLicenseText(showFullLicenseText).setNotices(notices).setStyle(style).build();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
        mCloseText = context.getString(R.string.notices_close);
    }

    public LicensesDialog(final Context context, final String titleText, final String licensesText, final String closeText) {
        mContext = context;
        mTitleText = titleText;
        mLicensesText = licensesText;
        mCloseText = closeText;
    }

    public LicensesDialog setOnDismissListener(final DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
        return this;
    }

    public Dialog create() {
        //Get resources
        final WebView webView = new WebView(mContext);
        webView.loadDataWithBaseURL(null, mLicensesText, "text/html", "utf-8", null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
            .setTitle(mTitleText)
            .setView(webView)
            .setPositiveButton(mCloseText, new Dialog.OnClickListener() {
                public void onClick(final DialogInterface dialogInterface, final int i) {
                    dialogInterface.dismiss();
                }
            });
        final AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                if (mOnDismissListener != null) {
                    mOnDismissListener.onDismiss(dialog);
                }
            }
        });
        return dialog;
    }

    public void show() {
        create().show();
    }

    //


}

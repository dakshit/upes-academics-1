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

import de.psdev.licensesdialog.model.Notice;

public class SingleLicenseDialog extends LicensesDialog {

    public SingleLicenseDialog(final Context context, final Notice notice, final boolean showFullLicenseText) {
        super(context, getTitleText(context), getLicenseText(context, notice, showFullLicenseText), getCloseText(context));
    }

    public SingleLicenseDialog(final Context context, final String titleText, final String licensesText, final String closeText) {
        super(context, titleText, licensesText, closeText);
    }

    private static String getTitleText(final Context context) {
        return context.getString(R.string.notices_title);
    }

    private static String getLicenseText(final Context context, final Notice notice, final boolean showFullLicenseText) {
        final String defaultStyle = context.getString(R.string.notices_default_style);
        return NoticesHtmlBuilder.create(context).setNotice(notice).setShowFullLicenseText(showFullLicenseText).setStyle(defaultStyle).build();
    }

    private static String getCloseText(final Context context) {
        return context.getString(R.string.notices_close);
    }


}

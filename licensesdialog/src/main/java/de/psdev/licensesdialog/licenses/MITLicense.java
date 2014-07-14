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

package de.psdev.licensesdialog.licenses;

import android.content.Context;

import de.psdev.licensesdialog.R;

public class MITLicense extends License {

    private static final long serialVersionUID = 5673599951781482594L;

	@Override
	public String getName() {
		return "MIT License";
	}

    @Override
    public String getSummaryText(final Context context) {
        return getContent(context, R.raw.mit_summary);
    }

    @Override
    public String getFullText(final Context context) {
        return getContent(context, R.raw.mit_full);
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public String getUrl() {
        return "http://opensource.org/de.psdev.licensesdialog.licenses/MIT";
    }

}

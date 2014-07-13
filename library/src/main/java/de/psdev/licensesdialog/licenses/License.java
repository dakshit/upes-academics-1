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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

public abstract class License implements Serializable {

    private static final long serialVersionUID = 3100331505738956523L;

    public abstract String getName();

    public abstract String getSummaryText(final Context context);

    public abstract String getFullText(final Context context);

    public abstract String getVersion();

    public abstract String getUrl();

    //

    protected String getContent(final Context context, final int contentResourceId) {
        BufferedReader reader = null;
        try {
            final InputStream inputStream = context.getResources().openRawResource(contentResourceId);
            if (inputStream != null) {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                return toString(reader);
            }
            throw new IOException("Error opening license file.");
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    // Don't care.
                }
            }
        }
    }

    private String toString(final BufferedReader reader) throws IOException {
        final StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }

}

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

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

public final class NoticesXmlParser {

    private NoticesXmlParser() {
    }

    public static Notices parse(final InputStream inputStream) throws Exception {
        try {
            final XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag();
            return parse(parser);
        } finally {
            inputStream.close();
        }
    }

    private static Notices parse(final XmlPullParser parser) throws IOException, XmlPullParserException {
        final Notices notices = new Notices();
        parser.require(XmlPullParser.START_TAG, null, "notices");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            // Starts by looking for the entry tag
            if ("notice".equals(name)) {
                notices.addNotice(readNotice(parser));
            } else {
                skip(parser);
            }
        }
        return notices;
    }

    private static Notice readNotice(final XmlPullParser parser) throws IOException,
        XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "notice");
        String name = null;
        String url = null;
        String copyright = null;
        License license = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String element = parser.getName();
            if ("name".equals(element)) {
                name = readName(parser);
            } else if ("url".equals(element)) {
                url = readUrl(parser);
            } else if ("copyright".equals(element)) {
                copyright = readCopyright(parser);
            } else if ("license".equals(element)) {
                license = readLicense(parser);
            } else {
                skip(parser);
            }
        }
        return new Notice(name, url, copyright, license);
    }

    private static String readName(final XmlPullParser parser) throws IOException, XmlPullParserException {
        return readTag(parser, "name");
    }

    private static String readUrl(final XmlPullParser parser) throws IOException, XmlPullParserException {
        return readTag(parser, "url");
    }

    private static String readCopyright(final XmlPullParser parser) throws IOException, XmlPullParserException {
        return readTag(parser, "copyright");
    }

    private static License readLicense(final XmlPullParser parser) throws IOException, XmlPullParserException {
        final String license = readTag(parser, "license");
        return LicenseResolver.read(license);
    }

    private static String readTag(final XmlPullParser parser, final String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tag);
        final String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, tag);
        return title;
    }

    private static String readText(final XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private static void skip(final XmlPullParser parser) {
    }
}

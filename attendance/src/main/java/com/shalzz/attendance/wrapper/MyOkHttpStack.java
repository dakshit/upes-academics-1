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

package com.shalzz.attendance.wrapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.Proxy;
import java.security.KeyStore;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.toolbox.HurlStack;
import com.shalzz.attendance.Miscellaneous;
import com.shalzz.attendance.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

public class MyOkHttpStack extends HurlStack {
    private final OkUrlFactory okUrlFactory;

	public MyOkHttpStack() {
        this(new OkUrlFactory(new OkHttpClient()));
    }

    public MyOkHttpStack(OkUrlFactory okUrlFactory) {
        if (okUrlFactory == null) {
            throw new NullPointerException("Client must not be null.");
        }
        this.okUrlFactory = okUrlFactory;
    }


    @Override
	protected HttpURLConnection createConnection(URL url) throws IOException {
		if(Miscellaneous.useProxy())
		{
			Log.i("MyOkHttpStack","Using Proxy!");
			Toast.makeText(MyVolley.getAppContext(), "Using Proxy!", Toast.LENGTH_LONG).show();
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.ddn.upes.ac.in", 8080));
            okUrlFactory.client().setProxy(proxy);
		}
		else if(okUrlFactory.client().getProxy()!=null)
		{
			Toast.makeText(MyVolley.getAppContext(), "Proxy removed!", Toast.LENGTH_LONG).show();
			Log.i("MyOkHttpStack","Proxy removed!");
            okUrlFactory.client().setProxy(null);
		}

        /* fix the SslHandShake exception */
        MySSLSocketFactory sslf;
        try {
            KeyStore ks = MySSLSocketFactory.getKeystoreOfCA(MyVolley.getAppContext().getResources().openRawResource(R.raw.gd_bundle));
            sslf = new MySSLSocketFactory(ks);
            okUrlFactory.client().setSslSocketFactory(sslf.getSSLSocketFactory());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

		return okUrlFactory.open(url);
	} 
}

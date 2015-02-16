package com.shalzz.attendance.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMRequest;
import com.shalzz.attendance.R;

public class AdFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ad, container, false);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        //Find the ad view for reference
        MMAdView adViewFromXml = (MMAdView) getView().findViewById(R.id.adView);
        MMRequest request = new MMRequest();
        request.setEducation(MMRequest.EDUCATION_SOME_COLLEGE);
        request.setEthnicity(MMRequest.ETHNICITY_INDIAN);
        request.setAge("23");
        adViewFromXml.setMMRequest(request);
        adViewFromXml.getAd();

    }

    @Override
    public void onPause() {
        super.onPause();
        onDestroy();
    }
}

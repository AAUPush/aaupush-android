package com.aaupush.aaupush;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    public static AboutFragment getInstance(){
        return new AboutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_fragment, container, false);

        view.findViewById(R.id.contribute_card).setOnClickListener(this);
        view.findViewById(R.id.open_source_licenses_card).setOnClickListener(this);
        view.findViewById(R.id.web_page_card).setOnClickListener(this);
        view.findViewById(R.id.copy_right_card).setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.contribute_card:
                // GitHub url
                String url = "http://github.com/AAUPush/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            case R.id.open_source_licenses_card:

                break;
            case R.id.web_page_card:
                // AAUPush url
                String pushUrl = "http://aaupush.com/";
                Intent i2 = new Intent(Intent.ACTION_VIEW);
                i2.setData(Uri.parse(pushUrl));
                startActivity(i2);
                break;
            case R.id.copy_right_card:

                break;
        }
    }
}

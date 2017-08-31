package com.aaupush.aaupush;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FirstRunIntro#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FirstRunIntro extends Fragment {

    public FirstRunIntro() {
        // Required empty public constructor
    }


    public static FirstRunIntro newInstance() {
        return new FirstRunIntro();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_intro, container, false);


        // Setup Button
        Button startSetup = (Button) view.findViewById(R.id.start_setup);
        startSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Request Permission
                requestPermissions(
                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        222);
            }
        });


        return view;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Create aaupush directory
            File createFolder = new File(Environment.getExternalStorageDirectory(), PushUtils.ROOT_FOLDER);
            if (!createFolder.exists()) {
                createFolder.mkdirs();
            }


            // Start second setup fragment
            FragmentManager fragmentManager = getFragmentManager();
            Fragment fragment = FirstRunSetUp.newInstance();
            fragmentManager.beginTransaction().replace(R.id.first_run_activity, fragment).addToBackStack(null).commit();

        } else {
            Snackbar.make(getView(), "You need to accept the permissions",
                    Snackbar.LENGTH_LONG).
                    setAction("TRY AGAIN", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request Permission
                            requestPermissions(
                                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    222
                            );
                        }
                    }).show();
        }
    }
}

package com.aaupush.aaupush;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.aaupush.aaupush.FirstRunAndSetup.FirstRunActivity;
import com.aaupush.aaupush.Setting.SettingActivity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    PagerAdapter pagerAdapter;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Init SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PushUtils.SP_KEY_NAME, MODE_PRIVATE);

        //...
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Check if this is the first time the app is launched
        if (preferences.getBoolean(PushUtils.SP_IS_FIRST_RUN, true)){
            // Launch FirstRunActivity
            startActivity(new Intent(this, FirstRunActivity.class));
            finish();
            return;
        }


        setContentView(R.layout.activity_main);

        startService(new Intent(this, PushService.class));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Fragment Pager
        // Initialize a list of the fragments
        final List<Fragment> fragmentList = new ArrayList<>();

        // Add the fragments to the list
        fragmentList.add(AnnouncementFragment.newInstance());
        fragmentList.add(MaterialFragment.newInstance());

        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), this, fragmentList);

        viewPager = (ViewPager) findViewById(R.id.main_pager);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        for( int i = 0; i < tabLayout.getTabCount(); i++ ){
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(pagerAdapter.getTabView(i));
            }
        }


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (MaterialFragment.FRAGMENT_STATUS ==
                MaterialFragment.FRAGMENT_STATUS_FOLDER_CONTENT) {
            sendBroadcast(new Intent(PushUtils.BACK_PRESSED_ON_FOLDER_VIEW_BROADCAST));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), SettingActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_announcement) {
            viewPager.setCurrentItem(0, true);
        } else if (id == R.id.nav_material) {
            viewPager.setCurrentItem(1, true);
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(getApplicationContext(), SettingActivity.class));
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_about) {
            AboutFragment aboutFragment = AboutFragment.getInstance();
            aboutFragment.show(getSupportFragmentManager(), "About");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Fragments onDetach is not called when home button is pressed. So we need to set
        // the is_running fragments to false here also so that notifications are raised
        SharedPreferences.Editor editor = getSharedPreferences(PushUtils.SP_KEY_NAME,
                Context.MODE_PRIVATE).edit();
        editor.putBoolean(PushUtils.SP_IS_MATERIAL_FRAGMENT_RUNNING, false);
        editor.putBoolean(PushUtils.SP_IS_ANNOUNCEMENT_FRAGMENT_RUNNING, false);
        editor.apply();
    }

    class PagerAdapter extends FragmentPagerAdapter {

        Resources resources = getResources();
        String a = "Announcement";
        String b = "Materials";
        String tabTitles[] = new String[]{a, b};

        Context context;

        List<Fragment> fragmentList;

        public PagerAdapter(FragmentManager fragmentManager, Context context, List<Fragment> fragmentList){
            super(fragmentManager);
            this.context = context;
            this.fragmentList = fragmentList;
        }
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle( int position ){
            return tabTitles[position];
        }

        public View getTabView (int position){
            View tab = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_tab, null);
            TextView title = (TextView) tab.findViewById(R.id.custom_text);
            title.setText(tabTitles[position]);
            return tab;
        }

    }

}
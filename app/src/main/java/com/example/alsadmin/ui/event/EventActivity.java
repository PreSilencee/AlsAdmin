package com.example.alsadmin.ui.event;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.example.alsadmin.Adapter.ViewPagerAdapter;
import com.example.alsadmin.R;
import com.google.android.material.tabs.TabLayout;

public class EventActivity extends AppCompatActivity {

    //toolbar
    Toolbar customizeToolbar;

    //view pager
    ViewPager eventViewPager;

    //maintablayout
    TabLayout eventTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        customizeToolbar = findViewById(R.id.customizeToolbar);
        customizeToolbar.setNavigationIcon(R.mipmap.alslogo);
        customizeToolbar.setTitle("Event");
        setSupportActionBar(customizeToolbar);

        eventViewPager = findViewById(R.id.eventViewPager);
        eventTabLayout = findViewById(R.id.eventTabLayout);

        //set up view pager
        setupViewPager(eventViewPager);

        //set up tab layout into view pager
        eventTabLayout.setupWithViewPager(eventViewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        adapter.addFragment(new EventAllFragment(), "All");
        adapter.addFragment(new EventAvailableFragment(), "Available");
        adapter.addFragment(new EventUpcomingFragment(), "Upcoming");
        adapter.addFragment(new EventPendingFragment(), "Pending");
        adapter.addFragment(new EventDeclinedFragment(), "Declined");

        viewPager.setAdapter(adapter);
    }
}
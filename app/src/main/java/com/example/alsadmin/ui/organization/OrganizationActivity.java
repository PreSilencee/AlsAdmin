package com.example.alsadmin.ui.organization;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.example.alsadmin.Adapter.ViewPagerAdapter;
import com.example.alsadmin.R;
import com.example.alsadmin.ui.event.EventAllFragment;
import com.google.android.material.tabs.TabLayout;

public class OrganizationActivity extends AppCompatActivity {

    //toolbar
    Toolbar organizationCustomizeToolbar;

    //view pager
    ViewPager organizationViewPager;

    //maintablayout
    TabLayout organizationTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization);

        organizationCustomizeToolbar = findViewById(R.id.customizeToolbar);
        organizationCustomizeToolbar.setNavigationIcon(R.mipmap.alslogo);
        organizationCustomizeToolbar.setTitle("Organization");
        setSupportActionBar(organizationCustomizeToolbar);

        organizationViewPager = findViewById(R.id.organizationViewPager);
        organizationTabLayout = findViewById(R.id.organizationTabLayout);

        //set up view pager
        setupViewPager(organizationViewPager);

        //set up tab layout into view pager
        organizationTabLayout.setupWithViewPager(organizationViewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        adapter.addFragment(new OrganizationAllFragment(), "All");
        adapter.addFragment(new OrganizationPendingFragment(), "Pending");
        adapter.addFragment(new OrganizationVerifiedFragment(), "Verified");

        viewPager.setAdapter(adapter);
    }
}
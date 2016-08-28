package com.transitangel.transitangel.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.transitangel.transitangel.Intent.ShakerService;
import com.transitangel.transitangel.Manager.TransitLocationManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.api.TripHelperApiFactory;
import com.transitangel.transitangel.api.TripHelplerRequestInterceptor;
import com.transitangel.transitangel.schedule.ScheduleActivity;
import com.transitangel.transitangel.utils.TAConstants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;

public class HomeActivity extends AppCompatActivity {

    public static final String ACTION_SHOW_ONGOING = "ACTION_SHOW_ONGOING";
    public static final String ACTION_TRIP_CANCELLED = "ACTION_TRIP_CANCELLED";
    public static final int ALARM_REQUEST_CODE = 111;
    private static SharedPreferences mSharedPreference;

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.home_pager)
    ViewPager homePager;

    @BindView(R.id.fabCancelTrip)
    FloatingActionButton fabCancelTrip;

    private HomePagerAdapter adapter;
    private TripHelperApiFactory mTripHelperApiFactory;
    private CompositeSubscription mSubscription = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        init();
        mTripHelperApiFactory = new TripHelperApiFactory(new TripHelplerRequestInterceptor(this));
//        TestManager.getSharedInstance().executeSampleAPICalls(this);
        Intent serviceIntent = new Intent(this, ShakerService.class);
        startService(serviceIntent);

    }

    private void init() {
        mSharedPreference = getApplicationContext().getSharedPreferences(TAConstants.SharedPrefGeofences, Context.MODE_PRIVATE);
        TabLayout.Tab nearbyTab = tabLayout.newTab();
        nearbyTab.setText("Near By");
        nearbyTab.setContentDescription("Near by");
        nearbyTab.setIcon(R.drawable.ic_explore_white_48dp);
        tabLayout.addTab(nearbyTab);

        TabLayout.Tab recentsTab = tabLayout.newTab();
        recentsTab.setText("Recents");
        recentsTab.setContentDescription("Recents");
        recentsTab.setIcon(R.drawable.ic_restore_white_48dp);
        tabLayout.addTab(recentsTab);

        TabLayout.Tab onGoingTab = tabLayout.newTab();
        onGoingTab.setText("Live Trip");
        onGoingTab.setContentDescription("Live trip");
        onGoingTab.setIcon(R.drawable.ic_play_arrow_white_48dp);
        tabLayout.addTab(onGoingTab);

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        adapter = new HomePagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        homePager.setAdapter(adapter);
        homePager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                homePager.setCurrentItem(tab.getPosition());
                Fragment fragment = adapter.getRegisteredFragment(tab.getPosition());
                if(fragment instanceof LiveTripFragment) {
                    fabCancelTrip.show();
                    ((LiveTripFragment)fragment).onSelected();
                } else {
                    fabCancelTrip.hide();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        String action = getIntent().getAction();
        if (!TextUtils.isEmpty(action)) {
            if (action.equalsIgnoreCase(ACTION_SHOW_ONGOING)) {
                Toast.makeText(this, "Show on going screen here.", Toast.LENGTH_LONG).show();
                launchOnGoingScreen();
            } else if (action.equalsIgnoreCase(ACTION_TRIP_CANCELLED)) {
                Toast.makeText(this, "Show on cancelled trip clicked.", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void launchOnGoingScreen() {
        // Set the current item to live notifications.
        homePager.setCurrentItem(2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == TransitLocationManager.GET_LOCATION_REQUEST_CODE) {
            TransitLocationManager.getSharedInstance().getCurrentLocation(this, new TransitLocationManager.LocationResponseHandler() {
                @Override
                public void OnLocationReceived(boolean isSuccess, LatLng latLng) {
                    //testHandleOnLocationReceived(isSuccess,latLng);
                }
            });
        } else if (requestCode == TransitLocationManager.GET_UPDATES_LOCATION_REQUEST_CODE) {
            TransitLocationManager.getSharedInstance().getLocationUpdates(this);
        }
    }


    @OnClick(R.id.search)
    public void onSearchClicked() {
        onScheduleClicked();
    }

    public void onScheduleClicked() {
        Intent intent = new Intent(this, ScheduleActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @OnClick(R.id.fabCancelTrip)
    public void onCancelTrip() {
        Fragment fragment = adapter.getRegisteredFragment(tabLayout.getSelectedTabPosition());
        if(fragment instanceof LiveTripFragment) {
            fabCancelTrip.show();
            ((LiveTripFragment)fragment).onCancelTrip();
        } else {
            fabCancelTrip.hide();
        }
    }

    @Override
    protected void onDestroy() {
        if (mSubscription != null)
            mSubscription.clear();
        super.onDestroy();
    }
}

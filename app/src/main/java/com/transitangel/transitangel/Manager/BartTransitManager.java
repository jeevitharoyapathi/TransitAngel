package com.transitangel.transitangel.Manager;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.transitangel.transitangel.model.Transit.Service;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by vidhurvoora on 8/18/16.
 */
public class BartTransitManager extends TransitManager {
    private static BartTransitManager sInstance;

    public static synchronized BartTransitManager getSharedInstance() {
        if ( sInstance == null ) {
            sInstance = new BartTransitManager();
            httpClient = new AsyncHttpClient();
            mTransitType = TAConstants.TRANSIT_TYPE.BART;
        }
        return sInstance;
    }

    public ArrayList<Stop> getStops() {
        //load the json from files
        return getStops("Bart_Stops.json");
    }

    public HashMap<String, Stop> getStopLookup() {
        if ( mStopLookup == null || mStopLookup.size() == 0 ) {
            getStops();
        }
        return mStopLookup;
    }

    private void populateServices() {
        ArrayList<String> filenames = new ArrayList<String>() {{
            add("Timetable_Bart_BayPt_SFIA.json");
            add("Timetable_Bart_Cols_Oakl.json");
            add("Timetable_Bart_Daly_Dublin.json");
            add("Timetable_Bart_Daly_Fremont.json");
            add("Timetable_Bart_Dublin_Daly.json");
            add("Timetable_Bart_Fremont_Daly.json");
            add("Timetable_Bart_Fremont_Rich.json");
            add("Timetable_Bart_Mill_Rich.json");
            add("Timetable_Bart_Oak_Cols.json");
            add("Timetable_Bart_Rich_Fremont.json");
            add("Timetable_Bart_Rich_Mill.json");
            add("Timetable_Bart_SFIA_BayPt.json");
        }};

        ArrayList<TAConstants.SERVICE_TYPE> serviceTypes = new ArrayList<TAConstants.SERVICE_TYPE>() {{
            add(TAConstants.SERVICE_TYPE.BART_BAYPT_SFIA);
            add(TAConstants.SERVICE_TYPE.BART_COLS_OAKL);
            add(TAConstants.SERVICE_TYPE.BART_DALY_DUBLIN);
            add(TAConstants.SERVICE_TYPE.BART_DALY_FREMONT);
            add(TAConstants.SERVICE_TYPE.BART_DUBLIN_DALY);
            add(TAConstants.SERVICE_TYPE.BART_FREMONT_DALY);
            add(TAConstants.SERVICE_TYPE.BART_FREMONT_RICH);
            add(TAConstants.SERVICE_TYPE.BART_MILL_RICH);
            add(TAConstants.SERVICE_TYPE.BART_OAK_COLS);
            add(TAConstants.SERVICE_TYPE.BART_RICH_FREMONT);
            add(TAConstants.SERVICE_TYPE.BART_RICH_MILL);
            add(TAConstants.SERVICE_TYPE.BART_SFIA_BAYPT);
        }};

        populateServices(filenames,serviceTypes);
    }

    public ArrayList<Train> fetchTrains(
            String fromStopId //from station
            , String toStopId //to station
            , int limit // number of results to return, 0 or -ve implies no limit
            , Date leavingAfter //determines its a weekday/weekend , defaults to today
            , boolean shouldIncludeAllTrainsForThatDay // includes all the trains for that day irrespective of time

    ) {
        return fetchTrains(fromStopId
                ,toStopId
                ,limit
                ,leavingAfter
                ,shouldIncludeAllTrainsForThatDay
                ,getServices()
        );
    }

    //Given a destination and the hour limit, fetch all the trains which will arrive at the destination
    //within that hour limit
    //TODO missed an important part, we need to know if it is north bound or south bound.
    public ArrayList<Train> fetchTrainsArrivingAtDestination(
            String toStopId //to station
            , int hourLimit //

    ) {
        return fetchTrainsArrivingAtDestination(
                toStopId
                ,hourLimit
                ,getServices()
        );
    }

    public ArrayList<Train> fetchTrainsDepartingFromStation(
            String toStopId //to station
            , int hourLimit //
    ) {
        return fetchTrainsDepartingFromStation(toStopId,hourLimit,getServices());
    }

    public ArrayList<Service> getServices() {
        if (mServices == null) {
            populateServices();
        }
        return mServices;
    }

    public Stop getNearestStop(double lat, double lon) {
        return  getNearestStop(lat,lon,getStops());
    }

    public void fetchTrainsDepartingFromNearestStation(
            Context context
            , int hourLimit //
            , TrainsDepartingFromStationResponseHandler handler
    ) {

        //check if location is accessible
        if ( !TransitLocationManager.getSharedInstance().isLocationAccessible() ) {
            handler.trainsDeparting(false,null);
        }

        ArrayList<Train> departingTrains = new ArrayList<>();
        getNearestStop(context, new NearestStopResponseHandler() {
            @Override
            public void nearestStop(boolean isSuccesss, Stop stop) {
                if ( isSuccesss ) {
                    ArrayList<Train> trains = fetchTrainsDepartingFromStation(stop.getId(),hourLimit,getServices());
                    if ( trains != null ) {
                        handler.trainsDeparting(true,trains);
                    }
                    else {
                        handler.trainsDeparting(false,null);
                    }
                }
                else {
                    handler.trainsDeparting(false,null);
                }
            }
        });


    }

    public  void getNearestStop(Context context,NearestStopResponseHandler handler) {

        //check if location is accessible
        if ( !TransitLocationManager.getSharedInstance().isLocationAccessible() ) {
            handler.nearestStop(false,null);
        }

        TransitLocationManager.getSharedInstance().getCurrentLocation(context, new TransitLocationManager.LocationResponseHandler() {
            @Override
            public void OnLocationReceived(boolean isSuccess, LatLng latLng) {
                if ( isSuccess ) {
                    Stop stop  = getNearestStop(latLng.latitude,latLng.longitude,getStops());
                    if ( stop != null ) {
                        handler.nearestStop(true,stop);
                    }
                    else {
                        handler.nearestStop(false,null);
                    }

                }
                else {
                    handler.nearestStop(false,null);
                }

            }
        });
    }

}

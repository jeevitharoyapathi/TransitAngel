package com.transitangel.transitangel.Manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.transitangel.transitangel.model.Transit.Line;
import com.transitangel.transitangel.model.Transit.Service;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.TrafficNewsAlert;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.model.Transit.Trip;
import com.transitangel.transitangel.model.Transit.Tweet;
import com.transitangel.transitangel.utils.TAConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by vidhurvoora on 8/18/16.
 */
public class TransitManager {


    private static TransitManager sInstance;

    protected String apiBaseUrl = "http://api.511.org/transit";
    protected String apiKey = "a24b8b61-63e2-4571-a41b-11490cd9ada9";

    protected Context mApplicationContext;
    public static TAConstants.TRANSIT_TYPE mTransitType;

    public HashMap<String, Stop> mStopLookup = new HashMap<String, Stop>();

    public static AsyncHttpClient httpClient;
    public ArrayList<Service> mServices;

    public static synchronized TransitManager getSharedInstance() {
        if ( sInstance == null ) {
            sInstance = new TransitManager();

        }
        return sInstance;
    }

    public void setup(Context context) {
        mApplicationContext = context;
    }

    public RequestParams getBaseParams() {
        RequestParams baseParams = new RequestParams();
        baseParams.put("api_key",apiKey);
        baseParams.put("format","json");
        if ( mTransitType == TAConstants.TRANSIT_TYPE.BART) {
            baseParams.put("operator_id","BART");
        }
        else if ( ( mTransitType == TAConstants.TRANSIT_TYPE.CALTRAIN)) {
            baseParams.put("operator_id","Caltrain");
        }

        return baseParams;
    }

    protected ArrayList<Line> fetchLineArrFromJson(JSONArray lineArr) throws JSONException {
        ArrayList<Line> lines = new ArrayList<Line>();
        for (int i=0;i<lineArr.length();i++){
            JSONObject lineObj = lineArr.getJSONObject(i);
            Line line = new Line(lineObj);
            lines.add(line);
        }
        return lines;
    }

    protected ArrayList<Stop> fetchStopArrFromJson(JSONArray stopArr) throws JSONException {
        ArrayList<Stop> stops = new ArrayList<Stop>();
        for (int i=0;i<stopArr.length();i++){
            JSONObject stopObj = stopArr.getJSONObject(i);
            Stop stop = new Stop(stopObj);
            stops.add(stop);
        }
        return stops;
    }

    public void fetchLines(LineResponseHandler handler){
        String lineUrl = apiBaseUrl + "/lines";
        httpClient.get(lineUrl,getBaseParams(), new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("JSON response",response.toString());
                try {
                    JSONArray lineArr = response.getJSONArray(0);
                    ArrayList<Line> lines = fetchLineArrFromJson(lineArr);
                    handler.OnLinesResponseReceived(true,lines);
                } catch (JSONException e) {
                    e.printStackTrace();
                    handler.OnLinesResponseReceived(false,null);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                handler.OnLinesResponseReceived(false,null);
            }
        });
    }

    public void fetchLatestTrafficNewsAlerts(TrafficNewsAlertResponseHandler handler) {

        String newsUrl = "https://proxy-prod.511.org/api-proxy/api/v1/traffic/news/";

        httpClient.get(newsUrl, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("JSON response",response.toString());
                try {
                    JSONArray newsArr = response.getJSONArray("News");
                    ArrayList<TrafficNewsAlert> trafficNewsAlerts = new ArrayList<TrafficNewsAlert>();
                    for (int i = 0; i< newsArr.length(); i++ ) {
                        JSONObject newsObj = newsArr.getJSONObject(i);
                        TrafficNewsAlert trafficNewsAlert = new TrafficNewsAlert(newsObj);
                        trafficNewsAlerts.add(trafficNewsAlert);
                    }

                    handler.onNewsAlertsReceived(true, trafficNewsAlerts);

                } catch (JSONException e) {
                    e.printStackTrace();
                    handler.onNewsAlertsReceived(false,null);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                handler.onNewsAlertsReceived(false,null);
            }
        });
    }

    public void fetchTweetAlerts(TweetAlertResponseHandler handler) {

        String newsUrl = "https://proxy-prod.511.org/api-proxy/api/v1/common/twitter/";

        httpClient.get(newsUrl, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    JSONArray tweetsArr = response.getJSONArray("Timeline");
                    ArrayList<Tweet> tweetAlerts = new ArrayList<Tweet>();
                    for (int i = 0; i< tweetsArr.length(); i++ ) {
                        JSONObject tweetObj = tweetsArr.getJSONObject(i);
                        Tweet tweet = new Tweet(tweetObj);
                        tweetAlerts.add(tweet);
                    }

                    handler.onTweetsReceived(true, tweetAlerts);

                } catch (JSONException e) {
                    e.printStackTrace();
                    handler.onTweetsReceived(false, null);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                handler.onTweetsReceived(false, null);
            }
        });
    }


    public void fetchStops(StopResponseHandler handler){
        String stopUrl = apiBaseUrl + "/stops";
        httpClient.get(stopUrl,getBaseParams(), new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {

                    JSONArray stopArr = response.getJSONObject("Contents").getJSONObject("dataObjects").getJSONArray("ScheduledStopPoint");
                    ArrayList<Stop> stops = fetchStopArrFromJson(stopArr);
                    handler.OnStopsResponseReceived(true,stops);
                } catch (JSONException e) {
                    e.printStackTrace();
                    handler.OnStopsResponseReceived(false,null);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                handler.OnStopsResponseReceived(false,null);
            }
        });
    }


    public String loadJSONFromAsset(String fileName) {
        String json = null;
        try {

            InputStream is = mApplicationContext.getAssets().open(fileName);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    protected ArrayList<Stop> getStops(String filename) {
        //load the json from files
        try {
            String jsonStopsString = loadJSONFromAsset(filename);
            JSONObject stopsObj = new JSONObject(jsonStopsString);
            JSONArray stopArr = stopsObj.getJSONObject("Contents").getJSONObject("dataObjects").getJSONArray("ScheduledStopPoint");
            ArrayList<Stop> stops = fetchStopArrFromJson(stopArr);
            //populate hashmap
            for (Stop stop : stops) {
                mStopLookup.put(stop.getId(), stop);
            }

            return stops;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void populateServices(ArrayList<String> filenames,ArrayList<TAConstants.SERVICE_TYPE> serviceTypes) {
        try {
            mServices = new ArrayList<Service>();

            int i= 0;
            for (String filename : filenames) {
                String jsonString = loadJSONFromAsset(filename);
                JSONObject serviceObj = new JSONObject(jsonString);
                Service service = new Service(serviceObj, serviceTypes.get(i));
                mServices.add(service);
                i++;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //NOTE from Vidhur: I have edited the original model ( for Local and Babybullet) and removed few holiday schedule trains and other
    //weekend schedule train to make the fetchup logic simpler.
    //We can add the original model back once we understand the duplicate weekend train schedules in the current model
    protected ArrayList<Train> fetchTrains(
            String fromStopId //from station
            , String toStopId //to station
            , int limit // number of results to return, 0 or -ve implies no limit
            , Date leavingAfter //determines its a weekday/weekend , defaults to today
            , boolean shouldIncludeAllTrainsForThatDay // includes all the trains for that day irrespective of time
            , ArrayList<Service> trainServices

    ) {

        if (leavingAfter == null) {
            //if leaving after  is null default it to today
            leavingAfter = new Date();
        }
        ArrayList<Train> trains = new ArrayList<Train>();
        //foreach service
        ArrayList<Service> services = trainServices;
        for (Service service : services) {
            //fetch the trains
            //check if weekday or weekend.
            String day = new SimpleDateFormat("EE").format(leavingAfter);

            ArrayList<Train> trainList = new ArrayList<Train>();
            if ( day.contains("Sat") || day.contains("Sun")) {
                trainList = service.getWeekendTrains();
            }
            else {
                trainList = service.getWeekendTrains();
            }

            for (Train train : trainList) {
                //fetch stops
                TrainStop fromStop = null;
                TrainStop toStop = null;

                ArrayList<TrainStop> trainStopList = train.getTrainStops();

                //check if the train has the fromStopId and toStopId
                // and check if the fromStopOrder < toStopOrder
                for (TrainStop trainStop : trainStopList) {

                    if (trainStop.getStopId().equals(fromStopId)) {
                        fromStop = trainStop;
                    } else if (trainStop.getStopId().equals(toStopId)) {
                        toStop = trainStop;
                    }

                    //check the order
                    if (fromStop != null
                            && toStop != null
                            && fromStop.getStopOrder() < toStop.getStopOrder()) {

                        if ( shouldIncludeAllTrainsForThatDay) {
                            trains.add(train);

                            if (limit > 0 && trains.size() == limit) {
                                return trains;
                            }
                            //reset the from stop and to stop to avoid duplicates
                            fromStop = null;
                            toStop = null;
                        }
                        else {
                            //matches our list of train
                            //check if arrival time is greater than the from time
                            String arrivalTimeStr = fromStop.getArrrivalTime();
                            String[] parts = arrivalTimeStr.split(":");
                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                            cal.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                            Date arrivalTime = cal.getTime();
                            //add only if the arrival time is after the leavingAfter time
                            if (arrivalTime.after(leavingAfter)) {
                                trains.add(train);

                                if (limit > 0 && trains.size() == limit) {
                                    return trains;
                                }

                                //reset the from stop and to stop to avoid duplicates
                                fromStop = null;
                                toStop = null;
                            }
                        }
                    }
                }
            }
        }

        //return train list
        return trains;
    }

    //Given a destination and the hour limit, fetch all the trains which will arrive at the destination
    //within that hour limit
    //TODO missed an important part, we need to know if it is north bound or south bound.
    protected ArrayList<Train> fetchTrainsArrivingAtDestination(
            String toStopId //to station
            , int hourLimit //
            , ArrayList<Service> trainServices
    ) {

        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        cal.add(Calendar.HOUR_OF_DAY, hourLimit); //adds the hour
        Date arrivingOnOrBefore = cal.getTime();
        ArrayList<Train> trains = new ArrayList<Train>();

        Date arrivingAfter = new Date(); //the train should arrive after now

        //foreach service
        ArrayList<Service> services = trainServices;
        for (Service service : services) {
            //fetch the trains
            //check if weekday or weekend.
            String day = new SimpleDateFormat("EE").format(arrivingOnOrBefore);

            ArrayList<Train> trainList = new ArrayList<Train>();
            if ( day.contains("Sat") || day.contains("Sun")) {
                trainList = service.getWeekendTrains();
            }
            else {
                trainList = service.getWeekendTrains();
            }

            for (Train train : trainList) {

                TrainStop toStop = null;

                ArrayList<TrainStop> trainStopList = train.getTrainStops();

                //check if the train has the fromStopId and toStopId
                // and check if the fromStopOrder < toStopOrder
                for (TrainStop trainStop : trainStopList) {

                    if (trainStop.getStopId().equals(toStopId)) {
                        toStop = trainStop;

                        //get arrival time
                        String arrivalTimeStr = toStop.getArrrivalTime();
                        String[] parts = arrivalTimeStr.split(":");
                        Calendar arrivalCal = Calendar.getInstance();
                        arrivalCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                        arrivalCal.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                        Date arrivalTime = arrivalCal.getTime();

                        //TODO we need to know if it is a northbound or southbound
                        if (arrivalTime.before(arrivingOnOrBefore)
                                && arrivalTime.after(arrivingAfter)
                                && toStop.getStopOrder() > 1) {
                            trains.add(train);
                        }

                    }
                }
            }
        }

        //return train list
        return trains;
    }

    public HashMap<String, Stop> getStopLookup() {
        return mStopLookup;
    }

    //save recents

    public ArrayList<Trip> fetchRecents() {
        //ArrayList<Trip> recents = new ArrayList<Trip>();
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Trip>>(){}.getType();
        SharedPreferences recentPref = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
        String recentJSON = recentPref.getString("recents","");
        ArrayList<Trip> recents = gson.fromJson(recentJSON,type);
        return recents;
    }

    public void saveRecent(Trip trip) {
        SharedPreferences recentPref = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
        SharedPreferences.Editor prefsEditor = recentPref.edit();
        //fetch recents
        String existingRecent = recentPref.getString("recents","");
        Type type = new TypeToken<ArrayList<Trip>>(){}.getType();
        Gson gson = new Gson();
        ArrayList<Trip> recents = gson.fromJson(existingRecent,type);
        if ( recents == null ) {
            recents = new ArrayList<Trip>();
        }
        recents.add(trip);

        String newRecent = gson.toJson(recents,type);
        prefsEditor.putString("recents",newRecent);
        prefsEditor.commit();
    }

    //ref:http://stackoverflow.com/questions/8383863/how-can-find-nearest-place-from-current-location-from-given-data
    protected double distance(double lat1, double lon1, double lat2, double lon2) {
        // haversine great circle distance approximation, returns meters
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60; // 60 nautical miles per degree of seperation
        dist = dist * 1852; // 1852 meters per nautical mile
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    protected Stop getNearestStop(double lat, double lon, ArrayList<Stop> stops) {
        Stop nearestStop = null;
        double minDistance = 0;

        for (Stop stop : stops ) {
            double stopLat = Double.parseDouble(stop.getLatitude());
            double stopLon = Double.parseDouble(stop.getLongitude());
            double distanceToStop = distance(lat,lon,stopLat,stopLon);
            if ( nearestStop == null ) {
                nearestStop = stop;
                minDistance = distanceToStop;
            }
            else if ( distanceToStop < minDistance ) {
                minDistance = distanceToStop;
                nearestStop = stop;
            }
        }

        return nearestStop;
    }

}
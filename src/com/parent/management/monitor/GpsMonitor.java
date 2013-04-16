package com.parent.management.monitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.parent.management.ManagementApplication;
import com.parent.management.R;
import com.parent.management.db.ManagementProvider;

public class GpsMonitor extends Monitor {
    private static final String TAG = ManagementApplication.getApplicationTag() + "." +
            GpsMonitor.class.getSimpleName();
    Context mContext = null;
    private LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    
    private BDLocation mLastLocation = null;
     
    public GpsMonitor(Context context) {
        super(context);
        mContext = ManagementApplication.getContext();
        mLocClient = new LocationClient(mContext);
        mLocClient.registerLocationListener(myListener);
    }
    
    @Override
    public void startMonitoring() {
        setLocationOption();
        if (!mLocClient.isStarted()) {
            Log.d(TAG, "start locClient");
            mLocClient.start();
        }
        else {
            mLocClient.requestLocation();
        }
        this.monitorStatus = true;
    }
    
    @Override
    public void stopMonitoring() {
        mLocClient.stop();
        this.monitorStatus = false;
    }

    private void setLocationOption(){
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(false); 
        option.setCoorType("bd09ll");
        option.setServiceName("com.baidu.location.service_v2.9");
        option.setPoiExtraInfo(false);  
        option.setAddrType("");
        option.setPriority(LocationClientOption.NetWorkFirst);
        option.setPoiNumber(10);
        option.disableCache(true);      
        mLocClient.setLocOption(option);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null ||
                    (location != null && location.getLatitude() == 0.00 && location.getLongitude() == 0.00)) {
                return ;
            }
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation){
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
            } 
     
//            Log.v(TAG, "lat=" + location.getLatitude() + ";lon=" + location.getLongitude()
//                    + ";alt=" + location.getAltitude() + ";rad=" + location.getRadius()
//                    + ";tim=" + location.getTime() + ";spd=" + location.getSpeed());
            updateLocation(location);
        }

        @Override
        public void onReceivePoi(BDLocation poiLocation) {
        }
        
    }
    
    private boolean isNeedUpdateLocation(BDLocation newLocation) {
        if (null == newLocation) {
            Log.d(TAG, "Get empty location, ignore it...");
            return false;
        }
        if (null != mLastLocation && isBDLocationGeoEquals(mLastLocation, newLocation)) {
            Log.d(TAG, "Get same location, ignore it...");
            return false;
        }
        if (null != mLastLocation) {
            long timeThreshold = ManagementApplication.getContext().getResources()
                    .getInteger(R.attr.location_update_time_threshold);
            double latThreshold = Double.parseDouble(ManagementApplication.getContext().getResources()
                    .getString(R.string.location_update_latitude_threshold));
            double lonThreshold = Double.parseDouble(ManagementApplication.getContext().getResources()
                    .getString(R.string.location_update_longitude_threshold));
            long currentTime = getIntegerTimeFromString(newLocation.getTime());
            long lastTime = getIntegerTimeFromString(mLastLocation.getTime());
            double lastLat = mLastLocation.getLatitude();
            double lastLon = mLastLocation.getLongitude();
            double currentLat = newLocation.getLatitude();
            double currentLon = newLocation.getLongitude();
            if((currentTime - lastTime < timeThreshold)
                    && (Math.abs(lastLat - currentLat) - latThreshold > Double.MIN_VALUE)
                    && (Math.abs(lastLon - currentLon) - lonThreshold > Double.MIN_VALUE)) {
                Log.d(TAG, "Get abnormal location, sub-time=" + (currentTime - lastTime)
                        + ", sub-lat=" + Math.abs(lastLat - currentLat)
                        + ", sub-lon=" + Math.abs(lastLon - currentLon)
                        + ", ignore it...");
                return false;
            }
        }
        return true;
    }
    
    public boolean isBDLocationGeoEquals(BDLocation left, BDLocation right) {
        if (Math.abs(left.getAltitude() - right.getAltitude()) > Double.MIN_VALUE) {
            return false;
        }
        if (Math.abs(left.getLatitude() - right.getLatitude()) > Double.MIN_VALUE) {
            return false;
        }
        if (Math.abs(left.getLongitude() - right.getLongitude()) > Double.MIN_VALUE) {
            return false;
        }
        if (Math.abs(left.getRadius() - right.getRadius()) > Float.MIN_VALUE) {
            return false;
        }
        if (Math.abs(left.getSpeed() - right.getSpeed()) > Float.MIN_VALUE) {
            return false;
        }
        return true;
    }

    public long getIntegerTimeFromString(String timeStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }
    
    public void updateLocation(BDLocation location) {
        if (!isNeedUpdateLocation(location)) {
            return;
        }
        Log.d(TAG, "Get new location !");
        mLastLocation = location;
        double altitude = location.getAltitude();
        double latidude = location.getLatitude();
        double lontitude = location.getLongitude();
        float radius = location.getRadius();
        float speed = location.getSpeed();
        Log.v(TAG, location.getTime());
        long time = getIntegerTimeFromString(location.getTime());

        final ContentValues values = new ContentValues();
        values.put(ManagementProvider.Gps.ALTITUDE, altitude);
        values.put(ManagementProvider.Gps.LATIDUDE, latidude);
        values.put(ManagementProvider.Gps.LONGITUDE, lontitude);
        values.put(ManagementProvider.Gps.RADIUS, radius);
        values.put(ManagementProvider.Gps.SPEED, speed);
        values.put(ManagementProvider.Gps.TIME, time);
        
        mContext.getContentResolver().insert(
                ManagementProvider.Gps.CONTENT_URI, values);
        Log.d(TAG, "insert gps: altitude=" + altitude + ";latidude=" + latidude + ";lontitude=" + lontitude
                + ";radius=" + radius + ";speed=" + speed + ";time=" + time);
    }

    @Override
    public JSONArray extractDataForSend() {
        try {
            JSONArray data = new JSONArray();

            String[] GpsProj = new String[] {
            		ManagementProvider.Gps._ID,
                    ManagementProvider.Gps.ALTITUDE,
                    ManagementProvider.Gps.LATIDUDE,
                    ManagementProvider.Gps.LONGITUDE,
                    ManagementProvider.Gps.RADIUS,
                    ManagementProvider.Gps.SPEED,
                    ManagementProvider.Gps.TIME};
            String GpsSel = ManagementProvider.Gps.IS_SENT + " = \""
                    + ManagementProvider.IS_SENT_NO + "\"";
            Cursor gpsCur = mContext.getContentResolver().query(
                    ManagementProvider.Gps.CONTENT_URI,
                    GpsProj, GpsSel, null, null);

            if (gpsCur == null) {
                Log.e(TAG, "open gps table failed");
                return null;
            }
            if (gpsCur.moveToFirst() && gpsCur.getCount() > 0) {
                while (gpsCur.isAfterLast() == false) {
                	long id = gpsCur.getLong(gpsCur.getColumnIndex(ManagementProvider.Gps._ID));
                    double alt = gpsCur.getDouble(
                            gpsCur.getColumnIndex(ManagementProvider.Gps.ALTITUDE));
                    double lat = gpsCur.getDouble(
                            gpsCur.getColumnIndex(ManagementProvider.Gps.LATIDUDE));
                    double lon = gpsCur.getDouble(
                            gpsCur.getColumnIndex(ManagementProvider.Gps.LONGITUDE));
                    double rad = gpsCur.getFloat(
                            gpsCur.getColumnIndex(ManagementProvider.Gps.RADIUS));
                    float spd = gpsCur.getFloat(
                            gpsCur.getColumnIndex(ManagementProvider.Gps.SPEED));
                    long date = gpsCur.getLong(
                            gpsCur.getColumnIndex(ManagementProvider.Gps.TIME));
                    JSONObject raw = new JSONObject();
                    raw.put(ManagementProvider.Gps._ID, id);
                    raw.put(ManagementProvider.Gps.ALTITUDE, alt);
                    raw.put(ManagementProvider.Gps.LATIDUDE, lat);
                    raw.put(ManagementProvider.Gps.LONGITUDE, lon);
                    raw.put(ManagementProvider.Gps.RADIUS, rad);
                    raw.put(ManagementProvider.Gps.SPEED, spd);
                    raw.put(ManagementProvider.Gps.TIME, date);

                    data.put(raw);
                    gpsCur.moveToNext();
                }
            }
            if (null != gpsCur) {
                gpsCur.close();
            }
            
            Log.v(TAG, "data === " + data.toString());
            
            final ContentValues values = new ContentValues();
            values.put(ManagementProvider.Gps.IS_SENT, ManagementProvider.IS_SENT_YES);
            mContext.getContentResolver().update(
                    ManagementProvider.Gps.CONTENT_URI,
                    values,
                    ManagementProvider.Gps.IS_SENT + "=\"" + ManagementProvider.IS_SENT_NO +"\"",
                    null);
            
            return data;
        } catch (JSONException e) {
            Log.v(TAG, "Json exception:" + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void updateStatusAfterSend(JSONArray failedList) {
    	if (null != failedList && failedList.length() != 0) {
    		for (int i = 0; i < failedList.length(); ++i) {
    			JSONObject obj = failedList.optJSONObject(i);
    			if (null != obj) {
    				long id = obj.optLong(ManagementProvider.Gps._ID);
    		        final ContentValues values = new ContentValues();
    		        values.put(ManagementProvider.Gps.IS_SENT, ManagementProvider.IS_SENT_NO);
    		        mContext.getContentResolver().update(
    		        		ManagementProvider.Gps.CONTENT_URI,
    		                values,
    		                ManagementProvider.Gps._ID + "=\"" + id +"\"",
    		                null);
    			}
    		}
    	}
        String gpsSel = ManagementProvider.Gps.IS_SENT
        		+ " = \"" + ManagementProvider.IS_SENT_YES + "\"";
    	ManagementApplication.getContext().getContentResolver().delete(
    			ManagementProvider.Gps.CONTENT_URI,
    			gpsSel, null);
    }
   
}

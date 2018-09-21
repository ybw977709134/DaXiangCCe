package com.daxiangce123.android.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.util.LogUtil;

/**
 * @project DaXiangCe
 * @time 2014-6-1
 * @author
 */
public class Location implements Parcelable {

	private final static String TAG = "Location";
	private double lat;
	private double lon;
	private String city;
	private String addr;
	private int accuracy;

	public Location() {
		super();
	}

	public Location(double lat, double lon, int accuracy, String city,
			String addr) {
		super();
		this.lat = lat;
		this.lon = lon;
		this.accuracy = accuracy;
		this.city = city;
		this.addr = addr;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public int getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[Lat		" + lat + "]\n");
		builder.append("[Lon		" + lon + "]\n");
		builder.append("[city		" + city + "]\n");
		builder.append("[address	" + addr + "]\n");
		return builder.toString();
	}

	public long distanceTo(Location location) {
		if (location == null) {
			if (App.DEBUG) {
				LogUtil.d(TAG, "location is null");
			}
			return -1;
		}
		double radLat1 = (lat * Math.PI / 180.0);
		double radLat2 = (location.getLat() * Math.PI / 180.0);
		double a = radLat1 - radLat2;
		double b = (lon - location.getLon()) * Math.PI / 180.0;
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * Consts.EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		if (App.DEBUG) {
			LogUtil.d(TAG, "distanceTo" + s);
		}
		return (long) s;
	}

	// /////////////////////////////////////////////////////////////////////////////////

	public final static Creator<Location> CREATOR = new Creator<Location>() {
		@Override
		public Location createFromParcel(Parcel source) {
			return null;
		}

		@Override
		public Location[] newArray(int size) {
			return null;
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub

	}

}

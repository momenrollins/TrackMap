package com.trackmap.gps.homemap.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class RefreshTokenModel{

	@SerializedName("tm")
	private int tm;

	@SerializedName("events")
	private List<Object> events;

	public void setTm(int tm){
		this.tm = tm;
	}

	public int getTm(){
		return tm;
	}

	public void setEvents(List<Object> events){
		this.events = events;
	}

	public List<Object> getEvents(){
		return events;
	}

	@Override
 	public String toString(){
		return 
			"RefreshTokenModel{" + 
			"tm = '" + tm + '\'' + 
			",events = '" + events + '\'' + 
			"}";
		}
}
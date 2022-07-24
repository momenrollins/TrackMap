package com.houseofdevelopment.gps.homemap.model;
import com.google.gson.annotations.SerializedName;

public class UnitSessionModel{

	@SerializedName("units")
	private int units;

	public void setUnits(int units){
		this.units = units;
	}

	public int getUnits(){
		return units;
	}
}
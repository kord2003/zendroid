package com.zendroid;

import com.google.gson.annotations.SerializedName;

public class ZenResponse {
	@SerializedName("usd")
	private String usd;

	@SerializedName("eur")
	private String eur;
	
	@SerializedName("brent")
	private String brent;

	public String getUsd() {
		return usd;
	}

	public String getEur() {
		return eur;
	}

	public String getBrent() {
		return brent;
	}	

}

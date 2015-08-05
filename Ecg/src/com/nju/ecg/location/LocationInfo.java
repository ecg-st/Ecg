package com.nju.ecg.location;

/**
 * 地址位置信息Bean
 * @author zhuhf
 * @version [2013-3-21]
 */
public class LocationInfo
{
    /** 纬度*/
    private double latitude;
    /** 经度*/
    private double longitude;
    /** 位置描述*/
    private String addressInfo;
    
    public String getAddressInfo() {
		return addressInfo;
	}
	public void setAddressInfo(String addressInfo) {
		this.addressInfo = addressInfo;
	}
	/**
     * @return the latitude
     */
    public double getLatitude()
    {
        return latitude;
    }
    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }
    /**
     * @return the longitude
     */
    public double getLongitude()
    {
        return longitude;
    }
    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }
    
}


package com.androidyug.uberdemo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Category implements Serializable{

    public String id;
    public String display_name;
    public String currency;
    public String distance_unit;
    public String time_unit;
    public Integer eta;
    public Integer distance;
    public String ride_later_enabled;
    public String image;
    public List<FareBreakup> fare_breakup = new ArrayList<FareBreakup>();

}

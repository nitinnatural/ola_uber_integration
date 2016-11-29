
package com.androidyug.uberdemo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



public class OlaResponse implements Serializable {

    public List<Category> categories = new ArrayList<Category>();
    public List<RideEstimate> ride_estimate = new ArrayList<RideEstimate>();

}

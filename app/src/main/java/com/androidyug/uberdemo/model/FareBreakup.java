
package com.androidyug.uberdemo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class FareBreakup implements Serializable{

    public String type;
    public String fare;
    public List<Object> surcharge = new ArrayList<Object>();

}

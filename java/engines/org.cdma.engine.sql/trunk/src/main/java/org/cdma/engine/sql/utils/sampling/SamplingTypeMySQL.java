//******************************************************************************
// Copyright (c) 2011 Synchrotron Soleil.
// The CDMA library is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version.
// Contributors :
// See AUTHORS file
//******************************************************************************
package org.cdma.engine.sql.utils.sampling;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map.Entry;

import org.cdma.engine.sql.utils.SamplingType;
import org.cdma.engine.sql.utils.SamplingType.SamplingPeriod;


public enum SamplingTypeMySQL implements SamplingType {
    MONTH      ("%Y-"),
    DAY        ("%Y-%m-"),
    HOUR       ("%Y-%m-%d "),
    MINUTE     ("%Y-%m-%d %H:"),
    SECOND     ("%Y-%m-%d %H:%i:"),
    FRACTIONAL ("%Y-%m-%d %H:%i:%s."),
    ALL        ("%Y-%m-%d %H:%i:%s.%f"),
    NONE       ("%Y-%m-%d %H:%i:%s.%f");
    
    private String mSampling;
    static private HashMap<String, String> mCorrespondance;
    
    static {
    	synchronized( SamplingTypeMySQL.class ) {
    		mCorrespondance = new HashMap<String, String>();
    		mCorrespondance.put("yyyy", "%Y");
    		mCorrespondance.put("MM", "%m");
    		mCorrespondance.put("dd", "%d");
    		mCorrespondance.put("HH", "%H");
    		mCorrespondance.put("mm", "%i");
    		mCorrespondance.put("ss", "%s");
    		mCorrespondance.put("SSS", "%f");
    	}
    	
    }
    
    private SamplingTypeMySQL( String sampling ) {
    	mSampling = sampling;
    }
    
    @Override
    public String getPattern(SamplingPeriod period) {
    	String result = SamplingTypeMySQL.valueOf(period.name()).mSampling;
    	
    	for( Entry<String, String> entry : mCorrespondance.entrySet() ) {
    		result = result.replace(entry.getValue(), entry.getKey());
    	}
    	
    	return result;
    }
    
    @Override
    public SamplingType getType(SamplingPeriod time) {
    	SamplingType result = SamplingTypeMySQL.valueOf( time.name() );
    	return result;
    }
    
    @Override
    public String getSQLRepresentation(SimpleDateFormat format) {
    	String result = format.toPattern();
    	
    	for( Entry<String, String> entry : mCorrespondance.entrySet() ) {
    		result = result.replace(entry.getKey(), entry.getValue());
    	}
    	
    	return result;
    }
    
	@Override
    public String getSQLRepresentation() {
    	return mSampling;
    }
    
	@Override
    public String getSamplingSelectClause(String field, SamplingPolicy policy, String name) {
    	String result = field;
    	switch( policy ) {
    		case NONE:
    			break;
    		case AVERAGE:
    			result = "AVG(" + field + " AS CHAR) AS " + name;
    			break;
    		case MAX:
    			result = "MAX(" + field + " AS CHAR) AS " + name;
    			break;
    		case MIN:
    			result = "MIN(" + field + " AS CHAR) AS " + name;
    			break;
    		default:
    			break;
    	}
    	
    	return result;
    }
    
	@Override
    public String getFieldAsStringSelector( String field ) {
    	return "CAST(" + field + " AS CHAR)";
    }
    
    @Override
    public String getDateSampling(String field, SamplingPeriod period, int factor) {
    	String result = "";
    	String periodPattern = getSamplingPeriodUnit(period);
    	
    	
    	
    	if( periodPattern != null ) {
    		if( period.equals(SamplingPeriod.MONTH) || period.equals(SamplingPeriod.DAY) ) {
    			result = "IF(";
    		}
    		result += "FLOOR(to_number(to_char(" + field + " , '" + periodPattern + "')) / " + factor + ") * " + factor;
    		if( period.equals(SamplingPeriod.MONTH) || period.equals(SamplingPeriod.DAY) ) {
    			result += "=0,1,";
    			result += "FLOOR(to_number(to_char(" + field + " , '" + periodPattern + "')) / " + factor + ") * " + factor;
    			result += ")";
    		}
    	}
    	
		return result;
    }

    @Override
    public String getPatternPeriodUnit(SamplingPeriod period) {
    	String result = getSamplingPeriodUnit(period);
    	
    	for( Entry<String, String> entry : mCorrespondance.entrySet() ) {
    		result = result.replace(entry.getValue(), entry.getKey());
    	}
    	
    	return result;
    }
    
	@Override
	public String getSamplingPeriodUnit(SamplingPeriod period) {
		String result = null;
		switch (period) {
		case FRACTION:
			result = "%f";
			break;
		case SECOND:
			result = "%s";
    	case MINUTE:
    		result = "%i";
			break;
    	case HOUR:
    		result = "%H";
			break;
		case DAY:
			result = "%d";
			break;
		case MONTH:
			result = "%m";
			break;
		}
		return result;
	}
}

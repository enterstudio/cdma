package org.cdma.engine.archiving.internal;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;

import org.cdma.Factory;
import org.cdma.engine.archiving.internal.attribute.Attribute;
import org.cdma.engine.archiving.internal.attribute.AttributePath;
import org.cdma.engine.archiving.internal.attribute.AttributeProperties;
import org.cdma.engine.archiving.internal.sql.ArchivingQueries;
import org.cdma.engine.archiving.navigation.ArchivingDataItem;
import org.cdma.engine.archiving.navigation.ArchivingDataset;
import org.cdma.engine.archiving.navigation.ArchivingDimension;
import org.cdma.engine.archiving.navigation.ArchivingGroup;
import org.cdma.engine.sql.navigation.SqlCdmaCursor;
import org.cdma.engine.sql.navigation.SqlDataItem;
import org.cdma.engine.sql.navigation.SqlDataset;
import org.cdma.engine.sql.utils.DateFormat;
import org.cdma.interfaces.IArray;
import org.cdma.interfaces.IAttribute;
import org.cdma.interfaces.IGroup;

public class GroupUtils {

	private GroupUtils() {}
	
	/**
	 * Seek in the given group and its ancestor for the given attribute.
	 * 
	 * @param group where to start the search of the attribute
	 * @param name of the IAttribute to look for
	 * @return IAttribute object or null if not found
	 */
	public static IAttribute seekAttributeInAncestors(ArchivingGroup group, String name) {
		IAttribute result = null;
		if ( name != null && group != null ) {
			IGroup tmpGrp = group;

			// Check if group or its ancestors have 'name' attribute
			while (result == null && tmpGrp != null) {
				result = tmpGrp.getAttribute(name);
				tmpGrp = tmpGrp.getParentGroup();
			}
		}

		return result;
	}

	/**
	 * Initialize the children group list.
	 * 
	 * @param group to initialize
	 */
	public static void initGroupList(ArchivingGroup group) {
			if( group != null ) {
			// Determines on which DB to execute query: HDB / TDB
			ArchivingDataset dataset = group.getDataset();
			
			if( dataset != null ) {
			    SqlDataset dbDataset = group.getDataset().getSqldataset();
			    
			    // Prepare query for child group
			    String query = ArchivingQueries.queryChildGroups( group.getArchivedAttribute() );
			    if( query != null ) {
				    SqlCdmaCursor cursor = dbDataset.executeQuery(query);
				    
				    // All groups' names are stored in the first SqlDataItem's values
			    	List<SqlDataItem> sql_items = cursor.getDataItemList();
			    	ArchivingGroup child;
			    	for( SqlDataItem item : sql_items ) {
			    		try {
			    			// Get an array of group names
			    			IArray values = item.getData();
			    			String[] names = (String[]) values.getArrayUtils().copyTo1DJavaArray();
			    			
			    			// Prepare group for each name
			    			for( String name : names ) {
			    				child = new ArchivingGroup(group.getFactoryName(), dataset, group, name);
			    				group.addSubgroup(child);
			    			}
						} catch (IOException e) {
						}
			    		break;
			    	}
			    }
			}
		}
	}
	
	/**
	 * Initialize group the children item list
	 */
	public static void initItemList(ArchivingGroup group) {
		if( group != null ) {
			// Check this group is eligible for child items
			Attribute dbAttr = group.getArchivedAttribute();
			AttributePath path = dbAttr.getPath();
			if( path != null && path.isFullyQualified() ) {
				AttributeProperties properties = dbAttr.getProperties();
			    // Get starting, ending dates and time format
			    Timestamp start = getStartDate(group);
			    Timestamp end   = getEndDate(group);
			    String format   = getDateFormat(group);
			    
			    Object[] params = new Object[] {start, end};
			    // Prepare query for child items
			    String query = null;
			    if( format != null ) {
			    	query = ArchivingQueries.queryChildItems( dbAttr, format );
			    }
			    if( query != null && ! query.isEmpty() ) {
			    	SqlDataset dataset = dbAttr.getDbConnector().getSqlDataset();
	
			    	// Execute the query
			    	SqlCdmaCursor cursor = dataset.executeQuery(query, params);
			    	
			    	// Temporary items
			    	ArchivingDataItem child;
			    	ArchivingDimension dimension;
			    	
				    // For each SQL items
			    	List<SqlDataItem> sql_items = cursor.getDataItemList();
			    	String interp;
			    	for( SqlDataItem item : sql_items ) {
			    		try {
			    			// Get the array of values
			    			IArray array = item.getData();
			    			
			    			// Check if the found item is a dimension
				    		if( dbAttr.isDimension( item.getShortName() ) ) {
				    			// Create a child dimension 
								dimension = new ArchivingDimension( group.getFactoryName(), array, item.getShortName());
								group.addOneDimension(dimension);
								
				    		}
				    		// The found item is a data item
				    		else {
				    			// Create a child data item
			    				child = new ArchivingDataItem(group.getFactoryName(), item.getShortName(), group, array);
			    				interp = getInterpretationFormat( properties );
			    				if( interp != null ) {
			    					child.addStringAttribute( Constants.INTERPRETATION, interp );
			    				}
			    				group.addDataItem(child);
				    		}
						} catch (IOException e) {
							Factory.getLogger().log(Level.SEVERE, "Unable to initialize item list!", e);
						}
			    	}
			    }
			}
		}
	}
	
	/**
	 * Seek in the hierarchy if a date format has been set.
	 * 
	 * @return string representation of the date format
	 * @note the 'end' date is carried by  IAttribute {@link Constants.DATE_FORMAT}
	 */
	public static String getDateFormat(ArchivingGroup group) {
		String format = null;
		if( group != null ) {
		    IAttribute dateFormat = seekAttributeInAncestors(group, Constants.DATE_FORMAT);
		    if( dateFormat != null && dateFormat.isString() ) {
		    	format = dateFormat.getStringValue();
		    }
		}
	    return format;
	}

	/**
	 * Seek in the hierarchy if an 'start' date has been set and return the corresponding 
	 * Timestamp or null if not found.
	 * 
	 * @return a Timestamp
	 * @note the 'end' date is carried by  IAttribute {@link Constants.START_DATE}
	 */
	public static Timestamp getStartDate(ArchivingGroup group) {
		Timestamp start = null;
		
		if( group != null ) {
			// Seek a 'start' date attribute
			IAttribute startTime = seekAttributeInAncestors(group, Constants.START_DATE);
			if( startTime != null ) {
				Class<?> clazz = startTime.getType();
				if( Number.class.isAssignableFrom(clazz) ) {
					start = new Timestamp( (Long) startTime.getNumericValue() );
				}
				else {
					IAttribute dateFormat = seekAttributeInAncestors(group, Constants.DATE_FORMAT);
					if( dateFormat != null ) {
						try {
							String date = DateFormat.convertDate(startTime.getStringValue(), dateFormat.getStringValue() );
							SimpleDateFormat format = new java.text.SimpleDateFormat( dateFormat.getStringValue() );
							start = new Timestamp( format.parse(date).getTime() );
						} catch (ParseException e) {
							Factory.getLogger().log(Level.SEVERE, "Unable to initialize start date", e);
						}
					}
				}
			}
			else {
				start = new Timestamp( System.currentTimeMillis() - 3600 * 1000 );
			}
		}
		return start;
	}
	
	/**
	 * Seek in the hierarchy if an 'end' date has been set and return the corresponding 
	 * Timestamp or null if not found.
	 * 
	 * @return a Timestamp
	 * @note the 'end' date is carried by IAttribute {@link Constants.END_DATE}
	 */
	public static Timestamp getEndDate(ArchivingGroup group) {
		Timestamp end = null;
		
		if( group != null ) {
			// Seek a 'end' date attribute
			IAttribute endTime = seekAttributeInAncestors(group, Constants.END_DATE);
			
			if( endTime != null ) {
				Class<?> clazz = endTime.getType();
				if( Number.class.isAssignableFrom(clazz) ) {
					end = new Timestamp( (Long) endTime.getNumericValue() );
				}
				else {
					IAttribute dateFormat = seekAttributeInAncestors(group, Constants.DATE_FORMAT);
					if( dateFormat != null ) {
						try {
							String date = DateFormat.convertDate(endTime.getStringValue(), dateFormat.getStringValue() );
							SimpleDateFormat format = new java.text.SimpleDateFormat( dateFormat.getStringValue() );
							end = new Timestamp( format.parse(date).getTime() );
						} catch (ParseException e) {
							Factory.getLogger().log(Level.SEVERE, "Unable to initialize start date", e);
						}
					}
				}
			}
			else {
				end = new Timestamp(System.currentTimeMillis());
			}
		}
		return end;
	}
	
	public static String getInterpretationFormat(AttributeProperties properties) {
		int format = properties.getFormat();
		String result = null;
		switch( format ) {
			case 0:
				result = Constants.INTERPRETATION_SCALAR;
				break;
			case 1:
				result = Constants.INTERPRETATION_SPECTRUM;
				break;
			case 2:
				result = Constants.INTERPRETATION_IMAGE;
				break;
		}
		return result;
	}
}

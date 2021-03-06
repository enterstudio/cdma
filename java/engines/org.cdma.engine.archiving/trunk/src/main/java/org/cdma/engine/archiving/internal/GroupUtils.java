/*******************************************************************************
 * Copyright (c) 2008 - ANSTO/Synchrotron SOLEIL
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 * Tony Lam (nxi@Bragg Institute) - initial API and implementation
 * Majid Ounsy (SOLEIL Synchrotron) - API v2 design and conception
 * St�phane Poirier (SOLEIL Synchrotron) - API v2 design and conception
 * Clement Rodriguez (ALTEN for SOLEIL Synchrotron) - API evolution
 * Gregory VIGUIER (SOLEIL Synchrotron) - API evolution
 ******************************************************************************/
package org.cdma.engine.archiving.internal;

import java.io.IOException;
import java.sql.SQLException;
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
import org.cdma.engine.sql.navigation.SqlDataItem;
import org.cdma.engine.sql.navigation.SqlDataset;
import org.cdma.engine.sql.utils.DateFormat;
import org.cdma.engine.sql.utils.ISqlArrayAppender;
import org.cdma.engine.sql.utils.SamplingType.SamplingPeriod;
import org.cdma.engine.sql.utils.SqlCdmaCursor;
import org.cdma.exception.CDMAException;
import org.cdma.interfaces.IArray;
import org.cdma.interfaces.IAttribute;
import org.cdma.interfaces.IGroup;

public class GroupUtils {

    private GroupUtils() {
    }

    /**
     * Seek in the given group and its ancestor for the given attribute.
     * 
     * @param group where to start the search of the attribute
     * @param name of the IAttribute to look for
     * @return IAttribute object or null if not found
     */
    public static IAttribute seekAttributeInAncestors(final IGroup group, final String name) {
        IAttribute result = null;
        if ((name != null) && (group != null)) {
            IGroup tmpGrp = group;

            // Check if group or its ancestors have 'name' attribute
            while ((result == null) && (tmpGrp != null)) {
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
     * @throws SQLException
     */
    public static void initGroupList(final ArchivingGroup group) throws CDMAException {
        if (group != null) {
            // Determines on which DB to execute query: HDB / TDB
            ArchivingDataset dataset = group.getDataset();

            if (dataset != null) {
                SqlDataset dbDataset = group.getDataset().getSqldataset();

                // Prepare query for child group
                String query = ArchivingQueries.queryChildGroups(group.getArchivedAttribute());
                if (query != null) {
                    SqlCdmaCursor cursor = dbDataset.executeQuery(query);

                    // All groups' names are stored in the first SqlDataItem's values
                    List<SqlDataItem> sql_items = cursor.getDataItemList();
                    ArchivingGroup child;
                    for (SqlDataItem item : sql_items) {
                        try {
                            // Get an array of group names
                            IArray values = item.getData();
                            String[] names = (String[]) values.getArrayUtils().copyTo1DJavaArray();

                            // Prepare group for each name
                            for (String name : names) {
                                child = new ArchivingGroup(group.getFactoryName(), dataset, group, name);
                                group.addSubgroup(child);
                            }
                        } catch (IOException e) {
                            throw new CDMAException("Cannot read data on " + item.getName() + "\n" + e.getMessage());
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Initialize group the children item list
     * 
     * @throws SQLException
     * @throws IOException
     */
    public static void initItemList(final ArchivingGroup group) throws CDMAException {
        if (group != null) {
            // Check this group is eligible for child items
            Attribute dbAttr = group.getArchivedAttribute();
            AttributePath path = dbAttr.getPath();
            if ((path != null) && path.isFullyQualified()) {
                // Get starting, ending dates and time format
                Timestamp start = getStartDate(group);
                Timestamp end = getEndDate(group);
                String format = getDateFormat(group);

                // Get the sampling type
                SamplingPeriod sampling = getSamplingType(group);
                int factor = getSamplingFactor(group);
                dbAttr.getProperties().setSampling(sampling);
                dbAttr.getProperties().setSamplingFactor(factor);

                Object[] params = new Object[] { start, end };

                // Prepare query for child items
                ArchivingDataset dataset = group.getDataset();
                if ((dataset != null) && dataset.getNumericalDate()) {
                    format = null;
                }
                String query = ArchivingQueries.queryChildItems(dbAttr, format);
                if ((query != null) && !query.isEmpty()) {
                    SqlDataset sqlDataset = dbAttr.getDbConnector().getSqlDataset();

                    // Execute the query
                    SqlCdmaCursor cursor = sqlDataset.executeQuery(query, params);
                    ISqlArrayAppender appender = new ArchivingArrayAppender(dbAttr, SqlFieldConstants.CELL_SEPARATOR);
                    cursor.setAppender(appender);

                    // For each SQL items
                    List<SqlDataItem> sql_items = cursor.getDataItemList();
                    for (SqlDataItem item : sql_items) {
                        try {
                            initDataItem(group, item, dbAttr);
                        } catch (IOException e) {
                            throw new CDMAException(e);
                        }

                    }
                }
            }
        }
    }

    /**
     * Seek in the hierarchy if a sampling attribute has been set.
     * 
     * @return SamplingPeriod expected for attribute extraction
     * @note the 'samplingType' type is carried by IAttribute {@link Constants.SAMPLING_TYPE}
     */
    public static SamplingPeriod getSamplingType(final IGroup group) {
        SamplingPeriod type = SamplingPeriod.NONE;
        if (group != null) {
            IAttribute dateFormat = seekAttributeInAncestors(group, Constants.SAMPLING_TYPE);
            if (dateFormat != null) {
                Number sampNum = dateFormat.getNumericValue();
                if (sampNum != null) {
                    type = SamplingPeriod.instantiate(sampNum.intValue());
                } else if (dateFormat.isString()) {
                    String value = dateFormat.getStringValue();
                    try {
                        type = SamplingPeriod.valueOf(value);
                    } catch (IllegalArgumentException e) {
                        Factory.getLogger().log(Level.SEVERE, "Unable to get the sampling type!", e);
                    }
                }
            }
        }
        return type;
    }

    /**
     * Seek in the hierarchy if a sampling factor attribute has been set.
     * 
     * @return int representation of the date format
     * @note the 'samplingFactor' type is carried by IAttribute {@link Constants.SAMPLING_FACTOR}
     */
    public static int getSamplingFactor(final IGroup group) {
        int result = 1;
        if (group != null) {
            IAttribute dateFormat = seekAttributeInAncestors(group, Constants.SAMPLING_FACTOR);
            if (dateFormat != null) {
                Number sampNum = dateFormat.getNumericValue();
                if (sampNum != null) {
                    result = sampNum.intValue();
                }
            }
        }
        return result;
    }

    /**
     * Seek in the hierarchy if a date format has been set.
     * 
     * @return string representation of the date format
     * @note the 'end' date is carried by IAttribute {@link Constants.DATE_FORMAT}
     */
    public static String getDateFormat(final IGroup group) {
        String format = null;
        if (group != null) {
            IAttribute dateFormat = seekAttributeInAncestors(group, Constants.DATE_FORMAT);
            if ((dateFormat != null) && dateFormat.isString()) {
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
     * @note the 'end' date is carried by IAttribute {@link Constants.START_DATE}
     */
    public static Timestamp getStartDate(final ArchivingGroup group) {
        Timestamp start = null;

        if (group != null) {
            // Seek a 'start' date attribute
            IAttribute startTime = seekAttributeInAncestors(group, Constants.START_DATE);
            if (startTime != null) {
                Class<?> clazz = startTime.getType();
                if (Number.class.isAssignableFrom(clazz)) {
                    start = new Timestamp((Long) startTime.getNumericValue());
                } else {
                    IAttribute dateFormat = seekAttributeInAncestors(group, Constants.DATE_FORMAT);
                    if (dateFormat != null) {
                        try {
                            String date = DateFormat.convertDate(startTime.getStringValue(),
                                    dateFormat.getStringValue());
                            SimpleDateFormat format = new java.text.SimpleDateFormat(dateFormat.getStringValue());
                            start = new Timestamp(format.parse(date).getTime());
                        } catch (ParseException e) {
                            Factory.getLogger().log(Level.SEVERE, "Unable to initialize start date", e);
                        }
                    }
                }
            } else {
                start = new Timestamp(System.currentTimeMillis() - 3600 * 1000);
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
    public static Timestamp getEndDate(final ArchivingGroup group) {
        Timestamp end = null;

        if (group != null) {
            // Seek a 'end' date attribute
            IAttribute endTime = seekAttributeInAncestors(group, Constants.END_DATE);

            if (endTime != null) {
                Class<?> clazz = endTime.getType();
                if (Number.class.isAssignableFrom(clazz)) {
                    end = new Timestamp((Long) endTime.getNumericValue());
                } else {
                    IAttribute dateFormat = seekAttributeInAncestors(group, Constants.DATE_FORMAT);
                    if (dateFormat != null) {
                        try {
                            String date = DateFormat.convertDate(endTime.getStringValue(), dateFormat.getStringValue());
                            SimpleDateFormat format = new java.text.SimpleDateFormat(dateFormat.getStringValue());
                            end = new Timestamp(format.parse(date).getTime());
                        } catch (ParseException e) {
                            Factory.getLogger().log(Level.SEVERE, "Unable to initialize start date", e);
                        }
                    }
                }
            } else {
                end = new Timestamp(System.currentTimeMillis());
            }
        }
        return end;
    }

    public static String getInterpretationFormat(final AttributeProperties properties) {
        return properties.getFormat().getName();
    }

    static private void initDataItem(final ArchivingGroup group, final SqlDataItem item, final Attribute dbAttr)
    throws IOException {
        // Temporary items
        AttributeProperties properties = dbAttr.getProperties();

        // Get the array of values and transform it if necessary
        IArray array = item.getData();

        // Check if the found item is a dimension
        if (dbAttr.isDimension(item.getShortName())) {
            // Create a child dimension
            ArchivingDimension dimension = new ArchivingDimension(group.getFactoryName(), array, item.getShortName());
            group.addOneDimension(dimension);

        }
        // The found item is a data item
        else {
            // Create a child data item
            ArchivingDataItem child = new ArchivingDataItem(group.getFactoryName(), item.getShortName(), group, array);

            // Set interpretation attribute
            String interp = getInterpretationFormat(properties);
            if (interp != null) {
                child.addStringAttribute(Constants.INTERPRETATION, interp);
            }

            // Add child to this group
            group.addDataItem(child);
        }
    }
}

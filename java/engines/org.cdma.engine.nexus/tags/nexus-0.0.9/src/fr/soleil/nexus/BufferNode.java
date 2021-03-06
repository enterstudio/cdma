//******************************************************************************
// Copyright (c) 2011 Synchrotron Soleil.
// The CDMA library is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version.
// Contributors :
// See AUTHORS file
//******************************************************************************
package fr.soleil.nexus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import fr.soleil.nexus.NexusNode.NameCollator;

/**
 * @brief The BufferNode class stores in memory the physical tree structure of a NexusFileInstance.
 * 
 * The aim is to prevent accessible multiple time to a file when not needed. It 
 * avoids some issues of the NexusAPI such as lack of performance when largely populated groups
 * or to avoid the fact that iterators are not reseted when listing children or attributes.
 * <p>
 * Each path are associated to each nodes' name below it. When the buffer is starting to get
 * full, its lighter half is automatically flushed. Indeed, a cost in time is generated, 
 * while accessing each path. When a flush is needed the costlier path are kept,
 * whereas the other are removed.
 *  
 * @author rodriguez
 */

public class BufferNode {
   
    // Maximum size of the buffer (number of available slots)
    private int m_iBufferSize; 
    
    // TreeMap containing all node for a specific path (path in file => [node name => node class])
    private final Map<String, TreeMap<String, NexusNode>> m_tNodeInPath;
    
    // TreeMap containing path usage count (used to remove less used path when cleaning buffer)
    private final Map<String, Integer> m_hPathUsageWeigth;
    
    // Constructor
    public BufferNode(int size) {
        m_iBufferSize      = size;
        m_tNodeInPath      = new HashMap<String, TreeMap<String, NexusNode> >();
        m_hPathUsageWeigth = new HashMap<String, Integer>();
    }

    /**
     * Returns the current maximum size of the node buffer (in number of slots)
     */
    public int getBufferSize() {
        return m_iBufferSize;
    }
    
    /**
     * Set the current maximum size of the node buffer (in number of slots)
     * 
     * @param iSize new number of available slots in the node buffer
     */
    public void setBufferSize(int iSize) {
        if (iSize > 10)
            m_iBufferSize = iSize;
    }

    /**
     * Reset all information stored into that buffer 
     */
    public void resetBuffer() {
        m_tNodeInPath.clear();
        m_hPathUsageWeigth.clear();
    }
    
    // ---------------------------------------------------------
    // ---------------------------------------------------------
    // Protected methods
    // ---------------------------------------------------------
    // ---------------------------------------------------------
    /**
     * Returns the buffered nodes' collection for the given path
     * 
     * @param path where the nodes are requested 
     * @return node collection (or null if not found)
     */
    protected Collection<NexusNode> getNodeInPath(PathNexus path) {
        Integer value = m_hPathUsageWeigth.get(path.toString());
        if (value == null) {
            value = 1;
        }
        else {
            value++;
        }
        m_hPathUsageWeigth.put(path.toString(), value);

        
        if( m_tNodeInPath.containsKey(path.toString()) ) {
            Collection<NexusNode> result = m_tNodeInPath.get(path.toString()).values();
            return result;
        }
        else {
            return null;
        }
    }

    /**
     * Update the buffer with the given node
     * 
     * @param path where the node belongs to
     * @param node to store
     */
    protected void pushNodeInPath(PathNexus path, NexusNode node) {
        pushNodeInPath(path, node, 1);
    }

    /**
     * Stores given nodes in the buffer for the given path.
     * 
     * @param path the nodes belong to
     * @param nodes found at the given path
     * @param time spent to get the list of children 
     */
    protected void putNodesInPath(PathNexus path, ArrayList<NexusNode> nodes, int time) {
        TreeMap<String, NexusNode> tmpSet = m_tNodeInPath.get(path.toString());
        if (tmpSet == null) {
            tmpSet = new TreeMap<String, NexusNode> (new NameCollator());
        }
        for( NexusNode node : nodes ) {
            tmpSet.put(node.getNodeName(), node);
        }
        putNodesInPath(path, tmpSet, time);
    }
    
    
    // ---------------------------------------------------------
    // ---------------------------------------------------------
    // Private methods
    // ---------------------------------------------------------
    // ---------------------------------------------------------
    private void putNodesInPath(PathNexus path, TreeMap<String, NexusNode> tmNodes, int iTimeToAccessNode) {
        freeBufferSpace();

        Integer value = m_hPathUsageWeigth.get(path.toString());
        if (value == null) {
            value = iTimeToAccessNode;
        }
        else {
            value += iTimeToAccessNode + 1;
        }
        m_hPathUsageWeigth.put(path.toString(), value);
        m_tNodeInPath.put(path.toString(), tmNodes);
    }
    
    private void pushNodeInPath(PathNexus path, NexusNode node, int iTimeToAccessNode) {
        TreeMap<String, NexusNode> tmpSet = m_tNodeInPath.get(path.toString());
        if (tmpSet == null) {
            tmpSet = new TreeMap<String, NexusNode> (new NameCollator());
            tmpSet.put( node.getNodeName(), node );
            putNodesInPath(path, tmpSet, iTimeToAccessNode);
        }
        else {
            tmpSet.put( node.getNodeName(), node );
        }
    }
    
    private void freeBufferSpace() {
        if (m_tNodeInPath.size() > m_iBufferSize) {
            int iNumToRemove = (m_iBufferSize / 2), iRemovedItem = 0, iInfLimit;
            Object[] frequency = m_hPathUsageWeigth.values().toArray();
            java.util.Arrays.sort(frequency);
            iInfLimit = (Integer) frequency[frequency.length / 2];
            Iterator<String> keys_iter = m_hPathUsageWeigth.keySet().iterator();
            int freq;
            String key;
            while (keys_iter.hasNext() && iRemovedItem < iNumToRemove) {
                key = keys_iter.next();
                freq = m_hPathUsageWeigth.get(key);

                if (freq <= iInfLimit) {
                    keys_iter.remove();
                    m_tNodeInPath.remove(key);
                    iRemovedItem++;
                }
            }
        }
    }
}

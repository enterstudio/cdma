package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cdma.interfaces.INode;



public class HdfPath {

    public static final String PATH_SEPARATOR = "/";
    protected List<INode> nodes = new ArrayList<INode>();

    public HdfPath(INode[] nodes) {
        this.nodes = Arrays.asList(nodes);
    }

    /**
     * Split a string representing a NexusPath to extract each node name
     * 
     * @param path
     */
    public static String[] splitStringPath(String path) {
        if (path.startsWith(HdfPath.PATH_SEPARATOR)) {
            return path.substring(1).split(HdfPath.PATH_SEPARATOR);
        }
        else {
            return path.split(HdfPath.PATH_SEPARATOR);
        }
    }

    public static INode[] splitStringToNode(String sPath) {
        String[] names = splitStringPath(sPath);
        HdfNode[] nodes = null;

        int nbNodes = 0;
        for (String name : names) {
            if (!name.isEmpty()) {
                nbNodes++;
            }
        }

        if (nbNodes > 0) {
            nodes = new HdfNode[nbNodes];
            int i = 0;
            for (String name : names) {
                if (!name.isEmpty()) {
                    nodes[i] = new HdfNode(name);
                    i++;
                }
            }
        }
        else {
            nodes = new HdfNode[0];
        }
        return nodes;
    }

    public INode[] getNodes() {
        return nodes.toArray(new INode[nodes.size()]);
    }


    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        for (INode node : nodes) {
            result.append(node.toString());
        }
        return result.toString();
    }

}
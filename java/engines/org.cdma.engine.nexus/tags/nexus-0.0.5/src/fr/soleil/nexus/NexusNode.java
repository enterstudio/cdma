package fr.soleil.nexus;

import java.text.Collator;
import java.util.Comparator;

import org.cdma.utilities.performance.Benchmarker;

public class NexusNode implements Cloneable {
    // Private definitions
    private static final String CLASS_SEPARATOR_START  = "<";
    private static final String CLASS_SEPARATOR_START2 = "{";
    private static final String CLASS_SEPARATOR_END2   = "}";

    private String              m_sNodeName            = "";
    private String              m_sClassName           = "";
    private boolean             m_bIsGroup             = false;

    public NexusNode() {
    }

    public NexusNode(String sNodeName, String sClassName) {
        m_sNodeName = sNodeName;
        m_sClassName = sClassName;
        m_bIsGroup = (!"SDS".equals(sClassName) || "".equals(sNodeName));
    }

    public NexusNode(String sNodeName, String sClassName, boolean bIsGroup) {
        m_sNodeName = sNodeName;
        m_sClassName = sClassName;
        m_bIsGroup = bIsGroup;
    }

    public void setNodeName(String sNodeName) {
        m_sNodeName = sNodeName;
    }

    public void setClassName(String sClassName) {
        m_sClassName = sClassName;
    }

    public void setIsGroup(boolean bIsGroup) {
        m_bIsGroup = bIsGroup;
    }

    public String getNodeName() {
        return m_sNodeName;
    }

    public String getClassName() {
        return m_sClassName;
    }

    public boolean isGroup() {
        return m_bIsGroup;
    }

    public boolean isRealGroup() {
        return !m_sClassName.equals("SDS");
    }

    protected NexusNode clone() {
        NexusNode nNewNode = new NexusNode();
        nNewNode.m_sNodeName = m_sNodeName;
        nNewNode.m_sClassName = m_sClassName;
        nNewNode.m_bIsGroup = m_bIsGroup;

        return nNewNode;
    }

    @Override
    public boolean equals(Object node) {
        return (node instanceof NexusNode && m_sNodeName.equals(((NexusNode) node).m_sNodeName)
                && m_sClassName.equals(((NexusNode) node).m_sClassName) && m_bIsGroup == ((NexusNode) node).m_bIsGroup);
    }

    @Override
    public int hashCode() {
        return m_sNodeName.hashCode() + m_sClassName.hashCode();
    }

    public String toString() {
        String sName = getNodeName();
        if (!getClassName().trim().equals("") && isRealGroup())
            sName += CLASS_SEPARATOR_START2 + getClassName() + CLASS_SEPARATOR_END2;
        return sName;
    }

    public static NexusNode getNexusNode(String sNodeFullName, boolean bIsGroup) {
        NexusNode node = null;
        String tmpNodeName = NexusNode.extractName(sNodeFullName);
        String tmpClassName = NexusNode.extractClass(sNodeFullName);

        if (!"".equals(tmpNodeName) || !"".equals(tmpClassName))
            node = new NexusNode(tmpNodeName, tmpClassName, bIsGroup);

        return node;
    }

    public static String getNodeFullName(String sNodeName, String sNodeClass) {
        return sNodeName + (sNodeClass.equals("SDS") ? "" : (CLASS_SEPARATOR_START2 + sNodeClass + CLASS_SEPARATOR_END2));
    }

    public static String extractName(String sNodeName) {
        int iPosClassSep;
        String tmpNodeName = "";
        iPosClassSep = sNodeName.indexOf(CLASS_SEPARATOR_START);
        if (iPosClassSep < 0)
            iPosClassSep = sNodeName.indexOf(CLASS_SEPARATOR_START2);
        iPosClassSep = iPosClassSep < 0 ? sNodeName.length() : iPosClassSep;
        tmpNodeName = sNodeName.substring(0, iPosClassSep);
        return tmpNodeName;
    }

    public static String extractClass(String sNodeName) {
        int iPosClassSep;
        String tmpClassName = "";
        iPosClassSep = sNodeName.indexOf(CLASS_SEPARATOR_START);
        if (iPosClassSep < 0)
            iPosClassSep = sNodeName.indexOf(CLASS_SEPARATOR_START2);
        iPosClassSep = iPosClassSep < 0 ? sNodeName.length() : iPosClassSep;
        tmpClassName = iPosClassSep < sNodeName.length() ? sNodeName.substring(iPosClassSep + 1, sNodeName.length() - 1) : "";
        return tmpClassName;
    }

    /**
     * Return true when the given node (which is this) matches this node.
     * 
     * @param node
     *            NexusNode that is a pattern referent: it should have name XOR
     *            class name defined
     * @return true when this node fit the given pattern node
     */
    public boolean matchesNode(NexusNode node) {
        boolean classMatch, nameMatch;

        classMatch = "".equals(node.getClassName()) || node.getClassName().equalsIgnoreCase(this.getClassName());
        nameMatch = "".equals(node.getNodeName()) || this.getNodeName().equalsIgnoreCase(node.getNodeName());

        return (classMatch && nameMatch);
    }

    public boolean matchesPartNode(NexusNode node) {
        boolean classMatch, nameMatch;
        classMatch = "".equals(node.getClassName()) || node.getClassName().equalsIgnoreCase(this.getClassName());
        nameMatch = "".equals(node.getNodeName()) || this.getNodeName().toLowerCase().matches(node.getNodeName().toLowerCase().replace("*", ".*"));
        return (classMatch && nameMatch);
    }
    
    static public class NameCollator implements Comparator<String> {
        @Override
        public int compare(final String arg0, final String arg1) {
            int iCmp;
            if (arg0.matches(".*[0-9].*") && arg1.matches(".*[0-9].*")) {
                // Prepare string by marking up every digit
                String argA, argB;
                argA = arg0.replaceAll("(\\d+)", "#$1#");
                argB = arg1.replaceAll("(\\d+)", "#$1#");

                // Separate characters and digit
                String[] arg0Parts, arg1Parts;
                arg0Parts = argA.split("#");
                arg1Parts = argB.split("#");

                // Compare strings until one is lesser than the other
                iCmp = 0;
                int index = 0;
                while (iCmp == 0) {
                    // If remains string in both parts
                    if (index < arg0Parts.length && index < arg1Parts.length) {
                        // If digits
                        if (arg0Parts[index].matches("[0-9]+") && arg1Parts[index].matches("[0-9]+")) {
                            int iArg0 = Integer.parseInt(arg0Parts[index]);
                            int iArg1 = Integer.parseInt(arg1Parts[index]);

                            if (iArg0 > iArg1)
                                iCmp = 1;
                            else if (iArg0 < iArg1)
                                iCmp = -1;
                            else
                                iCmp = 0;
                        }
                        // If characters
                        else {
                            iCmp = Collator.getInstance().compare(arg0Parts[index], arg1Parts[index]);
                        }
                    }
                    // One of the part is empty
                    else {
                        if (arg0Parts.length == arg1Parts.length)
                            iCmp = 0;
                        else
                            iCmp = (arg0Parts.length > arg1Parts.length) ? 1 : -1;
                        break;
                    }
                    index++;
                }
            } else {
                iCmp = Collator.getInstance().compare(arg0, arg1);
            }
            return iCmp;
        }
    }
}

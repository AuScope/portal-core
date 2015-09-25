package org.auscope.portal.core.util;

/**
 * An enum representing the current portal implementation.
 */
public enum Portal {
    AUSCOPE("auscope"),
    GEOSCIENCE("geoscience");
    
    private String name;
    
    private Portal(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    public static boolean isGeosciencePortal(String portalName) {
        return GEOSCIENCE.name.equals(portalName);
    }
}

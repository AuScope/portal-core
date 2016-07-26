package org.auscope.portal.core.services.namespaces;

/**
 * A simple implementation of <a href="http://java.sun.com/javase/6/docs/api/javax/xml/namespace/NamespaceContext.html"> NamespaceContext </a>. Instances are
 * immutable.
 *
 * This is suitable for reading AWS EC2 response XML
 *
 * @version $Id$
 */
public class AWSEC2NamespaceContext extends IterableNamespace {

    public AWSEC2NamespaceContext() {
        map.put("aws", "http://ec2.amazonaws.com/doc/2015-10-01/");
    }
}

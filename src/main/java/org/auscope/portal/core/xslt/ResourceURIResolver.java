package org.auscope.portal.core.xslt;

import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

/**
 * A URI resolver for picking up resources on the classpath
 * 
 * @author Josh Vote
 *
 */
public class ResourceURIResolver implements URIResolver {

    /** The base from where resources should be looked up from */
    private Class<?> baseClass;

    /**
     * Creates a new resolver with this class as the base
     */
    public ResourceURIResolver() {
        baseClass = getClass();
    }

    /**
     * Creates a new resolver, relative to clazz
     * 
     * @param clazz
     *            Will be used as the basis for all resource lookups
     */
    public ResourceURIResolver(Class<?> clazz) {
        baseClass = clazz;
    }

    /**
     * See URIResolver interface
     */
    @SuppressWarnings("resource")
    @Override
    public Source resolve(String href, String base) throws TransformerException {
        InputStream is = baseClass.getResourceAsStream(href);
        if (is == null) {
            throw new TransformerException(String.format("URI cannot be resolved href='%1$s' base='%2$s'", href, base));
        }

        return new StreamSource(is);
    }

}

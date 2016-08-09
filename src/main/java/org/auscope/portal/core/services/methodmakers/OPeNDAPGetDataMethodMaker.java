package org.auscope.portal.core.services.methodmakers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.auscope.portal.core.services.responses.opendap.AbstractViewVariable;
import org.auscope.portal.core.services.responses.opendap.SimpleAxis;
import org.auscope.portal.core.services.responses.opendap.SimpleBounds;
import org.auscope.portal.core.services.responses.opendap.SimpleGrid;

import ucar.ma2.Array;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * An class for generating HttpMethods that will query an OPeNDAP Service for data in a given format which is constrained by a list of constraints (which are a
 * simplification of variables in ds).
 *
 * @author JoshVote
 *
 */
public class OPeNDAPGetDataMethodMaker extends AbstractMethodMaker {
    public enum OPeNDAPFormat {
        ASCII,
        DODS
    }

    private final Log logger = LogFactory.getLog(getClass());

    /**
     * Given a ViewVariable, this function will populate the appropriate "SimpleBounds" field representing an index bounds IFF the value bounds is specified AND
     * the index bounds are unspecified
     *
     * @param ds
     * @param var
     * @throws IOException 
     */
    private void calculateIndexBounds(NetcdfDataset ds, AbstractViewVariable var) throws IOException {
        if (var instanceof SimpleAxis) {
            calculateIndexBounds(ds, (SimpleAxis) var);
        } else if (var instanceof SimpleGrid) {
            calculateIndexBounds(ds, (SimpleGrid) var);
        } else {
            throw new IllegalArgumentException(String.format("Unable to calculate index bounds for class '%1$s'",
                    var.getClass()));
        }
    }

    private static void calculateIndexBounds(NetcdfDataset ds, SimpleAxis axis) throws IOException {
        //Only calculate dimension bounds if required (and possible)
        if (axis.getValueBounds() != null && axis.getDimensionBounds() == null) {
            String parentGroupName = "";
            if (axis.getParentGroupName() != null)
                parentGroupName = axis.getParentGroupName();

            //Find the raw variable in the dataset
            Variable rawVar = null;
            if (parentGroupName.isEmpty())
                rawVar = ds.findVariable("/" + axis.getName());
            else
                rawVar = ds.findVariable("/" + parentGroupName + "/" + axis.getName());

            //Lookup the data
            Array entireBounds = rawVar.read();
            if (entireBounds.getSize() > Integer.MAX_VALUE)
                throw new IllegalArgumentException("Bounds contains too many indexes");

            int indexFrom = 0, indexTo = (int) entireBounds.getSize() - 1;

            //Calculate minimum
            for (indexFrom = 0; indexFrom < entireBounds.getSize(); indexFrom++) {
                //Find the first index that contains a value inside our value bounds
                if (entireBounds.getDouble(indexFrom) >= axis.getValueBounds().getFrom()) {
                    break;
                }
            }

            //Calculate maximum
            for (indexTo = (int) entireBounds.getSize() - 1; indexTo >= 0; indexTo--) {
                //Find the first index that contains a value inside our value bounds
                if (entireBounds.getDouble(indexTo) <= axis.getValueBounds().getTo()) {
                    break;
                }
            }

            axis.setDimensionBounds(new SimpleBounds(indexFrom, indexTo));
        }
    }

    private void calculateIndexBounds(NetcdfDataset ds, SimpleGrid grid) throws IOException {
        for (AbstractViewVariable var : grid.getAxes()) {
            calculateIndexBounds(ds, var);
        }
    }

    private static String simpleBoundsToQuery(SimpleBounds bounds) {
        return String.format("[%1$d:%2$d]", (int) bounds.getFrom(), (int) bounds.getTo());
    }

    /**
     * Given a variable, this function generates an OPeNDAP query string that will query according to the specified constraints (Each ViewVariable must have
     * their appropriate dimension bounds specified).
     *
     * @param vars
     *            The list of constraints
     * @return
     */
    private static String generateQueryForConstraints(AbstractViewVariable[] vars) {
        StringBuilder result = new StringBuilder();

        for (AbstractViewVariable var : vars) {

            if (result.length() > 0)
                result.append(",");

            result.append(var.getName());

            //Append the body of the constraint
            if (var instanceof SimpleAxis) {
                SimpleAxis axis = (SimpleAxis) var;
                result.append(simpleBoundsToQuery(axis.getDimensionBounds()));
            } else if (var instanceof SimpleGrid) {
                SimpleGrid grid = (SimpleGrid) var;

                StringBuilder sb = new StringBuilder();
                for (AbstractViewVariable child : grid.getAxes()) {
                    if (child instanceof SimpleAxis) {
                        sb.append(simpleBoundsToQuery(((SimpleAxis) child).getDimensionBounds()));
                    } else {
                        throw new IllegalArgumentException("Unsupported child of SimpleGrid " + child.getClass());
                    }
                }

                result.append(sb.toString());
            } else {
                throw new IllegalArgumentException(String.format("Unable to calculate index bounds for class '%1$s'",
                        var.getClass()));
            }
        }

        return result.toString();
    }

    public HttpRequestBase getMethod(String opendapUrl, OPeNDAPFormat format, NetcdfDataset ds,
            AbstractViewVariable[] constraints) throws IOException {

        //Generate our base URL (which depends on the format)
        HttpRequestBase method = null;
        switch (format) {
        case ASCII:
            method = new HttpGet(opendapUrl + ".ascii");
            break;
        case DODS:
            method = new HttpGet(opendapUrl + ".dods");
            break;
        default:
            throw new IllegalArgumentException("Unsupported format " + format.toString());
        }

        //We may only have a value constraint (when we need to know the actual index constraints)
        //We can convert from value to index by taking the minimum bounding box.
        if (constraints != null) {
            for (AbstractViewVariable constraint : constraints) {
                calculateIndexBounds(ds, constraint);
            }

            URIBuilder builder = new URIBuilder(method.getURI());
            builder.setParameters(parseQuery(URLEncoder.encode((generateQueryForConstraints(constraints)), "UTF-8"), Consts.UTF_8));
            try {
                method.setURI(builder.build());
            } catch (URISyntaxException e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        logger.debug(String.format("url='%1$s' query='%2$s'", opendapUrl, method.getURI()));

        return method;
    }
    
    private static List <NameValuePair> parseQuery(String query, Charset charset) {
        if (query != null && query.length() > 0) {
            return URLEncodedUtils.parse(query, charset);
        }
        return null;
    }

}

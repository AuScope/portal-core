package org.auscope.portal.server.domain.opendap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Contains factory methods for generating ViewVariables
 * @author vot002
 *
 */
public class ViewVariableFactory {

    private static final Log log = LogFactory.getLog(ViewVariableFactory.class);

    private static String attemptGetString(JSONObject obj, String key) {
        if (obj.containsKey(key))
            return obj.getString(key);
        else
            return null;
    }

    /**
     * Parses a variable recursively into appropriate ViewVariable implementations
     * @throws IOException
     * @throws
     */
    private static ViewVariable parseVariableRecursive(Variable var) throws IOException  {
        List<Dimension> dimensions = var.getDimensions();

        //A single dimension means we can parse a SimpleAxis
        if (dimensions.size() == 1) {
            SimpleAxis axis = new SimpleAxis(var.getName(), var.getDataType().name(), var.getUnitsString(), null, null);
            Dimension d = dimensions.get(0);

            axis.setDimensionBounds(new SimpleBounds(0, d.getLength()));

            //Read our first and last values
            Array first = null, last = null;
            try {
                first = var.read(new int[] {0}, new int[] {1});
                last = var.read(new int[] {d.getLength() - 1}, new int[] {1});
            } catch (InvalidRangeException ex) {
                throw new IllegalArgumentException(String.format("Unable to read variable ranges '%1$s'", var), ex);
            }

            axis.setValueBounds(new SimpleBounds(first.getDouble(0), last.getDouble(0)));

            return axis;
        //Otherwise we have a multi dimensional variable that we can parse as a grid
        } else if (dimensions.size() > 0) {
            SimpleGrid grid = new SimpleGrid(var.getName(), var.getDataType().name(), var.getUnitsString(), null);
            List<ViewVariable> childAxes = new ArrayList<ViewVariable>();

            //Recursively parse each dimension (which should map to a variable in the parent group)
            for (Dimension d : dimensions) {
                Variable mappedVariable = d.getGroup().findVariable(d.getName());
                if (mappedVariable == null) {
                    //If the dimension doesn't map to a variable, we can't pull much information out of it
                    //So instead we'll have to introduce an axis that only includes dimension bounds
                    log.warn(String.format("Dimension '%1$s' has no matching variable in parent group '%2$s'", d, d.getGroup()));

                    SimpleAxis axis = new SimpleAxis(d.getName(), DataType.FLOAT.name(), "????", null, null);
                    axis.setDimensionBounds(new SimpleBounds(0, d.getLength() - 1));
                    childAxes.add(axis);
                } else {
                    ViewVariable parsedVar = parseVariableRecursive(mappedVariable);

                    if (parsedVar != null)
                        childAxes.add(parsedVar);
                }
            }

            if (childAxes.size() > 0) {
                grid.setAxes(childAxes.toArray(new ViewVariable[childAxes.size()]));
                return grid;
            } else {
                return null;
            }
        } else {
            //Currently unsupported...
            log.warn(String.format("Variables with 0 dimensions are currently unsupported. var='%1$s'", var));
            return null;
        }
    }

    /**
     * Parses a JSONObject into its appropriate ViewVariable implementation
     * @param obj
     * @return
     */
    private static ViewVariable parseJSONRecursive(JSONObject obj) {

        if (obj == null || obj.isNullObject())
            throw new NullArgumentException("obj");

        String type = obj.getString("type");
        if (type == null)
            throw new IllegalArgumentException("Object missing type " + obj);

        //Parse a SimpleAxis
        if (type.equals(SimpleAxis.TYPE_STRING)) {
            SimpleAxis axis = new SimpleAxis(attemptGetString(obj,"name"), attemptGetString(obj, "dataType"), attemptGetString(obj, "units"), null, null);

            JSONObject dimensionBounds = obj.getJSONObject("dimensionBounds");
            JSONObject valueBounds = obj.getJSONObject("valueBounds");

            if (dimensionBounds != null && !dimensionBounds.isNullObject()) {
                axis.setDimensionBounds(new SimpleBounds(dimensionBounds.getDouble("from"), dimensionBounds.getDouble("to")));
            }

            if (valueBounds != null && !valueBounds.isNullObject()) {
                axis.setValueBounds(new SimpleBounds(valueBounds.getDouble("from"), valueBounds.getDouble("to")));
            }

            return axis;

        //Parse a SimpleGrid
        } else if (type.equals(SimpleGrid.TYPE_STRING)) {
            JSONArray axes = obj.getJSONArray("axes");
            SimpleGrid grid = new SimpleGrid(attemptGetString(obj, "name"), attemptGetString(obj, "dataType"), attemptGetString(obj, "units"), null);
            List<ViewVariable> childAxes = new ArrayList<ViewVariable>();

            for (int i = 0; i < axes.size(); i++) {
                ViewVariable var = parseJSONRecursive(axes.getJSONObject(i));
                if (var != null)
                    childAxes.add(var);
            }

            if (childAxes.size() > 0) {
                grid.setAxes(childAxes.toArray(new ViewVariable[childAxes.size()]));
                return grid;
            } else {
                return null;
            }

        } else {
            throw new IllegalArgumentException("Unable to parse type " + type);
        }
    }

    /**
     * Generate a list of ViewVariables direct from a dataset.
     * @param ds
     * @return
     * @throws IOException
     */
    public static ViewVariable[] fromNetCDFDataset(NetcdfDataset ds) throws IOException {
        return fromNetCDFDataset(ds, null);
    }

    /**
     * Generate a list of ViewVariables direct from a dataset. The list will be filtered to only include variables
     * with the specified name
     * @param ds
     * @param variableNameFilter if not null, all ViewVariables in the response will have the name variableNameFilter
     * @return
     * @throws IOException
     */
    public static ViewVariable[] fromNetCDFDataset(NetcdfDataset ds, String variableNameFilter) throws IOException {
        List<ViewVariable> result = new ArrayList<ViewVariable>();

        for (Variable var : ds.getVariables()) {

            if (variableNameFilter != null) {
                if (!var.getName().equals(variableNameFilter)) {
                    continue;
                }
            }

            ViewVariable parsedViewVar = parseVariableRecursive(var);
            if (parsedViewVar != null)
                result.add(parsedViewVar);
        }

        return result.toArray(new ViewVariable[result.size()]);
    }



    /**
     * Generates a list of ViewVariables from an already encoded JSONArray
     * @param arr
     * @return
     */
    public static ViewVariable[] fromJSONArray(JSONArray arr) {
        List<ViewVariable> result = new ArrayList<ViewVariable>();

        for (int i = 0; i < arr.size(); i++) {
            ViewVariable parsedViewVar = parseJSONRecursive(arr.getJSONObject(i));
            if (parsedViewVar != null)
                result.add(parsedViewVar);
        }

        return result.toArray(new ViewVariable[result.size()]);
    }
}

/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.gridjob;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A Hibernate-backed GeodesySeries data object
 *
 * @author Cihan Altinay
 * @author Abdi Jama  -- Modified for Geodesy
 */
public class GeodesySeriesDao extends HibernateDaoSupport {

    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Queries for series matching the given criteria. Some but not all of
     * the parameters may be <code>null</code>.
     *
     * @return a list of matching <code>GeodesySeries</code> objects.
     */
    public List<GeodesySeries> query(final String user, final String name,
                                 final String desc) {
        String queryString = new String("from GeodesySeries s where");
        boolean first = true;
        if (user != null) {
            queryString += " s.user like '%"+user+"%'";
            first = false;
        }

        if (name != null) {
            if (!first) {
                queryString += " and";
            }

            queryString += " s.name like '%"+name+"%'";
            first = false;
        }

        if (desc != null) {
            if (!first) {
                queryString += " and";
            }

            queryString += " s.description like '%"+desc+"%'";
            first = false;
        }

        if (first) {
            logger.warn("All parameters were null!");
            return null;
        }
        
        return (List<GeodesySeries>) getHibernateTemplate().find(queryString);
    }

    /**
     * Retrieves the series with given ID.
     *
     * @return <code>GeodesySeries</code> object with given ID.
     */
    public GeodesySeries get(final int id) {
        return (GeodesySeries) getHibernateTemplate().get(GeodesySeries.class, id);
    }

    /**
     * Saves or updates the given series.
     */
    public void save(final GeodesySeries series) {
        getHibernateTemplate().saveOrUpdate(series);
    }
    
    /**
     * Delete the given series.
     */
    public void delete(final GeodesySeries series) {
        getHibernateTemplate().delete(series);
    }
}


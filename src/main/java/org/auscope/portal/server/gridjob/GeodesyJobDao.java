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
 * A Hibernate-backed GeodesyJob data object
 *
 * @author Cihan Altinay
 */
public class GeodesyJobDao extends HibernateDaoSupport {

    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Retrieves jobs that are grouped under given series
     *
     * @param seriesID the ID of the series
     *
     * @return list of <code>GeodesyJob</code> objects belonging to given series
     */
    public List<GeodesyJob> getJobsOfSeries(final int seriesID) {
        return (List<GeodesyJob>) getHibernateTemplate()
            .findByNamedParam("from GeodesyJob j where j.seriesId=:searchID",
                    "searchID", seriesID);
    }

    /**
     * Retrieves jobs that belong to a specific user
     *
     * @param user the user whose jobs are to be retrieved
     *
     * @return list of <code>GeodesyJob</code> objects belonging to given user
     */
    public List<GeodesyJob> getJobsByUser(final String user) {
        return (List<GeodesyJob>) getHibernateTemplate()
            .findByNamedParam("from GeodesyJob j where j.user=:searchUser",
                    "searchUser", user);
        /*
        return sessionFactory.getCurrentSession()
            .createQuery("from jobs j where j.user=:searchUser")
            .setString("searchUser", user)
            .list();
        */
    }

    /**
     * Retrieves the job with given ID.
     *
     * @return <code>GeodesyJob</code> object with given ID.
     */
    public GeodesyJob get(final int id) {
        return (GeodesyJob) getHibernateTemplate().get(GeodesyJob.class, id);
    }
    
    
    /**
     * Deletes the job with given ID.
     *
     * @return <code>GeodesyJob</code> object with given ID.
     */
    public void deleteJob(final GeodesyJob job) {
        getHibernateTemplate().delete(job);
    }

    /**
     * Saves or updates the given job.
     */
    public void save(final GeodesyJob job) {
        getHibernateTemplate().saveOrUpdate(job);
    }
}


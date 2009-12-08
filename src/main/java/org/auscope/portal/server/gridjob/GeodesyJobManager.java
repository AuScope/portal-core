/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.gridjob;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class that talks to the data objects to retrieve or save data
 *
 * @author Cihan Altinay
 * @author Abdi  Jama
 */
public class GeodesyJobManager {
    protected final Log logger = LogFactory.getLog(getClass());

    private GeodesyJobDao geodesyJobDao;
    private GeodesySeriesDao geodesySeriesDao;

    public List<GeodesySeries> querySeries(String user, String name, String desc) {
        return geodesySeriesDao.query(user, name, desc);
    }

    public List<GeodesyJob> getSeriesJobs(int seriesId) {
        return geodesyJobDao.getJobsOfSeries(seriesId);
    }

    public GeodesyJob getJobById(int jobId) {
        return geodesyJobDao.get(jobId);
    }

    public void deleteJob(GeodesyJob job) {
        geodesyJobDao.deleteJob(job);
    }
    
    public GeodesySeries getSeriesById(int seriesId) {
        return geodesySeriesDao.get(seriesId);
    }

    public void saveJob(GeodesyJob geodesyJob) {
        geodesyJobDao.save(geodesyJob);
    }

    public void deleteSeries(GeodesySeries series) {
        geodesySeriesDao.delete(series);
    }    
    public void saveSeries(GeodesySeries series) {
        geodesySeriesDao.save(series);
    }

    public void setGeodesyJobDao(GeodesyJobDao geodesyJobDao) {
        this.geodesyJobDao = geodesyJobDao;
    }

    public void setGeodesySeriesDao(GeodesySeriesDao geodesySeriesDao) {
        this.geodesySeriesDao = geodesySeriesDao;
    }
}


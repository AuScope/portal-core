package org.auscope.portal.pressuredb;

import java.io.Serializable;

/**
 * Represents the response from a Pressure DB web service 'getAvailableOM' request.
 * @author Josh Vote
 *
 */
public class AvailableOMResponse implements Serializable {
    private static final long serialVersionUID = 8604798910766216119L;
    private String wellID;
    private String omUrl;
    private boolean obsTemperature;
    private boolean obsPressureData;
    private boolean obsSalinity;
    private boolean pressureRft;
    private boolean pressureDst;
    private boolean pressureFitp;
    private boolean salinityTds;
    private boolean salinityNacl;
    private boolean salinityCl;
    private boolean temperatureT;



    /**
     * @return the omUrl
     */
    public String getOmUrl() {
        return omUrl;
    }
    /**
     * @param omUrl the omUrl to set
     */
    public void setOmUrl(String omUrl) {
        this.omUrl = omUrl;
    }
    /**
     * @return the wellID
     */
    public String getWellID() {
        return wellID;
    }
    /**
     * @param wellID the wellID to set
     */
    public void setWellID(String wellID) {
        this.wellID = wellID;
    }
    /**
     * @return the obsTemperature
     */
    public boolean isObsTemperature() {
        return obsTemperature;
    }
    /**
     * @param obsTemperature the obsTemperature to set
     */
    public void setObsTemperature(boolean obsTemperature) {
        this.obsTemperature = obsTemperature;
    }
    /**
     * @return the obsPressureData
     */
    public boolean isObsPressureData() {
        return obsPressureData;
    }
    /**
     * @param obsPressureData the obsPressureData to set
     */
    public void setObsPressureData(boolean obsPressureData) {
        this.obsPressureData = obsPressureData;
    }
    /**
     * @return the obsSalinity
     */
    public boolean isObsSalinity() {
        return obsSalinity;
    }
    /**
     * @param obsSalinity the obsSalinity to set
     */
    public void setObsSalinity(boolean obsSalinity) {
        this.obsSalinity = obsSalinity;
    }
    /**
     * @return the pressureRft
     */
    public boolean isPressureRft() {
        return pressureRft;
    }
    /**
     * @param pressureRft the pressureRft to set
     */
    public void setPressureRft(boolean pressureRft) {
        this.pressureRft = pressureRft;
    }
    /**
     * @return the pressureDst
     */
    public boolean isPressureDst() {
        return pressureDst;
    }
    /**
     * @param pressureDst the pressureDst to set
     */
    public void setPressureDst(boolean pressureDst) {
        this.pressureDst = pressureDst;
    }
    /**
     * @return the pressureFitp
     */
    public boolean isPressureFitp() {
        return pressureFitp;
    }
    /**
     * @param pressureFitp the pressureFitp to set
     */
    public void setPressureFitp(boolean pressureFitp) {
        this.pressureFitp = pressureFitp;
    }
    /**
     * @return the salinityTds
     */
    public boolean isSalinityTds() {
        return salinityTds;
    }
    /**
     * @param salinityTds the salinityTds to set
     */
    public void setSalinityTds(boolean salinityTds) {
        this.salinityTds = salinityTds;
    }
    /**
     * @return the salinityNacl
     */
    public boolean isSalinityNacl() {
        return salinityNacl;
    }
    /**
     * @param salinityNacl the salinityNacl to set
     */
    public void setSalinityNacl(boolean salinityNacl) {
        this.salinityNacl = salinityNacl;
    }
    /**
     * @return the salinityCl
     */
    public boolean isSalinityCl() {
        return salinityCl;
    }
    /**
     * @param salinityCl the salinityCl to set
     */
    public void setSalinityCl(boolean salinityCl) {
        this.salinityCl = salinityCl;
    }
    /**
     * @return the temperatureT
     */
    public boolean isTemperatureT() {
        return temperatureT;
    }
    /**
     * @param temperatureT the temperatureT to set
     */
    public void setTemperatureT(boolean temperatureT) {
        this.temperatureT = temperatureT;
    }


}

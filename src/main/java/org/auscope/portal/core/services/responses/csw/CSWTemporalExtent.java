package org.auscope.portal.core.services.responses.csw;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents the gmd:EX_TemporalExtent element of a CSW record response
 * 
 * @author woo392
 *
 */
public class CSWTemporalExtent implements Serializable {
	
	private static final long serialVersionUID = 2417258609681750738L;
	
	private Date beginPosition;
	private Date endPosition;
	
	public CSWTemporalExtent() {}
	
	public CSWTemporalExtent(Date beginPosition, Date endPosition) {
		this.beginPosition = beginPosition;
		this.endPosition = endPosition;
	}
	
	public Date getBeginPosition() {
		return beginPosition;
	}

	public void setBeginPosition(Date beginPosition) {
		this.beginPosition = beginPosition;
	}

	public Date getEndPosition() {
		return endPosition;
	}

	public void setEndPosition(Date endPosition) {
		this.endPosition = endPosition;
	}

}

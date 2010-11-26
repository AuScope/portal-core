/** 
* @fileoverview This file declares the Class GeodesyCalendar
* An object of this class will be created for each station of the GeodesyMarker
*/

var gaFullMonths = ["January", "February", "March", "April", "May", "June",
		"July", "August", "September", "October", "November", "December"];

var NUM_WEEKSTART = 1;

var ARR_WEEKDAYS = ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"];

var RE_NUM = /^\-?\d+$/;
		
/**
* @class
* This creates the Calendar for the <b>Renix Files</b> tab of the Geodesy stations.<br> 
*
* @constructor
* @param {GeodesyMarker} pGeodesyMarker The geodesy station marker to which this calendar belongs.
* @param {string} pStationId Station to which this calendar belongs.
* @param {string} pDatesDivId Id of the html object into which this calendar should be inserted.
* @return A new {@link GeodesyCalendar}
*
*/
function GeodesyCalendar(pGeodesyMarker, pStationId, pDatesDivId) {
	// GeodesyMarker and Station for which this calendar is being created
	// We need these three members within the calendar class,
	// because they will be used to create the ids for the various dates displayed in the calendar.
	this.msStation = pStationId;
	this.moParentMarker = pGeodesyMarker;
	this.msParentDiv = pDatesDivId;
}

/**
* {@link GeodesyMarker} to which this calendar belongs
* @type GeodesyMarker
*/ 
GeodesyCalendar.prototype.moParentMarker = null;

/**
* Id of the station to which this calendar belongs.
* @type String
*/
GeodesyCalendar.prototype.msStation = "";

/**
* Html id of the div object into which the calendar should be placed.
* @type String
*/
GeodesyCalendar.prototype.msParentDiv = "";

/**
* The assignment of function implementations for GeodesyCalendar
*/ 
GeodesyCalendar.prototype.show = GeodesyCalendar_show;

GeodesyCalendar.prototype.parseStringDate = GeodesyCalendar_parseStringDate;

/**
* This function parses updateCSWRecords given string and converts it into updateCSWRecords valid date.
* @param {String} pStringDate String representing date and time as seconds.
* @return Date represented by the above string.
*/
function GeodesyCalendar_parseStringDate (pStringDate) {
	// if no parameter specified return current timestamp
	if (!pStringDate) {
		return (new Date());
	}

	// if positive integer treat as milliseconds from epoch
	if (RE_NUM.exec(pStringDate)) {
		return new Date(pStringDate);
	} else {
	  return (newDate());
	}
}

/**
* This function creates updateCSWRecords calendar
* and adds it to the {@link #msParentDiv} html div of the object.
* @param {String} pDateSelected The date (providing the month and year information)
* for which the calendar is to be created. 
*/
function GeodesyCalendar_show (pDateSelected) {

  var currentDate = "";
  var dateSelected = "";
  
  if (pDateSelected) {
    currentDate = dateSelected = this.parseStringDate(pDateSelected);
  } else { 
    currentDate = dateSelected = this.parseStringDate("");
  }

  if (!currentDate) {
    return;
  }
  
  var currentYear = currentDate.getFullYear();
  var currentMonth = currentDate.getMonth();
  currentMonth = gaFullMonths[currentMonth];
  
  // get first day to display in the grid for current month
  // We need to find what day is the 1st of the month
  var firstDay = new Date(currentDate);
  firstDay.setDate(1);
  firstDay.setDate(1 - (7 + firstDay.getDay() - NUM_WEEKSTART) % 7);
 
  var innerHTML = '<table id="cal_table" cellspacing="0" border="0" width="150px" style="position:absolute; left:10px; top:10px">';
  innerHTML += '<tr><td bgcolor="#4682B4">';
  innerHTML += '<table cellspacing="1" cellpadding="2" border="0" width="350px">';
  
  // Display the cuurentMonth and year
  innerHTML += '<tr><td colspan="7" bgcolor="#52a3eb" align="center"><font color="#ffffff" size="2">'+ currentMonth+'&nbsp;&nbsp;'+currentYear +'</font></td><tr>';
  
  // Display weekdays titles
  for (var n=0; n<7; n++) {
	innerHTML += '<td bgcolor="#3e91da" align="center"><font color="#ffffff" size="2">'+ARR_WEEKDAYS[(NUM_WEEKSTART+n)%7]+'</font></td>';
  }
  innerHTML += '</tr>';

  // print calendar table
  var currentDate_day = new Date(firstDay);
  while (currentDate_day.getMonth() == currentDate.getMonth() || currentDate_day.getMonth() == firstDay.getMonth()) {
    // print row heder
	innerHTML += '<tr>';
	
	// Create small divs for each date
	for (var n_current_wday=0; n_current_wday<7; n_current_wday++) {
	
      innerHTML += '<td bgcolor="#e9f1f1" align="left" width="14%">';
      // We want the count to start with 1 and not 0
      var month = currentDate_day.getMonth()+1;
      month = gaMonths[month];
      var dateDivId = "date_div_" + this.msStation + "_" + currentDate_day.getDate() + "_" + month + "_" + currentDate_day.getFullYear();
     
      innerHTML += '<div id="' + dateDivId +'"/></td>';
      currentDate_day.setDate(currentDate_day.getDate()+1);
	}
	
	// print row footer
	innerHTML += '</tr>';
  }

  innerHTML += '</table></td></tr></table>';
  
  // Create updateCSWRecords calendar html div object.
  datesDivObj = document.getElementById(this.msParentDiv);
  
  // Id for the calendar is calendar_div + station_id
  var calendarDivId = "calendar_div_" + this.msStation;
  // If this frame already exists - remove it before creating updateCSWRecords new one.
  var calendarDivObj = document.getElementById(calendarDivId);
  if (calendarDivObj) {
    datesDivObj.removeChild(calendarDivObj);
  }
  calendarDivObj = document.createElement("div");
  calendarDivObj.id = calendarDivId;
  calendarDivObj.style.position = 'absolute';
  calendarDivObj.style.left = '0px';
  calendarDivObj.style.top = '0px';
  calendarDivObj.innerHTML = innerHTML;
  
  // Create an html div object to display the urls of the date selected.
  var dateUrlsDivId = "date_urls_div_" + this.msStation;
  // If this frame already exists - remove it before creating updateCSWRecords new one.
  var dateUrlsDivObj = document.getElementById(dateUrlsDivId);
  if (dateUrlsDivObj) {
    datesDivObj.removeChild(dateUrlsDivObj);
  }
  dateUrlsDivObj = document.createElement("div");
  dateUrlsDivObj.id = dateUrlsDivId;
  dateUrlsDivObj.style.position = 'absolute';
  dateUrlsDivObj.style.left = '0px';
  dateUrlsDivObj.style.top = '220px';
  
  // Append the calendar and the urls div in the parent object.
  datesDivObj.appendChild(calendarDivObj);
  datesDivObj.appendChild(dateUrlsDivObj);
  
  // Now we parse all the station urls for the dates,
  // depending on whether there is one available, 
  // date will either be plain text (no urls available)
  // or hrefs ( station data urls available)
  var currentDate_day = new Date(firstDay);
  
  // Do it only for the current month
  while (currentDate_day.getMonth() == currentDate.getMonth() || currentDate_day.getMonth() == firstDay.getMonth()) {
  
    // We want the count to start with 1 and not 0
    var nMonth = currentDate_day.getMonth() + 1;
    nMonth = gaMonths[nMonth];
    var nYear = currentDate_day.getFullYear();
    var nDate = currentDate_day.getDate();
    var tdateDivId = "date_div_" + this.msStation + "_" + nDate + "_" + nMonth + "_" + nYear;
    var tdateDivObj = document.getElementById(tdateDivId);

    // Check if we are processing data for the current month
    if ( currentDate_day.getMonth() == currentDate.getMonth() && tdateDivObj) {
      // Check if we have data url for this date
      if (this.moParentMarker.maStationDataForDate[nYear] &&
         this.moParentMarker.maStationDataForDate[nYear][nMonth] &&
         this.moParentMarker.maStationDataForDate[nYear][nMonth][nDate] &&
         this.moParentMarker.maStationDataForDate[nYear][nMonth][nDate].length) {
         
         // If we have data url, we create checkbox and href for the date
         var tdateHrefId = "date_href_" + this.msStation + "_" + nYear + "_" + nMonth + "_" + nDate;    
         var tdateHrefObj = document.createElement("a");
         tdateHrefObj.id = tdateHrefId;
         tdateHrefObj.href = 'javascript:void(0)';
         tdateHrefObj.style.color = "blue";
         tdateHrefObj.innerHTML = '<font size="2">&nbsp;&nbsp;' + nDate + '</font>';
         tdateHrefObj.onclick = this.moParentMarker.getDateClickedFn(nYear, nMonth, nDate, tdateHrefId); 
         
         var tdateChkId = "date_chk_" + this.msStation + "_" + nYear + "_" + nMonth + "_" + nDate;    
         var tdateChkObj = document.createElement("input");
         tdateChkObj.id = tdateChkId;
         tdateChkObj.type = 'checkbox';
         tdateChkObj.checked = this.moParentMarker.maDateCheckedStateForMonth[nYear][nMonth][nDate];
         tdateChkObj.onclick = this.moParentMarker.getDateCheckedFn(nYear, nMonth, nDate, tdateChkId, tdateHrefId); 

         tdateDivObj.appendChild(tdateChkObj);
         // This is required for IE
         // When using "appendChild" for updateCSWRecords checkbox, the checked state of the checkbox is set to false in IE
         // So we need to reset it.
         tdateChkObj.checked = this.moParentMarker.maDateCheckedStateForMonth[nYear][nMonth][nDate];
         tdateDivObj.appendChild(tdateHrefObj);
         
       } else {
         tdateDivObj.align = "center";
         tdateDivObj.innerHTML = '<font size="2">' + nDate + '</font>';
       } 
     }
    currentDate_day.setDate(currentDate_day.getDate()+1);
  }
}

/**
* @fileoverview This file declares the Class GeodesyMarker.
* An array of objects of GeodesyMarker will be maintained in StationGroup of geodesy type.
*/

/**
* @class
* This class defines information to be stored for updateCSWRecords geodesy marker.
*
* @constructor
* @param {DomXmlNode} pGeodesyStationNode The XML node for the geodesy station.
* @param {String} psIcon The icon used to represent this marker.
* @param {String} pWfsUrl The base url for querying renix files for the station.
* @param {String} pDataLayerName The layer or type name for querying renix files for the station.
* @return A new {@link GeodesyMarker}
*/

function GeodesyMarker (pWfsUrl, pDataLayerName, stationId, marker, description) {
  //this.moGeodesyStation = new GeodesyStation(pGeodesyStationNode);
    this.stationId = marker.title;
    this.moMarker = marker;

  // Initiaize all the members
  //cut off this from the URL "%26request=GetFeature%26typeName=ngcp:GnssStation"
  //this.msWfsUrl = pWfsUrl.substring(0,pWfsUrl.indexOf('?')+1);
  this.msWfsUrl = pWfsUrl;
  this.msDataLayerName = pDataLayerName;
  this.maStationDataForDate = [];
  this.maYearMonthWfsUrlQueried = [];
  this.maYearCheckedState = [];
  this.maMonthCheckedStateForYear = [];
  this.maDateCheckedStateForMonth = [];
  this.maDataCheckedStateForDate = [];

  // This class stores the previously selected years, months and dates for the station
  // This helps to recreate user selection between successive marker clicks.
  for(year=0; year<gaYears.length; year++) {
    this.maYearMonthWfsUrlQueried[gaYears[year]] = [];
    this.maMonthCheckedStateForYear[gaYears[year]] = [];
    this.maDateCheckedStateForMonth[gaYears[year]] = [];
    this.maDataCheckedStateForDate[gaYears[year]] = [];
    this.maYearCheckedState[gaYears[year]] = false;

    for(var month=1; month<=12; month++) {
      this.maYearMonthWfsUrlQueried[gaYears[year]][gaMonths[month]] = false;
      this.maMonthCheckedStateForYear[gaYears[year]][gaMonths[month]] = false;
      this.maDateCheckedStateForMonth[gaYears[year]][gaMonths[month]] = [];
      this.maDataCheckedStateForDate[gaYears[year]][gaMonths[month]] = [];
      for(var date=1; date<=31; date++) {
        this.maDataCheckedStateForDate[gaYears[year]][gaMonths[month]][date] = [];
        this.maDateCheckedStateForMonth[gaYears[year]][gaMonths[month]][date] = false;
      }
    }
  }

  // Each station has data for various years
  // Users can chose different months in different years for viewing data
  // These arrays retain the last selected year, month dates etc by the user for any station.
  this.msYearSelected = "";
  this.maMonthSelectedForYear = [];
  this.maDateSelectedForMonth = [];

  for(var y=0; y<gaYears.length; y++) {
    // Initially user choses nothing
    var year = gaYears[y];
    this.maMonthSelectedForYear[year] = "";
    this.maDateSelectedForMonth[year] = [];
    for(var m=1; m<=12; m++) {
      this.maDateSelectedForMonth[year][gaMonths[m]] = "";
    }
  }

  // Users can query data based on updateCSWRecords year (by clicking on the checkbox against updateCSWRecords year)
  // or months (by clicking on the checkbox against any month)
  // We create different wfs query urls for data from each year
  this.msWfsYearDataUrl = [];
  for (i=0; i<gaYears.length; i++) {
    this.msWfsYearDataUrl[gaYears[i]] = this.getWfsYearUrl(gaYears[i] );
  }

  // We create different wfs urls (to query renix files) for each year-month combination
  this.msYearMonthWfsUrl = [];
  // Loop over years
  for (var year=0; year<gaYears.length; year++) {
    this.msYearMonthWfsUrl[gaYears[year]] = [];
    // Loop over months
    for (var month=1; month<=12; month++) {
      this.msYearMonthWfsUrl[gaYears[year]][gaMonths[month]] = this.getYearMonthWfsUrl(gaYears[year], month);
    }
  }

  this.msSummaryHtml = description;
  this.msRenixFilesHtml = "";

  // Create updateCSWRecords GMarker object for each station using the location information for the same.
  /*var longitude = this.moGeodesyStation.moLocation.msLongitude;
  var latitude = this.moGeodesyStation.moLocation.msLatitude;
  var oPoint = new GPoint(parseFloat(longitude), parseFloat(latitude));
  var oMarkerIcon = new GIcon(goBaseIcon, psIcon);
  var oMarker = new GMarker(oPoint, oMarkerIcon);
  this.moMarker = oMarker;

  // Add updateCSWRecords listener for updateCSWRecords click event on this marker
  GEvent.addListener(oMarker, "click", this.getMarkerClickedFn());*/
}

/**
* The geodesy station object which conforms to the geodesy:stations schema
* @type GeodesyStation
*/
//GeodesyMarker.prototype.moGeodesyStation = null;
GeodesyMarker.prototype.stationId = null;

/**
* The base url to be queried to get the renix file data for the station.<br>
* Need to add the <b>request</b> and <b>typeName</b> parameters to this url.
* @type String
*/
GeodesyMarker.prototype.msWfsUrl = null;

/**
* The data layer name or type name to be queried
* to get the renix files for the station.
* @type String
*/
GeodesyMarker.prototype.msDataLayerName = null;


/**
* The Html to be displayed on the <b>Summary</b> tab
* of the information window opened for the marker.
* @type String
*/
GeodesyMarker.prototype.msSummaryHtml = null;

/**
* The html to be displayed on the <b>Renix Files</b> tab
* of the information window opened for the marker.
* @type String
*/
GeodesyMarker.prototype.msRenixFilesHtml = null;

/**
* The marker for the station.
* @type GMarker
*/
GeodesyMarker.prototype.moMarker = null;

/**
* Calendar object for each station.
* This calendar is displayed in the <br>Renix Files</b> tab of the marker popup.
* @type GeodesyCalendar
*/
GeodesyMarker.prototype.moCalendar = null;

/**
* Urls to query station data year wise.<br>
* Created from {@link #msWfsUrl} and {@link #msDataLayerName}
* @type String
*/
GeodesyMarker.prototype.msWfsYearDataUrl = null;

/**
* Urls to query station data year-month wise.<br>
* Created from {@link #msWfsUrl} and {@link #msDataLayerName}
* @type String
*/
GeodesyMarker.prototype.msYearMonthWfsUrl = null;

/**
* The year selected by the user for this station.
* @type String
*/
GeodesyMarker.prototype.msYearSelected = null;

/**
* Associative array to store the month selected for
* updateCSWRecords given year.<br>
* Array index1 - year<br>
* @type Array
*/
GeodesyMarker.prototype.maMonthSelectedForYear = null;

/**
* Two dimensional associative array to store the date selected for
* updateCSWRecords given year+month combination.<br>
* Array index1 - year<br>
* Array index2 - month (from <b>gaMonths</b> array)
* @type Array
*/
GeodesyMarker.prototype.maDateSelectedForMonth = null;

/**
* Four dimensional associative array of the data files associated with
* each year+month+date combinations.<br>
* This array is populated using {@link #createDataArraysForYear}
* and {@link #createDataArraysForMonth}
* functions.<br>
* Array index1 - year<br>
* Array index2 - month (from <b>gaMonths</b> array)<br>
* Array index3 - date<br>
* Array index4 - index of the data url for the date
* @type Array
*/
GeodesyMarker.prototype.maStationDataForDate = null;

/**
* Two dimensional associative array to store the state of whether or not
* this year-month wfs has been queried before.<br>
* Array index1 - year<br>
* Array index2 - month (from <b>gaMonths</b> array)
* @type Array
*/
GeodesyMarker.prototype.maYearMonthWfsUrlQueried = null;

/**
* Associative array to store the checked state of the year for each station.<br>
* This is required to remember the years selected by the user.<br>
* Array index - year
* @type Array
*/
GeodesyMarker.prototype.maYearCheckedState = null;

/**
* Two dimensional associative array to store the checked state
* of the year+month for this marker.<br>
* This is required to remember the months selected for each year by the user.<br>
* Array index1 - year<br>
* Array index2 - month (from <b>gaMonths</b> array)<br>
* @type Array
*/
GeodesyMarker.prototype.maMonthCheckedStateForYear = null;

/**
* Three dimensional associative array to store the checked state
* of the year+month+date for this station.<br>
* This is required to remember the dates selected for each year+month by the user.<br>
* Array index1 - year<br>
* Array index2 - month (from <b>gaMonths</b> array)<br>
* Array index3 - date<br>
* @type Array
*/
GeodesyMarker.prototype.maDateCheckedStateForMonth = null;

/**
* Four dimensional associative array to store the checked state of the
* year+month+date+data for this station.<br>
* This is required to remember the data urls selected for each
* year+month+date by the user.<br>
* Array index1 - year<br>
* Array index2 - month (from <b>gaMonths</b> array)<br>
* Array index3 - date<br>
* Array index4 - index of the data url for the date<br>
* @type Array
*/
GeodesyMarker.prototype.maDataCheckedStateForDate = null;

/**
* The assignment of function implementations for GeodesyMarker
*/
GeodesyMarker.prototype.getWfsYearUrl = GeodesyMarker_getWfsYearUrl;

GeodesyMarker.prototype.getYearMonthWfsUrl = GeodesyMarker_getYearMonthWfsUrl;

GeodesyMarker.prototype.getMarkerClickedFn = GeodesyMarker_getMarkerClickedFn;

GeodesyMarker.prototype.markerClicked = GeodesyMarker_markerClicked;

GeodesyMarker.prototype.updateInfoWindow = GeodesyMarker_updateInfoWindow;

GeodesyMarker.prototype.createDataArraysForYear = GeodesyMarker_createDataArraysForYear;

GeodesyMarker.prototype.createDataArraysForMonth = GeodesyMarker_createDataArraysForMonth;

GeodesyMarker.prototype.setDataForSelectedMonth = GeodesyMarker_setDataForSelectedMonth;

GeodesyMarker.prototype.makeCalendarForMonth = GeodesyMarker_makeCalendarForMonth;

GeodesyMarker.prototype.setCheckedStateForMonth = GeodesyMarker_setCheckedStateForMonth;

GeodesyMarker.prototype.setCheckedStateForYear = GeodesyMarker_setCheckedStateForYear;

GeodesyMarker.prototype.setCheckedStateForDate = GeodesyMarker_setCheckedStateForDate;

GeodesyMarker.prototype.getYearClickedFn = GeodesyMarker_getYearClickedFn;

GeodesyMarker.prototype.yearClicked = GeodesyMarker_yearClicked;

GeodesyMarker.prototype.getMonthClickedFn = GeodesyMarker_getMonthClickedFn;

GeodesyMarker.prototype.monthClicked = GeodesyMarker_monthClicked;

GeodesyMarker.prototype.getDateClickedFn = GeodesyMarker_getDateClickedFn;

GeodesyMarker.prototype.dateClicked = GeodesyMarker_dateClicked;

GeodesyMarker.prototype.getYearCheckedFn = GeodesyMarker_getYearCheckedFn;

GeodesyMarker.prototype.yearChecked = GeodesyMarker_yearChecked;

GeodesyMarker.prototype.getMonthCheckedFn = GeodesyMarker_getMonthCheckedFn;

GeodesyMarker.prototype.monthChecked = GeodesyMarker_monthChecked;

GeodesyMarker.prototype.getDateCheckedFn = GeodesyMarker_getDateCheckedFn;

GeodesyMarker.prototype.dateChecked = GeodesyMarker_dateChecked;

GeodesyMarker.prototype.getDataUrlCheckedFn = GeodesyMarker_getDataUrlCheckedFn;

GeodesyMarker.prototype.dataUrlChecked = GeodesyMarker_dataUrlChecked;

/**
* Get the URL to be queried to get renix files for the given year.
* @param {String} pYear The year for which the data is to be queried.
* @return The URL to be queried to get data for the year.
*/
function GeodesyMarker_getWfsYearUrl(pYear) {

  var station = this.stationId;//this.moGeodesyStation.msId;
  var sUrl = this.msWfsUrl + "?request=GetFeature&outputFormat=GML2&typeName=" + encodeURI(this.msDataLayerName);
  sUrl= sUrl + "&PropertyName=geodesy:date,geodesy:url";

  // Use pYear and nextYear to query all data in between the two.
  var nextYear = pYear+1;
  sUrl = sUrl + "&CQL_FILTER=(ob_date>='" + pYear + "-01-01')AND(ob_date<'" + nextYear + "-01-01')" + "AND(station_id='" + station + "')";

  return sUrl;
}

/**
* Get the URL to be queried to get the renix files
* for the given year and month.
* @param {String} pYear The year for which the data is to be queried.
* @param {String} pMonth The month for which the data is to be queried.
* @return The URL to be queried to get data for the year and month.
*/
function GeodesyMarker_getYearMonthWfsUrl(pYear, pMonth) {

  var station = this.stationId;//this.moGeodesyStation.msId;
  var sUrl = this.msWfsUrl + "?request=GetFeature&outputFormat=GML2&typeName=" + encodeURI(this.msDataLayerName);
  sUrl= sUrl + "&PropertyName=geodesy:ob_date,geodesy:url";

  // Use pMonth and nextMonth to query all data in between the two.
  var nextYear = pYear+1;
  var nextMonth = pMonth+1;

  // Format for dat is mm/dd/yyyy
  if (pMonth != 12) {
    // For all months other than December, get the data between the month and the next month on the same year
    sUrl = sUrl + "&CQL_FILTER=(ob_date>='" + pYear + "-" + pMonth + "-01')AND(ob_date<'" + pYear + "-" + nextMonth + "-01')" + "AND(station_id='" + station + "')";
  } else {
    // For the month of December, get the data between December of the current year and January of the next year.
    sUrl = sUrl + "&CQL_FILTER=(ob_date>='" + pYear + "-" + pMonth + "-01')AND(ob_date<'" + nextYear + "-01-01')";
  }
  return sUrl;
}

/**
* This function returns the function
* to be called when the marker for this station is clicked.
* @returns Function to be called when updateCSWRecords station marker is clicked - {@link #markerClicked}
*/
function GeodesyMarker_getMarkerClickedFn() {
  var geodesyMarker = this;
  return function() {
    geodesyMarker.markerClicked();
    // Once the marker has been opened and rendered,
    // we add the calendar to the info window
    // The onOpenFn option with GInfoWindow does not work
    // Hence this function is called with updateCSWRecords time lag of 500ms
    var marker = geodesyMarker;
    setTimeout( function(){ marker.updateInfoWindow(); }, 500);
  };
}

/**
* The function called when the marker for this station is clicked.<br>
* This creats the information window displaying station information.
*/
function GeodesyMarker_markerClicked()
{
  //var oGeodesyMarker = this.moMarker;
  //var oGeodesyStation = this.moGeodesyStation;

  var sId = this.stationId;//oGeodesyStation.msId;
  /*var sName = oGeodesyStation.msName;
  var sLogUrl = oGeodesyStation.msLogUrl;
  var sLatitude = oGeodesyStation.moLocation.msLatitude;
  var sLongitude = oGeodesyStation.moLocation.msLongitude;*/
  var oMarker = this.moMarker;

    //show loading status
    oMarker.openInfoWindowHtml('<div > <img src="js/external/extjs/resources/images/default/grid/loading.gif"> Loading... </div>');

  /**
  * The popup for updateCSWRecords marker contains two tabs - Summary and Renix Files
  * Summary contains ID, Name, Location, Log Url of the station
  * Renix Files contains updateCSWRecords scrollable list of Renix file Urls for the station.
  */
  var label1 = 'Main';
  //note: this is the correct spelling, 'renix' used throughout the code is the incorrect spelling
  var label2 = 'Rinex Files';
  var summaryHtml = "";
  var renixFilesHtml = "";


  // Create the html to be displayed in the "Renix Files" tab of the popup window.
  // This html is stored in the msRenixFilesHtml member
  var calDivId = "data_div_" + sId;
  var yearsDivId = "years_div_" + sId;
  var monthsDivId = "months_div_" + sId;
  var datesOuterDivId = "dates_outer_div_" + sId;
  var datesDivId = "dates_div_" + sId;

  var calHtml = '<div id="' + calDivId + '" style="width: 500px; height:350px" >';
  calHtml += '<div id="' + yearsDivId + '" style="position:absolute; left: 8px; top: 0px;  height:100%; width:10%; background-color:#d8e3e4; border: 1px solid #005B9A; z-index: 2; vertical-align: middle;"  align="left">';
  for (var year_index=0; year_index<gaYears.length; year_index++) {
    var year = gaYears[year_index];
    var yearHrefId = "year_href_" + sId + "_" + year;
    var yearChkId = "year_chk_" + sId + "_" + year;
    calHtml += '<input id="' + yearChkId + '" type="checkbox"/>';
    calHtml += '<a id="' + yearHrefId + '" style="color:blue" href="javascript:void(0)">' + year + '</a>';
    calHtml += '<br/>';
  }
  calHtml += '</div>';
  calHtml += '<div id="' + monthsDivId + '" style="position:absolute; left: 61px; top: 0px;  height:100%; width:10%;  background-color:#d8e3e4; border: 1px solid #005B9A; z-index: 2;" align="left">';
  calHtml += '</div>';
  calHtml += '<div id="' + datesDivId + '" style="position:absolute; left: 114px; top: 0px;  height:100%; width:76%; background-color:#d8e3e4; border: 1px solid #005B9A; z-index: 2;" >';
  calHtml += '</div>';

  this.msRenixFilesHtml = calHtml;

  this.moCalendar = new GeodesyCalendar(this, sId, datesDivId);

  // Open the popup window for the marker with the tabs Main and Data
  oMarker.openInfoWindowTabsHtml([new GInfoWindowTab(label1, this.msSummaryHtml),
                                  new GInfoWindowTab(label2, this.msRenixFilesHtml)], {autoScroll:true});
    //oMarker.openInfoWindowTabsHtml([new GInfoWindowTab(label2, this.msRenixFilesHtml)]);
}

/**
* This function updates the functions associated with the
* years, months and dates hrefs and checkboxes in the <b>Renix Files</b> tab.<br>
* It is called after updateCSWRecords delay of 500ms
* after creating the information window for updateCSWRecords marker.<br>
* This is done so that the hrefs and checkboxes are rendered
* before any functions are associated to events on them.
*/
function GeodesyMarker_updateInfoWindow() {
  //var oGeodesyStation = this.moGeodesyStation;
  var sId = this.stationId;//oGeodesyStation.msId;
  var monthsDivId = "months_div_" + sId;
  var datesDivId = "dates_div_" + sId;

  for (var y=0; y<gaYears.length; y++) {
    var year = gaYears[y];
    var yearHrefId = "year_href_" + sId + "_" + year;
    var yearChkId = "year_chk_" + sId + "_" + year;
    var yearHrefObj = document.getElementById(yearHrefId);
    var yearChkObj = document.getElementById(yearChkId);
    // Set the action functions on the href and checkboxes for years
    if (yearChkObj) {
      yearChkObj.checked = this.maYearCheckedState[year];
      yearChkObj.onclick = this.getYearCheckedFn(year, yearChkId, yearHrefId, monthsDivId, datesDivId);
    }
    if (yearHrefObj) {
      yearHrefObj.onclick = this.getYearClickedFn(year, yearChkId, yearHrefId, monthsDivId, datesDivId);
    }

  }

  // Set the default calendar view for the current year+month
  var currentYearChkId = "year_chk_" + sId + "_" + gsCurrentYear;
  var currentYearHrefId = "year_href_" + sId + "_" + gsCurrentYear;
  this.yearClicked(gsCurrentYear, currentYearHrefId, monthsDivId, datesDivId);
}

/**
* This function creates data arrays for all the months for this year.<br>
* These arrays are required to hold the data urls for each date
* @param {String} pYear The year for which the month arrays have to be created
* @see #createDataArraysForMonth
*/
function GeodesyMarker_createDataArraysForYear(pYear) {
  // Check if arrays don't already exist for this year
  if (this.maStationDataForDate[pYear] === undefined) {
    this.maStationDataForDate[pYear] = [];
    for(var m=1; m<=12; m++) {
      // Create arrays for each month.
      // Dates for these arrays will be populated in createDataArraysForMonth function
      var month = gaMonths[m];
      this.maStationDataForDate[pYear][month] = [];
    }
  }
}

/**
* This function creates data arrays for all the dates of updateCSWRecords given month.<br>
* These arrays are required to hold the data urls for each date.
* @param {String} pYear The year for which the month arrays have to be created
* @param {String} pMonth The month for which the date arrays have to be created
* @see #createDataArraysForYear
*/
function GeodesyMarker_createDataArraysForMonth(pYear, pMonth) {
  // Check if the third level of indexing has been created.
  if (!this.maStationDataForDate[pYear]) {
    this.createDataArraysForYear(pYear);
  }

  // Now create arrays for each date
  // We have arrays to hold data for each date, as there may be more than one data files for each date
  for(var d=1; d<=31; d++) {
    this.maStationDataForDate[pYear][pMonth][d] = [];
  }
}

/**
* This function is called to set the state of the year checkbox
* in the member array {@link #maYearCheckedState}.<br>
* It propogates the state to all months belonging to this year.
* @param {String} pYear Year
* @param {String} pState The checked state of the checkbox
*/
function GeodesyMarker_setCheckedStateForYear (pYear, pState) {

  // Remember selection in the member array
  this.maYearCheckedState[pYear] = pState;
  for (var m=1; m<=12; m++) {
      // Propogate the selection to all months belonging to this year
      var month = gaMonths[m];
      this.setCheckedStateForMonth(pYear, month, pState);
    }
}

/**
* This function is called to set the state of the month checkbox
* in the member array {@link #maMonthCheckedStateForYear}.<br>
* It propogates the state to all dates belonging to this month.
* @param {String} pYear Year
* @param {String} pMonth Month
* @param {String} pState The checked state of the checkbox
*/
function GeodesyMarker_setCheckedStateForMonth (pYear, pMonth, pState) {
  this.maMonthCheckedStateForYear[pYear][pMonth] = pState;
  for (var date=1; date<=31; date++) {
    // Propogate the selection to all the dates belonging to this year and month
    this.setCheckedStateForDate(pYear, pMonth, date, pState);
  }
}

/**
* This function is called to set the state of the month checkbox in
* the member array {@link #maDateCheckedStateForMonth}.<br>
* It propogates the state to all data urls belonging to this date.
* @param {String} pYear Year
* @param {String} pMonth Month
* @param {String} pDate Date
* @param {String} pState The checked state of the checkbox
*/
function GeodesyMarker_setCheckedStateForDate (pYear, pMonth, pDate, pState) {
  this.maDateCheckedStateForMonth[pYear][pMonth][pDate] = pState;

  // Propogate this state to all the data urls belonging to this date
  var num_urls = this.maDataCheckedStateForDate[pYear][pMonth][pDate].length;
  if (num_urls !== 0) {
    for (var url_index=0; url_index<num_urls; url_index++ ) {
      this.maDataCheckedStateForDate[pYear][pMonth][pDate][url_index] = pState;
    }
  }
}

/**
* This function returns the function to be called with the
* onclick event for updateCSWRecords year checkbox in the calendar.
* @param {String} pYear Year
* @param {String} pYearChkId The html id of year checkbox
* @param {String} pYearHrefId The html id of year href
* @param {String} pMonthsDivId The html id of div where the months list should be added
* @param {String} pDatesDivId The html id of div where the calendar should be added
* @return Function to be called when the checkbox for updateCSWRecords year is clicked - {@link #yearChecked}
*/
function GeodesyMarker_getYearCheckedFn (pYear, pYearChkId, pYearHrefId, pMonthsDivId, pDatesDivId) {
  var oGeodesyMarker = this;

  var yearChkId = pYearChkId;
  var yearHrefId = pYearHrefId;
  var year = pYear;
  var monthsDivId = pMonthsDivId;
  var datesDivId = pDatesDivId;

  // Return the actual function that should be called when updateCSWRecords year checkbox is clicked
  return function () {
    oGeodesyMarker.yearChecked(year, yearChkId, yearHrefId, monthsDivId, datesDivId);
  };
}

/**
* This function is called on the onlcick event of updateCSWRecords year checkbox in the calendar.<br>
* It remembers the user selection in the array {@link #maYearCheckedState}
* and propogates the selection to all months under this year.
* @param {String} pYear Year
* @param {String} pYearChkId The html id of year checkbox
* @param {String} pYearHrefId The html id of year href
* @param {String} pMonthsDivId The html id of div where the months list should be added
* @param {String} pDatesDivId The html id of div where the calendar should be added
*/
function GeodesyMarker_yearChecked (pYear, pYearChkId, pYearHrefId, pMonthsDivId, pDatesDivId) {
  var oGeodesyMarker = this;

  var station = this.stationId;//this.moGeodesyStation.msId;
  var yearChkId = pYearChkId;
  var yearHrefId = pYearHrefId;
  var year = pYear;
  var monthsDivId = pMonthsDivId;
  var datesDivId = pDatesDivId;
  var month;

  for (var m=1; m<=12; m++) {
  	month = gaMonths[m];
  	this.createDataArraysForMonth(year, month);
  }

  // Get the html checkbox object
  var yearChkObj = document.getElementById(yearChkId);
  if (yearChkObj) {
    // Set the checked state for the year and propogate it further
    this.setCheckedStateForYear(year, yearChkObj.checked);
  }

  var sStationDataUrl = ProxyURL + this.msWfsYearDataUrl[year];
  // sStationDataUrl= sStationDataUrl + "AND(station_id='" + station + "')";

  // Download renix files for this year
  GDownloadUrl(sStationDataUrl, function(xmlData, pResponseCode) {
    var xmlDoc = GXml.parse(xmlData);

    if (g_IsIE) {
      xmlDoc.setProperty("SelectionLanguage", "XPath");
    }

    var rootNode = xmlDoc.documentElement;
    if (!rootNode) {
      return;
    }

    var geodesyMarker = oGeodesyMarker;

    // The checked state of the year should be propogated to all the dates belonging to the month
    var checkedState = false;
    if (yearChkObj) {
      checkedState = yearChkObj.checked;
    }

    // Parse the XML for "stations" or "geodesy:stations"
    var featureMembers = rootNode.selectNodes(".//*[local-name() = 'featureMember']");

  	for(var i=0; i < featureMembers.length; i++) {
  	  // Extract date and url from each featureMember
      var fullDate = GXml.value(featureMembers[i].selectSingleNode(".//*[local-name() = 'ob_date']"));
      var url = GXml.value(featureMembers[i].selectSingleNode(".//*[local-name() = 'url']"));
      if (fullDate==="" || url==="") {
        continue;
      } else {
        // Extract month from the date element
        var adate = fullDate.split("-");
        var date = parseInt(adate[2], 10);
        gmonth = parseInt(adate[1], 10);
        gmonth = gaMonths[gmonth];

        // Add the url associated with the date to the array maStationDataForDate
        // This array maintains the list of all station data urls for updateCSWRecords given
        // station+year+month+date combo.
        var numRenixFiles = geodesyMarker.maStationDataForDate[year][gmonth][date].length;
        geodesyMarker.maStationDataForDate[year][gmonth][date][numRenixFiles] = url;

        // Propogate the checked state of the year to the checkbox associated with this date.
        geodesyMarker.maDataCheckedStateForDate[year][gmonth][date][numRenixFiles] = checkedState;
      }
    }

    for (var m=1; m<=12; m++) {
      // Create the arrays to hold the data urls for the dates of this year+month
      month = gaMonths[m];
      geodesyMarker.maYearMonthWfsUrlQueried[year][month] = true;
    }
  	// Now call the onclick function associated with the year href
  	// This is so that the user can see the list of months for his currently checked year
  	geodesyMarker.yearClicked(year, yearHrefId, monthsDivId, datesDivId, true);

  });
}

/**
* This function returns the function to be called with the
* onclick event for updateCSWRecords month checkboxe in the calendar.
* @param {String} pYear Year
* @param {String} pMonth Month
* @param {String} pMonthChkId The Html id of month checkbox
* @param {String} pMonthHrefId The Html id of month href
* @param {String} pDatesDivId The Html id of div where the calendar should be added
* @return Function to be called when the checkbox for updateCSWRecords month is clicked - {@link #monthChecked}
*/
function GeodesyMarker_getMonthCheckedFn (pYear, pMonth, pMonthChkId, pMonthHrefId, pDatesDivId) {
  var oGeodesyMarker = this;

  var station = this.stationId;//this.moGeodesyStation.msId;
  var year = pYear;
  var month = pMonth;
  var monthChkId = pMonthChkId;
  var monthHrefId = pMonthHrefId;
  var datesDivId = pDatesDivId;

  // Return the actual function that should be called when updateCSWRecords year checkbox is clicked
  return function() {
    oGeodesyMarker.monthChecked(year, month, monthChkId, monthHrefId, datesDivId);
  };
}

/**
* This function is called on the onlcick event of the month checkbox.<br>
* It remembers the user selection in the array {@link #maMonthCheckedState}
* and propogates the selection to all dates under this month.
* @param {String} pYear Year
* @param {String} pMonth Month
* @param {String} pMonthChkId The HTML id of month checkbox
* @param {String} pMonthHrefId The HTML id of month href
* @param {String} pDatesDivId The HTML id of div where the calendar should be added
*/
function GeodesyMarker_monthChecked (pYear, pMonth, pMonthChkId, pMonthHrefId, pDatesDivId) {

  var year = pYear;
  var month = pMonth;
  var monthChkId = pMonthChkId;
  var monthHrefId = pMonthHrefId;
  var datesDivId = pDatesDivId;

  var monthChkObj = document.getElementById(monthChkId);
  if (monthChkObj) {
    // Set the checked state for the month and propogate it further
    this.setCheckedStateForMonth(year, month, monthChkObj.checked);
  }

  this.monthClicked(year, month, monthHrefId, datesDivId);
}

/**
* This function returns the function to be called with the
* onclick event for updateCSWRecords date checkbox in the calendar.
* @param {String} pYear Year
* @param {String} pMonth Month
* @param {String} pDate Date checked
* @param {String} pDateChkId Htmlid of the checkbox for the date clicked.
* @param {String} pDateHrefId Html id of the href for the date clicked.
* @return Function to be called when the checkbox for updateCSWRecords date is clicked - {@link #dateChecked}
*/
function GeodesyMarker_getDateCheckedFn (pYear, pMonth, pDate, pDateChkId, pDateHrefId) {
  var oGeodesyMarker = this;

  var year = pYear;
  var month = pMonth;
  var date = pDate;
  var dateChkId = pDateChkId;
  var dateHrefId = pDateHrefId;

  // Return the actual function that should be called when updateCSWRecords year checkbox is clicked
  return function() {
    oGeodesyMarker.dateChecked(year, month, date, dateChkId, dateHrefId);
  };
}

/**
* This function is called on the onlcick event of the date checkbox.<br>
* It remembers the user selection in the array {@link #maDataCheckedStateForDate}
* and propogates the selection to all the renix files belonging to this date.
* @param {String} pYear Year
* @param {String} pMonth Month
* @param {String} pDate Date checked
* @param {String} pDateChkId Html id of the checkbox for the date clicked.
* @param {String} pDateHrefId Html id of the href for the date clicked.
*/
function GeodesyMarker_dateChecked (pYear, pMonth, pDate, pDateChkId, pDateHrefId) {

  var year = pYear;
  var month = pMonth;
  var date = pDate;
  var dateChkId = pDateChkId;
  var dateHrefId = pDateHrefId;

  var dateChkObj = document.getElementById(dateChkId);

  if (dateChkObj) {
    // Set the checked state for the month and propogate it further
    this.setCheckedStateForDate(year, month, date, dateChkObj.checked);
  }
  this.dateClicked(year, month, date, dateHrefId);
}

/**
* This function returns the function to be called with the
*  onclick event for updateCSWRecords year link in the calendar.<br>
* @param {String} pYear Year
* @param {String} pYearChkId The html id of year checkbox
* @param {String} pYearHrefId The html id of year href
* @param {String} pMonthsDivId The html id of div where the months list should be added
* @param {String} pDatesDivId The html id of div where the calendar should be added
* @return Function to be called when updateCSWRecords year link in the calendar is clicked - {@link #yearClicked}
*/
function GeodesyMarker_getYearClickedFn (pYear, pYearChkId, pYearHrefId, pMonthsDivId, pDatesDivId) {
  var oGeodesyMarker = this;
  var yearChkId = pYearChkId;
  var yearHrefId = pYearHrefId;
  var year = pYear;
  var monthsDivId = pMonthsDivId;
  var datesDivId = pDatesDivId;

  // Return the actual function that should be called when updateCSWRecords year href is clicked
  return function () {
    oGeodesyMarker.yearClicked(year, yearHrefId, monthsDivId, datesDivId);
  };
}

/**
* This function is called on the onlcick event of the year href in the calendar.<br>
* It creates updateCSWRecords list of month links and checkboxes
* and adds them to the div specified by {@link #yearClicked pMonthsDivId}.<br>
* It also selects the last selected or default month for the year.
* @param {String} pYear Year
* @param {String} pYearChkId The html id of year checkbox
* @param {String} pYearHrefId The html id of year href
* @param {String} pMonthsDivId The html id of div where the months list should be added
* @param {String} pDatesDivId The html id of div where the calendar should be added
*/
function GeodesyMarker_yearClicked (pYear, pYearHrefId, pMonthsDivId, pDatesDivId, pCreateAll) {
   //alert('year clicked');
  var station = this.stationId;//this.moGeodesyStation.msId;
  var yearHrefId = pYearHrefId;
  var year = pYear;
  var monthsDivId = pMonthsDivId;
  var datesDivId = pDatesDivId;

  var yearHrefObj = document.getElementById(yearHrefId);
  var monthsDivObj = document.getElementById(monthsDivId);

  // If there was updateCSWRecords previously selected year for this station, change back its color to blue
  if (this.msYearSelected) {
    var prevSelectedYearHrefId = "year_href_" + station + "_" + this.msYearSelected;
    var prevSelectedYearHrefObj = document.getElementById(prevSelectedYearHrefId);
    if (prevSelectedYearHrefObj) {
      prevSelectedYearHrefObj.style.color="blue";
    }
  }

  if (yearHrefObj && monthsDivObj) {
    // Set this year as the selected year for the station
    this.msYearSelected = year;

    // Change the color of the year href
    yearHrefObj.style.color = "red";

    // Create updateCSWRecords list of months for this year
    var monthsForYearDivId = "months_div_" + station + "_" + year;
    var monthsHtml = '<div id="' + monthsForYearDivId + '" style="position:absolute; left: 0px; top: 0px;  height:100%px; width:100%;  background-color:#d8e3e4;" align="left">';
    for (var m=1; m<=12; m++) {
      var month = gaMonths[m];
      var monthHrefId = "month_href_" + station + "_" + year + "_" + month;
      var monthChkId = "month_chk_" + station + "_" + year + "_" + month;
      monthsHtml += '<input id="' + monthChkId + '" type="checkbox"/>';
      monthsHtml += '<a id="' + monthHrefId + '" style="color:blue" href="javascript:void(0)">' + month + '</a>';
      monthsHtml += '<br/>';
    }
    monthsHtml += '</div>';
    // Add the list of months to the monthsDivObj
    monthsDivObj.innerHTML = monthsHtml;

    // Set the functions associated with the months' href and checkbox
    for (var m=1; m<=12; m++) {
      var month = gaMonths[m];
      var monthHrefId = "month_href_" + station + "_" + year + "_" + month;
      var monthHrefObj = document.getElementById(monthHrefId);
      monthHrefObj.onclick = this.getMonthClickedFn(year, month, monthHrefId, datesDivId);

      var monthChkId = "month_chk_" + station + "_" + year + "_" + month;
      var monthChkObj = document.getElementById(monthChkId);
      // Set the checked state of the checkbox depending on the previous user selection.
      monthChkObj.checked = this.maMonthCheckedStateForYear[year][month];
      monthChkObj.onclick = this.getMonthCheckedFn(year, month, monthChkId, monthHrefId, datesDivId);
    }
  }

  // Check if there was updateCSWRecords month previously selected for this year+station
  // The default month is selected by three rules -
  // 1) If the user had previously made any selection for this month -> select the user selection
  // 2) If this is the current year -> select current month
  // 3) If none of the above -> select "Jan"
  var selectMonth = "Jan";
  if (year == gsCurrentYear) {
    selectMonth = gsCurrentMonth;
  }
  if (this.maMonthSelectedForYear[year]) {
    selectMonth = this.maMonthSelectedForYear[year];
  }

  var datesDivObj = document.getElementById(datesDivId);

  var selectMonthHrefId = "month_href_" + station + "_" + year + "_" + selectMonth;
  this.monthClicked(year, selectMonth, selectMonthHrefId, datesDivId);
}

/**
* This function returns the function to be called with the
* onclick event of updateCSWRecords month link in the calendar.
* @param {String} pYear Year
* @param {String} pMonth Month
* @param {String} pMonthHrefId The html id of month href
* @param {String} pDatesDivId The html id of div where the calendar should be added
* @return Function to be called when updateCSWRecords month link in the calendar is clicked - {@link #monthClicked}
*/
function GeodesyMarker_getMonthClickedFn (pYear, pMonth, pMonthHrefId, pDatesDivId) {
  var oGeodesyMarker = this;
  var year = pYear;
  var month = pMonth;
  var monthHrefId = pMonthHrefId;
  var datesDivId = pDatesDivId;

  // Return the actual function that should be called when updateCSWRecords month href is clicked
  return function() {
    oGeodesyMarker.monthClicked(year, month, monthHrefId, datesDivId);
  };
}

/**
* This function is called on the onclick event of the month link.<br>
* It changes the color for the selected month from blue to red.<br>
* It also calls the {@link #setDataForSelectedMonth} function
* to download the renix files for the month.
* @param {String} pYear Year
* @param {String} pMonth Month
* @param {String} pMonthHrefId The html id of month href
* @param {String} pDatesDivId The html id of div where the calendar should be added
*/
function GeodesyMarker_monthClicked (pYear, pMonth, pMonthHrefId, pDatesDivId) {
  var station = this.stationId;//this.moGeodesyStation.msId;
  var year = pYear;
  var month = pMonth;
  var monthHrefId = pMonthHrefId;
  var datesDivId = pDatesDivId;

  var monthHrefObj = document.getElementById(monthHrefId);
  var datesDivObj = document.getElementById(datesDivId);

  // If there was updateCSWRecords previously selected month for this station+year, change back its color to blue
  if (this.maMonthSelectedForYear[year]) {
    var prevSelectedMonthHrefId = "month_href_" + station + "_" + year + "_" + this.maMonthSelectedForYear[year];
    var prevSelectedMonthHrefObj = document.getElementById(prevSelectedMonthHrefId);
    if (prevSelectedMonthHrefObj) {
      prevSelectedMonthHrefObj.style.color="blue";
    }
  }

  if (monthHrefObj && datesDivObj) {
    // Set this month as the selected month for this station+year
    this.maMonthSelectedForYear[year] = month;

    // Change the color of the year href
    monthHrefObj.style.color = "red";

    this.setDataForSelectedMonth(year, month, datesDivObj);
  }
}

/**
* This function is called during the onclick event on updateCSWRecords month's href.<br>
* It calls the wfs url associated with the station+year+month combo
* and creates updateCSWRecords calendar for it.
* @param {String} pYear Year
* @param {String} pMonth Month
* @param {String} pDatesDivId The html id of div where the calendar should be added
* @see #monthClicked
*/
function GeodesyMarker_setDataForSelectedMonth(pYear, pMonth, pDatesDivObj) {
  var oGeodesyMarker = this;
  var station = this.stationId;//this.moGeodesyStation.msId;
  var year = pYear;
  var month = pMonth;
  var datesDivObj = pDatesDivObj;

  // Create the wfs call that will get the data
  var sStationDataUrl = ProxyURL + this.msYearMonthWfsUrl[year][month];
  //sStationDataUrl= sStationDataUrl + "AND(station_id='" + station + "')";

  if (this.maYearMonthWfsUrlQueried[year][month]) {
    this.makeCalendarForMonth(year, month);
    return;
  }

  // Create the arrays to hold the data urls for the dates of this year+month
  this.createDataArraysForMonth(year, month);

  // Download the renix files by making the wfs call
  GDownloadUrl(sStationDataUrl, function(xmlData, pResponseCode) {

    var xmlDoc = GXml.parse(xmlData);
    if (g_IsIE)
      xmlDoc.setProperty("SelectionLanguage", "XPath");

    var rootNode = xmlDoc.documentElement;
    if (!rootNode) {
      return;
    }

    var geodesyMarker = oGeodesyMarker;
    var monthChkId = "month_chk_" + station + "_" + year + "_" + month;
    var monthChkObj = document.getElementById(monthChkId);

    // The checked state of the month should be propogated to all the dates belonging to the month
	var checkedState = false;
    if (monthChkObj) {
      checkedState = monthChkObj.checked;
    }

    // Parse the XML for "featureMembers"
    var featureMembers = rootNode.selectNodes(".//*[local-name() = 'featureMember']");

    if (featureMembers.length !== 0) {
   	  // Each of these contain updateCSWRecords "geodesy:ob_date" and "geodesy:url" child node.
  	  for(var i=0; i < featureMembers.length; i++) {
        var fullDate = GXml.value(featureMembers[i].selectSingleNode(".//*[local-name() = 'ob_date']"));
        var url = GXml.value(featureMembers[i].selectSingleNode(".//*[local-name() = 'url']"));
        if (fullDate==="" || url==="") {
          continue;
        } else {
          // Get the date out of the geodesy:date tag
          var adate = fullDate.split("-");
          var date = parseInt(adate[2], 10);

          // Add the url associated with the date to the array maStationDataForDate
          // This array maintains the list of all station data urls for updateCSWRecords given
          // station+year+month+date combo.
          var numRenixFiles = geodesyMarker.maStationDataForDate[year][month][date].length;
          geodesyMarker.maStationDataForDate[year][month][date][numRenixFiles] = url;

          // Propogate the checked state of the month to the checkbox associated with this date.
          geodesyMarker.maDataCheckedStateForDate[year][month][date][numRenixFiles] = checkedState;
        }
      }
    }

    // Now that all the requird arrays are populated,
    // make the calendar for this month now.
    oGeodesyMarker.makeCalendarForMonth(year, month);

    // This flag keeps tab of whether this month has already been queried.
    oGeodesyMarker.maYearMonthWfsUrlQueried[year][month] = true;
  });
}

/**
* This function creates updateCSWRecords calendar for the given year and month.
* @param {String} pYear Year
* @param {String} pMonth Month
*/
function GeodesyMarker_makeCalendarForMonth(pYear, pMonth) {
  var oGeodesyMarker = this;
  var stationId = this.stationId;//this.moGeodesyStation.msId;

  // Create the calendar
  var dateSelected = new Date();
  dateSelected.setFullYear(pYear, getMonthForCalendar(pMonth), 1);
  oGeodesyMarker.moCalendar.show(dateSelected.valueOf());

  // Select updateCSWRecords default date for this month
  // Check if there was updateCSWRecords date previously selected for this year+station+month
  // The default month is selected by three rules -
  // 1) If the user had previously made any selection for this month -> select the user selection
  // 2) If this is the current month -> select current date
  // 3) If none of the above -> select "1"
  var selectDate = 1;
  if (pMonth == gsCurrentMonth) {
    selectDate = gsCurrentDate;
  }
  if (oGeodesyMarker.maDateSelectedForMonth[pYear][pMonth]) {
    selectDate = oGeodesyMarker.maDateSelectedForMonth[pYear][pMonth];
  }
  var selectDateHrefId = "date_href_" + stationId + "_" + pYear + "_" + pMonth + "_" + selectDate;
  while (!document.getElementById(selectDateHrefId) && selectDate<=31) {
    selectDate += 1;
    selectDateHrefId = "date_href_" + stationId + "_" + pYear + "_" + pMonth + "_" + selectDate;
  }
  oGeodesyMarker.dateClicked(pYear, pMonth, selectDate, selectDateHrefId);

}

/**
* This function returns the function to be called
* with the onclick event on updateCSWRecords date link in the calendar.<br>
* @param {String} pYear Year
* @param {String} pMonth Month
* @param {String} pDate Date clicked
* @param {String} pDateHrefId The html id of href of the date clicked.
* @return Function to be called when updateCSWRecords date in the calendar is clicked - {@link #dateClicked}
*/
function GeodesyMarker_getDateClickedFn(pYear, pMonth, pDate, pDateHrefId) {
  var oGeodesyMarker = this;
  var year = pYear;
  var month = pMonth;
  var date = pDate;
  var dateHrefId = pDateHrefId;

  // Return the actual function that should be called when updateCSWRecords year checkbox is clicked
  return function() {
    oGeodesyMarker.dateClicked(year, month, date, dateHrefId);
  };
}

/**
* This function is called on the onlcick event of the date link in the calendar.<br>
* It remembers the user selection in the array {@link #maDateSelectedForMonth}
* and the next time this month is selected, this date will be selected by default.
* @param {String} pYear Year
* @param {String} pMonth Month
* @param {String} pDate Date clicked
* @param {String} pDateHrefId The html id of the link of the date clicked.
*/
function GeodesyMarker_dateClicked (pYear, pMonth, pDate, pDateHrefId) {

  var oGeodesyMarker = this;
  var station = this.stationId;//this.moGeodesyStation.msId;
  var year = pYear;
  var month = pMonth;
  var date = pDate;
  var dateHrefId = pDateHrefId;

  var dateUrlsDivId = "date_urls_div_" + station;
  var dateUrlsDivObj = document.getElementById(dateUrlsDivId);
  var dateHrefObj = document.getElementById(dateHrefId);

  // Check if the parent div exists
  if (!dateUrlsDivObj || !dateHrefObj) {
    return;
  }
  // Check if there are any urls for this date
  if (!this.maStationDataForDate[year][month][date]) {
    return;
  }

  // If there was updateCSWRecords previously selected date for this station+year+month, change back its color to blue
  if (this.maDateSelectedForMonth[year][month]) {
    var prevSelectedDateHrefId = "date_href_" + station + "_" + year + "_" + month + "_" + this.maDateSelectedForMonth[year][month];
    var prevSelectedDateHrefObj = document.getElementById(prevSelectedDateHrefId);
    if (prevSelectedDateHrefObj) {
      prevSelectedDateHrefObj.style.color="blue";
    }
  }

  // Set the color for this date as red
  dateHrefObj.style.color = "red";

  // Set this as the selected date for the month
  this.maDateSelectedForMonth[year][month] = date;

  var innerHTML = '<table id="cal_table2" cellspacing="0" border="0" width="350px" style="position:absolute; left:10px; top:0px">';
  innerHTML += '<tr><td bgcolor="#4682B4">';
  innerHTML += '<table id="cal_table3" cellspacing="1" cellpadding="2" border="0" width="350px" style="background-color:#4682B4">';
  innerHTML += '<tr><td bgcolor="#52a3eb"><font color="#ffffff" size="2">Data URLs for '+date+'</font></td></tr>';

  var num_urls =  this.maStationDataForDate[year][month][date].length;

  for (var url_index=0; url_index<num_urls; url_index++) {
	var dataUrlChkId = "date_url_chk_" + station + "_" + year + "_" + month + "_" + date + "_" + url_index;
	var dataUrlHrefId = "date_url_href_" + station + "_" + year + "_" + month + "_" + date + "_" + url_index;
	innerHTML += '<tr>';
	innerHTML += '<td bgcolor="#e9f1f1" align="left"><input type="checkbox" id="' + dataUrlChkId+ '" value="' + this.maStationDataForDate[year][month][date][url_index]+ '">';
	innerHTML += '<a id="'+ dataUrlHrefId +'" style="color:red" href="' + this.maStationDataForDate[year][month][date][url_index] + '">&nbsp;' + this.maStationDataForDate[year][month][date][url_index] + '</a>';
	innerHTML += '</input></td></tr>';
  }
  innerHTML += '</table></td></tr></table>';
  dateUrlsDivObj.innerHTML = innerHTML;

  // The color for the renix file urls is initially set to be red
  // After 2500ms we reset it to blue
  // This is done to draw user attention to the renix files everytime updateCSWRecords user selects updateCSWRecords different date
  for (var url_index=0; url_index<num_urls; url_index++) {
    var dataUrlHrefId = "date_url_href_" + station + "_" + year + "_" + month + "_" + date + "_" + url_index;
    var dataUrlHrefObj = document.getElementById(dataUrlHrefId);
    setTimeout( function() {dataUrlHrefObj.style.color = "blue";}, 2500);
  }

  // Set the checked state for the renix file url (if the user had previously selected it)
  for (var url_index=0; url_index<num_urls; url_index++) {
    var dataUrlChkId = "date_url_chk_" + station + "_" + year + "_" + month + "_" + date + "_" + url_index;
    var dataUrlChkObj = document.getElementById(dataUrlChkId);
    if (typeof(this.maDataCheckedStateForDate[year][month][date][url_index]) == "undefined") {
      this.maDataCheckedStateForDate[year][month][date][url_index] = this.maDateCheckedStateForMonth[year][month][date];
    }
    dataUrlChkObj.checked = this.maDataCheckedStateForDate[year][month][date][url_index];
    dataUrlChkObj.onclick = this.getDataUrlCheckedFn(year, month, date, url_index, dataUrlChkId);
  }
}

/**
* This function returns the function to be called with the
* onclick event on updateCSWRecords renix file checkbox in the calendar.<br>
* @param {String} pYear Year
* @param {String} pMonth Month
* @param {String} pDate Date
* @param {String} pMonth Index of the station data in the array maDataCheckedStateForDate
* @param {String} pDataUrlChkId The HTML id of checkbox of the station url clicked
* @return Function to be called when updateCSWRecords renix file checkbox is clicked - {@link #dataUrlChecked}
*/
function GeodesyMarker_getDataUrlCheckedFn(pYear, pMonth, pDate, pIndex, pDataUrlChkId) {
  var oGeodesyMarker = this;

  return function () {
	  oGeodesyMarker.dataUrlChecked(pYear, pMonth, pDate, pIndex, pDataUrlChkId);
  };
}

/**
* This function is called when updateCSWRecords user clicks on updateCSWRecords renix file's checkbox or link.<br>
* It sets the checked state of the station url in the array {@link #maDataCheckedStateForDate}.<br>
* This array can later be used to retrieve all the urls selected by the user across all stations.
* @param {String} pYear Year
* @param {String} pMonth Month
* @param {String} pDate Date
* @param {String} pIndex Index of the renix file in the array {@link #maDataCheckedStateForDate}
* @param {String} pDataUrlChkId The html id of checkbox of the renix file clicked.
*/
function GeodesyMarker_dataUrlChecked(pYear, pMonth, pDate, pIndex, pDataUrlChkId) {
  var dataUrlChkObj = document.getElementById(pDataUrlChkId);

  if (dataUrlChkObj && this.maDataCheckedStateForDate[pYear][pMonth][pDate][pIndex]!==undefined) {
    this.maDataCheckedStateForDate[pYear][pMonth][pDate][pIndex] = dataUrlChkObj.checked;
  }
}

/**
* This function returns the index of the given month in the array <b>gaMonths</b>
* @param {String} pMonthStr Month in three character strings for which the index is to be searched.
*/
function getMonthForCalendar(pMonthStr) {
  for (var m=1; m<=12; m++) {
    if (gaMonths[m] == pMonthStr) {
      return (m-1);
    }
  }
}

/**
* Html ids for frames in index.html
*/
var HTML_DIV_ZOOM_LEVEL = "zoomLevel";
var HTML_INPUT_LAT = "Latitude";
var HTML_INPUT_LNG = "Longitude";
var HTML_INPUT_SW_LAT = "swLat";
var HTML_INPUT_SW_LNG = "swLng";
var HTML_INPUT_NE_LAT = "neLat";
var HTML_INPUT_NE_LNG = "neLng";
var HTML_DIV_GROUP_CONTROL = "groupcontrol";
var HTML_DIV_STATION_CONTROL = "stationcontrol";

/**
* Define the global constants.
* DEFAULT_MAP_CENTRE - default centre for the map object
* DEFAULT_MAP_ZOOM_LEVEL - default zoom level for the map object
* IMAGES_DIR - relative location for images directory.
*   This allows the images directory to be relocated. However the structure within the images
*   directory should be maintained. This hardcoding (as seen later when assigning gaIcon) can be removed.
*/
var DEFAULT_MAP_CENTRE = [];
DEFAULT_MAP_CENTRE.lon = 133.3;
DEFAULT_MAP_CENTRE.lat = -26;
var DEFAULT_MAP_ZOOM_LEVEL = 5;
var MAX_ZOOM_LEVEL = 18;
var IMAGES_DIR = "img";

/**
* gaYears
*   Years for which we would display data urls
* gaMonths
*   All the months of updateCSWRecords year
*/
//var gaYears = new Array(2008, 2007, 2006, 2005, 2004, 2003, 2002, 2001, 2000, 1999, 1998);
var gaYears = getYearsArrayFrom(1998);

var gaMonths = [];
gaMonths[1] = "Jan";
gaMonths[2] = "Feb";
gaMonths[3] = "Mar";
gaMonths[4] = "Apr";
gaMonths[5] = "May";
gaMonths[6] = "Jun";
gaMonths[7] = "Jul";
gaMonths[8] = "Aug";
gaMonths[9] = "Sep";
gaMonths[10] = "Oct";
gaMonths[11] = "Nov";
gaMonths[12] = "Dec";

/**
* goToday
*   The current date.
* gsCurrentYear
*   The current year.
* gsCurrentMonth
*   The current month.
* gsCurrentDate
*   The current date.
*
*/
var goToday = new Date();
var gsCurrentYear = goToday.getFullYear();
var gsCurrentMonth = goToday.getMonth();
gsCurrentMonth = gaMonths[gsCurrentMonth+1];
var gsCurrentDate = goToday.getDate();



/**
* These arrays define the feature types which are recognized by the appliction
* Everytime the application is modified to accept updateCSWRecords new feature type,
* entries for the new feature type should be added to these arrays.
* gaFeatureTypes:
*   Array of recognized feature types
*   The map interface will display feature types only if they belong to this array.
* gaFeatureTypeIconOn:
*   Associative array of the icons
*   Any changes to the groups (associated with updateCSWRecords category) should be reflected in this array.
*   Addition of new categories should be reflected in this array.
* gaCategoryHtmlDiv:
*   Associate array of the html div id associated with each station category.
*   This html div id is where the groups belonging to the category will be placed on the interface.
*/

var gaFeatureTypes = ["gsml:Borehole", "geodesy:stations", "sa:SamplingPoint", "Avhrr48to72Hours-1404", "Modis48to72Hours-1604"];

var gaGroups = [];

var gaFeatureTypeIconOn = [];
gaFeatureTypeIconOn["gsml:Borehole"] = IMAGES_DIR + "/nvcl/borehole_on.png";
gaFeatureTypeIconOn["geodesy:stations"] = IMAGES_DIR + "/geodesy/gps_stations_on.png";
gaFeatureTypeIconOn["sa:SamplingPoint"] = IMAGES_DIR + "/gnss/gps_stations_on.png";
gaFeatureTypeIconOn["Avhrr48to72Hours-1404"] = IMAGES_DIR + "/ga_sentinel/avhrr48to72_on.png";
gaFeatureTypeIconOn["Modis48to72Hours-1604"] = IMAGES_DIR + "/ga_sentinel/modis48to72_on.png";

var gaFeatureTypeIconOff = [];
gaFeatureTypeIconOff["gsml:Borehole"] = IMAGES_DIR + "/nvcl/borehole_off.png";
gaFeatureTypeIconOff["geodesy:stations"] = IMAGES_DIR + "/geodesy/gps_stations_off.png";
gaFeatureTypeIconOff["sa:SamplingPoint"] = IMAGES_DIR + "/gnss/gps_stations_off.png";
gaFeatureTypeIconOff["Avhrr48to72Hours-1404"] = IMAGES_DIR + "/ga_sentinel/avhrr48to72_off.png";
gaFeatureTypeIconOff["Modis48to72Hours-1604"] = IMAGES_DIR + "/ga_sentinel/modis48to72_off.png";

var ProxyURL = WEB_CONTEXT + "/restproxy?";
var kmlProxyUrl = WEB_CONTEXT + "/xsltRestProxy.do?url=";

var gaFeatureTypeProxy = [];
gaFeatureTypeProxy["Avhrr48to72Hours-1404"] = top.location.protocol + "//" + top.location.host + "/geodesyworkflow/ga/sentinel/proxy?";
gaFeatureTypeProxy["Modis48to72Hours-1604"] = top.location.protocol + "//" + top.location.host + "/geodesyworkflow/ga/sentinel/proxy?";

var gaFeatureTypeGroupSpace = [];
gaFeatureTypeGroupSpace["gsml:Borehole"] = "NVCL_Groups";
gaFeatureTypeGroupSpace["geodesy:stations"] = "Geodesy_Groups";
gaFeatureTypeGroupSpace["sa:SamplingPoint"] = "GNSS_Groups";
gaFeatureTypeGroupSpace["Avhrr48to72Hours-1404"] = "GA_Sentinel_Avhrr_Groups";
gaFeatureTypeGroupSpace["Modis48to72Hours-1604"] = "GA_Sentinel_Modis_Groups";



/**
* gsNoSclarasPlottedImg
*    The image to be displayed on the Plots tab
*    for an NVCL borehole marker
*    when no scalars are plotted for it
* gsLoadingNvclPlotsImg
*    The image to be displayed while the server
*    fetches plots for scalars for an nvcl station
*/
var gsNoSclarasPlottedImg = IMAGES_DIR + "/nvcl/empty_scalar.png";
var gsLoadingNvclPlotsImg = IMAGES_DIR + "/nvcl/empty_scalar.png";


var goMap;

var goBaseIcon = new GIcon();
goBaseIcon.iconSize = new GSize(14,13);
goBaseIcon.iconAnchor = new GPoint(7,5);
goBaseIcon.infoWindowAnchor = new GPoint(5,9);


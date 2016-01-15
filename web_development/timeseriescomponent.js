//Time series JSON
var timeseriesJSON;
var maxValues = new Array();
var minValues = new Array();
var posTimeSeries;

//Context
var dataset;
var posInit, posEnd;
var first = true;


function getTimeSeriesJSON(url) {
	$.getJSON(url, function( data ) {
		timeseriesJSON = data;
		// Update min and max values for each time series
		updateMinMaxValues();
		initTimeSeries(0,0,false);
	});
}

function changeDataset() {
	var selectBox = document.getElementById("grain");
	dataset = selectBox.options[selectBox.selectedIndex].value;
	getTimeSeries();
}

function updateMinMaxValues() {
	for (var j = 0; j < timeseriesJSON.length; j++) {
		var min = 0, max = 0;
		for (var i = 0; i < timeseriesJSON[0].data.length; i++) {
			var value = timeseriesJSON[j].data[i].value;
			if (value > max) max = value;
			if (value < min) min = value;
		}
		minValues[j] = min;
		maxValues[j] = max;
	}
}


function showPoints(){
	if (first) first = false;
	$("#reset-button").attr('disabled', false);
	getTimeSeries();
}


function getTimeSeries() {
	dataset = getTableName();
	var dates = getLimitDates();	
	posInit = dates[0];
	posEnd = dates[1];
	
	/*alert(posInit);
	alert(posEnd);*/

	if(!isRestricted) { 
		requestTimeUrl = myip + ":" + myport + "/spatialdata?posInit=" + posInit + "&posEnd=" + posEnd + "&tableName=" +  dataset 
		+ "&isRestricted=false&timeGranularity=" + getTimeLOD() + "&aamaps=" + getAAMapsValue() +  "&gridSize=" +getGridSize() 
		+ "&attenFunction=" + getAttenFunction();
		console.log("time request url: " + requestTimeUrl);
		getGeoJSON(requestTimeUrl);
	}
	else {
		requestTimeUrl = myip + ":" + myport + "/spatialdata?posInit=" + posInit + "&posEnd="+ posEnd + "&tableName=" +  dataset 
		+ "&isRestricted=false&timeGranularity=" + getTimeLOD() + "&aamaps=" + getAAMapsValue() +  "&gridSize=" +getGridSize() 
		+ "&attenFunction=" + getAttenFunction();
		console.log("time request url: " + requestTimeUrl);
		getGeoJSON(requestTimeUrl);
	}
}



function getTimeRange(){
	requestTimeUrl = myip + ":" + myport + "/timedata?timeGranularity="+ getTimeLOD();
	console.log("time request url: " + requestTimeUrl);
	getTimeRangeJSON(requestTimeUrl);
}


function getTimeRangeJSON(url) {
	$.getJSON(url, function(rangeData) {
		console.log(rangeData);
		setSliderDates(rangeData.range[0], rangeData.range[1]);
	}).success(function() {
		var timeLOD = getTimeLOD();
		if (timeLOD=="day") {
			createDayTimeSlider();
			if (!first) getTimeSeries();
		}
		else if (timeLOD=="week") createWeekTimeSlider();
		else if (timeLOD=="month") {
			createMonthTimeSlider();
			getTimeSeries();
		}
		else createYearTimeSlider();
		
	}).error(function(jqXHR, textStatus, errorThrown) {
		console.log("error " + textStatus);
	})
}



function setNewContext(){
	dataset = getTableName();
	requestTimeUrl = myip + ":" + myport + "/context?tableName=" +  dataset 
	+ "&isRestricted=false&timeGranularity=" + getTimeLOD()+ "&aamaps=" + getAAMapsValue() +  "&gridSize=" +getGridSize() 
	+ "&attenFunction=" + getAttenFunction();
	console.log("time request url: " + requestTimeUrl);
}



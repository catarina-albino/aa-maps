var maxValues = new Array();
var minValues = new Array();
var posTimeSeries;

//Context
var dataset;
var posInit, posEnd;
var first = true;
var accumF, attenF;


function getTimeSeriesJSON(url) {
	$.getJSON(url, function( data ) {
		timeseriesJSON = data;
		// Update min and max values for each time series
		updateMinMaxValues();
		initTimeSeries(0,0,false);
	});
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
	if (first || contextChanged) {
		first = false;
		pn = false;
		getTimeSeries();
	}
	else drawCanvas(data);
	$("#pn-button").removeClass("active");
	$("#show-button").addClass("active");
	$("#reset-button").attr('disabled', false);
}


function getTimeSeries() {
	dataset = getTableName();
	var dates = getLimitDates();	
	posInit = dates[0];
	posEnd = dates[1];

	requestTimeUrl = myip + ":" + myport + "/spatialdata?posInit=" + posInit + "&posEnd=" + posEnd + "&tableName=" +  dataset 
	+ "&isRestricted=false&timeGranularity=" + getTimeLOD() + "&aamaps=" + getAAMapsValue() +  "&gridSize=" +getGridSize() 
	+ "&attenFunction=" + getAttenFunction()+"&accumFunction=" + getAccumFunction();
	console.log("time request url: " + requestTimeUrl);
	getGeoJSON(requestTimeUrl);
}



function getSGrains(){
	requestTimeUrl = myip + ":" + myport + "/spatialgrains";
	console.log("sgrains request url: " + requestTimeUrl);
	getSGrainsJSON(requestTimeUrl);
}


function getSGrainsJSON(url) {
	$.getJSON(url, function(data) {
		$.each(data.grains, function (i, item) {
		    $('#grid_size').append($('<option>', { 
		        value: "_"+item,
		        text : item 
		    }));
		});
	$("#grid_size option:first").prop('selected','selected');
	gridSize = $("#grid_size").val();
	}).error(function(jqXHR, textStatus, errorThrown) {
		console.log("error " + textStatus);
	});
}


function getTimeRange(){
	requestTimeUrl = myip + ":" + myport + "/timedata?timeGranularity="+ getTimeLOD();
	console.log("time request url: " + requestTimeUrl);
	getTimeRangeJSON(requestTimeUrl);
}


function getTimeRangeJSON(url) {
	$.getJSON(url, function(rangeData) {
		setSliderDates(rangeData.range[0], rangeData.range[1]);
	}).success(function() {
		var timeLOD = getTimeLOD();
		if (timeLOD=="day") {
			createDayTimeSlider();
			if (!first && !pn) getTimeSeries();
		}
		else if (timeLOD=="week") createWeekTimeSlider();
		else if (timeLOD=="month") {
			createMonthTimeSlider();
			if (!pn) getTimeSeries();
		}
		else createYearTimeSlider();

	}).error(function(jqXHR, textStatus, errorThrown) {
		console.log("error " + textStatus);
	})
}



function getAccumFunctions(){
	requestTimeUrl = myip + ":" + myport + "/AAFunctions";
	console.log("AA Functions request url: " + requestTimeUrl);
	getAccumFunctionsJSON(requestTimeUrl);
}


function getAccumFunctionsJSON(url) {
	$.getJSON(url, function(functions) {
		accumF = functions.accumFunctions;
		attenF = functions.attenFunctions;
		setAAFunctions();
	}).error(function(jqXHR, textStatus, errorThrown) {
		console.log("error " + textStatus);
	})
}

function setAAFunctions(){
	$.each(accumF, function() {
		addAccumItem(this.name, this.id,this.descp);
	});
	$.each(attenF, function() {
		addAttenItem(this.name, this.id,this.descp);
	});
	//$("#attenSlider li:nth-child(2)").addClass(ative);
	
	bindChangeFunction();
}


function setNewContext(){
	dataset = getTableName();
	requestTimeUrl = myip + ":" + myport + "/context?tableName=" +  dataset 
	+ "&isRestricted=false&timeGranularity=" + getTimeLOD()+ "&aamaps=" + getAAMapsValue() +  "&gridSize=" +getGridSize() 
	+ "&attenFunction=" + getAttenFunction();
	console.log("time request url: " + requestTimeUrl);
}


function getaccumFObj(){
	return accumF;
}

function abortServerReq(){
	if (request !== null) request.abort();
	request = null;
	hideLoadingDialog(spinner);
}

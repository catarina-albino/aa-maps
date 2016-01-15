//Global Variables
var log;
var aamaps = true, fixed = true;
var data; //data is an array of points (longitude, latitude)
var gis;
var map = null, points = null, igs = null;
var calcEffects = new Array();
var defaultZoom = 7, maxColor = 255;
var shape = "circle", geom = "point";
var pointSize = 4;
var shapes = ['circle', 'square'];
var opacityFunctions = ['Fixed', 'X/XMax', 'Range', 'Z-Score', '75% Percentil'];
var attenuationFunctions = ['Linear','None','Exponential','Hal-Life'];
var accumulationFunctions = ['Sum', 'Max', 'Min', 'Average'];
var accidentsPT = {name:'Accidents (PT)', table:'accidents_portugal'};
var datasets = [accidentsPT];
var datasetDefault = "Indíce Gravidade (IG)";
var tableDefault = "accidents_portugal";
var table;
var timeLODs = ['days', 'weeks', 'months', 'years'];
var spaceLODs = ['grid','polygon'];

var attenFunction = 0;

//Connection variables to the server
var myip = "http://localhost";
var myport = "8080";

//Visual variables
var color;
var pointOptions;
var defaultColor = "ffff00";
var opacity = 0.5;
var numberFeatures = 0;

//Restricted Area
var isRestricted = false;
var geometryRestriction;
var timeLOD, spaceLOD, gridSize, polygonSize;
var dateSlider = document.getElementById('time_slider');
var gui, params, colorFolder, log;
var fromDate, endDate;



/********************************************************************/
/****************** Map/Drawing Points Functions ********************/
/********************************************************************/

function autoOpacity(){
	fixed = true;
	addMessage(">> Appling fixed opacity...");
	if (points==null) getTimeSeries();
	redrawPoints();
}

function xMaxOpacity(){
	fixed = false;
	addMessage(">> Appling x / xMax opacity...");
	if (points == null) getTimeSeries();
	else {
		calcEffects = xMaxNorm();
		redrawPoints();
	}
}


function rangeOpacity(){
	fixed = false;
	addMessage(">> Appling IG Range opacity...");
	if (points == null) getTimeSeries();
	else {
		calcEffects = rangeNorm();
		redrawPoints();
	}
}

function zscoreOpacity(){
	fixed = false;
	addMessage(">> Appling IG Z-score opacity...");
	if (points == null) getTimeSeries();
	else {
		calcEffects = zScoreNorm();
		redrawPoints();
	}
}

function percentilOpacity(){
	fixed = false;
	addMessage(">> Appling IG 75Percentil opacity...");
	if (points == null) getTimeSeries();
	else {
		calcEffects = percentilNorm();
		redrawPoints();
	}
}


function AAMapsOpacity(){
	fixed = false;
	calcEffects = AAMapsRangeNorm(points.maxEffect, points.minEffect);
	redrawPoints();
}


function gisDraw(){
	gis.draw();
}

function clearPoints(){
	$("#reset-button").attr('disabled', true);
	resetFields();
	gis.clear();
	map.off('move', gisDraw);
	map.setZoom(defaultZoom);
	map.panTo(new L.LatLng(39.5, -8));
}


function initGIS() {
	pointOptions = { 
			geometry: geom,
			shape: shape,
			size: pointSize,
			fixed: fixed,
			color: [color[0], color[1], color[2], opacity]
	};
	gis = new gisplay("map", map, pointOptions); 
}


function init_map() {
	if (map!= undefined) {
		map.remove();
		$("#map").empty();
		map = null;
	} 
	map = new L.Map('map', {center: new L.LatLng(39.5, -8), 
		zoom: defaultZoom,
		contextmenu: true,
		contextmenuWidth: 140,
		attributionControl: false}
	); 
	
	L.esri.basemapLayer('DarkGray',{logo:false}).addTo(map);

	var drawnItems = new L.FeatureGroup();
	map.addLayer(drawnItems);
	drawControl = new L.Control.Draw({
		draw : {
			position : 'topleft',
			polygon : false,
			polyline : true,
			rectangle : true,
			circle : true,
			marker : false
		},
		edit : false
	});

	var searchControl = L.esri.Geocoding.Controls.geosearch({
		useMapBounds: false
	}).addTo(map);

	// Share the same tile url...
	var tileUrl = 'http://services.arcgisonline.com/arcgis/rest/services/Canvas/World_Dark_Gray_Base/MapServer/tile/{z}/{y}/{x}';
	var magnifiedTiles = L.tileLayer(tileUrl);
	
	var magnifyingGlass = L.magnifyingGlass({
		zoomOffset: 4,
		layers: [magnifiedTiles]
	});

	L.control.magnifyingglass(magnifyingGlass, { 
		    forceSeparateButton: true
	}).addTo(map);

	var results = L.layerGroup().addTo(map);
	searchControl.on('results', function(data){});
	color = hexToRgb('#'+defaultColor);
	
	initGIS();
}



function drawCanvas(geojson) {
	points = geojson;
	numberFeatures = points.features.length;
	igs = new Array();
	$.each(points.features, function(i,feature){
		igs.push(feature.effect);
	});
	initCollection(igs);
	if (aamaps) AAMapsOpacity();
	else redrawPoints();
}



function getGeoJSON(url) {
	if(map!=null) {
		var start_time = performance.now();
		if (aamaps) addMessage("************ AAMaps Mode ************", undefined);
		else addMessage("********** Visual Accum. Mode **********");
		addMessage(">> Loading Points...", undefined);
		var spinner = new Spinner(opts).spin();
		showLoadingDialog(spinner);
		$.getJSON(url, function(data) {
			var getRequest_time = new Date();
			drawCanvas(data);
			map.on('move', gisDraw);
			var finishRequest_time = performance.now();
			hideLoadingDialog(spinner);
			addMessage("	" + numberFeatures + " Points Loaded", Math.round(finishRequest_time - start_time)/1000);
			addMessage("\tMaxIG = " + getMax() + "\n	MinIG = " + getMin() + 
					"\n\tMeanIG = " + Math.round(getMean()*100) / 100 );
		});
	}
}




function initDataBuffer() {
	data = new Array();
	if (fixed){
		for (var i = 0; i < points.features.length; i++) {
			var loc = points.features[i].geometry.coordinates;
			data.push(loc[1],loc[0]);
		}
	}
	else{
		for (var i = 0; i < points.features.length; i++) {
			var loc = points.features[i].geometry.coordinates;
			var palete = colorF(calcEffects[i]);
			var color = palete[0];
			var opacity = palete[1];
			data.push(loc[1],loc[0],color[0],color[1],color[2],opacity);
		}
	}
}


function redrawPoints(){
	if (aamaps) initColorParams(calcEffects);
	initGIS();
	gis.clear();
	initDataBuffer();
	gis.points(data); 	
	gis.draw();
}

function hexToRgb(hex) {
	r = parseInt(hex.substring(1,3), 16) / maxColor;
	g = parseInt(hex.substring(3,5), 16) / maxColor;
	b = parseInt(hex.substring(5,7), 16) / maxColor;
	return [r,g,b];
};



/*******************************************************************
/************************* GUI Functions ***************************
 *******************************************************************/

/* Load the map and init UI fields*/
function init() {
	drawPlot();
	bindKeyEvents();
	bindTimeEvents();
	createOptionsPanel();
	changeSLODoptions();
	$("li.title").bind("click", function (event) {
		$(this).siblings("li").toggle();
	});
	$("#reset-button").attr('disabled', true);
	toggleHeaderBackground();
	getLimitDates();
	getTimeRange();
	resetFields();
	init_map();
	zoomMap = map.getZoom();
};



function welcomeDialog() {
	$("#dialog-message").dialog({
		draggable: false,
		modal: true,
		position: { my: 'top', at: 'top+200' },
		modal: true,
		resizable: false,
		closeOnEscape: false,
		minHeight: 150,
		minWidth:500
	});
}


function aboutMessage() {
	$("#dialog-about").dialog({
		draggable: false,
		modal: true,
		position: { my: 'top', at: 'top+150' },
		modal: true,
		resizable: false,
		closeOnEscape: false,
		minWidth:600,
		close: function( event, ui ) {
			$("#about-button").removeClass("active");
		},
		open: function( event, ui ) {
			$("#about-button").addClass("active");
		}
	});
}

function toggleAAMaps() {
	$('#button').on('click', function(){
		$(this).toggleClass('on');
	});
}	

var guiParams = function() {
	this.EffectMetric = datasets[0].name;
	this.TimeGranularity = timeLODs[0];
	this.SpaceGranularity = spaceLODs[0];
	this.Shape = shapes[0];
	this.Size = pointSize;
	this.Color = "#"+defaultColor;
	this.A = a;
	this.NClasses = classes;
	this.ColorScheme = schemaNames[0];
	this.OpacityFunctions = opFunctionNames[0];
	this.Opacity = opacity;
	this.Function = opacityFunctions[0];
	this.AttenFunctions = attenuationFunctions[0];
	this.AccumFunctions = accumulationFunctions[0];
};





function show_div(Fdiv) {
	$(Fdiv).show();
}

function updatePointSize(value){
	pointSize = value;
	redrawPoints();
}

function updateOpacity(value){
	opacity = value;
	redrawPoints();
}

function updateShape(value){
	shape=value.toLowerCase();
	redrawPoints();
}


function createOptionsPanel() {
	params = new guiParams();
	gui = new dat.GUI({resizable : false,
						width : 290,
						height : 850});
	addDatasetFolder();
	addPointFolder();
	addBaseColorFolder();
	if (aamaps) attachAAMapsFields(4);
	else addColorFolder();
	addLogFolder();
	attachCustomFields();
	$( "div.dg.ac > div >" ).remove( ".close-button" );
};


function addLogFolder(){
	var f5 = gui.addFolder('Log');
	f5.open();
}



function addDatasetFolder() {
	var f1 = gui.addFolder('Dataset');
	f1.add(params, 'EffectMetric', [datasetDefault]);
	f1.add(params, 'TimeGranularity',[]);
	f1.add(params, 'SpaceGranularity',[]);
	$('.dg.ac ul > li:nth-child(1) li:nth-child(3) select').remove();
	$('.dg.ac ul > li:nth-child(1) li:nth-child(4) select').remove();
	f1.open();
}


function addPointFolder() {
	var f2 = gui.addFolder('Geometry Options');
	f2.add(params, 'Shape', shapes).onFinishChange(function(value) {
		updateShape(value);
	});
	f2.add(params, 'Size', 1, 5).step(1).onFinishChange(function(value) {
		updatePointSize(value);
	});
	f2.open();
}


function addBaseColorFolder() {
	colorFolder = gui.addFolder('Color Options');
	colorFolder.add(params, 'NClasses', 1, 7).step(1).onFinishChange(function(value) {
		classes = value;
		redrawPoints();
	});
	var opacityFunctions = colorFolder.add(params,'OpacityFunctions',opFunctionNames).onChange(function(value) {
		opacityFunction(value.toLowerCase());
		redrawPoints();
	});
	colorFolder.add(params, 'A', 0.1, 1).step(0.1).onFinishChange(function(value) {
		a = value;
		redrawPlot();
		redrawPoints();
	});
	
	$('body > div.dg.ac > div > ul > li:nth-child(3)').attr('id', 'color_folder');
	$('body > div.dg.ac > div > ul > li:nth-child(3)').append(canvas);
	colorFolder.open();
}


function addColorFolder() {
	colorFolder = gui.addFolder('Color Options');
	var colorPicker = colorFolder.addColor(params, 'Color').onChange(function(value) {
	color = hexToRgb(value);
	redrawPoints();
	});
	colorFolder.add(params, 'Opacity', 0, 1).step(0.1).onFinishChange(function(value) {
		updateOpacity(value);
	});
	var colorFunction = colorFolder.add(params,'Function', opacityFunctions);
	colorFunction.name('Opacity Function');
	colorFunction.onFinishChange(function(value) {
		changeEffect(value);
	});
	$('body > div.dg.ac > div > ul > li:nth-child(3) .slider').attr('id', 'color_slider');
	colorFolder.open();
}


function addAttenuationFolder(pos) {
	var atten = gui.addFolder('Attenuation Functions');
	var attFunctions = atten.add(params,'AttenFunctions', attenuationFunctions);
	attFunctions.name('Attenuation Function');
	attFunctions.onFinishChange(function(value) {
		attenFunction = document.getElementById("attenuation").selectedIndex;
		getTimeSeries();
	});
	$('body > div.dg.ac > div > ul > li:nth-child('+pos+') > div > ul > li.cr.string > div > div > select').attr('id', 'attenuation');
	$('#attenuation').find("option:gt(1)").attr('disabled', 'disabled');
	atten.open();
}


function addAccumulationFolder(pos) {
	var accum = gui.addFolder('Accumulation Functions');
	var accFunctions = accum.add(params,'AccumFunctions', accumulationFunctions);
	accFunctions.name('Accum. Function');
	$('body > div.dg.ac > div > ul > li:nth-child('+pos+')  > div > ul > li.cr.string > div > div > select').attr('id', 'accumulation');
	$('#accumulation').find("option:gt(0)").attr('disabled', 'disabled');
	accum.open();
}


function changeEffect(value) {
	if (value==opacityFunctions[0]) {
		$('.dg.ac ul > li:nth-child(3) .slider').show();
		autoOpacity();
	}
	else {
		$('.dg.ac > div > ul > li:nth-child(3) .slider').hide();
		if (value==opacityFunctions[1]) xMaxOpacity();
		else if (value==opacityFunctions[2]) rangeOpacity();
		else if (value==opacityFunctions[3]) zscoreOpacity();
		else if (value==opacityFunctions[4]) percentilOpacity();
	}
}


function attachCustomFields(){
	var SLOD_div = $('.dg div:nth-child(1) li:nth-child(4) > div > div')[0]; 
	SLOD_div.appendChild($('#TLOD')[0]);
	SLOD_div.appendChild($('#SLOD')[0]);
	SLOD_div.appendChild($('#grid_size')[0]);
	$('.dg div:nth-child(1) li:nth-child(3) > div > div')[0].appendChild($('#TLOD')[0]); //Add TLOD select
	$("#SLOD option:first").siblings().attr("disabled","disabled");
	createLogSection();
}


function createLogSection(){
	var logSection = '<li id="log-section"><div id="logArea">' +
	'<textarea id="log" rows="6" cols="35" readonly>'+
	'<input type="button" value="Reset" onclick="clearLog()">'+
	'</textarea></div></li>';
	$('.dg.ac li:last-child > div > ul').append(logSection); 
}


$('#log').on('shown', function (e, editable) {
    if (arguments.length != 2) return
    if (!editable.input.$input.closest('.control-group').find('.editable-input >textarea').length > 0 || !editable.options.clear || editable.input.$input.closest('.control-group:has(".btn-clear")').length > 0) return
    editable.input.$input.closest('.control-group').find('.editable-buttons').append('<br><button class="btn btn-clear"><i class="icon-trash"></i></button>');
});

$('body').on('click', '.editable-buttons > .btn-clear', function (e) {
    $(this).closest('.control-group').find('.editable-input >textarea').val('');
    return false
});



function attachAAMapsFields(pos){
	addAttenuationFolder(pos);
	addAccumulationFolder(pos+1);
}


function resetFields(){
	clearLog();
	fixed=false;
	points = null;
	$("#color_function").val(opacityFunctions[0]);
}


function toggleVizMode(){
	$('#aamaps-button').toggleClass('on');
	//toggleCanvas();
	if (aamaps) {
		aamaps = false;
		fixed = true;
		gui.removeFolder('Color Options');
		gui.removeFolder('Attenuation Functions');
		gui.removeFolder('Accumulation Functions');
		addColorFolder();
	}
	else {
		aamaps = true;
		fixed = false;
		gui.removeFolder('Color Options');
		addBaseColorFolder();
		attachAAMapsFields(5);
		$('body > div.dg.ac > div > ul > li:nth-child(4)').append(canvas);
	}
	retachLog(gui);
	gis.clear();
	getTimeSeries();
}


function retachLog(gui){
	var log = $('#log-section').detach();
	gui.removeFolder('Log');
	addLogFolder(); 
	$('.dg.ac li:last-child > div > ul').append(log);
}


function getAAMapsValue(){
	return aamaps;
}

function clearLog(){
	log = document.getElementById("log");
	log.value = "";
}


/* Add a new log message*/
function addMessage(message, time) {
	if (time === undefined) {}
	else message = message + ' [' + time.toString() + ' s]';
	log.scrollTop = log.scrollHeight;
	log.value = log.value + message + '\n';
}


function getLimitDates(){
	var dates = getSliderDates();
	var lastFrom = dates[1], from = dates[0];
	var lastTo = dates[3], to = dates[2]; 

	var windowChanged = false;

	//Check if the time window has change
	if (lastTo!=null && lastFrom!=from) windowChanged = true;

	if (aamaps && !windowChanged) {
		if (lastTo!=null && isPrevDate(lastTo,to)){
			if (lastTo == to) from = to;
			else from = lastTo;
		}
	}
	return [from, to];
}


function getSTGrain(){
	return [timeLOD, spaceLOD];
}


function changeSLODoptions(){
	timeLOD = $("#TLOD").val().toLowerCase();
	spaceLOD = $("#SLOD").val().toLowerCase();
	gridSize = $("#grid_size").val();
}


function changeTLODoptions(){
	changeSLODoptions();
	unbindSliderEvents();
	getTimeRange();
}


function changeGridSize(){
	gridSize = $("#grid_size").val();
	lastTo = null;
	getTimeSeries();
}

function getGridSize(){
	return gridSize.substring(1, gridSize.length);
}

function getTableName(){
	return tableDefault+gridSize+$("#TLOD").val().toLowerCase();
}

function getTimeLOD(){
	return timeLOD.slice(0, -1);
}



function getAttenFunction(){
	return attenFunction;
}


function toggleHeaderBackground(){
	$("#header").hover(function () {
		$(this).toggleClass("solid");
	});
}


$(".result").hover(
		function () {
			$(this).addClass("result_hover");
		},
		function () {
			$(this).removeClass("result_hover");
		}
);


function bindKeyEvents(){
	$(document).bind('keypress', function(e) {
			if(e.keyCode==50) showPoints();
			else if(e.keyCode==51) clearPoints();
			else if (e.keyCode==53) hideOptions();
			else if (e.keyCode==52) aboutMessage();
			else if (e.keyCode==49) toggleVizMode();
	});
}

function bindTimeEvents(){
	$('#time_container').hover(function () {
	    $(this).fadeTo(400, 1);
	    //$(this).animate({opacity: '1'}, 300);
	});
	$('#time_container').mouseleave(function () {
	    $(this).fadeTo(400, 0);
	    //$(this).animate({opacity: '0'}, 300);
	});
}


function hideOptions(){
	$( ".dg .main" ).toggle( "fast" );
}


function toggleTimeSlider(){
	$('#time_container').toggle( "fast", function() {
		$(this).hide();
		$(this).off();
	});
}


function pinTime() {
	$('#time_container').off();
    $(this).one("click", unpinTime);
}

function unpinTime() {
	$('#time_container').hide();
	$('#time_container').on();
    $(this).one("click", pinTime);
}

$("#pin-button").one("click", pinTime);





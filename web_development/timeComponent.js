/****************************** TIME SLIDER *****************************/

var from, to, minDate, maxDate, lastTo = null, lastFrom = null;

var splitChar = "/";

var years = ["2007","2008","2009","2010","2011","2012"];
var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"];
var months_num = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"];

var opts = {
		lines: 15 // The number of lines to draw
		, length: 49 // The length of each line
		, width: 14 // The line thickness
		, radius: 45 // The radius of the inner circle
		, scale: 0.25 // Scales overall size of the spinner
		, corners: 1 // Corner roundness (0..1)
		, color: '#000' // #rgb or #rrggbb or array of colors
			, opacity: 0.25 // Opacity of the lines
			, rotate: 36 // The rotation offset
			, direction: 1 // 1: clockwise, -1: counterclockwise
			, speed: 0.6 // Rounds per second
			, trail: 83 // Afterglow percentage
			, fps: 20 // Frames per second when using setTimeout() as a fallback for CSS
			, zIndex: 2e9 // The z-index (defaults to 2000000000)
			, className: 'spinner' // The CSS class to assign to the spinner
				, top: '55%' // Top position relative to parent
					, left: '50%' // Left position relative to parent
						, shadow: false // Whether to render a shadow
						, hwaccel: false // Whether to use hardware acceleration
						, position: 'absolute' // Element positioning
};


function showLoadingDialog(spinner){
	$("#dialog").append(spinner.el);
	$("#dialog").dialog({dialogClass: "no-close"});
	$("#dialog").dialog({
		draggable: false,
		position: { my: 'top', at: 'top+150' },
		modal: true,
		resizable: false,
		closeOnEscape: false
	});
	$("#time_container").attr("disabled", "disabled");
}

function hideLoadingDialog(spinner){
	spinner.stop();
	$("#dialog").dialog("destroy");
	$("#time_container").removeAttr("disabled");
}

function overlay() {
	el = document.getElementById("overlay");
	el.style.visibility = (el.style.visibility == "visible") ? "hidden" : "visible";
}


function createDayTimeSlider(){
	$("#time_slider").dateRangeSlider({
		step: { days: 1},
		bounds: {min: minDate, max: maxDate},
		defaultValues: {min: from , max: to},
		scales: [{
			first: function(value){ return value; },
			end: function(value) {return value; },
			next: function(value){
				var next = new Date(value);
				return new Date(next.setFullYear(value.getFullYear() + 1));
			},
			label: function(value){
				return value.getFullYear();
			},
			format: function(tickContainer, tickStart, tickEnd){
				tickContainer.addClass("myCustomClass");
			}
		},   
		{ // Secondary scale
			first: function(val){ return val; },
			end: function(value) {return value; },
			next: function(value){
				var next = new Date(value);
				return new Date(next.setMonth(value.getMonth() + 1));
			},
			stop: function(val){ return false; },
			label: function(){ return null; }
		}]
	});
	bindSliderEvents();
}


function createMonthTimeSlider(){
	$("#time_slider").dateRangeSlider({
		step: { months: 1},
		bounds: {min: minDate, max: maxDate},
		defaultValues: {min: from , max: to},
		scales: [{
			first: function(value){ return value; },
			end: function(value) {return value; },
			next: function(value){
				var next = new Date(value);
				return new Date(next.setFullYear(value.getFullYear() + 1));
			},
			label: function(value){
				return value.getFullYear();
			},
			format: function(tickContainer, tickStart, tickEnd){
				tickContainer.addClass("myCustomClass");
			}
		},   
		{ // Secondary scale
			first: function(val){ return val; },
			end: function(value) {return value; },
			next: function(value){
				var next = new Date(value);
				return new Date(next.setMonth(value.getMonth() + 1));
			},
			stop: function(val){ return false; },
			label: function(){ return null; }
		}]
	});
	bindSliderEvents();
}


function createWeekTimeSlider(){
	$("#time_slider").dateRangeSlider({
		step: { weeks: 1},
		bounds: {min: minDate, max: maxDate},
		defaultValues: {min: from , max: to},
		scales: [{
			first: function(value){ return value; },
			end: function(value) {return value; },
			next: function(value){
				var next = new Date(value);
				return new Date(next.setFullYear(value.getFullYear() + 1));
			},
			label: function(value){
				return value.getFullYear();
			},
			format: function(tickContainer, tickStart, tickEnd){
				tickContainer.addClass("myCustomClass");
			}
		},   
		{ // Secondary scale
			first: function(val){ return val; },
			end: function(value) {return value; },
			next: function(value){
				var next = new Date(value);
				return new Date(next.setMonth(value.getMonth() + 1));
			},
			stop: function(val){ return false; },
			label: function(){ return null; }
		}]
	});
	bindSliderEvents();
}


function createYearTimeSlider(){
	$("#time_slider").dateRangeSlider({
		step: { years: 1},
		bounds: {min: minDate, max: maxDate},
		defaultValues: {min: from , max: to},
		scales: [{
			first: function(value){ return value; },
			end: function(value) {return value; },
			next: function(value){
				var next = new Date(value);
				return new Date(next.setFullYear(value.getFullYear() + 1));
			},
			label: function(value){
				return value.getFullYear();
			},
			format: function(tickContainer, tickStart, tickEnd){
				tickContainer.addClass("myCustomClass");
			}
		},   
		{ // Secondary scale
			first: function(val){ return val; },
			end: function(value) {return value; },
			next: function(value){
				var next = new Date(value);
				return new Date(next.setMonth(value.getMonth() + 1));
			},
			stop: function(val){ return false; },
			label: function(){ return null; }
		}]
	});
	bindSliderEvents();
}



function bindSliderEvents(){	
	$("#time_slider").bind("userValuesChanged", function(e, data){
		lastTo = to;
		lastFrom = from;
		from = data.values.min;
		to = data.values.max;
	});
	$("#time_slider").bind("valuesChanged", function(e, data){
		lastTo = to;
		lastFrom = from;
		from = data.values.min;
		to = data.values.max;
		getTimeSeries();
	});
}

function getCreationFlag(){
	return creationComplete;
}

function unbindSliderEvents(){
	$("#time_slider").unbind();
	$("#time_slider").dateRangeSlider("destroy");
}



function setSliderDates(d1, d2){
	creationComplete = false;
	var df = d1.split(" ")[0].split(splitChar);
	var de = d2.split(" ")[0].split(splitChar);
	from = new Date(df[2], (pad(df[1]) - 1), pad(df[0]));
	to = new Date(df[2], pad(df[1] - 1 + 3), pad(df[0]));
	minDate = from;
	maxDate = new Date(de[2], pad(de[1] - 1), pad(de[0]));
}



function getSliderDates(){	
	return [convertDate(from), convertDate(lastFrom), convertDate(to), convertDate(lastTo)];
}



function checkDate(date){
	var allowBlank = true;
	var minYear = 1902;
	var maxYear = (new Date()).getFullYear();
	var errorMsg = "";

	// regular expression to match required date format
	re = /^(\d{4})\/(\d{1,2})\/(\d{1,2})$/;

	if(date != '') {
		if(regs = date.match(re)) {
			if(regs[1] < 1 || regs[3] > 31) {
				errorMsg = "Invalid value for day: " + regs[1];
			} else if(regs[2] < 1 || regs[2] > 12) {
				errorMsg = "Invalid value for month: " + regs[2];
			} else if(regs[1] < minYear || regs[1] > maxYear) {
				errorMsg = "Invalid value for year: " + regs[1] + " - must be between " + minYear + " and " + maxYear;
			}
		} else {
			errorMsg = "Invalid date format: " + date;
		}
	} else if(!allowBlank) {
		errorMsg = "Empty date not allowed!";
	}
	if(errorMsg != "") {
		alert(errorMsg);
		return false;
	}
	return true;
}


function timeRange(initDate, finalDate){
	var oneDay = 24*60*60*1000; // hours*minutes*seconds*milliseconds
	var firstDate = new Date(initDate);
	var secondDate = new Date(finalDate);
	var diffDays = Math.round(Math.abs((firstDate.getTime() - secondDate.getTime())/(oneDay)));
	return diffDays+1;
}


function addDaysToDate(date,days) {
	var d = new Date(date);
	d.setHours(d.getHours() + (24 * days));
	var dd = d.getDate();
	var mm = d.getMonth()+1; //January is 0!
	var yyyy = d.getFullYear();

	if(dd<10){
		dd='0'+dd
	} 
	if(mm<10){
		mm='0'+mm
	} 
	d = yyyy+'/'+mm+'/'+dd;
	document.getElementById("time_range").innerHTML=d;
	return d;
}

/* AUX FUNCTIONS  */

//Create a new date from a string, return as a timestamp.
function timestamp(str){
	return new Date(str).getTime();   
}


function isRightMovement(date1, date2){
	return (timestamp(date1) < timestamp(date2));
}

function isPrevDate(date1, date2) {
	return (date1 <= date2);
}


//Create a list of day and monthnames.
var weekdays = ["Sunday", "Monday", "Tuesday","Wednesday", 
                "Thursday", "Friday", "Saturday"],

                months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
                          "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"];


//Append a suffix to dates.
//Example: 23 => 23rd, 1 => 1st.
function nth (d) {
	if(d>3 && d<21) return 'th';
	switch (d % 10) {
	case 1:  return "st";
	case 2:  return "nd";
	case 3:  return "rd";
	default: return "th";
	}
}

//Create a string representation of the date.
function formatDate ( date ) {
	return weekdays[date.getDay()] + ", " +
	date.getDate() + nth(date.getDate()) + " " +
	months[date.getMonth()] + " " +
	date.getFullYear();
}


function pad(n){return n<10 ? '0'+n : n}

function convertDate(date) {
	if (date!=null) return date.getFullYear() + "-" + pad((date.getMonth()+1)) + "-" + pad((date.getDate()));
	return null;
}







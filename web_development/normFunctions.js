var at_type="Constant";
var unit=1;
var events;
var items;
var max, min, mean, stdev, range, qtindex, quantil;
var collection, items;
var calcEffects;


/*************************************************************************
********************* Initialization &  Utils ****************************
*************************************************************************/


function initCollection(points){
	// Data Statistics
	calcEffects = new Array();
	items = points;
   	collection = new geostats(items);
	max = collection.max();
	min = collection.min();
	mean = collection.mean();
	stdev = collection.stddev();
	range = max - min;
	qtindex = Math.round(0.75 * items.length)-1;
	quantil = items[qtindex];
}

function getMax(){
	return max;
}

function getMin(){
	return min;
}

function getMean(){
	return mean;
}

function getStdev(){
	return stdev;
}


/*************************************************************************
********************* Effect Calculation Functions **********************
*************************************************************************/


/**** Range Function ****/

function rangeCalc(effect){
	var newEffect = (effect - min) / range;
	//if (newEffect < 0.1) return 0;
	return newEffect;
}

function rangeNorm(){
	calcEffects = new Array();
	$.each(items, function(i,effect){
   		calcEffects.push(rangeCalc(effect));
   	});
	return calcEffects;
}

/**** X/Xmax Function ****/

function maxRelativeCalc(effect){
	return effect / max;
}


function xMaxNorm(){
	calcEffects = new Array();
	$.each(items, function(i,effect){
   		calcEffects.push(maxRelativeCalc(effect));
   	});
	return calcEffects;
}

/**** ZScore Function ****/

function zScoreCalc(effect){
	var zscore = (effect - mean) / stdev;
	if (zscore > 0) return Math.min(0.5+zscore, 1);
	else return Math.max(0.5+zscore, 0);
}

function zScoreNorm(){
	calcEffects = new Array();
	$.each(items, function(i,effect){
   		calcEffects.push(zScoreCalc(effect));
   	});
	return calcEffects;
}


/**** 75Percentil Function ****/

function percentilCalc(effect){
	var newRange = max - quantil;
	if (effect <= quantil) return 0;
	else return effect / max;
}

function percentilNorm(){
	calcEffects = new Array();
	$.each(items, function(i,effect){
   		calcEffects.push(percentilCalc(effect));
   	});
	return calcEffects;
}


/*************************************************************************
********************* AA Maps Calculation Functions **********************
*************************************************************************/


function AAMapsRangeCalc(effect){
	return (effect - min) / range;
}

function AAMapsRangeNorm(maxEffect, minEffect){
	calcEffects = new Array();
	$.each(items, function(i,effect){
   		calcEffects.push(AAMapsRangeCalc(effect));
   	});
	return calcEffects;
}


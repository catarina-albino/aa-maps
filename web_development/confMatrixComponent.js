
/****************************** Confusion Matrix *****************************/

var year, version = 2, threshold = 20;
var percentage = false;
var TP, FP, FN, TN;
var cm = false;
var cmArray = new Array(3), years = [2010, 2011, 2012];
var tColor = "#33ff33", fColor = "#ff4d4d";
var cmSpinner, recomputeCM = true;


function isPercentage(){
	return percentage;
}

function getVersion(){
	return version;
}

function getThreshold(){
	return threshold;
}


function getYearIndex(){
	if (initYear==2010) return 0;
	else if (initYear ==2011) return 1;
	else return 2;
}


function getConfusionMatrix(){
	updateVersion();
	var curCM = cmArray[getYearIndex()];
	if (initYear !== null && (curCM === undefined || contextChanged || recomputeCM)){
		loadCMSpinner();
		clearCM();
		requestTimeUrl = myip + ":" + myport + "/confusionmatrix?year=" + initYear +"&gridSize=" + getGridSize() 
		+ "&attenF=" + getAttenFunction() + "&accumF=" + getAccumFunction() + "&version=" + version 
		+ "&percentage=" + percentage + "&threshold=" + threshold;
		console.log("confusionmatrix request url: " + requestTimeUrl);
		getConfusionMatrixJSON(requestTimeUrl);
	}
	else setCurCM(curCM);
}


function setCurCM(cm){
	document.getElementById('a').innerHTML = round2Places(cm.true_positives);
	document.getElementById('b').innerHTML = round2Places(cm.false_positives);
	document.getElementById('c').innerHTML = round2Places(cm.false_negatives);
	document.getElementById('d').innerHTML = round2Places(cm.true_negatives);
	document.getElementById('precision').innerHTML = "Prec = "+round2Places(cm.ppv)+"%";
	document.getElementById('recall').innerHTML = "Rec = "+round2Places(cm.recall)+"%";
	TP = cm.TP.elements;
	FP = cm.FP.elements;
	FN = cm.FN.elements;
	TN = cm.TN.elements;
}



function getConfusionMatrixJSON(url) {
	$.getJSON(url, function(cm) {
		cmArray[getYearIndex()] = cm;
		setCurCM(cm);
		spinner.stop();
		recomputeCM = false;
	}).error(function(jqXHR, textStatus, errorThrown) {
		console.log("error " + textStatus);
	})
}



function drawTP(){
	cm = true;
	color = hexToRgb(tColor);
	drawCMPoints(TP);
}


function drawFP(){
	cm = true;
	color = hexToRgb(fColor);
	drawCMPoints(FP);
}


function drawFN(){
	cm = true;
	color = hexToRgb(fColor);
	drawCMPoints(FN);
}


function drawTN(){
	cm = true;
	color = hexToRgb(tColor);
	drawCMPoints(TN);
}



function round2Places(value){
	return Math.round(value * 100) / 100;
}


function bindGetCMMetrics(){
	$('#a').on('click', '', function (){
		drawTP();
		selectCMMetric('#a');
	});
	$('#b').on('click', '', function (){
		drawFP();
		selectCMMetric('#b');
	});
	$('#c').on('click', '', function (){
		drawFN();
		selectCMMetric('#c');
	});
	$('#d').on('click', '', function (){
		drawTN();
		selectCMMetric('#d');
	});
}


function selectCMMetric(id){
	$('#cm td').removeClass('selected');
	$(id).addClass('selected');
}


function loadCMSpinner(){
	spinner = new Spinner(opts).spin();
	$("#cm").append(spinner.el);
}


function updateVersion(){
	/*var newVal = $("input[name=version]:checked").val();
	if (newVal != version) recomputeCM = true;
	version = newVal;*/
}


function clearCM(){
	$('#a').empty();
	$('#b').empty();
	$('#c').empty();
	$('#d').empty();
	$('#precision').empty();
	$('#recall').empty();
}


function deleteCM(){
	cmArray = new Array(3);
}


$(function() {
    $( "#threshold-slider" ).slider({
      range: "min",
      value: 20,
      step: 5,
      min: 5,
      max: 50,
      slide: function( event, ui ) {
        $( "#threshold" ).val( ui.value );
      },
      stop:  function( event, ui ) {
    	  threshold = ui.value;
    	  recomputeCM = true;
          getConfusionMatrix();
      }
    });
    $( "#threshold" ).val( $( "#threshold-slider" ).slider( "value" ) );
  });


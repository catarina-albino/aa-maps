var initYear, endYear;


function getPN(){
	fixed = true;
	pn = true;
	$("#pn-panel").show();
	$("#pn-button").addClass("active");
	$("#show-button").removeClass("active");
	requestTimeUrl = myip + ":" + myport + "/pontosnegros?posInit=" + initYear + 
					"&posEnd="+ endYear+"&timeGranularity=" +getGridSize();
	console.log("Pontos Negros request url: " + requestTimeUrl);
	getPNJSON(requestTimeUrl);
}


function changeYear() {
	cm = false;
    var selectBox = document.getElementById("pn-year");
    var selectedValue = selectBox.options[selectBox.selectedIndex].value;
    if (selectedValue == "all") {
    	initYear = null;
    	clearCM();
    }
    else {
    	initYear = selectedValue;
    	endYear = initYear;
    }
    color = hexToRgb(defaultColor);
	getPN();
	if (initYear!==null) getConfusionMatrix();
}


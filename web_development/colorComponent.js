var classes = 4;
var limitsI, colorsI, colorF, a = 0.2, curOpIndex = 0;
var opFunctionNames = ['EaseOut','EaseIn'];
var opFunctions = [expEaseOutFunction,expEaseInFunction ];
var opFunction=expEaseOutFunction;
var schemaNames = ['Viridis'];
var viridis =  viridis_schema;
var plasma =  plasma_schema;
var magma =  magma_schema;
var inferno = inferno_schema;
var palete = viridis;
var ctx, canvas = [];
var plotColor = "rgb(47, 161, 214)";
var font = "bold 11px Arial";


/*******************************************************************
/************************* Color Map Functions  ********************
 *******************************************************************/


function redrawAllPlots(){
	for (i=0; i < opFunctions.length; i++){
		redrawPlot(i);
	}
}


function initColorParams(data){
	limitsI = chroma.limits(data, 'k', classes);
	colorsI = buildColorVector(limitsI,palete);
	colorF = colorFunction(colorsI, limitsI);
	redrawPlot(curOpIndex);
}


function buildColorVector(limits, colorVector) {
	var init_pos = 0;
	var length = limits.length;
	var colors = [];
	var step = Math.trunc((colorVector.length-init_pos)/(length - 1));
	for (var i = init_pos; i < colorVector.length; i+=step) 
		colors.push(colorVector[i]);
	return colors;
}


function colorFunction(colorsI, limitsI) {
	var f =  function (value) {
		var limits = limitsI;
		var colors = colorsI;
		var opacity = null;
		if (name != "identity") opacity = opFunction(value);
		for(var j=0; j <limits.length-1; j++) {
			if (opacity == null) opacity = limits[j];
			if(j+1==limits.length-1) {
				if(value >= limits[j] && value <= limits[j+1])
					return [chroma(colors[j]).gl(), opacity];
			}
			else {
				if(value >= limits[j] && value < limits[j+1]){
					return [chroma(colors[j]).gl(), opacity];
				}	
			}
		}
	};
	return f;
}


function identityFunction(value) {return value;}
function expEaseInFunction(value) {return Math.pow(value, (1/a));}
function expEaseOutFunction(value) {return Math.pow(value, a);}


function changeOpacityF(e, index) {
	opFunction = opFunctions[index];
	curOpIndex = index;
}


/*******************************************************************
/************************* Plot Functions   ********************
 *******************************************************************/


function addNCanvas(){
	for (i=0;i<opFunctionNames.length;i++){
		var canv = createCanvas(i);
		drawPlot(i);
	}
}

function createCanvas(n){
	var id = "canvas"+n;
	var canvasElem = '<canvas class="canvas" id='+id+' width="60" height="60"></canvas>';
	opacityFrame.add('<li value='+n+' title="'+opFunctionNames[n]+'">'+ canvasElem + '</li>');
} 

function drawPlot(n) {
	canvas[n] = document.getElementById("canvas"+n);
	if (null==canvas[n] || !canvas[n].getContext) return;
	redrawPlot(n);
}


function redrawPlot(n){
	var canv = canvas[n];
	var axes={}, ctx=canv.getContext("2d");
	axes.x0 = .1 + .1*canv.width;  // x0 pixels from left to x=0
	axes.y0 = .9 + .9*canv.height; // y0 pixels from top to y=0
	axes.scale = 40;                 // pixels from x=0 to x=1
	axes.doNegativeX = true;
	ctx.clearRect(0, 0, canv.width, canv.height);
	showAxes(ctx,axes);
	funGraph(ctx,axes,opFunctions[n],plotColor,2,n);
}


function funGraph (ctx,axes,func,color,thick,n) {
	var xx, yy, dx=4, x0=axes.x0, y0=axes.y0, scale=axes.scale;
	var iMax = Math.round((ctx.canvas.width-x0)/dx);
	var iMin = axes.doNegativeX ? Math.round(-x0/dx) : 0;
	ctx.font = font;
	ctx.fillText(opFunctionNames[n],7,10);
	ctx.beginPath();
	ctx.lineWidth = thick;
	ctx.strokeStyle = color;
	
	for (var i=iMin;i<=iMax;i++) {
		xx = dx*i; yy = scale*func(xx/scale);
		if (i==iMin) ctx.moveTo(x0+xx,y0-yy);
		else         ctx.lineTo(x0+xx,y0-yy);
	}
	ctx.stroke();
}


function showAxes(ctx,axes) {
	var x0=axes.x0, w=ctx.canvas.width;
	var y0=axes.y0, h=ctx.canvas.height;
	var xmin = axes.doNegativeX ? 0 : x0;
	ctx.beginPath();
	ctx.strokeStyle = "rgb(0,0,0)"; 
	ctx.moveTo(xmin,y0); ctx.lineTo(w,y0);  // X axis
	ctx.moveTo(x0,0);    ctx.lineTo(x0,h);  // Y axis
	ctx.stroke();
}




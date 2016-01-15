var classes = 4;
var limitsI, colorsI, colorF, a = 0.2, opFunction = expEaseOutFunction;
var opFunctionNames = ['Exp_EaseOut','Exp_EaseIn'];
var schemaNames = ['Viridis'];
var viridis =  viridis_schema;
var plasma =  plasma_schema;
var magma =  magma_schema;
var inferno = inferno_schema;
var palete = viridis;
var axes, ctx, canvas;


/*******************************************************************
/************************* Color Map Functions  ********************
 *******************************************************************/

var options = {
	    horizontal: 1,
	    itemNav: 'basic',
	    speed: 300,
	    mouseDragging: 1,
	    touchDragging: 1
};
$('#frame').sly(options);



function initColorParams(data){
	limitsI = chroma.limits(data, 'k', classes);
	colorsI = buildColorVector(limitsI,palete);
	colorF = colorFunction(colorsI, limitsI); 
	redrawPlot();
}

function buildColorVector(limits, colorVector) {
	var init_pos = 0;
	var length = limits.length;
	var colors = [];
	var step = Math.trunc((colorVector.length-init_pos)/(length - 1));
	console.log(step);
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


function opacityFunction(name, value) {
	if (name == 'identity') opFunction = identityFunction;
	else if (name == 'exp_easein') opFunction = expEaseInFunction;
	else opFunction = expEaseOutFunction;
	redrawPlot();
}


/*******************************************************************
/************************* Plot Functions   ********************
 *******************************************************************/

function addCanvas(){
	return '<canvas id="canvas" width="70" height="70"></canvas>';
}

function toggleCanvas(){
	$('#canvas').toggleClass('hidden');
}

function fun1(x) {return Math.sin(x);  }
function fun2(x) {return Math.cos(3*x);}

function drawPlot() {
 canvas = document.getElementById("canvas");
 if (null==canvas || !canvas.getContext) {
	 alert('aaa');
	 return;
 }

 axes={}, ctx=canvas.getContext("2d");
 axes.x0 = .1 + .1*canvas.width;  // x0 pixels from left to x=0
 axes.y0 = .9 + .9*canvas.height; // y0 pixels from top to y=0
 axes.scale = 40;                 // 40 pixels from x=0 to x=1
 axes.doNegativeX = true;
 redrawPlot();
}

function redrawPlot(){
	ctx.clearRect(0, 0, canvas.width, canvas.height);
	showAxes(ctx,axes);
	funGraph(ctx,axes,opFunction,"rgb(11,153,11)",2);
}


function funGraph (ctx,axes,func,color,thick) {
 var xx, yy, dx=4, x0=axes.x0, y0=axes.y0, scale=axes.scale;
 var iMax = Math.round((ctx.canvas.width-x0)/dx);
 var iMin = axes.doNegativeX ? Math.round(-x0/dx) : 0;
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




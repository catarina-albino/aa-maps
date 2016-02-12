var attenFrame, accumFrame, opacityFrame;
var options = {
		horizontal: 1,
		itemNav: 'forceCentered',
		smart: 1,
		activateMiddle: 1,
		activateOn: 'click',
		mouseDragging: 1,
		touchDragging: 1,
		releaseSwing: 1,
		startAt: 0,
		scrollBy: 1,
		speed: 50,
		elasticBounds: 1,
		easing: 'easeOutExpo',
		dragHandle: 1,
		dynamicHandle: 1,
		clickBar: 1,
};


function createAccumSly(){
	accumFrame = new Sly('#accumSlider', options).init();
}


function createAttenSly(){
	attenFrame = new Sly('#attenSlider', options).init();
}


function createOpacitySly(){
	opacityFrame = new Sly('#opacitySlider', options).init();
}


function bindChangeFunction(){
	attenFrame.on("active",  changeAttFunc);
	accumFrame.on("active", changeAccFunc);
	opacityFrame.on("active", changeOpacityF);
}


function changeAttFunc(e, index){
	var newF = this.items[index].el.value;
	if (newF != attenFunction) contextChanged = true;
	else contextChanged = false;
	attenFunction = newF;
}

function changeAccFunc(e, index){
	var newF = this.items[index].el.value;
	if (newF != accumFunction) contextChanged = true;
	else contextChanged = false;
	accumFunction = newF;
}

function onChangeAAFunc(){
	getTimeSeries();
	contextChanged = false;
}


function bindOnChangeAAF(attenID, accumID){
	$('#attenSlider').on('click', 'li', function (){
		onChangeAAFunc();
	});
	$('#accumSlider').on('click', 'li',function (){
		onChangeAAFunc();
	});
	$('#opacitySlider').on('click', 'li', function (e, index){
		redrawPoints();
	});
}

function addAccumItem(name, value, descp){
	accumFrame.add('<li id="'+name.toLowerCase()+'" value='+value+' title="'+descp+'">'+ name + '</li>');
}

function addAttenItem(name, value, descp){
	attenFrame.add('<li value='+value+' title="'+descp+'">'+ name + '</li>');
}



function addDescp(){
$('.masterTooltip').hover(function(){
    // Hover over code
    var title = $(this).attr('title');
    $(this).data('tipText', title).removeAttr('title');
    $('<p class="tooltip"></p>')
    .text(title)
    .appendTo('body')
    .fadeIn('slow');
}, function() {
    // Hover out code
    $(this).attr('title', $(this).data('tipText'));
    $('.tooltip').remove();
}).mousemove(function(e) {
    var mousex = e.pageX + 20; 
    var mousey = e.pageY + 10; 
    $('.tooltip')
    .css({ top: mousey, left: mousex })
});
}


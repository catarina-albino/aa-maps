/**
 * Javascript API to display information on a Leaflet Map using WebGL
 */

/* Requirements 
 * 		Leaflet API
 * */

/* Constructor */

/**
 * 
 * @id: div in which the map is displayed
 * @map: leaflet map 
 * @options: options to draw the map
 */
function gisplay(id, map, options) {

	var _options = options;

	var _webgl = { gl: null, 
			program: null,
			projection: null,
			pointBuffer: null};
	var _map = map;
	var Library = {}; 

	if (id) {

		if (window === this) {
			return new gisplay(id, map, options);
		}

		var div = document.getElementById(id);
		var id_canvas = buildCanvas(div);
		init_gl(id_canvas);
		program();
		createFunctions();

		this._options = _options;
		this._webgl = _webgl;
		this._buffers = new Array();
		this._data = [];
		this._map = map;
		this.Library = Library;

		return this;
	} else {
		return null;
	}

	/**
	 * WebGL Utilities  
	 */

	/**
	 * Creates the canvas element attached to the div which contains the map
	 */
	function buildCanvas(div) {
		var canvas = document.createElement('canvas');
		canvas.id = 'glcanvas'
		canvas.style.position = 'absolute';
		canvas.width = _map.getSize().x;
		canvas.height = _map.getSize().y;
		div.appendChild(canvas);
		return canvas.id;
	}

	function init_gl(id_canvas) {
		try {
			var canvas = document.getElementById(id_canvas);
			_webgl.gl = canvas.getContext('experimental-webgl');
			_webgl.projection = new Float32Array(16);
			_webgl.projection.set([2/canvas.width, 0, 0, 0, 0, -2/canvas.height, 0, 0, 0, 0, 0, 0, -1, 1, 0, 1]);

			_webgl.gl.viewport(0, 0, _map.getContainer().offsetWidth, _map.getContainer().offsetHeight);
			_webgl.gl.disable(_webgl.gl.DEPTH_TEST);

		} catch(e) {
		}
		if (!_webgl.gl) {
			console.log("ERROR: unable to initialise WebGL");
		}
	}

	function program() {
		_webgl.program = _webgl.gl.createProgram();

		var source_code = gen_source_code();

		var vertex_shader = shader(_webgl.gl.VERTEX_SHADER, source_code[0]);
		var fragment_shader = shader(_webgl.gl.FRAGMENT_SHADER, source_code[1]);

		_webgl.gl.attachShader(_webgl.program, vertex_shader);
		_webgl.gl.attachShader(_webgl.program, fragment_shader);

		_webgl.gl.linkProgram(_webgl.program);
		_webgl.gl.useProgram(_webgl.program);
		
		//Bind click event to the canvas container
		var canvas = document.getElementById(id_canvas);
		var vertexCoordLocation = _webgl.gl.getAttribLocation(_webgl.program, 'vertexCoord');
		
		$('#'+id_canvas+'').on('mousedown', function (evt) {
			$('#'+id_canvas+'').on('mouseup mousemove', function handler(evt) {
			    if (evt.type === 'mouseup') 
			    	click (evt, _webgl.gl, canvas, vertexCoordLocation)
			    else {} // drag 
			    $('#'+id_canvas+'').off('mouseup mousemove', handler);
			  });
			});
	}

	function gen_source_code() {
		var vextex_source = "";
		var fragment_source = "";

		if(_options.geometry == 'polygon' && !_options.fill) {
			var vextex_source = 'attribute vec4 vertexCoord; attribute float aPointSize; uniform mat4 projection; void main() {\n gl_Position = (projection * vertexCoord);';
			vextex_source += ' \n gl_PointSize = aPointSize;';
			vextex_source +=' \n }';
			fragment_source = 'precision mediump float; uniform vec4 u_color; void main(){\n	gl_FragColor = u_color;\n}';
		}

		if(_options.geometry == 'point' && !_options.fill) { //For now both if's are redundant but in the future will probably differ
			var vextex_source = 'attribute vec4 vertexCoord; attribute float aPointSize; uniform mat4 projection; \n';
			vextex_source += 'attribute vec4 a_color; varying vec4 v_color; \n';
			vextex_source += 'void main() {\n gl_Position = (projection * vertexCoord);';
			vextex_source += ' \n gl_PointSize = aPointSize; \n';
			vextex_source += 'v_color = a_color;'; // pass the color to the fragment shader
			vextex_source +=' \n }';
			fragment_source = 'precision mediump float; uniform vec4 u_color; \n';
			fragment_source += 'varying vec4 v_color; void main(){\n';
			if (_options.shape == 'square'){
				fragment_source += 'if (u_color[3] == -1.0){\n'
				//fragment_source += 'if (v_color[3] < 0.1) discard;\n';
				fragment_source += 'gl_FragColor = v_color;}\n';
				fragment_source += 'else{\n';
				//fragment_source += 'if (u_color[3] < 0.1) discard;\n';
				fragment_source += 'gl_FragColor = u_color;\n}}';	
			}
			else {
				fragment_source += 'float border = 0.05;\n float radius = 0.5;\n float centerDist = length(gl_PointCoord - 0.5);\n';
				fragment_source += 'float alpha;\n';
				fragment_source += 'if (u_color[3] == -1.0){\n' //AAMaps Opacity
				fragment_source += 'alpha =  v_color[3] * step(centerDist, radius);\n';
				fragment_source += 'gl_FragColor = vec4(v_color[0], v_color[1], v_color[2], alpha);\n }';
				fragment_source += 'else{\n'; //Auto Opacity*/
				fragment_source += 'alpha =  u_color[3] * step(centerDist, radius);\n';
				//fragment_source += 'if (alpha < 0.1) discard;\n';
				fragment_source += 'gl_FragColor = vec4(u_color[0], u_color[1], u_color[2], alpha);\n }}';		
			}
		}
		return [vextex_source, fragment_source];
	}

	function shader(type, source_code) {
		var shader = _webgl.gl.createShader(type);

		_webgl.gl.shaderSource(shader,source_code); 
		_webgl.gl.compileShader(shader);
		if (!_webgl.gl.getShaderParameter(shader, _webgl.gl.COMPILE_STATUS)) {
			throw _webgl.gl.getShaderInfoLog(shader);
		}
		return shader;
	}
	
	function createFunctions() {

		/**
		 * Auxiliary functions
		 */

		Library.Proj4Pixel = function (latitude, longitude) {
			var pi_180 = Math.PI / 180.0;
			var pi_4 = Math.PI * 4;

			var sinLatitude = Math.sin(latitude * pi_180);
			var pixelY = (0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) /(pi_4)) * 256;
			var pixelX = ((longitude + 180) / 360) * 256;

			var pixel =  {x: pixelX, y: pixelY};

			return pixel;
		}

		Library.scaleProjection = function (matrix, scaleX, scaleY) {
			// scaling x and y, which is just scaling first two columns of matrix
			matrix[0] *= scaleX;
			matrix[1] *= scaleX;
			matrix[2] *= scaleX;
			matrix[3] *= scaleX;

			matrix[4] *= scaleY;
			matrix[5] *= scaleY;
			matrix[6] *= scaleY;
			matrix[7] *= scaleY;
		}

		Library.translateProjection = function (matrix, tx, ty) {
			// translation is in last column of matrix
			matrix[12] += matrix[0]*tx + matrix[4]*ty;
			matrix[13] += matrix[1]*tx + matrix[5]*ty;
			matrix[14] += matrix[2]*tx + matrix[6]*ty;
			matrix[15] += matrix[3]*tx + matrix[7]*ty;
		}

	}
	
	
	function click(ev,gl,canvas,vertexCoordLocation){
		var ctx = canvas.getContext("experimental-webgl", {preserveDrawingBuffer: true});
		var x = ev.clientX;
		var y = ev.clientY;
		
		var rect = ev.target.getBoundingClientRect();
		x = ((x-rect.left) - canvas.width/2) / (canvas.width/2);
		y = (canvas.height/2 - (y - rect.top)) / (canvas.height/2);
	
		// check if you can read from this type of texture.
		//bool canRead = (gl.checkFramebufferStatus() == gl.FRAMEBUFFER_COMPLETE);
		// Unbind the framebuffer
		/*gl.bindFramebuffer(gl.FRAMEBUFFER, null);
		// make a framebuffer
		var fb = gl.createFramebuffer();
		*/ //var status = gl.checkFramebufferStatus(gl.FRAMEBUFFER);
		//if (status == gl.FRAMEBUFFER_COMPLETE){
			var pixels = new Uint8Array(3);
			//gl.readPixels(x , y , 1 , 1 , gl.GL_RGB , gl.UNSIGNED_BYTE , pixels);
			if (pixels[0] == color[0] && pixels[1] == color[1] && pixels[2] == color[2])
				alert("clickei");
		//}
	}
};

/*	_ Prototype Functions
============================*/

gisplay.prototype = {
		
		clear: function() {
			var gl = this._webgl.gl;
			gl.clear(gl.COLOR_BUFFER_BIT);
		},
		
		points: function(data) {

			this._data = process_data(this._options, this.Library);
			this._buffers = build_buffers(this._data, this._webgl, this._options);

			/** 
			 * Auxiliary Functions to process Input Data
			 */

			function process_data(options, library) {
				if (options.geometry = "point") {
					var result = [];
					if (!options.fixed){
						for (var f = 0; f < data.length-6; f+=6) {
							pixel = library.Proj4Pixel(data[f+1], data[f]);
							result.push(pixel.x, pixel.y,data[f+2],data[f+3],data[f+4],data[f+5]);
						}
					}
					else {
						for (var f = 0; f < data.length-2; f+=2) {
							pixel = library.Proj4Pixel(data[f+1], data[f]);
							result.push(pixel.x, pixel.y);
						}
					}
					return result;
			}
				
			}

			function build_buffers(data, webgl, options) {
				/** Buffers to draw points
				 *  For now we are just using one buffer. 
				 */
				var buffers = [];
				var gl = webgl.gl;
				if (webgl.pointBuffer!=null) gl.deleteBuffer(webgl.pointBuffer);
				webgl.pointBuffer = gl.createBuffer();
				
				buffers.push(webgl.pointBuffer);
				var points = new Float32Array(data);
				var fsize = points.BYTES_PER_ELEMENT;
				
				gl.bindBuffer(gl.ARRAY_BUFFER, buffers[0]);
				gl.bufferData(gl.ARRAY_BUFFER, points, gl.STATIC_DRAW);

				if (options.fixed) {
					buffers[0].itemSize=2;
					buffers[0].numItems=points.length/2;
				}
				else {
					buffers[0].itemSize=6;
					buffers[0].numItems=points.length/6;
				}
				buffers[0].fsize=fsize;
				return buffers;
			}
		},
		polygons: function (data) {

			this._data = process_data(data, this._options, this.Library);
			this._buffers = build_buffers(this._data, this._webgl.gl);

			/** 
			 * Auxiliary Functions to process Input Data
			 */
			function process_data(indata, options, library) {
				if (options.geometry == "polygon" && options.stroke) {
					return polygons_border(indata, library);
				}
			}

			/** 
			 * Auxiliary Functions to build WebGL Buffers
			 */

			function polygons_border(data, library) {
				var result = [];
				for (var f = 0; f < data.length; f++) {
					result[f] = [];

					for (var i = 0; i < data[f].length; i++) {
						pixel = library.Proj4Pixel(data[f][i].y, data[f][i].x);
						result[f].push(pixel.x, pixel.y);
					}
				}
				return result;
			}

			function build_buffers(data, gl) {

				var buffers = [];

				/** Buffers to draw polygon borders */
				for (var i = 0; i < data.length; i++) {

					buffers.push(gl.createBuffer());

					var polygon_vertices = new Float32Array(data[i]);
					gl.bindBuffer(gl.ARRAY_BUFFER, buffers[i]);
					gl.bufferData(gl.ARRAY_BUFFER, polygon_vertices, gl.STATIC_DRAW);

					buffers[i].itemSize=2;
					buffers[i].numItems=polygon_vertices.length/2;
				}
				return buffers;
			}

		},
		draw: function () { 

			var options = this._options;
			var webgl = this._webgl;
			var buffers = this._buffers;
			var map = this._map;
			var library = this.Library;

			if (options.geometry == "point") {
				drawPoints(options, webgl, buffers, map, library);
			}
			else if (options.geometry == "polygon" && options.stroke) {
				drawPolygonsBorder(webgl, buffers, map, library);
			}

			/**
			 * Functions to draw according to the geometry type
			 */

			function drawPoints(options, webgl, buffers, map, library) {
				var gl = webgl.gl;

				if (gl == null) return;
				var matrixProjection = new Float32Array(16);

				gl.clear(gl.COLOR_BUFFER_BIT);
				gl.enable(gl.BLEND);	
				gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);
				gl.blendEquation(gl.FUNC_ADD);
		
				var currentZoom = map.getZoom();
				var pointSize = Math.max(currentZoom - 5.0, options.size);

				matrixProjection.set(webgl.projection);

				var scale = Math.pow(2, currentZoom);
				library.scaleProjection(matrixProjection, scale, scale);

				var offset = library.Proj4Pixel(map.getBounds().getNorthWest().lat, map.getBounds().getNorthWest().lng);
				library.translateProjection(matrixProjection, -offset.x, -offset.y);

				var projectionLocation = gl.getUniformLocation(webgl.program, 'projection');
				gl.uniformMatrix4fv(projectionLocation, false, matrixProjection);

				/** Draw Point's Interior **/
				var vertexCoordLocation = gl.getAttribLocation(webgl.program, 'vertexCoord');
				var vertexSizeLocation = gl.getAttribLocation(webgl.program, 'aPointSize');
				var colorFixedLocation = gl.getUniformLocation(webgl.program, "u_color");
				var colorVarLocation = gl.getAttribLocation(webgl.program, "a_color");
				//var opacityLocation  = gl.getAttribLocation(webgl.program, "a_opacity");
				

				gl.vertexAttrib1f(vertexSizeLocation, pointSize);
				var color = options.color;

				if (!options.fixed){
					gl.vertexAttribPointer(vertexCoordLocation, 2, gl.FLOAT, false, buffers[0].fsize * 6, 0);
					gl.enableVertexAttribArray(vertexCoordLocation);
					gl.uniform4f(colorFixedLocation, color[0], color[1], color[2], -1.0);
			        gl.vertexAttribPointer(colorVarLocation, 3, gl.FLOAT, false, buffers[0].fsize * 6, buffers[0].fsize * 3);
			        gl.enableVertexAttribArray(colorVarLocation);
					gl.drawArrays(gl.POINTS, 0, buffers[0].numItems);
				}
				else {
					gl.disableVertexAttribArray(2);
					gl.vertexAttribPointer(vertexCoordLocation, 2, gl.FLOAT, false, 0, 0);
					gl.enableVertexAttribArray(vertexCoordLocation);
					gl.uniform4f(colorFixedLocation, color[0], color[1], color[2], color[3]);
					gl.drawArrays(gl.POINTS, 0, buffers[0].numItems);
				}
			}

			function drawIndividualPoint(){
				
			}
			
			
			function drawPolygonsBorder(webgl, buffers, map, library) {

				var gl = webgl.gl;

				if (gl == null) return;
				var matrixProjection = new Float32Array(16);

				gl.clear(gl.COLOR_BUFFER_BIT);
				gl.enable(gl.BLEND);
				gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);

				var currentZoom = map.getZoom();
				var pointSize = Math.max(currentZoom - 4.0, 1.0);
				gl.vertexAttrib1f(gl.aPointSize, pointSize);

				matrixProjection.set(webgl.projection);

				var scale = Math.pow(2, currentZoom);
				library.scaleProjection(matrixProjection, scale, scale);

				var offset = library.Proj4Pixel(map.getBounds().getNorthWest().lat, map.getBounds().getNorthWest().lng);
				library.translateProjection(matrixProjection, -offset.x, -offset.y);

				var projectionLocation = gl.getUniformLocation(webgl.program, 'projection');
				gl.uniformMatrix4fv(projectionLocation, false, matrixProjection);

				/** Draw Polygons' Interior **/
				var vertexCoordLocation = gl.getAttribLocation(webgl.program, 'vertexCoord');
				var colorFixedLocation = gl.getUniformLocation(webgl.program, "u_color");

				for (var i = 0; i < buffers.length; i++) {
					gl.bindBuffer(gl.ARRAY_BUFFER, buffers[i]);
					gl.enableVertexAttribArray(vertexCoordLocation);
					gl.vertexAttribPointer(vertexCoordLocation, 2, gl.FLOAT, false, 0, 0);

					gl.uniform4f(colorFixedLocation, 0, 0, 0, 1);
					gl.drawArrays(gl.LINE_LOOP, 0, buffers[i].numItems);	
				}
			}
		}
};
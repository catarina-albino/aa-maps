package server; 

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.Cache;

import com.google.gson.Gson;


@Path("confusionmatrix")
public class ConfMatrixResource {

	public class DataResponse implements StreamingOutput {		
		private int year, gridSize, attenFunction, accumFunction, version, threshold;
		private boolean percentage;
		
		public DataResponse(int year, int gridSize, int attenFunction, 
				int accumFunction, int version, boolean percentage, int threshold) {
			super();
			this.year = year; 
			this.gridSize = gridSize;
			this.attenFunction = attenFunction;
			this.accumFunction = accumFunction;
			this.version = version;
			this.percentage = percentage;
			this.threshold = threshold;
		}

		public void write(final OutputStream os) throws IOException,
		WebApplicationException {
			Gson gson = new Gson();
			Context context = Server.context;
			Writer writer = new BufferedWriter(new OutputStreamWriter(os));
			String geojson = gson.toJson(context.getPNLoader().getConfusionMatrix(context, gridSize, year, 
					attenFunction, accumFunction, version, percentage, threshold));
			
			
			writer.write(geojson);
			writer.flush();
			writer.close();
		}
	}
	
	
	@GZIP
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Cache(maxAge = 0)
	public Response getConfusionMatrix(@QueryParam("year") final int year, @QueryParam("gridSize") final int gridSize,
			@QueryParam("attenF") final int attenF, @QueryParam("accumF") final int accumF, 
			@QueryParam("version") final int version, @QueryParam("percentage") final boolean percentage,
			@QueryParam("threshold") final int threshold) {
				
		DataResponse info = new DataResponse(year, gridSize,attenF, 
				accumF, version, percentage, threshold);
		return Response.ok(info).build();
	}
	
}

package server; 

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;

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


@Path("spatialdata")
public class SpatialDataResource {
	private Context context;

	public class DataResponse implements StreamingOutput {		
		private String posInit;
		private String posEnd;
		
		public DataResponse(String posInit,String posEnd) {
			super();
			this.posInit = posInit; 
			this.posEnd = posEnd;
		}

		public void write(final OutputStream os) throws IOException,
		WebApplicationException {

			Writer writer = new BufferedWriter(new OutputStreamWriter(os));
			String geojson = "";
			if (context.isAAMapsMode()){
				//Create All Matrices in range
				context.getLoader().createAllMatrices(context, posInit,posEnd);
				
				//Get json points for the current obs moment
				try {
					
					geojson = context.getLoader().getFinalAAMapEvents(context, posInit,posEnd);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}	
			else if(posInit.equals(posEnd))
				geojson = context.getLoader().getInstSpatialEvents(context, posInit, context.getMetric());
			else 
				/*geojson = context.getLoader().getRangeSpatialEvents(context, posInit, posEnd, 
											context.getTimeGranularity(), context.getMetric());*/
				geojson = context.getLoader().getRangeCoordSpatialEvents(context, posInit, posEnd);
			writer.write(geojson);
			writer.flush();
			writer.close();
		}
	}
	

	

	@GZIP
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Cache(maxAge = 0)
	public Response getSpatial(@QueryParam("posInit") final String posInit,
			@QueryParam("posEnd") final String posEnd, @QueryParam("tableName") final String tableName, 
			@QueryParam("timeGranularity") final String timeGranularity, @QueryParam("aamaps") final boolean aamaps,
			@QueryParam("gridSize") final int gridSize, @QueryParam("attenFunction") final int attenFunction,
			@QueryParam("accumFunction") final int accumFunction) {
		try {
			context = Server.context;
			context.setTimeGranularity(timeGranularity);
			context.setTableToRead(tableName);
			context.setGridSize(gridSize);
			context.setAAMapsMode(aamaps);

			double[] gridInfo = context.getLoader().
					getGridMetaInfo(context,context.getSpatialDataTable(), gridSize);
			context.setGridInfo(gridSize, gridInfo[0], gridInfo[1], gridInfo[2]);

			ArrayList<String> datesBD = context.getLoader().getDaysInRange(posInit, posEnd, timeGranularity);
			context.initMap(posInit, posEnd, datesBD, gridSize, attenFunction, accumFunction);
			
			DataResponse info = new DataResponse(posInit, posEnd);
			return Response.ok(info).build();
		} catch (Exception exception) {
			throw new WebApplicationException(exception);
		}
	}
}

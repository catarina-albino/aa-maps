package server; 

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.resteasy.annotations.GZIP;


@Path("timedata")
public class TimeDataResource {

	public class DataResponse implements StreamingOutput {		
		private String timeGranularity;
		
		public DataResponse(String timeGranularity) {
			super();
			this.timeGranularity = timeGranularity;
		}

		public void write(final OutputStream os) throws IOException,
		WebApplicationException {

			Writer writer = new BufferedWriter(new OutputStreamWriter(os));
			Context context = Server.context;
			String geojson = "";
			try{
				geojson = context.getLoader().getTimeRange(context, timeGranularity);
			} catch (SQLException e) {
			e.printStackTrace();
			}
		
		writer.write(geojson);
		writer.flush();
		writer.close();
	}
	}


	@GZIP
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTimeRange(@QueryParam("timeGranularity") final String timeGranularity){
		try {
			DataResponse info = new DataResponse(timeGranularity);
			return Response.ok(info).build();
		} catch (Exception exception) {
			throw new WebApplicationException(exception);
		}
	}
}

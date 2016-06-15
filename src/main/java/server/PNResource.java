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


@Path("pontosnegros")
public class PNResource {

	public class DataResponse implements StreamingOutput {		
		private String posInit;
		private String posEnd;
		private String timeGranularity;
		
		public DataResponse(String posInit,String posEnd, String timeGranularity) {
			super();
			this.posInit = posInit; 
			this.posEnd = posEnd;
			this.timeGranularity = timeGranularity;
		}

		public void write(final OutputStream os) throws IOException,
		WebApplicationException {
			Context context = Server.context;
			Writer writer = new BufferedWriter(new OutputStreamWriter(os));
			String geojson = "";
			
			if(posInit.equals(posEnd)) geojson = context.getPNLoader().getInstPN(context, posInit, timeGranularity);
			else geojson = context.getPNLoader().getAllPN(context, timeGranularity);
			
			writer.write(geojson);
			writer.flush();
			writer.close();
		}
	}
	

	@GZIP
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Cache(maxAge = 0)
	public Response getPN(@QueryParam("posInit") final String posInit, @QueryParam("posEnd") final String posEnd,
			@QueryParam("timeGranularity") final String timeGranularity) {
		DataResponse info = new DataResponse(posInit, posEnd, timeGranularity);
		return Response.ok(info).build();
	}
	
	
}

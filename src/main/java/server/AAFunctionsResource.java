package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.Cache;

import com.google.gson.Gson;

import accum_functions.IAccumFunc;
import atten_functions.IAttenFunc;


@Path("AAFunctions")
public class AAFunctionsResource {

	public class DataResponse implements StreamingOutput {		
		
		public DataResponse() {
			super();
		}

		public void write(final OutputStream os) throws IOException,
		WebApplicationException {
			
			Gson gson = new Gson();
			Context context = Server.context;
			ArrayList<IAccumFunc> accumF = context.getAAMapsMetaInfo().getAllAccumFunctions();
			ArrayList<IAttenFunc> attenF = context.getAAMapsMetaInfo().getAllAttenFunctions();
			Writer writer = new BufferedWriter(new OutputStreamWriter(os));
	
			String acc = gson.toJson(accumF);
			String att = gson.toJson(attenF);
			String json= "{\"accumFunctions\": " +acc+", \"attenFunctions\": "+att+"}";
	
			writer.write(json);
			writer.flush();
			writer.close();
		}
	}
	
	
	@GZIP
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Cache(maxAge = 0)
    public Response getFunction() {
		DataResponse info = new DataResponse();
		return Response.ok(info).build();
    }

}

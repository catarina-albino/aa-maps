package server;

import org.jboss.resteasy.jsapi.JSAPIServlet;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;

import aa_maps.AAMaps;
import core.Config;

public class Server {

	public static Context context;
	
	public static void setContextAccidentsPortugal(TJWSEmbeddedJaxrsServer webServer) {
		context = new Context("day", 1024, "accidents_portugal_1024days", true);
		context.setSpatialDataTable("accidentes_portugal_spatialgranularity");
		context.setTimeDataTable("accidentes_portugal_temporalgranularity");
	}
	
	private static void addResources(TJWSEmbeddedJaxrsServer webServer) {
		webServer.getDeployment().getRegistry().addPerRequestResource(StaticResources.class);
		webServer.getDeployment().getRegistry().addPerRequestResource(SpatialDataResource.class);
		webServer.getDeployment().getRegistry().addPerRequestResource(ContextResource.class);
		webServer.getDeployment().getRegistry().addPerRequestResource(TimeDataResource.class);
		webServer.addServlet("/rest-js", new JSAPIServlet());
	}
	
	public static void main(final String[] args) {
		TJWSEmbeddedJaxrsServer webServer = new TJWSEmbeddedJaxrsServer();
		webServer.setPort(Config.getConfigInt("server_port"));
		webServer.setRootResourcePath("/");
		webServer.start();
		context = new Context();
		setContextAccidentsPortugal(webServer);
		AAMaps map = new AAMaps();
		context.setAAMapsMetaInfo(map);
		Server.addResources(webServer);
		System.out.print("Web server started...\n");
	}
	

}

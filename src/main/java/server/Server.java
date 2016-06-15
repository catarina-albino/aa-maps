package server;

import org.jboss.resteasy.jsapi.JSAPIServlet;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;

import aa_maps.AAMaps;
import core.Config;

public class Server{

	public static Context context;
	
	public static void setContextAccidentsPortugal(TJWSEmbeddedJaxrsServer webServer) {
		context = new Context("day", 512, "accidents_portugal_512days", true);
		context.setDefaultAAMapsTable("accidents_portugal_");
		context.setDefaultTable("accidents_portugal_1minutes");
		context.setSpatialDataTable("accidents_portugal_spatialgranularity");
		context.setTimeDataTable("accidentes_portugal_temporalgranularity");
		context.setDefaultPNFTable("pontos_negros_janela_fixa");
		context.setEffectMetric("ig");
	}
	
	
	public static void setContextFiresPortugal(TJWSEmbeddedJaxrsServer webServer) {
		context = new Context("day", 512, "fires_portugal", true);
		context.setDefaultAAMapsTable("fires_portugal_");
		context.setDefaultTable("fires_portugal");
		context.setSpatialDataTable("fires_portugal_spatialgranularity");
		context.setTimeDataTable("fires_portugal_temporalgranularity");
		context.setEffectMetric("area_ardida_ha");
	}
	

	private static void addResources(TJWSEmbeddedJaxrsServer webServer) {
		webServer.getDeployment().getRegistry().addPerRequestResource(StaticResources.class);
		webServer.getDeployment().getRegistry().addPerRequestResource(SpatialDataResource.class);
		webServer.getDeployment().getRegistry().addPerRequestResource(ContextResource.class);
		webServer.getDeployment().getRegistry().addPerRequestResource(TimeDataResource.class);
		webServer.getDeployment().getRegistry().addPerRequestResource(AAFunctionsResource.class);
		webServer.getDeployment().getRegistry().addPerRequestResource(ConfMatrixResource.class);
		webServer.getDeployment().getRegistry().addPerRequestResource(PNResource.class);
		webServer.getDeployment().getRegistry().addPerRequestResource(SpatialGrainResource.class);
		webServer.addServlet("/rest-js", new JSAPIServlet());
	}
	
		public static void main(final String[] args) {
		TJWSEmbeddedJaxrsServer webServer = new TJWSEmbeddedJaxrsServer();
		webServer.setPort(Config.getConfigInt("server_port"));
		webServer.setRootResourcePath("/");
		webServer.start();
		context = new Context();
		setContextAccidentsPortugal(webServer);
		//setContextFiresPortugal(webServer);
		AAMaps map = new AAMaps();
		context.setAAMapsMetaInfo(map);
		Server.addResources(webServer);
		System.out.print("Web server started...\n");
	}
}

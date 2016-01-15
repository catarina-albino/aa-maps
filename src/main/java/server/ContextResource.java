package server; 

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("context")
public class ContextResource {

	@POST
	public void setContext(@QueryParam("isRestricted") final boolean isRestricted,
			@QueryParam("dataset") final String mainDataset, 
			@QueryParam("tableName") final String tableName, 
			@QueryParam("timeGranularity") final String timeGranularity, 
			@QueryParam("aamaps") final boolean aamaps,
			@QueryParam("gridSize") final int gridSize, 
			@QueryParam("attenFunction") final int attenFunction) {
		  //@QueryParam("geometry") final String geometry
		
		Context context = Server.context;
		context.setRestricted(isRestricted);
		Server.context.setTableToRead(mainDataset);
		context.setTimeGranularity(timeGranularity);
		context.setTableToRead(tableName);
		context.setGridSize(gridSize);
		context.setAttenFunction(attenFunction);
		System.out.println("ooookk");
		//context.setAccumFunction();
		
		double[] gridInfo = context.getLoader().
				getGridMetaInfo(context,context.getSpatialDataTable(), gridSize);
		context.setGridInfo(gridSize, gridInfo[0], gridInfo[1], gridInfo[2]);

		/*if(!geometry.equals("null")) {
			System.out.println(geometry);
			String[] coordinates = geometry.split(",");
			Coordinate[] coords = new Coordinate[coordinates.length/2];

			int j = 0;
			for (int i = 0; i < coordinates.length; i+=2) {
				coords[j] = new Coordinate(Double.parseDouble(coordinates[i]),Double.parseDouble(coordinates[i+1]));
				j++;
			}
		}*/
		System.out.println("context updated: ");
	}
}

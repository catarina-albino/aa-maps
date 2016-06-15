package DB_Loaders;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import core.Config;
import core.load_data.DataStoreInfo;
import core.shared.Column;
import core.shared.Table;


public class FiresPortugalGrid {

	private static final long BATCHINSERT_SIZE = Config.getConfigInt("insert_chunk_size");

	private static String insertStatement;
	private static List<String> files = new ArrayList<String>();

	private static CellProcessor[] getProcessors() {					
		final CellProcessor[] processors = new CellProcessor[] { 
				new NotNull(), //geometry
				new NotNull(), //time
				new NotNull(), //area_ardida	
				new NotNull(), //f

		};
		return processors;
	}

	
	public static void createTable(String name) {
		Table firesPortugal = new Table(name, "id");
		firesPortugal.add(new Column("id", false, false, "INT"));
		firesPortugal.add(new Column("time", false, false, "TEXT"));
		firesPortugal.add(new Column("date", false, false, "TEXT"));
		firesPortugal.add(new Column("latitude", false, false, "NUMERIC"));
		firesPortugal.add(new Column("longitud", false, false, "NUMERIC"));
		firesPortugal.add(new Column("area_ardida_ha", false, false, "NUMERIC"));
		firesPortugal.add(new Column("f", false, false, "NUMERIC"));
		Connection connection = DataStoreInfo.getMetaStore();
		firesPortugal.createTable(connection);

		insertStatement = firesPortugal.insertStatement();
		System.out.println(insertStatement);
	}


	public static void insertGridData(String fileName) throws Exception {
		Connection connection = DataStoreInfo.getMetaStore();
		ICsvMapReader mapReader = null;
		try {
			PreparedStatement ps = connection.prepareStatement(insertStatement); 
			mapReader = new CsvMapReader(new FileReader(fileName), CsvPreference.STANDARD_PREFERENCE);
			
			// the header columns are used as the keys to the Map
			final String[] header = mapReader.getHeader(true);
			final CellProcessor[] processors = getProcessors();

			Map<String, Object> line;
			int batchCount = 0;

			while( (line = mapReader.read(header, processors)) != null ) {
				String datahora = (String) line.get("time");
				String date = datahora.split(" ")[0];	
				String[] date2 = date.split("-");
				
				String area = (String) line.get("area_ardida");
				String f = (String) line.get("f");
				
				String spatialText= (String) line.get("geometry");
				spatialText = spatialText.replace("POINT", "");
				String[] spatialInfo = spatialText.split(" ");
				
				String latitude= spatialInfo[0].replace("(", "");
				String longitude = spatialInfo[1].replace(")", "");
				
				ps.setString(1, date2[2]+"/"+date2[1]+"/"+date2[0]+" "+datahora.split(" ")[1]);
				ps.setString(2, date2[0]+"-"+date2[1]+"-"+date2[2]);
				ps.setDouble(3, Double.parseDouble(longitude));
				ps.setDouble(4, Double.parseDouble(latitude));
				
				ps.setDouble(5, (Double.parseDouble(area)*Integer.parseInt(f)));
				ps.setInt(6, Integer.parseInt(f));

				ps.addBatch();
				if (batchCount == BATCHINSERT_SIZE) {
					ps.executeBatch();
					batchCount = 0;
				} else
					batchCount++;
			}
			ps.executeBatch();
		}
		finally {
			if( mapReader != null ) {
				mapReader.close();
			}
		}
	}
	
	public static void getDatasetFiles(final File folder) {
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	        	getDatasetFiles(fileEntry);
	        } else {
	            System.out.println(fileEntry.getName());
	            files.add(fileEntry.getName());
	        }
	    }
	}

	public static void main(String[] args) {
		final File folder = new File("data/grid");
		getDatasetFiles(folder);

		for (int i=0; i< files.size(); i++){
			String file = files.get(i);
			FiresPortugalGrid.createTable("fires_portugal"+file.substring(file.lastIndexOf('_'), file.lastIndexOf('.'))+"s");
			try {
				FiresPortugalGrid.insertGridData("data/grid/"+file);
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
	}
}

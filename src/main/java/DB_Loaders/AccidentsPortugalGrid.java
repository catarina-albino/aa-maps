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


public class AccidentsPortugalGrid {

	private static final long BATCHINSERT_SIZE = Config.getConfigInt("insert_chunk_size");

	private static String insertStatement;
	private static List<String> files = new ArrayList<String>();

	private static CellProcessor[] getProcessors() {					
		final CellProcessor[] processors = new CellProcessor[] { 
				new NotNull(), //Datahora
				new NotNull(), //Latitude / Longitude
				new NotNull(), //Tipos_vias	
				new NotNull(), //F_atmosfericos	
				new NotNull(), //Fleves	
				new NotNull(), //Fgraves	
				new NotNull(), //Mortos	
				new NotNull(), //F
		};
		return processors;
	}

	private static int calcIG(int FL, int FG, int VM, int freq){
		return (3 * FL + 10 * FG  + 100 * VM ) * freq;
	}
	
	
	public static void createTable(String name) {
		Table accPortugal = new Table(name, "id");
		accPortugal.add(new Column("id", false, false, "INT"));
		accPortugal.add(new Column("time", false, false, "TEXT"));
		accPortugal.add(new Column("date", false, false, "TEXT"));
		accPortugal.add(new Column("latitude", false, false, "NUMERIC"));
		accPortugal.add(new Column("longitud", false, false, "NUMERIC"));
		accPortugal.add(new Column("tipos_vias", false, false, "TEXT"));
		accPortugal.add(new Column("f_atmosfericos", false, false, "TEXT"));
		accPortugal.add(new Column("fleves", false, false, "NUMERIC"));
		accPortugal.add(new Column("fgraves", false, false, "NUMERIC"));
		accPortugal.add(new Column("mortos", false, false, "NUMERIC"));
		accPortugal.add(new Column("ig", false, false, "NUMERIC"));
		accPortugal.add(new Column("f", false, false, "NUMERIC"));
		Connection connection = DataStoreInfo.getMetaStore();
		accPortugal.createTable(connection);

		insertStatement = accPortugal.insertStatement();
		System.out.println(insertStatement);
	}


	public static void insertGridData(String fileName) throws Exception {
		Connection connection = DataStoreInfo.getMetaStore();
		ICsvMapReader mapReader = null;
		try {
			PreparedStatement ps = connection.prepareStatement(insertStatement); 
			mapReader = new CsvMapReader(new FileReader(fileName), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
			
			// the header columns are used as the keys to the Map
			final String[] header = mapReader.getHeader(true);
			final CellProcessor[] processors = getProcessors();

			Map<String, Object> line;
			int batchCount = 0;

			while( (line = mapReader.read(header, processors)) != null ) {
				String datahora = (String) line.get("time");
				String date = datahora.split(" ")[0];
				
				String[] date2 = date.split("/");
				
				String spatialText= (String) line.get("st_astext");
				spatialText = spatialText.replace("POINT", "");
				String[] spatialInfo = spatialText.split(" ");
				
				String latitude= spatialInfo[0].replace(",", ".").replace("(", "");
				String longitude = spatialInfo[1].replace(",", ".").replace(")", "");

				String vias = (String) line.get("tipos_vias");
				String f_atmosf = (String) line.get("f_atmosfericos");
				String fleves = (String) line.get("fleves");
				String fgraves = (String) line.get("fgraves");
				String mortos = (String) line.get("mortos");
				String f = (String) line.get("f");
				
				int IG = calcIG(Integer.parseInt(fleves), Integer.parseInt(fgraves), 
						Integer.parseInt(mortos), Integer.parseInt(f));
				
				ps.setString(1, datahora);
				ps.setString(2, date2[2]+"/"+date2[1]+"/"+date2[0]);
				ps.setDouble(3, Double.parseDouble(longitude));
				ps.setDouble(4, Double.parseDouble(latitude));
				ps.setString(5, vias);
				ps.setString(6, f_atmosf);
				ps.setInt(7, Integer.parseInt(fleves));
				ps.setInt(8, Integer.parseInt(fgraves));
				ps.setInt(9, Integer.parseInt(mortos));
				ps.setInt(10, IG);
				ps.setInt(11, Integer.parseInt(f));

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
			AccidentsPortugalGrid.createTable(file.substring(0, file.lastIndexOf('.')));
			try {
				AccidentsPortugalGrid.insertGridData("data/grid/"+file);
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
	}
}

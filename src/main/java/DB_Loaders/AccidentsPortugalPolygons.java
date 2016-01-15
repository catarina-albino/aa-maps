package DB_Loaders;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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


public class AccidentsPortugalPolygons {

	private static final long BATCHINSERT_SIZE = Config.getConfigInt("insert_chunk_size");
	private static List<String> files = new ArrayList<String>();

	private static CellProcessor[] getProcessors() {					
		final CellProcessor[] processors = new CellProcessor[] { 
				new NotNull(), //Geometry
				new NotNull(), //Name
				new NotNull(), //Time
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
	
	private static String getGeometryTable(String name){
		if (name.contains("distritos")) return "distritos_portugal(id)";
		else if (name.contains("concelhos")) return "concelhos_portugal(id)";
		else return "freguesias_portugal(id)";
	}
	
	public static void createTable(String name) {
		Connection connection = DataStoreInfo.getMetaStore();
		java.sql.Statement stmt;
		try {
			String geomTable = getGeometryTable(name);
			stmt = connection.createStatement();
			String sql1 = "DROP TABLE IF EXISTS "+name+";";
			String sql2 = "CREATE TABLE "+ name + " ("+
					"geometry INTEGER references "+geomTable+","+
					"name TEXT,"+
					"time TEXT,"+
					"date TEXT,"+
					"tipos_vias TEXT,"+
					"f_atmosfericos TEXT,"+
					"fleves INTEGER,"+
					"fgraves INTEGER,"+
					"mortos INTEGER,"+
					"ig INTEGER,"+
					"f INTEGER);";
	    	stmt.executeUpdate(sql1);
	    	System.out.println(sql1);
	    	stmt.executeUpdate(sql2);
	    	System.out.println(sql2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public static void insertPolygonData(String fileName, String table) throws Exception {
		Connection connection = DataStoreInfo.getMetaStore();
		ICsvMapReader mapReader = null;
		try {
			String sql = "INSERT INTO " + table +" (geometry,name,time,date,tipos_vias,f_atmosfericos,fleves,fgraves,mortos,ig,f)"+
					" VALUES (?,?,?,?,?,?,?,?,?,?,?);";
			PreparedStatement ps = connection.prepareStatement(sql);
			mapReader = new CsvMapReader(new FileReader(fileName), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
			
			// the header columns are used as the keys to the Map
			final String[] header = mapReader.getHeader(true);
			final CellProcessor[] processors = getProcessors();

			Map<String, Object> line;
			int batchCount = 0;

			while( (line = mapReader.read(header, processors)) != null ) {
				String geometry = (String) line.get("geometry");
				String name = (String) line.get("name");
				String datahora = (String) line.get("time");
				String date = datahora.split(" ")[0];
				String[] date2 = date.split("/");

				String vias = (String) line.get("tipos_vias");
				String f_atmosf = (String) line.get("f_atmosfericos");
				String fleves = (String) line.get("fleves");
				String fgraves = (String) line.get("fgraves");
				String mortos = (String) line.get("mortos");
				String f = (String) line.get("f");
				
				int IG = calcIG(Integer.parseInt(fleves), Integer.parseInt(fgraves), 
						Integer.parseInt(mortos), Integer.parseInt(f));
				
				ps.setInt(1, Integer.parseInt(geometry));
				ps.setString(2, name);
				ps.setString(3, datahora);
				ps.setString(4, date2[2]+"/"+date2[1]+"/"+date2[0]);
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
		}catch (SQLException e) {
		    for (Throwable ex = e; ex != null; ex = e.getCause())
		        ex.printStackTrace();
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
		final File folder = new File("data/polygon");
		getDatasetFiles(folder);

		for (int i=0; i< files.size(); i++){
			String file = files.get(i);
			String tableName = file.substring(0, file.lastIndexOf('.'));
			AccidentsPortugalPolygons.createTable(tableName);
			try {
				AccidentsPortugalPolygons.insertPolygonData("data/polygon/"+file, tableName);
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
	}
}

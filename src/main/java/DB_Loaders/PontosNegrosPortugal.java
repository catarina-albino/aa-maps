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


public class PontosNegrosPortugal {

	private static final long BATCHINSERT_SIZE = Config.getConfigInt("insert_chunk_size");

	private static String insertStatement;
	private static List<String> files = new ArrayList<String>();

	private static CellProcessor[] getProcessors() {					
		final CellProcessor[] processors = new CellProcessor[] { 
				new NotNull(), 
				new NotNull(), 
				new NotNull(),	
				new NotNull(), 
				new NotNull(), 
				new NotNull(), 	
				new NotNull(), 
				new NotNull(), 
				new NotNull(), 
		};
		return processors;
	}
	
	public static void createTable(String name) {
		Table accPortugal = new Table(name, "id");
		accPortugal.add(new Column("id", false, false, "NUMERIC"));
		accPortugal.add(new Column("id_ano", false, false, "NUMERIC"));
		accPortugal.add(new Column("ano", false, false, "NUMERIC"));
		accPortugal.add(new Column("latitude", false, false, "NUMERIC"));
		accPortugal.add(new Column("longitud", false, false, "NUMERIC"));
		accPortugal.add(new Column("via", false, false, "TEXT"));
		accPortugal.add(new Column("tipo_via", false, false, "NUMERIC"));
		accPortugal.add(new Column("acidentes", false, false, "NUMERIC"));
		accPortugal.add(new Column("extensao_espacial", false, false, "NUMERIC"));
		Connection connection = DataStoreInfo.getMetaStore();
		accPortugal.createTable(connection);

		insertStatement = accPortugal.insertStatement();
		System.out.println(insertStatement);
	}


	public static void insertData(String fileName) throws Exception {
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
				
				//String id = (String) line.get("INDICE_PN");
				String id_ano = (String) line.get("INDICE_PN_ANO");
				String ano = (String) line.get("ANO");
				String latitude = (String) line.get("LATITUDE");
				latitude = latitude.replace(",", ".");
				String longitude = (String) line.get("LONGITUDE");
				longitude = longitude.replace(",", ".");
				String tipo_via = (String) line.get("TIPO_VIA");
				String via = (String) line.get("VIA");
				String acidentes = (String) line.get("AC");
				String extensao_espacial = (String) line.get("EXTENSAO_ESPACIAL");
				
				//ps.setInt(1, Integer.parseInt(id));
				ps.setInt(1, Integer.parseInt(id_ano));
				ps.setInt(2, Integer.parseInt(ano));
				ps.setDouble(3, Double.parseDouble(latitude));
				ps.setDouble(4, Double.parseDouble(longitude));
				ps.setString(5, via);
				ps.setInt(6, Integer.parseInt(tipo_via));
				ps.setInt(7, Integer.parseInt(acidentes));
				ps.setInt(8, Integer.parseInt(extensao_espacial));
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
		String fileName = "data/PontosNegrosJanelaFixa.csv";
		PontosNegrosPortugal.createTable("pontos_negros_janela_fixa");
		try {
			PontosNegrosPortugal.insertData(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

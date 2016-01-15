package DB_Loaders;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import core.Config;
import core.load_data.DataStoreInfo;
import core.shared.Column;
import core.shared.Table;


public class AccidentsPortugal {

	private static final long BATCHINSERT_SIZE = Config.getConfigInt("insert_chunk_size");

	private static String insertStatement;

	private static CellProcessor[] getProcessors() {
												
		
		final CellProcessor[] processors = new CellProcessor[] { 
				new NotNull(), // Id__Aciden
				new NotNull(), // Datahora
				new NotNull(), // Ano
				new NotNull(), // Nr_Process
				new NotNull(), // Nordem
				new StrNotNullOrEmpty(), // Entidades
				new NotNull(), // Codposto
				new NotNull(), // Localizaco
				new NotNull(), // Distrito
				new NotNull(), // Distrito2
				new NotNull(), //Concelho
				new NotNull(), //Concelho2
				new Optional(), //COD_Distrito
				new Optional(), //COD_Concelho
				new Optional(), //COD_Freguesia
				new NotNull(), //Freguesia3
				new NotNull(), //Freguesia2
				new NotNull(), //Pov__Proxi
				new NotNull(), //Latitude
				new NotNull(), //Longitude
				new NotNull(), //Tipos_Vias
				new NotNull(), //Tipos_Vias2
				new NotNull(), //Cod_Via
				new NotNull(), //Cod_Via2
				new NotNull(), //Km
				new NotNull(), //Km2
				new NotNull(), //N_arruamen
				new NotNull(), //N_arruam_1
				new NotNull(), //Sentidos
				new NotNull(), //Fgraves								
				new NotNull(), //Fleves
				new NotNull(), //Mortos
				new NotNull(), //Natureza
				new NotNull(), //N_Ciclomot
				new NotNull(), //N_Ligeiros
				new NotNull(), //N_Outros__
				new NotNull(), //N_Pesados
				new NotNull(), //Mortos_30		
				new NotNull(), //FGraves30
				new NotNull(), //FLeves30
				new NotNull(), //F_Atmosfericos
				new NotNull(), //C_Luminiosidade	
		};

		return processors;
	}


	public static void createTable(String tableName) {

		Table accPortugal = new Table(tableName, "pk_id");

		accPortugal.add(new Column("pk_id", false, false, "NUMERIC"));
		accPortugal.add(new Column("id_accident", false, false, "TEXT"));
		
		accPortugal.add(new Column("time", false, false, "TEXT"));
		
		accPortugal.add(new Column("year", false, false, "NUMERIC"));
		accPortugal.add(new Column("month", false, false, "NUMERIC"));
		accPortugal.add(new Column("day", false, false, "NUMERIC"));
		accPortugal.add(new Column("hour", false, false, "NUMERIC"));
		accPortugal.add(new Column("minute", false, false, "NUMERIC"));
		
		accPortugal.add(new Column("nr_process", false, false, "TEXT"));
		accPortugal.add(new Column("nr_ordem", false, false, "TEXT"));
		
		accPortugal.add(new Column("entidades", false, false, "TEXT"));
		accPortugal.add(new Column("codposto", false, false, "TEXT"));
		accPortugal.add(new Column("localizaco", false, false, "TEXT"));
		accPortugal.add(new Column("distrito", false, false, "TEXT"));
		accPortugal.add(new Column("distrito2", false, false, "TEXT"));
		accPortugal.add(new Column("concelho", false, false, "TEXT"));	
		accPortugal.add(new Column("concelho2", false, false, "TEXT"));

		accPortugal.add(new Column("COD_distrito", false, false, "TEXT"));
		accPortugal.add(new Column("COD_concelho", false, false, "TEXT"));
		accPortugal.add(new Column("COD_freguesia", false, false, "TEXT"));
		accPortugal.add(new Column("freguesia3", false, false, "TEXT"));
		accPortugal.add(new Column("freguesia2", false, false, "TEXT"));	
		accPortugal.add(new Column("pov__proxi", false, false, "TEXT"));
		
		accPortugal.add(new Column("latitude", false, false, "NUMERIC"));
		accPortugal.add(new Column("longitud", false, false, "NUMERIC"));
		accPortugal.add(new Column("tipos_vias", false, false, "TEXT"));
		accPortugal.add(new Column("tipos_vias2", false, false, "TEXT"));

		accPortugal.add(new Column("cod_via", false, false, "TEXT"));
		accPortugal.add(new Column("cod_via2", false, false, "TEXT"));
		accPortugal.add(new Column("m", false, false, "NUMERIC"));
		accPortugal.add(new Column("km2", false, false, "NUMERIC"));
		accPortugal.add(new Column("n_arruamen", false, false, "TEXT"));
		accPortugal.add(new Column("n_arruam_1", false, false, "TEXT"));
		accPortugal.add(new Column("sentidos", false, false, "TEXT"));
		
		accPortugal.add(new Column("fgraves", false, false, "NUMERIC"));
		accPortugal.add(new Column("fleves", false, false, "NUMERIC"));
		accPortugal.add(new Column("mortos", false, false, "NUMERIC"));
		accPortugal.add(new Column("ig", false, false, "NUMERIC"));
		
		accPortugal.add(new Column("natureza", false, false, "TEXT"));

		accPortugal.add(new Column("n_ciclomot", false, false, "NUMERIC"));
		accPortugal.add(new Column("n_ligeiros", false, false, "NUMERIC"));
		accPortugal.add(new Column("n_outros", false, false, "NUMERIC"));
		accPortugal.add(new Column("n_pesados", false, false, "NUMERIC"));
		
		accPortugal.add(new Column("mortes30", false, false, "NUMERIC"));
		accPortugal.add(new Column("fGraves30", false, false, "NUMERIC"));
		accPortugal.add(new Column("fLeves30", false, false, "NUMERIC"));
		accPortugal.add(new Column("f_atmosfericos", false, false, "TEXT"));
		accPortugal.add(new Column("c_luminiosidade", false, false, "TEXT"));
		accPortugal.add(new Column("f", false, false, "NUMERIC"));
		accPortugal.add(new Column("date", false, false, "TEXT"));
		
		Connection connection = DataStoreInfo.getMetaStore();
		accPortugal.createTable(connection);

		insertStatement = accPortugal.insertStatement();
		System.out.println(insertStatement);
	}


	public static void readAndWriteWithCsvMapReader(String fileName) throws Exception {
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

				
				// Obter informacao
				String id = (String) line.get("Id__Aciden");
				ps.setInt(1, Integer.parseInt(id));
				
				String datahora = (String) line.get("Datahora");
				
				
				String[] temp = datahora.split(" ");
				String[] tempfirst = temp[0].split(":");
				String[] tempSecond = temp[1].split(":");
				
				String date = tempfirst[0]+"/"+tempfirst[1]+"/"+tempfirst[2];
				
				
				ps.setString(2, datahora);
				ps.setInt(3, Integer.parseInt(tempfirst[0]));
				ps.setInt(4, Integer.parseInt(tempfirst[1]));
				ps.setInt(5, Integer.parseInt(tempfirst[2]));
				ps.setInt(6, Integer.parseInt(tempSecond[0]));
				ps.setInt(7, Integer.parseInt(tempSecond[1]));
				
				String nrproces = (String) line.get("Nr_Process");
				String nordem= (String) line.get("Nordem");

				String entidades  = (String) line.get("Entidades");
				String codposto = (String) line.get("Codposto");
				String localizacao = (String) line.get("Localizaco");
				String distrito = (String) line.get("Distrito");
				String distrito2 = (String) line.get("Distrito2");

				String concelho = (String) line.get("Concelho");
				String concelho2 = (String) line.get("Concelho2");
				
				ps.setString(8, nrproces);
				ps.setString(9, nordem);
				
				ps.setString(10, entidades);
				ps.setString(11, codposto);
				ps.setString(12, localizacao);
				ps.setString(13, distrito);
				ps.setString(14, distrito2);
				ps.setString(15, concelho);
				ps.setString(16, concelho2);
				
				String coddistrito = (String) line.get("COD_Distrito");
				String codconcelho = (String) line.get("COD_Concelho");
				String codfreguesia = (String) line.get("COD_Freguesia");
				String freguesia3 = (String) line.get("Freguesia3");
				String freguesia2 = (String) line.get("Freguesia2");
				String pov_proxi = (String) line.get("Pov__Proxi");
				
				ps.setString(17, coddistrito);
				ps.setString(18, codconcelho);
				ps.setString(19, codfreguesia);
				ps.setString(20, freguesia3);
				ps.setString(21, freguesia2);
				ps.setString(22, pov_proxi);
				
				String latitude = (String) line.get("Latitude");
				latitude = latitude.replace(",", ".");
				String longitude = (String) line.get("Longitude");
				longitude = longitude.replace(",", ".");
				
				ps.setDouble(23, Double.parseDouble(latitude));
				ps.setDouble(24, Double.parseDouble(longitude));
				
				String tipovias = (String) line.get("Tipos_Vias");
				String tipovias2 = (String) line.get("Tipos_Vias2");
				
				ps.setString(25, tipovias);
				ps.setString(26, tipovias2);

				String codvia = (String) line.get("Cod_Via");
				String codvia2 = (String) line.get("Cod_Via2");
				
				ps.setString(27, codvia);
				ps.setString(28, codvia2);
				
				String km = (String) line.get("Km");
				km = km.replace(",", ".");
				String km2 = (String) line.get("Km2");
				km2 = km2.replace(",", ".");
				
				ps.setDouble(29, Double.parseDouble(km));
				ps.setDouble(30, Double.parseDouble(km2));
				
				String nrarruamen = (String) line.get("N_arruamen");
				String nrarruamen1 = (String) line.get("N_arruam_1");
				String sentidos = (String) line.get("Sentidos");
				
				ps.setString(31, nrarruamen);
				ps.setString(32, nrarruamen1);
				ps.setString(33, sentidos);
				
				
				String fgraves = (String) line.get("Fgraves");
				String fleves = (String) line.get("Fleves");
				String mortos = (String) line.get("Mortos");
				String natureza = (String) line.get("Natureza");
				String nciclomot = (String) line.get("N_Ciclomot");
				String nligeiros = (String) line.get("N_Ligeiros");
				String noutros = (String) line.get("N_Outros__");
				String npesados = (String) line.get("N_Pesados");
				
				int IG = calcIG(Integer.parseInt(fleves), Integer.parseInt(fgraves), Integer.parseInt(mortos));
				
				ps.setInt(34, Integer.parseInt(fgraves));
				ps.setInt(35, Integer.parseInt(fleves));
				ps.setInt(36, Integer.parseInt(mortos));
				
				ps.setInt(37, IG);
				
				ps.setString(38, natureza);
				ps.setInt(39, Integer.parseInt(nciclomot));
				ps.setInt(40, Integer.parseInt(nligeiros));
				ps.setInt(41, Integer.parseInt(noutros));
				ps.setInt(42, Integer.parseInt(npesados));
				
				
				String mortos30 = (String) line.get("Mortos_30");
				String fgraves30 = (String) line.get("FGraves30");
				String fleves30 = (String) line.get("FLeves30");
				
				ps.setInt(43, Integer.parseInt(mortos30));
				ps.setInt(44, Integer.parseInt(fgraves30));
				ps.setInt(45, Integer.parseInt(fleves30));
				
				String fatmosfericos = (String) line.get("F_Atmosfericos");
				String cluminosidade = (String) line.get("C_Luminiosidade");
				
				ps.setString(46, fatmosfericos);
				ps.setString(47, cluminosidade);
				ps.setInt(48, 1); //Frequencia
				ps.setString(49, date);

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

	
	private static int calcIG(int FL, int FG, int VM){
		return 3 * FL + 10 * FG + 100 * VM;
	}
	
	
	public static void main(String[] args) {
		String fileName = "data/accidentsPortugal.csv";
		AccidentsPortugal.createTable("accidents_portugal_1minutes");
		try {
			AccidentsPortugal.readAndWriteWithCsvMapReader(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

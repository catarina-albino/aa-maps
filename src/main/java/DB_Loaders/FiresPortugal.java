package DB_Loaders;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import core.Config;
import core.load_data.DataStoreInfo;
import core.shared.Column;
import core.shared.Table;


public class FiresPortugal {

	private static final long BATCHINSERT_SIZE = Config.getConfigInt("insert_chunk_size");

	private static String insertStatement;

	private static CellProcessor[] getProcessors() {

		final CellProcessor[] processors = new CellProcessor[] { 
				new UniqueHashCode(), // codigo
				new NotNull(), // tipo
				new NotNull(), // distrito
				new NotNull(), // concelho 
				new NotNull(), // freguesia
				new NotNull(), // local
				new NotNull(), // INE
				new NotNull(), // Latitude
				new NotNull(), // Longitude
				new NotNull(), //data de extincao
				new NotNull(), //hora de extincao
				new NotNull(), //data interv
				new NotNull(), //hora interv
				new NotNull(), //fonte alerta
				new NotNull(), //NUT
				new NotNull(), //AA_apovoamento
				new NotNull(), //AA_mato
				new NotNull(), //AA_agricola
				new NotNull(), //AA_florestal
				new NotNull(), //AA_total
				new NotNull(), //reacendimento
				new NotNull(), //queimada
				new NotNull(), //falsoalarme
				new NotNull(), //fogacho
				new NotNull(), //incendio
				new NotNull(), //agricola
				new NotNull(), // perimetro
				new NotNull(), // aps
				new NotNull(), // causa
				new NotNull(), 
				new NotNull(), 
				new NotNull(), 
				new Optional(), 
				new Optional(), 
				new Optional(), 
				new Optional(), 
				new NotNull(), 
				new NotNull(), 
				new Optional(),
		};
		return processors;
	}


	public static void createTable(String tableName) {

		Table firesPortugal = new Table(tableName, "pk_id");

		firesPortugal.add(new Column("pk_id", false, false, "NUMERIC"));
		firesPortugal.add(new Column("codigo", false, false, "TEXT"));
		firesPortugal.add(new Column("tipo", false, false, "TEXT"));
		firesPortugal.add(new Column("distrito", false, false, "TEXT"));
		firesPortugal.add(new Column("concelho", false, false, "TEXT"));
		firesPortugal.add(new Column("freguesia", false, false, "TEXT"));
		firesPortugal.add(new Column("local", false, false, "TEXT"));
		firesPortugal.add(new Column("ine", false, false, "NUMERIC"));
		firesPortugal.add(new Column("latitude", false, false, "NUMERIC"));
		firesPortugal.add(new Column("longitud", false, false, "NUMERIC"));	

		firesPortugal.add(new Column("year", false, false, "NUMERIC"));
		firesPortugal.add(new Column("month", false, false, "NUMERIC"));
		firesPortugal.add(new Column("day", false, false, "NUMERIC"));
		firesPortugal.add(new Column("hour", false, false, "NUMERIC"));
		firesPortugal.add(new Column("minute", false, false, "NUMERIC"));
		firesPortugal.add(new Column("date", false, false, "TEXT"));

		firesPortugal.add(new Column("data_extincao", false, false, "TEXT"));
		firesPortugal.add(new Column("hora_extincao", false, false, "TEXT"));
		firesPortugal.add(new Column("data_interv", false, false, "TEXT"));
		firesPortugal.add(new Column("hora_interv", false, false, "TEXT"));
		firesPortugal.add(new Column("fonte_alerta", false, false, "TEXT"));		
		firesPortugal.add(new Column("NUT", false, false, "TEXT"));

		firesPortugal.add(new Column("aa_apovoamento", false, false, "NUMERIC"));
		firesPortugal.add(new Column("aa_mato", false, false, "NUMERIC"));
		firesPortugal.add(new Column("aa_agricola", false, false, "NUMERIC"));
		firesPortugal.add(new Column("aa_florestal", false, false, "NUMERIC"));
		firesPortugal.add(new Column("aa_total", false, false, "NUMERIC"));

		firesPortugal.add(new Column("reacendimento", false, false, "boolean"));
		firesPortugal.add(new Column("queimada", false, false, "BOOLEAN"));
		firesPortugal.add(new Column("falsoalarme", false, false, "BOOLEAN"));
		firesPortugal.add(new Column("fogacho", false, false, "BOOLEAN"));
		firesPortugal.add(new Column("incendio", false, false, "BOOLEAN"));
		firesPortugal.add(new Column("agricola", false, false, "BOOLEAN"));

		firesPortugal.add(new Column("perimetro", false, false, "TEXT"));
		firesPortugal.add(new Column("aps", false, false, "TEXT"));
		firesPortugal.add(new Column("causa", false, false, "TEXT"));
		firesPortugal.add(new Column("tipocausa", false, false, "TEXT"));

		Connection connection = DataStoreInfo.getMetaStore();
		
		firesPortugal.createTable(connection);

		insertStatement = firesPortugal.insertStatement();
		System.out.println(insertStatement);
	}
	

	public static void readAndWriteWithCsvMapReader(String fileName) throws Exception {
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
				String codigo = (String) line.get("codigo");
				String tipo = (String) line.get("tipo");
				String distrito = (String) line.get("distrito");
				String concelho = (String) line.get("concelho");
				String freguesia = (String) line.get("freguesia");
				String local = (String) line.get("local");

				int ine = Integer.parseInt((String)line.get("ine"));

				double longitud = Double.parseDouble((String)line.get("longitud"));
				double latitude = Double.parseDouble((String)line.get("latitude"));
				
				int year = Integer.parseInt((String)line.get("year"));
				int month = Integer.parseInt((String)line.get("month"));
				int day = Integer.parseInt((String)line.get("day"));
				int hour = Integer.parseInt((String)line.get("hour"));
				int minute = Integer.parseInt((String)line.get("minute"));
				
				String monthStr = month + "", dayStr = day + "";
				if (month < 10) monthStr = "0" + month;
				if (day < 10) dayStr = "0" + day;

				String dataExtincao = (String) line.get("data_extincao");
				String horaExtincao = (String) line.get("hora_extincao");
				String dataInterv = (String) line.get("data_interv");
				String horaInterv = (String) line.get("hora_interv");
				String fonteAlerta = (String) line.get("fonte_alerta");

				String nut = (String) line.get("nut");

				double aa_apovoamento = Double.parseDouble((String) line.get("aa_apovoamento"));
				double aa_mato = Double.parseDouble((String) line.get("aa_mato"));
				double aa_agricola = Double.parseDouble((String) line.get("aa_agricola"));
				double aa_espacoflorestal = Double.parseDouble((String) line.get("aa_florestal"));
				double aa_total = Double.parseDouble((String) line.get("aa_total"));

				boolean reacendimento = Boolean.parseBoolean((String) line.get("reacendimento"));
				boolean queimada = Boolean.parseBoolean((String) line.get("queimada"));
				boolean falsoAlarme = Boolean.parseBoolean((String)line.get("falsoalarme"));
				boolean fogacho = Boolean.parseBoolean((String) line.get("fogacho"));
				boolean incendio = Boolean.parseBoolean((String) line.get("incendio"));
				boolean agricola = Boolean.parseBoolean((String) line.get("agricola"));

				String perimetro = (String) line.get("perimetro");
				String aps = (String) line.get("aps");
				String causa = (String) line.get("causa");
				String tipoCausa = (String) line.get("tipocausa");

				ps.setString(1, codigo);
				ps.setString(2, tipo);
				ps.setString(3, distrito);
				ps.setString(4, concelho);
				ps.setString(5, freguesia);
				ps.setString(6, local);
				ps.setInt(7, ine);
				ps.setDouble(8, latitude);
				ps.setDouble(9, longitud);

				ps.setInt(10, year);
				ps.setInt(11, month);
				ps.setInt(12, day);
				ps.setInt(13, hour);
				ps.setInt(14, minute);
				ps.setString(15, year+"-"+monthStr+"-"+dayStr);

				ps.setString(16,  dataExtincao);
				ps.setString(17,  horaExtincao);
				ps.setString(18,  dataInterv);
				ps.setString(19,  horaInterv);
				ps.setString(20,  fonteAlerta);
				ps.setString(21, nut);

				ps.setDouble(22, aa_apovoamento);
				ps.setDouble(23, aa_mato);
				ps.setDouble(24, aa_agricola);
				ps.setDouble(25, aa_espacoflorestal);
				ps.setDouble(26, aa_total);

				ps.setBoolean(27, reacendimento);
				ps.setBoolean(28, queimada);
				ps.setBoolean(29, falsoAlarme);
				ps.setBoolean(30, fogacho);
				ps.setBoolean(31, incendio);
				ps.setBoolean(32, agricola);

				ps.setString(33, perimetro);
				ps.setString(34, aps);
				ps.setString(35, causa);
				ps.setString(36, tipoCausa);
				
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


	public static void main(String[] args) {
		String fileName = "data/FiresPortugal.csv";
		FiresPortugal.createTable("fires_portugal");
		try {
			FiresPortugal.readAndWriteWithCsvMapReader(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

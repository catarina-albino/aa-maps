package aamaps_results;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import aa_maps.AAMaps;
import aa_maps.ConfusionMatrix;
import core.load_data.PNLoader;
import server.Context;

public class AAMapsResults {

	private static final String DEFAUL_PN_TABLE = "pontos_negros_janela_fixa";
	private static final String DEFAUL_AAMAPS_TABLE = "accidents_portugal_";
	private static double threshold;
	private static AAMaps map;
	private static Context context;
	private static List<Integer> spatialGrains;
	private static ArrayList<Integer> years;
	private static ArrayList<String> timeGrains;
	private static PNLoader pnLoader;
	private static PrintWriter writer;
	private static int attenFunction = 2, accumFunction = 0;
	private static int version;
	private static boolean percentage; 


	public AAMapsResults(){
		init();
	}
	
	
	private void init(){
		initTimeGrains();
		initYears();
		percentage = false;
		version = 1;
		threshold = 0;
		context = new Context();
		pnLoader = context.getPNLoader();
		map = new AAMaps();
		context.setAAMapsMetaInfo(map);
		context.setSpatialDataTable("accidents_portugal_spatialgranularity");
		context.setDefaultPNFTable(DEFAUL_PN_TABLE);
		spatialGrains = new ArrayList<Integer>();
		spatialGrains = context.getLoader().getSpatialGranularitiesArray(context).subList(0, 5);
	}

	
	/****************************************************************
	 ********************* Confusion Matrix Calculation *************
	 ****************************************************************/


	private static void calcConfusionMatrix(ConfusionMatrix cm, int year, int sgrain){
		writer.println(year+";"+sgrain+";"+threshold+";"+cm.getCSVMetrics());
	}


	/****************************************************************
	 ************************ Initializations  **********************
	 ****************************************************************/

	private void initTimeGrains(){
		timeGrains = new ArrayList<String>();
		//timeGrains.add("day");
		timeGrains.add("month");
	}

	
	private void initYears(){
		years = new ArrayList<Integer>();
		years.add(2010);
		//years.add(2011);
		//years.add(2012);
	}



	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		AAMapsResults t = new AAMapsResults();
		t.init();
		pnLoader = context.getPNLoader();
		context.setAAMapsMode(true);
		writeCSVFile();
		writer.close();
	}	
	
	
	
	private static void writeCSVFile() throws FileNotFoundException, UnsupportedEncodingException{
		percentage = false;
		version = 2;
		String name ="";
		context.setDefaultAAMapsTable(DEFAUL_AAMAPS_TABLE);
		
		if (percentage) name = "percentage_v"+version+".txt";
		else name = "absolute_v"+version+".txt";
		writer = new PrintWriter("data/results_csv/"+name, "UTF-8");
		writer.println("year;sgrain;t;precision;recall;true_positives;false_positives; false_negatives;true_negatives");

		int minT = 5, maxT = 50, step = 5;

		for (Integer year : years) {
			
			for (Integer sgrain : spatialGrains) {
				System.out.println(sgrain);
				
				for (int i = minT; i<= maxT; i=i+step){
					threshold = i;
					context.setNewSTContext(sgrain, "day");	

					ConfusionMatrix cm = pnLoader.getConfusionMatrix(context, (int) sgrain, year, attenFunction, 
							accumFunction, version, percentage, (int) threshold);
					calcConfusionMatrix(cm, year, sgrain);
				}
			}
		}
		writer.close();
	}
}

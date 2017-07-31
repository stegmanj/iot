package de.farberg.spark.examples.batch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.codehaus.jettison.json.JSONObject;

import de.uniluebeck.itm.util.logging.Logging;
import scala.Tuple2;

public class CountExample2 {

	public static void main(String[] args) {
		
//		ArrayList<Object> clusterArray = new ArrayList<Object>();
		
//		double lng = -140;
//		double lat = 60;
//		double horizontalCluster = 25;
//		double stepVertical = 0.5;
//		double stepHorizontal = 1;
//		double lngMax = -116;
//		double lngMin = -140;
//		double latMax = 60;
//		double latMin = 48.5;
//		
//		double tmpLng = -10; //einlesen aus CSV
//		double tmpLat = 1; 
//		
//		double idCtr = 1;
//		
//		
//		while (lng <= tmpLng-stepHorizontal) {
//			if (lng > lngMax-stepHorizontal) break;
//			lng = lng + stepHorizontal;
//			idCtr++;
//			System.out.println("in first while" + idCtr);			
//		} 
//		while (lat >= tmpLat+stepVertical) {
//			if (lat < latMin+stepVertical) break;
//			lat = lat - stepVertical;
//			idCtr = idCtr + horizontalCluster;
//			System.out.println("in second while" + idCtr);
//		}
//		
//		System.out.println(idCtr);
		
		
//		for (int j = -140; j < -116; j++) {
//			
//			
//			idCtr++;
			
			
			
//			for (double i = 60; i < 47; i = i - 0.5) {
//				
//				
//			
//				
//				JSONObject item1 = new JSONObject();
//			    item1.put("aDataSort", new JSONArray(0, 1));
//			    item1.put("aTargets", new JSONArray(0));
//			    items.add(item1);
//				
//				clusterArray.add(new {"ID": "idCtr"
//					
//				});
//				idCtr++;
//			}
			
//		}
		
		
		
		
		Logging.setLoggingDefaults();

		SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("Simple Application");
		JavaSparkContext sc = new JavaSparkContext(conf);

		SQLContext sqlContext = new SQLContext(sc);
		DataFrame df = sqlContext.read().format("com.databricks.spark.csv").option("inferSchema", "true")
				.option("header", "true").load("src/main/resources/sumo-sim-out.csv");

		JavaRDD<Row> javaRDD = df.javaRDD();
		
		

		JavaPairRDD<Integer, Double> mapToPair = javaRDD
				.mapToPair(row -> new Tuple2<Integer, Double>(row.getInt(1), row.getDouble(9)));

		JavaPairRDD<Integer, Double> co2Sums = mapToPair.reduceByKey((a, b) -> a + b);

		co2Sums.foreach(tuple -> System.out.println(tuple._1 + ": " + tuple._2));
		sc.close();


	}
	
//	public void addColumn(String path,String fileName) throws IOException{
//	    BufferedReader br=null;
//	    BufferedWriter bw=null;
//	    final String lineSep=System.getProperty("line.separator");
//
//	    try {
//	        File file = new File(path, fileName);
//	        File file2 = new File(path, fileName+".1");//so the
//	                    //names don't conflict or just use different folders
//
//	        br = new BufferedReader(new InputStreamReader(new FileInputStream(file))) ;
//	        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file2)));
//	        String line = null;
//	                    int i=0;
//	        for ( line = br.readLine(); line != null; line = br.readLine(),i++)
//	        {               
//
//	            String addedColumn = String.valueOf(data.get(i));
//	            bw.write(line+addedColumn+lineSep);
//	    }
//
//	    }catch(Exception e){
//	        System.out.println(e);
//	    }finally  {
//	        if(br!=null)
//	            br.close();
//	        if(bw!=null)
//	            bw.close();
//	    }
//
//	}
}
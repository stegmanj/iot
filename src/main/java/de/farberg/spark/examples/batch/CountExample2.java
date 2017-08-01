package de.farberg.spark.examples.batch;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import de.uniluebeck.itm.util.logging.Logging;
import scala.Tuple2;

public class CountExample2 {

	public static void main(String[] args) {
		
		Logging.setLoggingDefaults();

		SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("Simple Application");
		JavaSparkContext sc = new JavaSparkContext(conf);

		SQLContext sqlContext = new SQLContext(sc);
		DataFrame df = sqlContext.read().format("com.databricks.spark.csv").option("inferSchema", "true")
				.option("header", "true").load("src/main/resources/Wildfire_bc_2017.csv");		

		JavaRDD<Row> javaRDD = df.javaRDD();
		
		JavaPairRDD<Double, Double> mapToPair = javaRDD.mapToPair(row -> new Tuple2<Double, Double> (calculateCluster(row.getDouble(1), row.getDouble(2)), row.getDouble(4)));
		JavaPairRDD<Double, Double> reduceToHectar = mapToPair.reduceByKey((a, b) -> a + b);

		reduceToHectar.foreach(tuple -> System.out.println(tuple._1 + ": " + tuple._2));
		sc.close();
	}
	
	private static double calculateCluster(double tmpLat, double tmpLng) {
		double lng = -140;
		double lat = 60;
		double horizontalCluster = 25;
		double stepVertical = 0.5;
		double stepHorizontal = 1;
		double lngMax = -116;
		double lngMin = -140;
		double latMax = 60;
		double latMin = 48.5;
		
		double idCtr = 1;
		
		while (lng <= tmpLng-stepHorizontal) {
			if (lng > lngMax-stepHorizontal) break;
			lng = lng + stepHorizontal;
			idCtr++;
//			System.out.println("in first while" + idCtr);			
		} 
		while (lat >= tmpLat+stepVertical) {
			if (lat < latMin+stepVertical) break;
			lat = lat - stepVertical;
			idCtr = idCtr + horizontalCluster;
//			System.out.println("in second while" + idCtr);
		}
		
//		System.out.println(idCtr);
		return idCtr;
	}
}
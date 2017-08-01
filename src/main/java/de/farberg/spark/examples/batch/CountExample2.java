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
				.option("header", "true").load("src/main/resources/Wildfire_bc_2017_2.csv");

		JavaRDD<Row> javaRDD = df.javaRDD();

		Row minLatRow = javaRDD.reduce((a, b) -> {
			double lat1 = a.getDouble(a.fieldIndex("LATITUDE"));
			double lat2 = b.getDouble(b.fieldIndex("LATITUDE"));
			return lat1<lat2 ? a:b;
		});
		
		Row maxLatRow = javaRDD.reduce((a, b) -> {
			double lat1 = a.getDouble(a.fieldIndex("LATITUDE"));
			double lat2 = b.getDouble(b.fieldIndex("LATITUDE"));
			return lat1>lat2 ? a:b;
		});
		
		Row minLngRow = javaRDD.reduce((a, b) -> {
			double lng1 = a.getDouble(a.fieldIndex("LONGITUDE"));
			double lng2 = b.getDouble(b.fieldIndex("LONGITUDE"));
			return lng1<lng2 ? a:b;
		});
		
		Row maxLngRow = javaRDD.reduce((a, b) -> {
			double lng1 = a.getDouble(a.fieldIndex("LONGITUDE"));
			double lng2 = b.getDouble(b.fieldIndex("LONGITUDE"));
			return lng1>lng2 ? a:b;
		});
		
		System.out.println("LAT min " + minLatRow.getDouble(minLatRow.fieldIndex("LATITUDE")));
		System.out.println("LAT max " + maxLatRow.getDouble(maxLatRow.fieldIndex("LATITUDE")));
		
		System.out.println("LNG min " + minLngRow.getDouble(minLatRow.fieldIndex("LONGITUDE")));
		System.out.println("LNG max " + maxLngRow.getDouble(maxLatRow.fieldIndex("LONGITUDE")));

		JavaPairRDD<Double, Double> mapToPair = javaRDD
				.mapToPair(row -> new Tuple2<Double, Double>(calculateCluster(row.getDouble(1), row.getDouble(2)),
						row.getDouble(4)));
		JavaPairRDD<Double, Double> reduceToHectar = mapToPair.reduceByKey((a, b) -> a + b);

		reduceToHectar.foreach(tuple -> System.out.println(tuple._1 + ": " + tuple._2));

		sc.close();
	}
	
	private static double distanceInKm(double lat1, double lon1, double lat2, double lon2) {
		
		int radius = 6371;
		double x;
		
		double lat = Math.toRadians(lat2 - lat1);
		double lon = Math.toRadians(lon2 - lon1);
		
		double a = Math.sin(lat/2) * Math.sin(lat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lon / 2) * Math.sin(lon/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double d = radius * c;
		
		x = Math.abs(d);
		
		System.out.println("Abstand beträgt: " + x);
		return x;
		
	}

	private static double calculateLatAndLng(double cluster) {
		double lat = 0.0;
		double lng;

		return lat;
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

		while (lng <= tmpLng - stepHorizontal) {
			if (lng > lngMax - stepHorizontal)
				break;
			lng = lng + stepHorizontal;
			idCtr++;
			// System.out.println("in first while" + idCtr);
		}
		while (lat >= tmpLat + stepVertical) {
			if (lat < latMin + stepVertical)
				break;
			lat = lat - stepVertical;
			idCtr = idCtr + horizontalCluster;
			// System.out.println("in second while" + idCtr);
		}

		// System.out.println(idCtr);
		return idCtr;
	}
}
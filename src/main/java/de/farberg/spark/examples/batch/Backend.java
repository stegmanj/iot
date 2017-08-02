package de.farberg.spark.examples.batch;

import static spark.Spark.staticFiles;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.mortbay.util.ajax.JSON;

import com.google.gson.Gson;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.shapes.GHPoint3D;

import de.uniluebeck.itm.util.logging.Logging;
import scala.Tuple2;
import spark.Spark;

public class Backend {
	public static class LatLon {
		double lat;
		double lon;
	}

	public static class LatLonDanger extends LatLon {
		double danger;
	}

	public static class FromWebbrowser {
		LatLon start;
		LatLon dest;
	}

	public static class ToWebbrowser {
		LatLonDanger waypoints[];
	}
	
	public static class returnValues {
		PathWrapper bestPath;
		String json;		
	}

	public static void main(String args[]) throws InterruptedException, IOException {

		Logging.setLoggingDefaults();

		System.out.println("Loading Map of British Columbia...");
		GraphhopperHelper helper = new GraphhopperHelper(
				new File("src/main/resources/british-columbia-latest.osm.pbf"));

		SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("Simple Application");
		JavaSparkContext sc = new JavaSparkContext(conf);

		SQLContext sqlContext = new SQLContext(sc);
		DataFrame df = sqlContext.read().format("com.databricks.spark.csv").option("inferSchema", "true")
				.option("header", "true").load("src/main/resources/Wildfire_bc_2017_2.csv");

		JavaRDD<Row> javaRDD = df.javaRDD();

		// Berechnung der Min- und Max-Werte für Längen- und Breitengrade
		Row minLatRow = javaRDD.reduce((a, b) -> {
			double lat1 = a.getDouble(a.fieldIndex("LATITUDE"));
			double lat2 = b.getDouble(b.fieldIndex("LATITUDE"));
			return lat1 < lat2 ? a : b;
		});

		Row maxLatRow = javaRDD.reduce((a, b) -> {
			double lat1 = a.getDouble(a.fieldIndex("LATITUDE"));
			double lat2 = b.getDouble(b.fieldIndex("LATITUDE"));
			return lat1 > lat2 ? a : b;
		});

		Row minLngRow = javaRDD.reduce((a, b) -> {
			double lng1 = a.getDouble(a.fieldIndex("LONGITUDE"));
			double lng2 = b.getDouble(b.fieldIndex("LONGITUDE"));
			return lng1 < lng2 ? a : b;
		});

		Row maxLngRow = javaRDD.reduce((a, b) -> {
			double lng1 = a.getDouble(a.fieldIndex("LONGITUDE"));
			double lng2 = b.getDouble(b.fieldIndex("LONGITUDE"));
			return lng1 > lng2 ? a : b;
		});

		// Speichern der Min- und Max-Werte in Variablen
		double latMin = minLatRow.getDouble(minLatRow.fieldIndex("LATITUDE"));
		double latMax = maxLatRow.getDouble(maxLatRow.fieldIndex("LATITUDE"));
		double lngMin = minLngRow.getDouble(minLatRow.fieldIndex("LONGITUDE"));
		double lngMax = maxLngRow.getDouble(maxLatRow.fieldIndex("LONGITUDE"));

		// Berechnung der nördlichsten und südlichsten Distanz
		double distance1 = distanceInKm(latMin, lngMin, latMin, lngMax);
		double distance2 = distanceInKm(latMax, lngMin, latMax, lngMax);

		double finalDist = 0.0;

		// größere Distanz wird zur Clustereinteilung verwendet
		if (distance1 < distance2) {
			finalDist = distance2;
		} else {
			finalDist = distance1;
		}

		// Einteilung in 50km-große Cluster
		int horizontalClusterAnz = (int) (finalDist / 50);

		System.out.println("Horizontale Clusteranzahl: " + horizontalClusterAnz);

		// Mapping der Cluster und ihrer brennenden Hectar in PairRDD
		JavaPairRDD<Double, Double> mapToPair = javaRDD
				.mapToPair(row -> new Tuple2<Double, Double>(calculateCluster(horizontalClusterAnz, row.getDouble(1),
						row.getDouble(2), latMax, latMin, lngMax, lngMin), row.getDouble(4)));
		
		// Reduzierung auf unterschiedliche Cluster
		JavaPairRDD<Double, Double> reduceToHectar = mapToPair.reduceByKey((a, b) -> a + b);

		staticFiles.externalLocation("Webresources");
		Spark.post("/waypoints", (req, res) -> {
			System.out.println("Received Post Request...");
			Gson gson = new Gson();

			FromWebbrowser fromWebbrowser = gson.fromJson(req.body(), FromWebbrowser.class);

			PathWrapper bestPath = helper.route(fromWebbrowser.start.lat, fromWebbrowser.start.lon,
					fromWebbrowser.dest.lat, fromWebbrowser.dest.lon);
			
			ToWebbrowser routePointsToWeb = new ToWebbrowser();
			routePointsToWeb.waypoints = new LatLonDanger[bestPath.getPoints().size()];
			
			// Speichern aller Wegpunkte zur Anzeige der Route auf der Webseite
			for (int i = 0; i < bestPath.getPoints().size(); i++) {
				routePointsToWeb.waypoints[i] = new LatLonDanger();
				routePointsToWeb.waypoints[i].lat = bestPath.getPoints().getLat(i);
				routePointsToWeb.waypoints[i].lon = bestPath.getPoints().getLon(i);
				routePointsToWeb.waypoints[i].danger = 0;
			}

			// Speichern nur jeweils eines Wegpunktes eines Clusters ein einer temporären HashMap
			Map<String, GHPoint3D> tmp = new HashMap<>();
			for (GHPoint3D point3d : bestPath.getPoints()) {
				String key = "" + calculateCluster(horizontalClusterAnz, point3d.getLat(), point3d.getLon(), latMax,
						latMin, lngMax, lngMin);
				tmp.put(key, point3d);
			}

			ToWebbrowser toWebBrowser = new ToWebbrowser();
			toWebBrowser.waypoints = new LatLonDanger[tmp.size()];

			int i = -1;
			for (GHPoint3D point : tmp.values()) {
				++i;
				double clusterId = calculateCluster(horizontalClusterAnz, point.getLat(), point.getLon(), latMax,
						latMin, lngMax, lngMin);
				JavaPairRDD<Double, Double> filtered = reduceToHectar.filter(entry -> entry._1 == clusterId);

				if (filtered.count() > 0) {
					Double danger = filtered.first()._2;
					toWebBrowser.waypoints[i] = new LatLonDanger();
					toWebBrowser.waypoints[i].lat = bestPath.getPoints().getLat(i);
					toWebBrowser.waypoints[i].lon = bestPath.getPoints().getLon(i);
					toWebBrowser.waypoints[i].danger = danger.doubleValue();
				}
			}
			
			// Rückgabe der Feuerpunkte auf der Route und aller Routenpunkte in zusammengefügtem JSON			
			res.type("application/json");
			
			String firepoints = "\"firepoints\": " + gson.toJson(toWebBrowser);
			String routepoints = "\"routepoints\": " + gson.toJson(routePointsToWeb);
			
			String returnJson = "{" + firepoints + ", " + routepoints + "}";
			
			return returnJson;
		});

		while (true) {
			Thread.sleep(1000);
		}
	}

	private static double distanceInKm(double lat1, double lon1, double lat2, double lon2) {

		int radius = 6371;
		double x;

		double lat = Math.toRadians(lat2 - lat1);
		double lon = Math.toRadians(lon2 - lon1);

		double a = Math.sin(lat / 2) * Math.sin(lat / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(lon / 2) * Math.sin(lon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = radius * c;

		x = Math.abs(d);

		System.out.println("Abstand beträgt: " + x);
		return x;

	}

	private static double calculateCluster(int horizontalClusterAnz, double tmpLat, double tmpLng, double latMax,
			double latMin, double lngMax, double lngMin) {
		double lng = lngMin;
		double lat = latMax;

		double stepHorizontal = (lngMax - lngMin) / horizontalClusterAnz;
		double stepVertical = (latMax - latMin) / horizontalClusterAnz;

		double idCtr = 1;

		while (lng <= tmpLng - stepHorizontal) {
			if (lng > lngMax - stepHorizontal)
				break;
			lng = lng + stepHorizontal;
			idCtr++;
		}
		while (lat >= tmpLat + stepVertical) {
			if (lat < latMin + stepVertical)
				break;
			lat = lat - stepVertical;
			idCtr = idCtr + horizontalClusterAnz;
		}
		return idCtr;
	}
}
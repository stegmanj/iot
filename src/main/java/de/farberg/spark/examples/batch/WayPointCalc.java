package de.farberg.spark.examples.batch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;

public class WayPointCalc {

	static class GraphhopperHelper {
		private GraphHopper hopper;

		public GraphhopperHelper(File osmFile) throws IOException {
			// create one GraphHopper instance
			this.hopper = new GraphHopper().forServer();
			hopper.setOSMFile(osmFile.getAbsolutePath());

			File tempDirectory = Files.createTempDir();
			FileUtils.forceDeleteOnExit(tempDirectory);
			hopper.setGraphHopperLocation(tempDirectory.toString());
			hopper.setEncodingManager(new EncodingManager("car"));
			hopper.importOrLoad();
		}

		public PathWrapper route(double fromLat, double fromLon, double toLat, double toLon) throws Exception {
			GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon).setWeighting("fastest").setVehicle("car")
					.setLocale(Locale.US);

			GHResponse rsp = hopper.route(req);

			if (rsp.hasErrors()) {
				String errorMessage = "";
				for (Throwable t : rsp.getErrors())
					errorMessage += t.toString();

				throw new Exception(errorMessage);
			}

			return rsp.getBest();
		}
	}

	public static void main(String[] args) throws Exception {
		double fromLat = -123.119464;
		double fromLon = 49.279031;
		
		double toLat = -123.123326;
		double toLon = 49.275300;
		
		// Obtain an instance of a logger for this class
		GraphhopperHelper helper = new GraphhopperHelper(
				new File("src/main/resources/british-columbia-latest.osm.pbf")); // "c:\\users\\hpadmin\\Desktop\\british-columbia-latest.osm.pbf"
		PathWrapper bestPath = helper.route(fromLon, fromLat, toLon, toLat);
		
			
		PointList pointList = bestPath.getPoints();
		double distance = bestPath.getDistance();
		
		PointList datalist = bestPath.getPoints();
		
			
		int listlength = 0;
		
		listlength = datalist.getSize();
		
		
		//for(int i = 0; i<listlength; i++) {
			//System.out.println("#" + i + "Lon: " + datalist.getLon(i) + " Lat: " + datalist.getLat(i) );
		//}
		
		System.out.println("Size:" + datalist.getSize());
		//System.out.println("Lat" + datalist.getLat(1));
		//System.out.println("Latitude" + datalist.getLatitude(1));
		System.out.println(datalist);
	}

	
}
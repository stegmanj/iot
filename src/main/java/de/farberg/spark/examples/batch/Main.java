//package de.farberg.spark.examples.batch;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.Locale;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import org.apache.commons.io.FileUtils;
//import org.kohsuke.args4j.CmdLineException;
//import org.kohsuke.args4j.CmdLineParser;
//import org.slf4j.LoggerFactory;
//
//import com.graphhopper.GHRequest;
//import com.graphhopper.GHResponse;
//import com.graphhopper.GraphHopper;
//import com.graphhopper.PathWrapper;
//import com.graphhopper.routing.util.EncodingManager;
//import com.graphhopper.util.PointList;
//import com.graphhopper.util.shapes.GHPoint3D;
//
//import de.uniluebeck.itm.util.logging.LogLevel;
//import de.uniluebeck.itm.util.logging.Logging;
//
//public class Main {
//
//    static {
//        Logging.setLoggingDefaults(LogLevel.INFO, "[%-5p; %c{1}::%M] %m%n");
//    }
//
//    static class GraphhopperHelper {
//        Logger log = LoggerFactory.getLogger(GraphhopperHelper.class);
//        private GraphHopper hopper;
//
//        public GraphhopperHelper(File osmFile) throws IOException {
//            // create one GraphHopper instance
//            this.hopper = new GraphHopper().forServer();
//            hopper.setOSMFile(osmFile.toString());
//
//            File tempDirectory = Files.createTempDir();
//            FileUtils.forceDeleteOnExit(tempDirectory);
//            log.debug("Using temp dir {}", tempDirectory.toString());
//hopper.setGraphHopperLocation(tempDirectory.toString());
//            hopper.setEncodingManager(new EncodingManager("car"));
//            hopper.importOrLoad();
//        }
//
//        public PathWrapper route(double fromLat, double fromLon, double toLat, double toLon) throws Exception {
//            GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon).setWeighting("fastest").setVehicle("car").setLocale(Locale.US);
//
//            GHResponse rsp = hopper.route(req);
//
//            if (rsp.hasErrors()) {
//                String errorMessage = "";
//                for (Throwable t : rsp.getErrors())
//                    errorMessage += t.toString();
//
//                throw new Exception(errorMessage);
//            }
//
//            return rsp.getBest();
//        }
//    }
//
//    public static void main(String[] args) throws Exception {
//        // Obtain an instance of a logger for this class
//        Logger log = LoggerFactory.getLogger(Main.class);
//        CommandLineOptions options = parseCmdLineOptions(args);
//
//        if (options.verbose) {
//org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);
//            log.debug("Debug enabled");
//        }
//
//        GraphhopperHelper helper = new GraphhopperHelper(options.osmFile);
//        PathWrapper bestPath = helper.route(49.409566, 8.714867, 49.364870, 8.675341);
//        PointList pointList = bestPath.getPoints();
//
//        double distance = bestPath.getDistance();
//        long timeInMs = bestPath.getTime();
//        log.debug("Ok, got {} points, total distance {}, and time {} ms", pointList.getSize(), distance, timeInMs);
//
//        for (GHPoint3D point3d : pointList) {
//            log.debug("Point: {}", point3d);
//        }
//
//    }
//
//    private static void printHelpAndExit(CmdLineParser parser) {
//        System.err.print("Usage: java " + Main.class.getCanonicalName());
//        parser.printSingleLineUsage(System.err);
//        System.err.println();
//        parser.printUsage(System.err);
//        System.exit(1);
//    }
//
//    private static CommandLineOptions parseCmdLineOptions(final String[] args) {
//        CommandLineOptions options = new CommandLineOptions();
//        CmdLineParser parser = new CmdLineParser(options);
//
//        try {
//            parser.parseArgument(args);
//            if (options.help)
//                printHelpAndExit(parser);
//        } catch (CmdLineException e) {
//            System.err.println(e.getMessage());
//            printHelpAndExit(parser);
//        }
//
//        return options;
//    }
//
//}
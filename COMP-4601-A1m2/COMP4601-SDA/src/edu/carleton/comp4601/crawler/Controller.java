package edu.carleton.comp4601.crawler;

import java.util.ArrayList;

import edu.carleton.comp4601.SDA.db.DatabaseManager;
import edu.carleton.comp4601.graph.PageGraph;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
	
	public static ArrayList<String> urls = new ArrayList<String>();
	
	static final public String SEED1 = "https://sikaman.dyndns.org/courses/4601/handouts/"; 
	static final public String SEED2 = "https://sikaman.dyndns.org/courses/4601/resources/";
	static final public String SEED3 = "https://www.reddit.com/";
	public static PageGraph pageGraph;
    
    public static void main(String[] args) throws Exception {
        int numCrawlers = 3;
        pageGraph = new PageGraph();
        CrawlConfig config = new CrawlConfig();
        
        String crawlStorageFolder = ".";

        config.setCrawlStorageFolder(crawlStorageFolder);
        
        config.setMaxPagesToFetch(10);

        config.setPolitenessDelay(1000);

        config.setIncludeBinaryContentInCrawling(true);

        config.setResumableCrawling(false);

   //   Instantiate the controller for this crawl.
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        controller.addSeed(SEED1);
        controller.addSeed(SEED2);
       // controller.addSeed(SEED3);
       
        controller.start(Crawler.class, numCrawlers);
        
        DatabaseManager.getInstance().addGraphToDb(pageGraph);
        PageGraph pg = DatabaseManager.getInstance().loadGraphFromDB();
        
    }
}
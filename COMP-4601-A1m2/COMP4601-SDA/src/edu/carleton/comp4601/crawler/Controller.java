package edu.carleton.comp4601.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import Jama.Matrix;
import edu.carleton.comp4601.SDA.db.DatabaseManager;
import edu.carleton.comp4601.graph.PageGraph;
import edu.carleton.comp4601.searching.MyLucene;
import edu.carleton.comp4601.pagerank.PageRank2;
import edu.carleton.comp4601.networking.Marshaller;
import edu.carleton.comp4601.pagerank.PageRank3;
import edu.carleton.comp4601.pagerank.PageRank2;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
	
	public static ArrayList<String> urls = new ArrayList<String>();
	
	static final public String SEED1 = "https://sikaman.dyndns.org/courses/4601/handouts/"; 
	static final public String SEED2 = "https://sikaman.dyndns.org/courses/4601/resources/N-0";
	static final public String SEED3 = "https://www.reddit.com/";
	public static PageGraph pageGraph;
    
    public static void main(String[] args) throws Exception {
        int numCrawlers = 3;
        pageGraph = new PageGraph();
        CrawlConfig config = new CrawlConfig();
        
        String crawlStorageFolder = ".";

        config.setCrawlStorageFolder(crawlStorageFolder);
        
        config.setMaxPagesToFetch(4);

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
        controller.addSeed(SEED3);
       
        controller.start(Crawler.class, numCrawlers);
        
        DatabaseManager.getInstance().addGraphToDb(pageGraph);
        PageGraph pg = DatabaseManager.getInstance().loadGraphFromDB();
        System.out.println(pg.getGraph().toString());;
        
        
        MyLucene.indexLucene(DatabaseManager.getInstance().getAllDocCursor());
        
        System.out.print("Results: " + MyLucene.query("+banana +coconut").toString());
   

        //DatabaseManager.getInstance().addGraphToDb(pageGraph);
        /*PageGraph pg = DatabaseManager.getInstance().loadGraphFromDB();
        Matrix m = PageRank2.computePageRank(pg.getGraph());
        m.print(m.getRowDimension(), m.getColumnDimension());*/
        ///PageRank2.computePageRank(pageGraph.getGraph());
        ArrayList<HashMap<String, Float>> pr = PageRank3.getInstance().computePageRank();
        System.out.println(PageRank3.getInstance().getBoostMap());

    }
}
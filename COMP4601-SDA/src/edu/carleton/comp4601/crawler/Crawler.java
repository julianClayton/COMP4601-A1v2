package edu.carleton.comp4601.crawler;

import java.util.regex.Pattern;

import edu.carleton.comp4601.graph.Vertex;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler{

	
     public boolean shouldVisit(Page referringPage, WebURL url) {
    	//prevent off-site visits
         String href = url.getURL().toLowerCase();
         return href.startsWith(Controller.SEED1) || href.startsWith(Controller.SEED2) || href.startsWith(Controller.SEED3);
     }


     @Override
     public void visit(Page page) {
    	 int docID = page.getWebURL().getDocid();
    	 String url = page.getWebURL().getURL();
         String parentUrl = page.getWebURL().getParentUrl();
         
         System.out.println("URL : " + url);
         System.out.println("DocID : " + docID);
         
         Vertex v = new Vertex (url, page);
     }


}

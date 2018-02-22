package edu.carleton.comp4601.crawler;

import edu.carleton.comp4601.graph.PageGraph;
import edu.carleton.comp4601.graph.Vertex;
import edu.carleton.comp4601.pagerank.PageRank;
import edu.carleton.comp4601.pagerank.PageRank2;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

import java.util.Date;

import Jama.Matrix;
import edu.carleton.comp4601.SDA.db.DatabaseManager;


public class Crawler extends WebCrawler{
	
 	private Date endTime;

	
     public boolean shouldVisit(Page referringPage, WebURL url) {
    	 endTime = new Date();
    	 
    	//prevent off-site visits
         String href = url.getURL().toLowerCase();
         return  true;
     }


     @Override
     public void visit(Page page) {
    	 
    	 Date startTime = new Date();
    	 
    	 int docID = page.getWebURL().getDocid();
    	 String url = page.getWebURL().getURL();
         String parentUrl = page.getWebURL().getParentUrl();
         
         System.out.println("URL : " + url);
         System.out.println("DocID : " + docID);
         
         int delay = this.getMyController().getConfig().getPolitenessDelay();
 	     
         //long time = startTime.getTime() - endTime.getTime();
         
         /*System.out.println("Current delay: " + delay);
         if (delay > time){
        	 System.out.println("Setting delay: " + time);
        	 this.getMyController().getConfig().setPolitenessDelay(delay*(int)time);
         }*/
         
         
         Vertex v = new Vertex(docID, url);
         
         PageParser p = new PageParser(url, page);
         
        
         DatabaseManager dm = DatabaseManager.getInstance();
         dm.addPageToDb(p);
         if (Controller.pageGraph.hasVertex(parentUrl)) {
        	 Controller.pageGraph.connectToExistingVertex(v, parentUrl);
         }
         else {
        	 Controller.pageGraph.addVertex(v);
          }      
     }    
} 
    
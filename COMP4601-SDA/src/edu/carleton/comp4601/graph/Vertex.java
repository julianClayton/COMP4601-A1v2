package edu.carleton.comp4601.graph;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;

public class Vertex implements Serializable {
	private static final long serialVersionUID = 1L;
	private String url;
	private String docID;
	private Vertex parent;
	private HashSet<Vertex> children;
	private Page page;
	
	private HashSet<String> links;
	private HashMap<String, String> imgAltMap;
	private HashSet<String> text;
	private HashSet<String> headings;
	
	private String title;
	private String type;
	
	public Vertex(Vertex parent, String url, Page page ){
		this.url = url;
		this.page = page;
		this.parent = parent;
		parseJsoup();
		parseTika();
		
	}
	
	public Vertex(String url, Page page){
		this.url = url;
		this.page = page;
		this.parent = null;
		parseJsoup();
		parseTika();
	}
	
	private void parseJsoup() {
		if ((page.getParseData() instanceof HtmlParseData)) {
        HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
        String html = htmlParseData.getHtml();
        
	        //parse elements with Jsoup
	        Document doc = Jsoup.parse(html);
	        
	        //convert Elements to Set so Vertex can be Serializable
	        this.headings = elementToSet(doc.select("h0,h1,h2,h3,h4,h5"));
	        this.text = elementToSet(doc.select("p"));
	        this.links = elementToSet(doc.select("a[href]")); 
	        this.imgAltMap = getImgAlts(doc);
	        
	        System.out.println("=====JSOUP PARSED DATA======");
	       	System.out.println("Text:" + text.toString());
	       	System.out.println("Images + Alts:" + imgAltMap.toString());
	       	System.out.println("Header:" + headings.toString());
	       	System.out.println("Links:" + links.toString());
		}
	}
	
	private void parseTika() {
       	//Parse metadata with Tika
	  	java.io.InputStream	input	= new ByteArrayInputStream(page.getContentData());	
	  	org.xml.sax.ContentHandler	handler	= new BodyContentHandler();
	  	org.apache.tika.metadata.Metadata	metadata	=	new org.apache.tika.metadata.Metadata();
	  	ParseContext	context	=	new ParseContext();	
	  	Parser	parser	=	new AutoDetectParser();	
	  	
		try {
		 parser.parse(input,	handler,	metadata,	context);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (TikaException e) {
			e.printStackTrace();
		}    	 
	
	  	title	=	metadata.get(org.apache.tika.metadata.Metadata.TITLE);	
	  	type	=	metadata.get(org.apache.tika.metadata.Metadata.CONTENT_TYPE);	
	  	
	  	System.out.println("=====TIKA PARSED DATA======");
	  	System.out.println("TITLE: " + title);
	  	System.out.println("TYPE : " + type);
	}
	
	
	private HashMap<String, String> getImgAlts(Document doc){
		HashMap<String, String> hm = new HashMap<String, String>();
	
		Elements images = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]");
		
		if (!images.isEmpty()){
			for(Element i: images){
				hm.put(i.attr("src"),i.attr("alt")); //src and alt info
			}
		}
		
		return hm;
	}
	
	private HashSet<String> elementToSet(Elements el){
		HashSet<String> hs = new HashSet<String>();
		for	(Element	e	:	el)	{	
      		 hs.add(e.text());
        }	
		return hs;
	}

}

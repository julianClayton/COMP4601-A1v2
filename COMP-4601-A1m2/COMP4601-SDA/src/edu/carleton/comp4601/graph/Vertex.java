package edu.carleton.comp4601.graph;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
	private int docID;
	private Vertex parent;
	private HashSet<Vertex> children;
	private Page page;
	
	private edu.carleton.comp4601.dao.Document SDAdoc;
	
	private ArrayList<String> links;
	private HashMap<String, String> imgAltMap;
	private String text;
	private ArrayList<String> headings;
	
	private Date timestamp;
	
	private String title;
	private String type;
	
	public Vertex(Vertex parent, String url, Page page){
		this.docID =  page.getWebURL().getDocid();
		
		timestamp = new Date();
		
		this.SDAdoc = new edu.carleton.comp4601.dao.Document();
		this.url = url;
		this.page = page;
		this.parent = parent;
		setSDAdoc();
		parseJsoup();
		parseTika();
	}
	//This is the constructor for the initial base vertexs
	public Vertex(Vertex parent, String url) {
		this.parent = parent;
		this.url = url;
		
	}
	public Vertex(String url, Page page){
		this.SDAdoc = new edu.carleton.comp4601.dao.Document();
		timestamp = new Date();
		this.url = url;
		this.page = page;
		this.parent = null;
		setSDAdoc();
		parseJsoup();
		parseTika();
	}
	
	public edu.carleton.comp4601.dao.Document getDoc(){
		return this.SDAdoc;
	}
	
	public int getID(){
		return this.docID;
	}
	
	public String getUrl(){
		return this.url;
	}
	
	public ArrayList<String> getLinks() {
		return this.links;
	}
	
	public String getText() {
		return this.text;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getTime(){
		return timestamp.toString();
	}
	
	public ArrayList<String> getHeadings() {
		return this.headings;
	}
	
	public HashMap<String, String> getImages() {
		return this.imgAltMap;
	}
	
	private void setSDAdoc(){
		SDAdoc.setId(this.docID);
		SDAdoc.setUrl(this.url);
	}
	
	private void parseJsoup() {
		if ((page.getParseData() instanceof HtmlParseData)) {
        HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
        String html = htmlParseData.getHtml();
        
	        //parse elements with Jsoup
	        Document jdoc = Jsoup.parse(html);
	        
	        //convert Elements to Set so Vertex can be Serializable
	        
	        this.headings = elementToList(jdoc.select("h0,h1,h2,h3,h4,h5"));
	        this.text = jdoc.body().text();
	        this.links = elementToList(jdoc.select("a[href]")); 
	        this.imgAltMap = getImgAlts(jdoc);
	        
	        SDAdoc.setText(this.text);
	        SDAdoc.setName(jdoc.select("h0").toString());
	        SDAdoc.setLinks(links);

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
	  	
	  	
	  	SDAdoc.setName(title);
	  	if (type != "text/html"){
	  		SDAdoc.setText(handler.toString());
	  		SDAdoc.setName(title);
	  	}
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
	
	private ArrayList<String> elementToList(Elements el){
		ArrayList<String> al = new ArrayList<String>();
		for	(Element	e	:	el)	{	
      		 al.add(e.text());
        }	
		return al;
	}

}
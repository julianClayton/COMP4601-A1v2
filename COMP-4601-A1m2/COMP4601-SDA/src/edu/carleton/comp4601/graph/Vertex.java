package edu.carleton.comp4601.graph;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;





import edu.uci.ics.crawler4j.parser.HtmlParseData;


public class Vertex implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int docID;
	private String url;
	//This is the constructor for the initial base vertexs
	
	public Vertex(int id, String url) {
		this.docID = id;
		this.url = url;
		
	}

	public int getID(){
		return this.docID;
	}
	
	public String getUrl(){
		return this.url;
	}
}
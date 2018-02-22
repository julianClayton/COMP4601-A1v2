package edu.carleton.comp4601.graph;

public class Vertex {
	
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
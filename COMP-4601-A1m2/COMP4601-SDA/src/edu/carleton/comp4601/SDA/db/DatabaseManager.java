package edu.carleton.comp4601.SDA.db;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.carleton.comp4601.graph.*;
import edu.carleton.comp4601.networking.Marshaller;
import edu.carleton.comp4601.pagerank.PageRank2;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import Jama.Matrix;
import edu.carleton.comp4601.dao.Document;

public class DatabaseManager {
	
	private final String GRAPH_COL = "graph";
	private final String DOC_COL = "documents";
	private final String DOC_NUM_COL = "docnum";

	
	private MongoClient	m;
	private DBCollection col;
	private DB db;
	private static DatabaseManager instance;
	
	public DatabaseManager() {
		instance = this;
		initConnection();
	}

	public boolean graphExists() {
		switchCollection(GRAPH_COL);
		DBCursor cur = col.find().limit(1);
		if (cur.hasNext()) {
			return true;
		}
		return false;
	}
	private int getDocNum() {
		switchCollection(DOC_NUM_COL);
		DBCursor cur = col.find().limit(1);
		int num = 0;
		if (cur.hasNext()) {
			DBObject obj = cur.next();
			num = (int) obj.get("docnum");
		}
		return num;
	}
	
	public Document findDoc(int docId){
		switchCollection(DOC_COL);
		DBCursor cur = col.find(new BasicDBObject("id", docId));
		DBObject searchObj = null;
		if (cur.hasNext()) {
			searchObj = cur.next();
		}
		DBObject obj = searchObj;
		
		if (obj != null){
			Document document = new Document();
			document.setId((Integer) obj.get("id"));
			document.setUrl((String) obj.get("url"));
			document.setLinks((ArrayList<String>) obj.get("links"));
			document.setTags((ArrayList<String>) obj.get("tags"));
			document.setText((String) obj.get("text"));
			document.setName((String) obj.get("name"));
			return document;
		}
		return null;
	}
	
	public void incrementDocNum() {
		switchCollection(DOC_NUM_COL);
		DBCursor cur = col.find().limit(1);
		int num = 0;
		DBObject obj;
		if (cur.hasNext()) {
			obj = cur.next();
			num = (int) obj.get("docnum");
		}
		col.remove(new BasicDBObject("name","docid"));
		num++;
		DBObject newDocId = BasicDBObjectBuilder.start("name", "docid").add("docnum", num).get();
		col.insert(newDocId);
	}
	
	public void addDocToDb(Document document) {
		incrementDocNum();
		int id = getDocNum();
		switchCollection(DOC_COL);
		document.setId(id);
		DBObject obj = BasicDBObjectBuilder
				.start("name", document.getName())
				.add("id", document.getId())
				.add("url", document.getUrl())
				.add("text", document.getText())
				.add("tags",document.getTags())
				.add("links", document.getLinks())
				.get();

		col.save(obj);
	}
	
	public void addVertexToDb(Vertex vert) {
		incrementDocNum();
		Document document = vert.getDoc();
		int id = getDocNum();
		switchCollection(DOC_COL);
		document.setId(id);
		DBObject obj = BasicDBObjectBuilder
				.start("name", document.getName())
				.add("id", document.getId())
				.add("url", document.getUrl())
				.add("text", document.getText())
				.add("tags",document.getTags())
				.add("links", document.getLinks())
				.add("metadata",vert.getType())
				.add("timestamp",vert.getTime())
				.get();

		col.save(obj);
	}
	

	public void updateDocLinks(int id, ArrayList<String> links) {
		switchCollection(DOC_COL);
		DBCursor cur = col.find(new BasicDBObject("id", id));
		DBObject obj = null;
		if (cur.hasNext()) {
			obj = cur.next();
		}
		DBObject newObject = obj;
		newObject.put("links", links);
		col.remove(new BasicDBObject("id",id));
		col.save(newObject);
	}
	public void updateDocTags(int id, ArrayList<String> tags) {
		switchCollection(DOC_COL);
		DBCursor cur = col.find(new BasicDBObject("id", id));
		DBObject obj = null;
		if (cur.hasNext()) {
			obj = cur.next();
		}
		DBObject newObject = obj;
		newObject.put("tags", tags);
		col.remove(new BasicDBObject("id",id));
		col.save(newObject);
	}
	
	public Document getDocument(int id) {
		switchCollection(DOC_COL);
		DBCursor cur = col.find(new BasicDBObject("id", id));
		DBObject obj = null;
		if (cur.hasNext()) {
			obj = cur.next();
		}
		Document document = new Document();
		document.setId((Integer) obj.get("id"));
		document.setUrl( (String) obj.get("url"));
		document.setLinks((ArrayList<String>) obj.get("links"));
		document.setTags((ArrayList<String>) obj.get("tags"));
		document.setText((String) obj.get("text"));
		document.setName((String) obj.get("name"));
		return document;
		
	}
	public boolean deleteDocument(int id) {
		switchCollection(DOC_COL);
		DBCursor cur = col.find(new BasicDBObject("id", id));
		boolean isFound = false;
		DBObject obj = null;
		if (cur.hasNext()) {
			obj = cur.next();
			isFound = true;
			col.remove(obj);
		}
		return isFound;
	}
	public boolean deleteDocumentsWithTags(ArrayList<String> tags) {
		switchCollection(DOC_COL);
		BasicDBObject inQuery = new BasicDBObject();
		inQuery.put("tags", new BasicDBObject("$in", tags));
		DBCursor cursor = col.find(inQuery);
		DBObject obj = null;
		boolean docsDeleted = false;
		
		while(cursor.hasNext()) {
			obj = cursor.next();
			col.remove(obj);
			docsDeleted = true;
		}
		return docsDeleted;
	}

	public boolean deleteAllDocuments() {
		switchCollection(DOC_COL);
		DBCursor cursor = col.find();
		boolean docsDeleted = false;

		while(cursor.hasNext()) {
			col.remove(cursor.next());
			docsDeleted = true;
		}

		return docsDeleted;
	}

	public ArrayList<Document> getDocumentsWithTags(ArrayList<String> tags) {
		switchCollection(DOC_COL);
		BasicDBObject inQuery = new BasicDBObject();
		inQuery.put("tags", new BasicDBObject("$in", tags));
		DBCursor cursor = col.find(inQuery);
		DBObject obj = null;
		ArrayList<Document> docs = new ArrayList<Document>();
		while(cursor.hasNext()) {
			obj = cursor.next();
			Document document = new Document();
			document.setId((Integer) obj.get("id"));
			document.setUrl((String) obj.get("url"));
			document.setLinks((ArrayList<String>) obj.get("links"));
			document.setTags((ArrayList<String>) obj.get("tags"));
			document.setText((String) obj.get("text"));
			document.setName((String) obj.get("name"));
			docs.add(document);
		}
		return docs;
	}
	
	public ArrayList<Document> getAllDocuments() {
		switchCollection(DOC_COL);
		DBCursor cursor = col.find();
		ArrayList<Document> docs = new ArrayList<Document>();
		DBObject obj = null;
		while(cursor.hasNext()) {
			obj = cursor.next();
			Document document = new Document();
			document.setId((Integer) obj.get("id"));
			document.setUrl((String) obj.get("url"));
			document.setLinks((ArrayList<String>) obj.get("links"));
			document.setTags((ArrayList<String>) obj.get("tags"));
			document.setText((String) obj.get("text"));
			document.setName((String) obj.get("name"));
			docs.add(document);
		}
		return docs;
	}
	
	public DBCursor getAllDocCursor(){
		switchCollection(DOC_COL);
		BasicDBObject query	= new BasicDBObject();	
		DBCursor cursor = col.find(query);	
		return cursor;
	}

	
	public static DatabaseManager getInstance() {
		if (instance == null)
			instance = new DatabaseManager();
		return instance;
	}
	
	public static void setInstance(DatabaseManager instance) {
		DatabaseManager.instance = instance;	
	}
	
	
	private void initConnection() {
		try {
			m = new	MongoClient("localhost", 27017);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}	
		db = m.getDB("sda");	
		switchCollection(GRAPH_COL);
	}
	
	private void switchCollection(String collection) {
		col = db.getCollection(collection);
	}
	public void removeOldGraph(PageGraph graph) {
		switchCollection(GRAPH_COL);
		col.remove(new BasicDBObject("name", graph.getName()));
	}
	
	public void addGraphToDb(PageGraph graph) {
		switchCollection(GRAPH_COL);
		removeOldGraph(graph);
		byte[] bytes = null;
		try {
			bytes = Marshaller.serializeObject(graph);
		} catch (IOException e) {
			e.printStackTrace();
		}
		DBObject pgraph = BasicDBObjectBuilder.start().add("graphbytes", bytes).add("iterations", graph.incrementIterations()).get();
		col.save(pgraph);
		
	}
	public PageGraph loadGraphFromDB() {
		switchCollection(GRAPH_COL);
		DBObject o = null;
		BasicDBObject whereQuery = new BasicDBObject();
		DBCursor cursor = col.find(whereQuery);
		while(cursor.hasNext()) {
			System.out.println("loading graph");
		     o = cursor.next();
		}
		System.out.println("obj: " + o);
		byte[] bytes = (byte[]) o.get("graphbytes");
		System.out.print("bytes: " + bytes);
		PageGraph g = null;
		try {
			g = (PageGraph) Marshaller.deserializeObject(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return g;
	}
	public byte[] loadGraphFromDB2() {
		switchCollection(GRAPH_COL);
		DBObject o = null;
		BasicDBObject whereQuery = new BasicDBObject();
		DBCursor cursor = col.find(whereQuery);
		while(cursor.hasNext()) {
			System.out.println("loading graph");
		     o = cursor.next();
		}
		System.out.println("obj: " + o);
		byte[] bytes = (byte[]) o.get("graphbytes");
		System.out.print("bytes: " + bytes);
		return bytes;
	}
	
	
	public boolean dropDocuments() {
		switchCollection(DOC_COL);
		BasicDBObject document = new BasicDBObject();
		col.remove(document);
		DBCursor cursor = col.find();
		boolean success = false;
		while (cursor.hasNext()) {
		    col.remove(cursor.next());
		    success = true;
		}
		return success;
	}
	
	public ArrayList<HashMap<String, Float>> getAllPageRanks(PageGraph pg) {
		ArrayList<Document> documents = getAllDocuments();
		ArrayList<HashMap<String, Float>> documentsWithRank = new ArrayList<HashMap<String, Float>>();
		Matrix prMatrix = PageRank2.computePageRank(pg.getGraph());
		System.out.println("pagerank");
		System.out.println(documents.size());
		System.out.println(prMatrix.getColumnDimension());
		System.out.println(prMatrix.getColumnDimension());
		for (int i = 0; i < documents.size(); i++) {
			HashMap map = new HashMap<String, Float>();
			map.put(documents.get(i).getName(), (float) prMatrix.get(0, i));
			documentsWithRank.add(map);
		}
		return documentsWithRank;
	}
}

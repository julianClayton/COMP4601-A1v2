package edu.carleton.comp4601.SDA.db;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

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

	public boolean graphExists(String name) {
		switchCollection(GRAPH_COL);
		DBCursor cur = col.find(new BasicDBObject("name", name)).limit(1);
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
				.add("text", document.getText())
				.add("tags",document.getTags())
				.add("links", document.getLinks())
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
			document.setLinks((ArrayList<String>) obj.get("links"));
			document.setTags((ArrayList<String>) obj.get("tags"));
			document.setText((String) obj.get("text"));
			document.setName((String) obj.get("name"));
			docs.add(document);
		}
		return docs;
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
}

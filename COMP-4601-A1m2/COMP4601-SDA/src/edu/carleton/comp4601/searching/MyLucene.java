package edu.carleton.comp4601.searching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.carleton.comp4601.SDA.db.DatabaseManager;

public class MyLucene {
	
	private static final String INDEX_DIR = "/Users/lauramcdougall/Documents/Carleton/COMP4601/Lucene";
	
	private static FSDirectory dir;
	private static IndexWriter	writer;
	
	private static final String URL = "url";
	private static final String DOC_ID = "docId";
	private static final String DATE = "date";
	private static final String CONTENT = "content";
	private static final String METADATA = "metadata";

	
	public static void indexLucene(DBCursor cursor){
	
	try	{	
		dir	=	FSDirectory.open(new File(INDEX_DIR));	
		Analyzer	analyzer	=	new	StandardAnalyzer(Version.LUCENE_45);	
		IndexWriterConfig iwc	=	new	IndexWriterConfig(Version.LUCENE_45, analyzer);	
		iwc.setOpenMode(OpenMode.CREATE);	
		writer = new IndexWriter(dir, iwc);	
		
		while(cursor.hasNext()){	
			indexADoc(cursor.next());	
		}
		
	} catch	(Exception	e)	{	
		e.printStackTrace();	
		}	finally	{	
			try	{	
			 	if	(writer	!=	null)	{	
					writer.close();	
			 	}
			 	if	(dir	!=	null)	{
					dir.close();	
			 	}
			 } catch (IOException	e)	{	
					e.printStackTrace();	
			 	
			 }
		}	
	}
	
	
	private static void indexADoc(DBObject object) throws IOException	{	
		
		try{
			Document lucDoc	=	new	Document();	
			
			int docId = (int)object.get("id");
			String url = (String)object.get("url");
			String text = (String)object.get("text");
			String type = (String)object.get("metadata");
			String ts = (String)object.get("timestamp");
		
			lucDoc.add(new	IntField(DOC_ID, docId, Field.Store.YES));	
			lucDoc.add(new	StringField(URL, url, Field.Store.YES));
			lucDoc.add(new	TextField(CONTENT, text, Field.Store.YES));
			lucDoc.add(new	StringField(DATE, ts, Field.Store.YES));
			lucDoc.add(new	StringField(METADATA, type, Field.Store.YES));
	
			writer.addDocument(lucDoc);
			
		}catch(Exception e){
			System.out.println("-------Error:  "+e);	
		}
	}
	
	
	public	static ArrayList<edu.carleton.comp4601.dao.Document> query(String searchStr)	{	
		try	{	
		    IndexReader reader	= DirectoryReader.open(FSDirectory.open(new File(INDEX_DIR)));	
			IndexSearcher	searcher = new IndexSearcher(reader);	
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);	
			QueryParser parser = new QueryParser(Version.LUCENE_45, CONTENT, analyzer);	
			Query q = parser.parse(searchStr);	
			TopDocs results = searcher.search(q, 100); 	
		 	
		 	ScoreDoc[]	hits =	results.scoreDocs;
		 	
		 	ArrayList<edu.carleton.comp4601.dao.Document> docs = new ArrayList<edu.carleton.comp4601.dao.Document>();	
			
		 	for	(ScoreDoc hit :	hits)	{	
			 	Document indexDoc = searcher.doc(hit.doc);	
			 	String id = indexDoc.get(DOC_ID);
			 	
			 	if	(id	!=	null) {	
				 	edu.carleton.comp4601.dao.Document d = DatabaseManager.getInstance().findDoc(Integer.valueOf(id));	
					if	(d	!=	null)	{	
						System.out.print(d.getId());
						d.setScore(hit.score);	
						docs.add(d);	
					}	
				 } 		
		 	}
		 	reader.close();
		 	return	docs;
		} catch (Exception e)	{	
			e.printStackTrace();
		}	
		return	null;	
	}
	
	
}
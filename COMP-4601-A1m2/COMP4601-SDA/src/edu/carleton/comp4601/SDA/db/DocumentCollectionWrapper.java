package edu.carleton.comp4601.SDA.db;

import edu.carleton.comp4601.dao.DocumentCollection;

public class DocumentCollectionWrapper {
	
	private static DocumentCollectionWrapper instance;
	private DocumentCollection dc;
	
	public DocumentCollectionWrapper() {
		instance = this;
		dc = new DocumentCollection();
	}
	
	public static DocumentCollectionWrapper getInstance() {
		if (instance == null) {
			instance = new DocumentCollectionWrapper();
		}
		return instance;
	}
	
	public DocumentCollection getDocumentCollection() {
		return dc;
	}
	

}

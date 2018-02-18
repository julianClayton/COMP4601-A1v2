package edu.carleton.comp4601.SDA.resources;

import java.awt.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import Logging.Logger;
import edu.carleton.comp4601.SDA.db.DatabaseManager;
import edu.carleton.comp4601.dao.Document;
import edu.carleton.comp4601.utility.ServiceRegistrar;


@Path("sda")
public class SDA {
	Logger logger = Logger.getInstance();
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	private String name;

	public SDA() {
		String sr = ServiceRegistrar.list();
		name = "COMP4601 Searchable Document Archive V2.1:Julian and Laura" + sr;
	}
	@GET
	public String sda2() {
		return name;
	}

	@POST
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response newDocument(
			@FormParam("name") String name,
			@FormParam("text") String text,
			@FormParam("tags") String tags,
			@FormParam("links") String links,
			@Context HttpServletResponse servletResponse) throws IOException {
		
		logger.println("POST new Document: " + name);
		
		ArrayList<String> tagsList = new ArrayList<String>(Arrays.asList(tags.split("\\s*,\\s*")));		
		ArrayList<String> linksList = new ArrayList<String>(Arrays.asList(links.split("\\s*,\\s*")));
		
		if (tagsList.size() == 0) {
			return Response.status(204).build();
		}
		Document document = new Document();
		document.setName(name);
		document.setText(text);
		document.setLinks(linksList);
		document.setTags(tagsList);
		
		
		try {
			DatabaseManager.getInstance().addDocToDb(document);
		}catch (Exception e) {
			return Response.status(204).build();
		}
		return Response.ok().build();
	}
	
	@POST
	@Path("{DOC_ID}")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response updateDocumentLinks(
			@PathParam("DOC_ID") String id,
			@FormParam("links") String links,
			@FormParam("tags") String tags) {
		
		logger.println("POST new Document: " + name);
		DatabaseManager dbm = DatabaseManager.getInstance();
		
		ArrayList<String> tagsList = new ArrayList<String>(Arrays.asList(tags.split("\\s*,\\s*")));		
		ArrayList<String> linksList = new ArrayList<String>(Arrays.asList(links.split("\\s*,\\s*")));
		
		
		try {
			if (tagsList.size() > 0) {
				dbm.updateDocTags(Integer.parseInt(id), tagsList);
			}
			if (linksList.size() > 0) {
				dbm.updateDocLinks(Integer.parseInt(id), linksList);
			}
			else if (linksList.size() == 0 && tagsList.size() == 0) {
				return Response.status(204).build();
			}
		} catch (Exception e) {
			return Response.status(204).build();
		}
		return Response.ok().build();
	}
	
	
	
	@GET
	@Path("{DOC_ID}")	
	@Produces(MediaType.TEXT_HTML)
	public String getDoc(@PathParam("DOC_ID") String id) {
		Document doc = DatabaseManager.getInstance().getDocument(Integer.parseInt(id));
		return "Name: " + doc.getName() + "\n Text: " + doc.getText() + "\n Links: " + doc.getLinks() + "\n Tags: " + doc.getTags();		
	}
	@Path("{DOC_ID}")	
	@Produces(MediaType.TEXT_XML)
	public String getDocXml(@PathParam("DOC_ID") String id) {
		Document doc = DatabaseManager.getInstance().getDocument(Integer.parseInt(id));
		return "Name: " + doc.getName() + "\n Text: " + doc.getText() + "\n Links: " + doc.getLinks() + "\n Tags: " + doc.getTags();		
	}
	
	@DELETE
	@Path("{DOC_ID}")
	public Response deleteDoc(@PathParam("DOC_ID") String id) {
		DatabaseManager dbm = DatabaseManager.getInstance();
		if (dbm.deleteDocument(Integer.parseInt(id))) {
			return Response.ok().build();
		}
		return Response.status(204).build();
	}
	
	@GET 
	@Path("delete/{TAGS}")
	@Produces(MediaType.TEXT_XML)
	public Response deleteDocumentWithTags(@PathParam("TAGS") String tags) {
		DatabaseManager dbm = DatabaseManager.getInstance();
		ArrayList<String> tagsList = new ArrayList<String>(Arrays.asList(tags.split("\\s*,\\s*")));		
		if (dbm.deleteDocumentsWithTags(tagsList)) {
			return Response.ok().build();

		}
		return Response.status(204).build();
	}
	@GET 
	@Path("search/{TAGS}")
	@Produces(MediaType.TEXT_HTML)
	public String searchDocumentWithTags(@PathParam("TAGS") String tags) {
		DatabaseManager dbm = DatabaseManager.getInstance();
		ArrayList<String> tagsList = new ArrayList<String>(Arrays.asList(tags.split("\\s*,\\s*")));	
		String titleString = "";
		for (String tag : tagsList) {
			titleString = titleString + tag + ", ";
		}
		ArrayList<Document> docs = dbm.getDocumentsWithTags(tagsList);
		String htmlList = "<ul>";
		for (Document doc : docs) {
			String link = "<a href=\"http://localhost:8080/COMP4601-SDA/rest/sda/"+doc.getId() + "\">" + doc.getName() +" </a>";
			htmlList = htmlList +  "<li>" + link + "</li>";
		}
		htmlList = htmlList + "</ul>";
		return "<html><head><title>Document List</title></head><body><h1>Documents with tag(s) " + titleString + "</h1>" + htmlList +"</body></html>";
	}
	@GET 
	@Path("documents")
	@Produces(MediaType.TEXT_HTML)
	public String getAllDocuments() {
		DatabaseManager dbm = DatabaseManager.getInstance();

		ArrayList<Document> docs = dbm.getAllDocuments();
		String htmlList = "<ul>";
		for (Document doc : docs) {
			String link = "<a href=\"http://localhost:8080/COMP4601-SDA/rest/sda/"+doc.getId() + "\">" + doc.getName() +" </a>";
			htmlList = htmlList +  "<li>" + link + "</li>";
		}
		htmlList = htmlList + "</ul>";
		return "<html><head><title>Document List</title></head><body><h1>All Documents</h1>" + htmlList +"</body></html>";
	}
	
	
	
	public String sayXML() {
		return "<?xml version=\"1.0\"?>" + "<bank> " + name + " </bank>";
	}
}

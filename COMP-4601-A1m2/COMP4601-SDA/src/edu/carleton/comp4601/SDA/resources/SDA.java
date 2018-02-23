package edu.carleton.comp4601.SDA.resources;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import java.util.concurrent.TimeUnit;
import com.mongodb.MongoException;
import edu.carleton.comp4601.SDA.db.DatabaseManager;
import edu.carleton.comp4601.dao.Document;
import edu.carleton.comp4601.dao.DocumentCollection;
import edu.carleton.comp4601.searching.MyLucene;
import edu.carleton.comp4601.pagerank.PageRank;
import edu.carleton.comp4601.utility.SDAConstants;
import edu.carleton.comp4601.utility.SearchResult;
import edu.carleton.comp4601.utility.SearchServiceManager;
import edu.carleton.comp4601.utility.ServiceRegistrar;


@Path("sda")
public class SDA {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	private String name;
	DocumentCollection docCollection;
	public SDA() {
		name = "COMP4601 Searchable Document Archive V2.1: Julian and Laura";
		DatabaseManager.getInstance().getAllDocuments();
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sda2() {
		return "<html><head><title>COMP 4601</title></head><body><h1>"+ name +"</h1></body></html>";
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
		document.setUrl(name);
		
		try {
			DatabaseManager.getInstance().addDocToDb(document);
			MyLucene.addDocument(document);
		}catch (Exception e) {
			e.printStackTrace();
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
		String regex = "\\d+";
		if (!id.matches(regex)) {
			return resetDocuments(id);
		}
		StringBuilder htmlBuilder = new StringBuilder();
		Document doc = DatabaseManager.getInstance().getDocument(Integer.parseInt(id));
		if (doc == null) {
			return pageNotFound();
		}
		htmlBuilder.append("<html>");
		htmlBuilder.append("<head><title>" + doc.getName() + "</title></head>");
		htmlBuilder.append("<body><h1>" + doc.getName() + "</h1>");
		htmlBuilder.append("<p>" + doc.getText() + "</p>");
		htmlBuilder.append("<h1> Links </h1>");
		htmlBuilder.append("<ul>");
		for (String s : doc.getLinks())
		{
			htmlBuilder.append("<li>");
			htmlBuilder.append("<a href=\"");
			htmlBuilder.append(s);
			htmlBuilder.append("\">");
			htmlBuilder.append(s);
			htmlBuilder.append("</a>");
			htmlBuilder.append("</li>");
		}
		
		htmlBuilder.append("</ul>");
		htmlBuilder.append("<h1> Tags </h1>");
		htmlBuilder.append("<ul>");
		for (String s : doc.getTags())
		{
			htmlBuilder.append("<li>");
			htmlBuilder.append(s);
			htmlBuilder.append("</li>");
		}
		htmlBuilder.append("<h1>" + doc.getScore() + "</h1>");
		htmlBuilder.append("</ul></body>");
		htmlBuilder.append("</html>");
		
		return htmlBuilder.toString();		
	}

	@GET
	@Path("{DOC_ID}")	
	@Produces(MediaType.TEXT_XML)
	public String getDocXml(@PathParam("DOC_ID") String id) {
		String regex = "\\d+";
		if (!id.matches(regex)) {
			return resetDocuments(id);
		}
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
		ArrayList<String> tagsList = new ArrayList<String>(Arrays.asList(tags.split("[+]")));	
			
		String titleString = "";
		for (String tag : tagsList) {
			titleString = titleString + tag + ", ";
		}
		ArrayList<Document> docsList = new ArrayList<Document>();
		ArrayList<Document> queryDocs = MyLucene.query(tags);
		SearchResult sr = SearchServiceManager.getInstance().query(tags); 
	    try {
	    	sr.await(SDAConstants.TIMEOUT, TimeUnit.SECONDS);
	    } catch (InterruptedException e){
	    	e.printStackTrace();
	    }
		docsList.addAll(sr.getDocs());
		docsList.addAll(queryDocs);
		String htmlList = "<ul>";
		for (Document doc : docsList) {
			String link = "<a href=\"http://localhost:8080/COMP4601-SDA/rest/sda/"+doc.getId() + "\">" + doc.getName() +" </a>";
			htmlList = htmlList +  "<li>" + link + "</li>";
		}
		htmlList = htmlList + "</ul>";
		for (Document doc : docsList) {
			System.out.println(doc.getName() + " " + doc.getTags());
		}
		
		return "<html><head><title>Document List</title></head><body><h1>Documents with tag(s) " + titleString + "</h1>" + htmlList +"</body></html>";
	}
	
	@GET 
	@Path("search/{TAGS}")
	@Produces(MediaType.TEXT_XML)
	public DocumentCollection searchDocumentWithTagsXML(@PathParam("TAGS") String tags) {
		ArrayList<String> tagsList = new ArrayList<String>(Arrays.asList(tags.split("[+]")));	
			
		String titleString = "";
		for (String tag : tagsList) {
			titleString = titleString + tag + ", ";
		}
		ArrayList<Document> docsList = new ArrayList<Document>();
		ArrayList<Document> queryDocs = MyLucene.query(tags);
	    DocumentCollection dc = new DocumentCollection();
		SearchResult sr = SearchServiceManager.getInstance().search(tags); 
	    try {
	    	sr.await(SDAConstants.TIMEOUT, TimeUnit.SECONDS);
	    } catch (InterruptedException e){
	    	e.printStackTrace();
	    }
		docsList.addAll(queryDocs);
		docsList.addAll(dc.getDocuments());
		dc.setDocuments(docsList);
		
		return dc;
	}
	
	@GET 
	@Path("query/{TERMS}")
	@Produces(MediaType.TEXT_HTML)
	public String queryDocsWithTerms(@PathParam("TERMS") String terms) {
	    ArrayList<Document> queryDocs = MyLucene.query(terms);
	    String htmlList = "<ul>";
		for (Document doc : queryDocs) {
			String link = "<a href=\"http://localhost:8080/COMP4601-SDA/rest/sda/"+doc.getId() + "\">" + doc.getName() + " Score: " + doc.getScore() +" </a>";
			htmlList = htmlList +  "<li>" + link + "</li>";
		}
		htmlList = htmlList + "</ul>";
		return "<html><head><title>Document List</title></head><body><h1>Documents that match terms(s) " + terms + "</h1>" + htmlList +"</body></html>";
	}
	@GET
	@Path("query/{TERMS}")
	@Produces(MediaType.TEXT_XML)
	public DocumentCollection queryDocsWithTermsXML(@PathParam("TERMS") String terms) {
	    ArrayList<Document> queryDocs = MyLucene.query(terms);
	    DocumentCollection docs = new DocumentCollection();
	    docs.setDocuments(queryDocs);
	    System.out.println("Issuing Query for " + terms);
		return  docs;
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
	
	@GET
	@Path("list")
	@Produces(MediaType.TEXT_HTML)
	public String listDiscoveredServices() {
		String sr = ServiceRegistrar.list();
		return sr;
	}
	
	@GET
	@Path("noboost")
	@Produces(MediaType.TEXT_HTML)
	public String resetBoost() {
		 //ArrayList<Document> queryDocs = MyLucene.query(terms);
		Float boost = 1.0f; 
		MyLucene.resetBoostLucene(DatabaseManager.getInstance().getAllDocCursor(), boost);
		return "Boost reset";
	}
	@GET
	@Path("boost")
	@Produces(MediaType.TEXT_HTML)
	public String boost() {
		try {
			PageRank.getInstance().computePageRank();
			MyLucene.reindexLucene(DatabaseManager.getInstance().getAllDocCursor(), PageRank.getInstance().getBoostMap());
		} catch (Exception e) {
			e.printStackTrace();
			return "<html><head><title>Documents Boost Failed</title></head><body><h1>Documents Boost Failed!</h1></body></html>";
		}
		return "<html><head><title>Documents Boosted!</title></head><body><h1>Documents Boosted!</h1></body></html>";
	}
	
	private String resetDocuments(String path) {
		if (!path.toLowerCase().equals("reset")) {
			return pageNotFound();
		}
		DatabaseManager dbm = DatabaseManager.getInstance();
		try {
			dbm.dropDocuments();
		} catch (MongoException e) {
			return "<html><head><title>Document Reset Failed!</title></head><body><h1>Documents Dropped!</h1></body></html>";
		}
		return "<html><head><title>Documents Dropped!</title></head><body><h1>Documents Dropped!</h1></body></html>";
	}
	public String pageNotFound() {
		return "<html><head><title>404: Resource not foudn</title></head><body><h1>404</h1> The page you are looking for does not exist</body></html>";
	}
	
	
	
	@GET
	@Path("pagerank")
	@Produces(MediaType.TEXT_HTML)
	public String getGraph2()  {
        ArrayList<HashMap<Integer, Float>> pr = PageRank.getInstance().computePageRank();
		ArrayList<HashMap<Integer, Float>> docsWithRank = PageRank.getInstance().computePageRank();
		
		StringBuilder htmlBuilder = new StringBuilder();
		htmlBuilder.append("<html>");
		htmlBuilder.append("<head><title> Document Page Ranks </title></head>");
		htmlBuilder.append("<body>");
		htmlBuilder.append("<table style=\"width:100%\">");	
		System.out.println(docsWithRank.size());
		for(HashMap map : docsWithRank) {
			htmlBuilder.append("<tr>");
			htmlBuilder.append("<td>" + map.keySet() + "</td>");
			htmlBuilder.append("<td>" + map.values() + "</td>");
			htmlBuilder.append("</tr>");
		}		
		return htmlBuilder.toString();
	}

}



package edu.carleton.comp4601.SDA.resources;

import java.awt.List;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import edu.uci.ics.crawler4j.crawler.Page;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;
import com.mongodb.MongoException;
import edu.carleton.comp4601.SDA.db.DatabaseManager;
import edu.carleton.comp4601.dao.Document;
import edu.carleton.comp4601.dao.DocumentCollection;
import edu.carleton.comp4601.searching.MyLucene;
import edu.carleton.comp4601.graph.PageGraph;
import org.jgrapht.*;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.Graph;
import edu.carleton.comp4601.graph.Vertex;
import edu.carleton.comp4601.networking.Marshaller;
import edu.carleton.comp4601.pagerank.PageRank3;
import edu.carleton.comp4601.utility.ServiceRegistrar;
import edu.uci.ics.crawler4j.crawler.Page;


@Path("sda")
public class SDA {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	private String name;
	
	public SDA() {
		name = "COMP4601 Searchable Document Archive V2.1: Julian and Laura";
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
		
		
		try {
			DatabaseManager.getInstance().addDocToDb(document);
			MyLucene.addDocument(document);
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
	@Path("/reset")
	@Produces(MediaType.TEXT_HTML)
	public String reset() {
		boolean reset = DatabaseManager.getInstance().deleteAllDocuments();
		if (reset){
			return "All documents reset";
		}
		return "ERROR: could not remove docs";
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
	@Path("query/{TERMS}")
	@Produces(MediaType.TEXT_HTML)
	public String queryDocsWithTerms(@PathParam("TERMS") String terms) {
	    ArrayList<Document> queryDocs = MyLucene.query(terms);
	    DocumentCollection docs = new DocumentCollection();
	    docs.setDocuments(queryDocs);
	    String htmlList = "<ul>";
		for (Document doc : queryDocs) {
			String link = "<a href=\"http://localhost:8080/COMP4601-SDA/rest/sda/"+doc.getId() + "\">" + doc.getName() + " Score: " + doc.getScore() +" </a>";
			htmlList = htmlList +  "<li>" + link + "</li>";
		}
		htmlList = htmlList + "</ul>";
		return "<html><head><title>Document List</title></head><body><h1>Documents that match terms(s) " + terms + "</h1>" + htmlList +"</body></html>";
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
	private String resetDocuments(String path) {
		if (!path.toLowerCase().equals("reset")) {
			return pageNotFound();
		}
		DatabaseManager dbm = DatabaseManager.getInstance();
		try {
			dbm.dropDocuments();
		} catch (MongoException e) {
			return "<html><head><title>Document Reset Failed!</title></head></html>";
		}
		return "<html><head><title>Documents Dropped!</title></head></html>";
	}
	public String pageNotFound() {
		return "<html><head><title>404: Resource not foudn</title></head><body><h1>404</h1> The page you are looking for does not exist</body></html>";
	}
	
	@GET
	@Path("pagerank")
	@Produces(MediaType.TEXT_HTML)
	public String getDocPageRanks() {
		DatabaseManager dbm = DatabaseManager.getInstance();
		PageGraph pg = new PageGraph();
		Vertex vertex = new Vertex("", new Page(null));
		Page page = new Page(null);
		dbm.getAllPageRanks();
		//ArrayList<HashMap<String, Float>> documents = dbm.getAllPageRanks();
		/*for (HashMap doc : documents) {
			System.out.println(doc.keySet());
			System.out.println(doc.values());
		}*/
		
		return "";
	}
	
	@GET
	@Path("graph")
	@Produces(MediaType.TEXT_HTML)
	public String getGraph() {
		PageGraph pg = new PageGraph();
		Vertex vertex = new Vertex("", new Page(null));
		Page page = new Page(null);
		Graph directedGraph = new DefaultDirectedGraph<Vertex, DefaultEdge>(DefaultEdge.class);
	  	java.io.InputStream	input;	
	  	org.apache.tika.metadata.Metadata	metadata	=	new org.apache.tika.metadata.Metadata();
	  	ParseContext	context	=	new ParseContext();	
	  	Parser	parser	=	new AutoDetectParser();
		DatabaseManager dbm = DatabaseManager.getInstance();
		byte[] b = dbm.loadGraphFromDB2();
		pg = null;
		try {
			pg = (PageGraph) Marshaller.deserializeObject(b);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(pg.getName());
		System.out.println(pg.getGraph());
		return pg.getGraph().toString();
	}
	@GET
	@Path("pagerank2")
	@Produces(MediaType.TEXT_HTML)
	public String getGraph2() {
		//PageGraph pg = new PageGraph();
		//Vertex vertex = new Vertex("", new Page(null));
		//Page page = new Page(null);
		//Graph directedGraph = new DefaultDirectedGraph<Vertex, DefaultEdge>(DefaultEdge.class);
	  	java.io.InputStream	input;	
	  	org.apache.tika.metadata.Metadata	metadata	=	new org.apache.tika.metadata.Metadata();
	  	ParseContext	context	=	new ParseContext();	
	  	Parser	parser	=	new AutoDetectParser();
		DatabaseManager dbm = DatabaseManager.getInstance();
		
		ArrayList<HashMap<String, Float>> psg = PageRank3.getInstance().computePageRank();
		return "";
	}
	
	public String sayXML() {
		return "<?xml version=\"1.0\"?>" + "<bank> " + name + " </bank>";
	}
}



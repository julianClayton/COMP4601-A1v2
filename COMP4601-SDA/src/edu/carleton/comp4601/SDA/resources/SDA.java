package edu.carleton.comp4601.SDA.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import edu.carleton.comp4601.utility.ServiceRegistrar;


@Path("sda")
public class SDA {
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
	public String printName() {
		
		return name;
		
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	public String sayXML() {
		return "<?xml version=\"1.0\"?>" + "<bank> " + name + " </bank>";
	}

}

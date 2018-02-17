package edu.carleton.comp4601.SDA.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;


@Path("sda")
public class SDA {
	
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	private String name;

	public SDA() {
		name = "Julian and Laura's SDA";
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

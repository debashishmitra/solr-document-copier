package com.debashish.solr.dataCopier.rest;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author Debashish Mitra
 *
 */
@ApplicationPath("rest")
public class RestBootstrap extends ResourceConfig {

	public RestBootstrap() {
		packages("com.macys");
	}
}
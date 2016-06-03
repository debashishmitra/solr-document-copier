/**
 * 
 */
package com.debashish.solr.dataCopier.rest.requestObjects;

/**
 * @author Debashish Mitra
 *
 */
public class UserDataSource {

	private String databaseConnectionString;
	private String username;
	private String password;

	public String getDatabaseConnectionString() {
		return databaseConnectionString;
	}

	public void setDatabaseConnectionString(String databaseConnectionString) {
		this.databaseConnectionString = databaseConnectionString;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}

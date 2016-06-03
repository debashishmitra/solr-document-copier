/**
 * 
 */
package com.debashish.solr.dataCopier.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Debashish Mitra
 *
 */
public class A {
	public static void main(String[] args) throws FileNotFoundException {
		Scanner s = new Scanner(new File("C:\\zzz.txt"));
		StringBuilder sb = new StringBuilder();
		while (s.hasNextLine()) {
			sb.append(s.nextLine());
		}
		s.close();
		String out = convert(sb.toString());
		System.out.println(out);
	}

	public static String convert(String in) {
		StringBuilder sb = new StringBuilder(in);
		String s = StringUtils.remove(sb.toString(), "\"");
		s = s.replaceAll("\\s", "");
		s = StringUtils.replace(s, "},{", ")+OR+(").replace("{", "(").replace("}", ")").replace(",", "+AND+");
		String a = "q=*:*&fq=" + s + "&wt=json&indent=true";
		return a;
	}
}

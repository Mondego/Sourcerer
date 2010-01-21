/*
 * Sourcerer: An infrastructure for large-scale source code analysis.
 * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package edu.uci.ics.sourcerer.scs.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 15, 2009
 *
 */
public class SCSServlet extends HttpServlet {
	
	protected void service(HttpServletRequest req, HttpServletResponse resp) {
		
		String query = (String) req.getParameter("query");
		
		try {
			resp.getWriter().print(getGETResult(query));
		} catch (IOException e) {
			// TODO handle this ?
			// should not happen
		}
	}
	
	private String getGETResult(String query) {
		
		//TODO move this to servlet config
		String urlPart = // "http://nile.ics.uci.edu:8983/solr/scs/select/";
			"http://localhost:8984/solr/scs/select/";
		
		
		//TODO handle this using query parser
		String[] queryParts = query.split("\\s");
		
		StringBuilder sbSname = new StringBuilder();
		sbSname.append("+sname_cc_split:(");
		for(int i=0; i<queryParts.length; i++){
			sbSname.append(queryParts[i]);
			if(i==queryParts.length-1)
				sbSname.append(")^3.0+OR+sname_cc_split:(");
			else
				sbSname.append(")^3.0");
		}
		
		try {
			query = URLEncoder.encode(query, "UTF-8");
			
		} catch (UnsupportedEncodingException e) {
			// e.printStackTrace();
		}
		String queryString = "start=0&rows=10&q=" + query + sbSname.toString();
		
		
		//?start=0&rows=10&";
		String result = "";
		
		try {
			result = sendGetCommand(queryString, urlPart);
		} catch (IOException e) {
			result = e.getMessage();
		}

		return result;

	}
	
	/**
     * Send the command to Solr using a GET
     * @param queryString
     * @param url
     * @return
     * @throws IOException
     */
    private String sendGetCommand(String queryString, String url)
            throws IOException
    {
        String results = null;
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(url);
        get.setQueryString(queryString.trim());

        client.executeMethod(get);
        try
        {
            // Execute the method.
            int statusCode = client.executeMethod(get);

            if (statusCode != HttpStatus.SC_OK)
            {
                System.err.println("Method failed: " + get.getStatusLine());
                results = "Method failed: " + get.getStatusLine();
            }

            // Read the response body.
            byte[] responseBody = get.getResponseBody();

            // Deal with the response.
            // Use caution: ensure correct character encoding and is not binary data
            results = new String(responseBody);
        }
        catch (HttpException e)
        {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        }
        catch (IOException e)
        {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            // Release the connection.
            get.releaseConnection();
        }
        return results;
    }


	
}

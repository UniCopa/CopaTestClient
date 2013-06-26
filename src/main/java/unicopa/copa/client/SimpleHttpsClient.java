/*
 * Copyright (C) 2013 UniCoPA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package unicopa.copa.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import unicopa.copa.base.com.request.AbstractRequest;
import unicopa.copa.base.com.request.AbstractResponse;
import unicopa.copa.base.com.exception.APIException;
import unicopa.copa.base.com.exception.InternalErrorException;
import unicopa.copa.base.com.exception.PermissionException;
import unicopa.copa.base.com.exception.RequestNotPracticableException;

/**
 * A simple HTTPS client based on Jetty.
 * 
 * @author Felix Wiemuth
 */
public class SimpleHttpsClient {

    private HttpClient client;
    private String loginURL;
    private String reqURL;

    public SimpleHttpsClient(String loginUrl, String reqUrl, boolean useSSL) {
	this.loginURL = loginUrl;
	this.reqURL = reqUrl;
	// Instantiate and configure the SslContextFactory
	SslContextFactory sslContextFactory = new SslContextFactory();
	sslContextFactory.setTrustAll(true);
	if (useSSL) {
	    // Instantiate HttpClient with the SslContextFactory
	    client = new HttpClient(sslContextFactory);
	} else {
	    client = new HttpClient();
	}

	// Configure HttpClient, for example:
	client.setFollowRedirects(false);
    }

    public void start() {
	try {
	    client.start();
	} catch (Exception ex) {
	    Logger.getLogger(SimpleHttpsClient.class.getName()).log(
		    Level.SEVERE, null, ex);
	}
    }

    public void authenticate(String user, String pwd) {
	try {
	    ContentResponse responseAuth = client.POST(loginURL)
		    .param("j_username", user).param("j_password", pwd).send();
	} catch (InterruptedException ex) {
	    Logger.getLogger(SimpleHttpsClient.class.getName()).log(
		    Level.SEVERE, null, ex);
	} catch (TimeoutException ex) {
	    Logger.getLogger(SimpleHttpsClient.class.getName()).log(
		    Level.SEVERE, null, ex);
	} catch (ExecutionException ex) {
	    Logger.getLogger(SimpleHttpsClient.class.getName()).log(
		    Level.SEVERE, null, ex);
	}
    }

    public AbstractResponse sendRequest(AbstractRequest request)
	    throws InterruptedException, TimeoutException, ExecutionException,
	    APIException, PermissionException, RequestNotPracticableException,
	    InternalErrorException {
	String send = request.serialize();
	System.out.println("[CLIENT] Send to server:\n" + send);
	ContentResponse response = client.POST(reqURL).param("req", send)
		.send();
	System.out.println("[CLIENT] Received from Server:\n"
		+ response.getContentAsString());
	return AbstractResponse.deserialize(response.getContentAsString());
    }

    /**
     * Send the request by text, not using automatic serialization. The POST
     * will contain the "req" parameter with the sepcified text as parameter.
     * 
     * @param text
     * @return the response from the server as text, or null if an exception
     *         occured
     */
    public String sendRequestAsText(String text) {
	try {
	    return client.POST(reqURL).param("req", text).send()
		    .getContentAsString();
	} catch (InterruptedException ex) {
	    Logger.getLogger(SimpleHttpsClient.class.getName()).log(
		    Level.SEVERE, null, ex);
	} catch (TimeoutException ex) {
	    Logger.getLogger(SimpleHttpsClient.class.getName()).log(
		    Level.SEVERE, null, ex);
	} catch (ExecutionException ex) {
	    Logger.getLogger(SimpleHttpsClient.class.getName()).log(
		    Level.SEVERE, null, ex);
	}
	return null;
    }

    public void stop() throws Exception {
	client.stop();
    }
}

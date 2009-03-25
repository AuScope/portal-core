package org.auscope.portal.server.web;

import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

/**
 * User: Mathew Wyatt
 * Date: 25/03/2009
 * Time: 11:37:40 AM
 */
public class HttpServiceCaller {

    public BufferedInputStream callHttpUrl(String serviceUrl, String postData) throws IOException {

        URL url = new URL(serviceUrl);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();

        conn.setRequestMethod("POST");
        conn.setAllowUserInteraction(false); // no user interact [like pop up]
        conn.setDoOutput(true); // want to send
        conn.setRequestProperty( "Content-type", "text/xml" );
        conn.setRequestProperty( "Content-length", Integer.toString(postData.length()));
        OutputStream ost = conn.getOutputStream();
        PrintWriter pw = new PrintWriter(ost);
        pw.print(postData); // here we "send" our body!
        pw.flush();
        pw.close();

        return new BufferedInputStream(conn.getInputStream());
    }

    public String responseToString(BufferedInputStream stream) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while((line = reader.readLine()) != null) {
            stringBuffer.append(line);
        }
        return stringBuffer.toString();
    }

    public BufferedInputStream stringToStream(String string) throws IOException {
        return new BufferedInputStream(new ByteArrayInputStream(string.getBytes()));
    }

    public static void main(String[] args) throws IOException {
        HttpServiceCaller htps = new HttpServiceCaller();
        System.out.println(htps.responseToString(htps.callHttpUrl("http://www.gsv-tb.dpi.vic.gov.au/AuScope-MineralOccurrence/services?",
                                                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                                        "<wfs:GetFeature version=\"1.1.0\" xmlns:mo=\"urn:cgi:xmlns:GGIC:MineralOccurrence:1.0\" xmlns:wfs=\"http://www.opengis.net/wfs\"\n" +
                                                                        "        xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                                                        "        xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" maxFeatures=\"20\">\n" +
                                                                        "    <wfs:Query typeName=\"mo:Mine\"/>\n" +
                                                                        "</wfs:GetFeature>")));
    }
}

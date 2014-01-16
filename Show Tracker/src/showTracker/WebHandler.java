package showTracker;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class WebHandler
{
	/**
	 * Gets a String representation of the contents of a webpage.
	 * @param url	A String containing the URL of the page to get the contents of.
	 * @param data	A Map<String, String> containing the request headers or null.
	 * @param c		A Cookies containing the cookies to send to the webpage or null.
	 * @return		A String containing the contents of the page at the specified URL.
	 * @throws IOException if an I/O exception occurs.
	 * @throws MalformedURLException if the url is malformed(e.g. https and not http).
	 */
	protected static String getPage(String url, Map<String, String> data, Cookies c) throws MalformedURLException, IOException
	{
		StringBuilder str = new StringBuilder();
		String line;
		
		//set up the URL connection and output stream
		HttpURLConnection huc = (HttpURLConnection) new URL(url).openConnection();
		//set cookies if we need to
		if(c != null)
			huc.setRequestProperty("Cookie", c.toString());
		huc.setRequestMethod("POST");
		huc.setDoInput(true);

		//if form data was supplied
		if(data != null)
		{
			//set doOutput to true
			huc.setDoOutput(true);
			//get the output stream
			DataOutputStream dos = new DataOutputStream(huc.getOutputStream());
			//build and send the request
			Iterator<String> keyIter = data.keySet().iterator();
			StringBuilder content = new StringBuilder();
			for(int i=0; keyIter.hasNext(); i++)
			{
				String key = keyIter.next();
				if(i>0) content.append("&");
				content.append(URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(data.get(key), "UTF-8"));
			}
			dos.writeBytes(content.toString());
			dos.close();
		}
		
		//get the page contents stream
		BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream()));
		
		//output the characters into a StringBuilder
		while((line=in.readLine()) != null)
			str.append(line+"\n");
		
		//return the string
		return str.toString();
	}
	
	/**
	 * Gets a String representation of the contents of a webpage.
	 * @param url	A String containing the URL of the page to get the contents of.
	 * @return		A String containing the contents of the page at the specified URL.
	 * @throws IOException if an I/O exception occurs.
	 * @throws MalformedURLException if the url is malformed(e.g. https and not http).
	 */
	protected static String getBarePage(String url) throws MalformedURLException, IOException
	{
		StringBuilder str = new StringBuilder();
		String line;
		
		//set up the URL connection and output stream
		HttpURLConnection huc = (HttpURLConnection) new URL(url).openConnection();
		
		//get the page contents stream
		BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream()));
		
		//output the characters into a StringBuilder
		while((line=in.readLine()) != null)
			str.append(line+"\n");
		
		//return the string
		return str.toString();
	}
	
	/**
	 * @param url	A String containing the URL of the page to get the contents of.
	 * @param data	A Map<String, String> containing the request headers.
	 * @param c		A Cookies containing the cookies to send to the webpage or null.
	 * @return		A Map<String, List<String>> that contains the response headers 
	 * @throws IOException if an I/O exception occurs.
	 * @throws MalformedURLException if the url is malformed(e.g. https and not http).
	 */
	protected static Map<String, List<String>> sendRequest(String url, Map<String, String> data, Cookies c) throws MalformedURLException, IOException
	{
		//set up the URL connection and output stream
		HttpURLConnection huc = (HttpURLConnection) new URL(url).openConnection();
		//set cookies if we need to
		if(c != null)
			huc.setRequestProperty("Cookie", c.toString());
		huc.setRequestMethod("POST");
		huc.setDoOutput(true);
		DataOutputStream dos = new DataOutputStream(huc.getOutputStream());

		//build and send the request
		Iterator<String> keyIter = data.keySet().iterator();
		StringBuilder content = new StringBuilder();
		for(int i=0; keyIter.hasNext(); i++)
		{
			String key = keyIter.next();
			if(i>0) content.append("&");
			content.append(URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(data.get(key), "UTF-8"));
		}
		dos.writeBytes(content.toString());
		dos.close();
		
		//return the response headers
		return huc.getHeaderFields();
	}
}
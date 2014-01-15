package showTracker;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cookies
{
	public HashMap<String, String> cookies;
	
	public Cookies(Map<String, List<String>> headers)
	{
		if(headers == null) return;
		cookies = new HashMap<String, String>();
		List<String> rawCookies = headers.get("Set-Cookie");
		for(int i=0; i<rawCookies.size(); i++)
		{
			String key = rawCookies.get(i).substring(0, rawCookies.get(i).indexOf('=')),
					value = rawCookies.get(i).substring(rawCookies.get(i).indexOf('=')+1, rawCookies.get(i).indexOf(';'));
			cookies.put(key, value);
		}
	}
	
	public String toString()
	{
		return cookies.toString().substring(1, cookies.toString().length()-1).replace(',', ';');
	}
}

package de.faeustl.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DHB_Rest_Services {

	private static OkHttpClient client = new OkHttpClient();
	
	public static JsonArray getTeamInfo( String pTeam)
	{
		
		
		
		String json = "";
		
		
		JsonArray standing = null;
		   HttpUrl url = new HttpUrl.Builder()
			       .scheme("https")
			       .host("dhbdata.fmp.sportradar.com")
			       .addPathSegment("feeds")
			       .addPathSegment("internal")
			       .addPathSegment("de")
			       .addPathSegment("Europe:Berlin")
			       .addPathSegment("gismo")
			       .addPathSegment("team_info")
			       .addPathSegment("1593")
			       .addPathSegment("579")
			       .addPathSegment(pTeam)
			       .build();
		   
//		   ttps://dhbdata.fmp.sportradar.com/feeds/internal/de/Europe:Berlin/gismo/standings/5393
			
	   Request request = new Request.Builder()
					.url(url)
					.addHeader("Content-Type", "application/json")
					.addHeader("Cache-Control", "no-cache")
					.get()				
					.build();
			Response response = null;
			
			try {
				response = client.newCall(request).execute();
				JsonParser jsonParsor = new JsonParser();
				json = response.body().string();
				JsonObject   arr = (JsonObject)jsonParsor.parse(json);   
				//System.out.println(arr);
				 standing = arr.get("doc").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonObject().get("team_info").getAsJsonObject().get("squad").getAsJsonArray();
						
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		
		return standing;
	}

	public static JsonArray getSpielplan( String pTournament, String pSeason)
	{
		
		
		
		String json = "";
		
		JsonObject obj = null;
		JsonArray matschDays = null;
	
		   HttpUrl url = new HttpUrl.Builder()
			       .scheme("https")
			       .host("dhbdata.fmp.sportradar.com")
			       .addPathSegment("feeds")
			       .addPathSegment("internal")
			       .addPathSegment("de")
			       .addPathSegment("Europe:Berlin")
			       .addPathSegment("gismo")
			       .addPathSegment("fixtures")
			       .addPathSegment(pTournament)
			       .addPathSegment(pSeason)
			       .build();
			
	   Request request = new Request.Builder()
					.url(url)
					.addHeader("Content-Type", "application/json")
					.addHeader("Cache-Control", "no-cache")
					.get()				
					.build();
			Response response = null;
			
			try {
				response = client.newCall(request).execute();
				JsonParser jsonParsor = new JsonParser();
				json = response.body().string();
				JsonObject   arr = (JsonObject)jsonParsor.parse(json);   
				
				 matschDays = arr.get("doc")
						.getAsJsonArray().get(0).getAsJsonObject()
						.getAsJsonObject("data")
						.getAsJsonObject("tournament")
						.getAsJsonArray("phases")
						.get(0).getAsJsonObject()
						.getAsJsonArray("matchdays")
						;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		
		return matschDays;
	}

	public static JsonArray getTabelle( String pTournament)
		{
			
			
			
			String json = "";
			
			
			JsonArray standing = null;
	
			   HttpUrl url = new HttpUrl.Builder()
				       .scheme("https")
				       .host("dhbdata.fmp.sportradar.com")
				       .addPathSegment("feeds")
				       .addPathSegment("internal")
				       .addPathSegment("de")
				       .addPathSegment("Europe:Berlin")
				       .addPathSegment("gismo")
	//			       .addPathSegment("fixtures")
				       .addPathSegment("standings")
				       .addPathSegment(pTournament)
				       .build();
			   
	//		   ttps://dhbdata.fmp.sportradar.com/feeds/internal/de/Europe:Berlin/gismo/standings/5393
				
		   Request request = new Request.Builder()
						.url(url)
						.addHeader("Content-Type", "application/json")
						.addHeader("Cache-Control", "no-cache")
						.get()				
						.build();
				Response response = null;
				
				try {
					response = client.newCall(request).execute();
					JsonParser jsonParsor = new JsonParser();
					json = response.body().string();
					JsonObject   arr = (JsonObject)jsonParsor.parse(json);   
					//System.out.println(arr);
					 standing = arr.get("doc").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonObject().get("standing_main").getAsJsonArray();
							
					 System.out.println(standing);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			
			return standing;
		}
	
	public static JsonObject getMatch_Info( String pMatchID)
	{
//		https://dhbdata.fmp.sportradar.com/feeds/internal/de/Europe:Berlin/gismo/match_info/30065
		
		String json = "";
		
		
		JsonObject match_info = null;

		   HttpUrl url = new HttpUrl.Builder()
			       .scheme("https")
			       .host("dhbdata.fmp.sportradar.com")
			       .addPathSegment("feeds")
			       .addPathSegment("internal")
			       .addPathSegment("de")
			       .addPathSegment("Europe:Berlin")
			       .addPathSegment("gismo")

			       .addPathSegment("match_info")
			       .addPathSegment(pMatchID)
			       .build();
		   
		   Request request = new Request.Builder()
					.url(url)
					.addHeader("Content-Type", "application/json")
					.addHeader("Cache-Control", "no-cache")
					.get()				
					.build();
			Response response = null;
			
			try {
				response = client.newCall(request).execute();
				JsonParser jsonParsor = new JsonParser();
				json = response.body().string();
				JsonObject   arr = (JsonObject)jsonParsor.parse(json);   
				//System.out.println(arr);
				match_info = arr.get("doc").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonObject().get("match_info").getAsJsonObject();
						
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally{
				response.close();
			}
		
		return match_info;
		   
	}
	
}

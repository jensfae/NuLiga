package de.faeustl.rest;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.faeustl.model.BHVSpielRest;
import de.faeustl.model.Tabelle;
import de.faeustl.model.TabellenPlatz;
import de.faeustl.wp.Search;
import de.faeustl.wp.Writer;
import nu.liga.open.rs.v2014.dto.championships.GroupTableTeamDTO;
import nu.liga.open.rs.v2014.dto.championships.TeamGroupTablesDTO;

public class ImportDHB {

	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
		// 1593/579
		JsonArray matchDays = DHB_Rest_Services.getSpielplan("1593", "579");

		JsonArray standing = DHB_Rest_Services.getTabelle("5393");
		
		try {
			Tabelle neueStanding = ImportDHB.updateTable(standing);
			Writer.updateRankingTable(neueStanding.getJSON(), "111952");
			
		} catch (OAuthSystemException | OAuthProblemException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		ImportDHB.importMatchDays(matchDays);

	}

	public static void importMatchDays(JsonArray pMatchDay) throws ParseException

	{

		for (int i = 0; i < pMatchDay.size(); i++) {
			JsonArray matches = pMatchDay.get(i).getAsJsonObject().get("matches").getAsJsonArray();
			ImportDHB.importMatches(matches);
		}

	}

	public static void importMatches(JsonArray pMatches) throws ParseException {

		JsonObject home_team = null;
		JsonObject away_team = null;
		JsonObject match = null;

		for (int i = 0; i < pMatches.size(); i++) {

			match = pMatches.get(i).getAsJsonObject();
			ImportDHB.createGame(match);
			
		}

	}

	private static HashMap<String, JsonObject> mannschaftenHashMap = new HashMap<>();
	private static HashMap<String, JsonObject> venuesHashMap = new HashMap<>();

	private static void createGame(JsonObject pMatch) throws ParseException {

		JsonObject gamesHome = null;
		JsonObject gamesGuest = null;
		JsonObject venue = null;
//		System.out.println(pMatch);
		if (mannschaftenHashMap.containsKey(pMatch.get("home_team").getAsJsonObject().get("_id").getAsString())) {

//			System.out.println("Found Team: " + pMatch.get("home_team").getAsJsonObject().get("_id"));
			gamesHome = mannschaftenHashMap.get(pMatch.get("home_team").getAsJsonObject().get("_id").getAsString());
		} else {
			gamesHome = Search.searchTeamByDHBNummer(pMatch.get("home_team").getAsJsonObject().get("_id").getAsString().trim()) ;
//			System.out.println(gamesHome);
			mannschaftenHashMap.put(pMatch.get("home_team").getAsJsonObject().get("_id").getAsString().trim(),
					gamesHome);
		}

		if (mannschaftenHashMap.containsKey(pMatch.get("away_team").getAsJsonObject().get("_id").getAsString())) {
//			System.out.println("Found Team: " + pMatch.get("away_team").getAsJsonObject().get("_id"));
			
			gamesGuest = mannschaftenHashMap.get(pMatch.get("away_team").getAsJsonObject().get("_id").getAsString());
		} else {
		    gamesGuest = Search.searchTeamByDHBNummer(pMatch.get("away_team").getAsJsonObject().get("_id").getAsString().trim());
		    System.out.println(gamesGuest);
			mannschaftenHashMap.put(pMatch.get("away_team").getAsJsonObject().get("_id").getAsString().trim(),
					gamesGuest);
		}

		if (venuesHashMap.containsKey(pMatch.get("location").getAsJsonObject().get("_id").getAsString())) {
			venue = venuesHashMap.get(pMatch.get("location").getAsJsonObject().get("_id").getAsString());
//			System.out.println("Found Venue: " + pMatch.get("location").getAsJsonObject().get("_id"));
		} else {
//			System.out.println("Search Venue: " + pMatch.get("location").getAsJsonObject().get("_id"));
			 venue = Search.findVenueByDHBNummer(pMatch.get("location").getAsJsonObject().get("_id").getAsString());
			venuesHashMap.put(pMatch.get("location").getAsJsonObject().get("_id").getAsString(), venue);
		}
		BHVSpielRest spiel = new BHVSpielRest();
		

		spiel.setHeimmannschaft(pMatch.get("home_team").getAsJsonObject().get("abbreviation").getAsString());
		spiel.setGastmannschaft(pMatch.get("away_team").getAsJsonObject().get("abbreviation").getAsString());
		 spiel.wpHeimmannschaftID = gamesHome.get("id").getAsString();
		 spiel.wpGastmannschaftID = gamesGuest.get("id").getAsString();
		
		 
		 try {
				spiel.setWpHallennummer(venue.get("id").getAsString());
			}
			catch (NullPointerException e)
			{
				System.out.println(pMatch);
				System.out.println("Halle nicht gefunden: " + spiel.getHeimmannschaft() + " " + pMatch.get("location").getAsJsonObject().get("_id").getAsString());
			}
		 
		 Date start =  new SimpleDateFormat("dd/MM/yy HH:mm").parse(pMatch.get("play_date").getAsJsonObject().get("date").getAsString() + " " +
				 pMatch.get("play_date").getAsJsonObject().get("time").getAsString() );
		 
		spiel.setDatum(start);
		spiel.setSpielnummer(pMatch.get("_id").getAsString());
		spiel.toreHeim = pMatch.get("home_team").getAsJsonObject().get("final_result").getAsInt();
		spiel.toreGast = pMatch.get("away_team").getAsJsonObject().get("final_result").getAsInt();

		String wpSpiel = null;
		try {
		wpSpiel= Search.searchDHBSpieByNumberl(pMatch.get("_id").getAsString());
		}
		catch (Exception exc)
		{
			exc.printStackTrace();
		}
		
		if (wpSpiel == null) {
			System.out.println("Erstelle Spiel: " + spiel.getSpielnummer() + "    " + spiel.wpHeimmannschaftID + " : "
					+ spiel.wpGastmannschaftID);
			 Writer.createSpiel(spiel.getJSONDHB());
		} else {
			System.out.println("Update Spiel: " + spiel.getSpielnummer());
			 Writer.update(wpSpiel, spiel.getJSONDHB());
		}
	}

	private static  Tabelle updateTable(JsonArray standing)
				throws OAuthSystemException, OAuthProblemException, IOException, JsonParseException, JsonMappingException {
	
		    Tabelle tabelleUpdate = new Tabelle();
	
			Iterator<JsonElement> tabellenItr = standing.iterator();
			while (tabellenItr.hasNext()) {
				JsonElement tabellenPlatz = tabellenItr.next();
				
				System.out.println(tabellenPlatz);
	
	
				TabellenPlatz platz = new TabellenPlatz();
				
	//			if (!mannschaftenHashMap.containsKey(tabellenPlatz.getTeamId()))
					try {
						platz.wpID = (Search.searchTeamByDHBNummer(tabellenPlatz.getAsJsonObject().get("_id").getAsString())).get("id").getAsString();
					} catch (NullPointerException nullExp) {
						nullExp.printStackTrace();
					}
	//			else
	//				platz.wpID = mannschaftenHashMap.get(tabellenPlatz.getTeamId()).get("id").toString();
	
				platz.position = tabellenPlatz.getAsJsonObject().get("position").getAsInt();
				platz.spiele = tabellenPlatz.getAsJsonObject().get("played_games").getAsInt();
				platz.punktePlus = tabellenPlatz.getAsJsonObject().get("points_pro").getAsInt();
				platz.punkteMinus = tabellenPlatz.getAsJsonObject().get("points_against").getAsString();
				platz.gewonnen = tabellenPlatz.getAsJsonObject().get("wins").getAsString();
				platz.unentschieden = tabellenPlatz.getAsJsonObject().get("draws").getAsString();
				platz.verloren = tabellenPlatz.getAsJsonObject().get("looses").getAsString();
	
				platz.torePlus = tabellenPlatz.getAsJsonObject().get("goals_pro").getAsInt();
				platz.toreMinus = tabellenPlatz.getAsJsonObject().get("goals_against").getAsInt();
				tabelleUpdate.plaetze.add(platz);
	
				// System.out.println(tabellenPlatz.getTableRank() + "; (" + platz.wpID + ");" +
				// tabellenPlatz.getTeam() + ";"
				// + tabellenPlatz.getMeetings() + ";" + tabellenPlatz.getOwnPoints().intValue()
				// + ":"
				// + tabellenPlatz.getOtherPoints().intValue() + ";" +
				// tabellenPlatz.getOwnMatches() + ":"
				// + tabellenPlatz.getOtherMatches());
	
			}
	
			return tabelleUpdate;
		}

}

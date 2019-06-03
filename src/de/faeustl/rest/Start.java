package de.faeustl.rest;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import de.faeustl.model.BHVSpielRest;
import de.faeustl.model.Tabelle;
import de.faeustl.model.TabellenPlatz;
import de.faeustl.wp.Search;
import de.faeustl.wp.Writer;
import nu.liga.open.rs.v2014.dto.championships.GroupTableTeamDTO;
import nu.liga.open.rs.v2014.dto.championships.MeetingAbbrDTO;
import nu.liga.open.rs.v2014.dto.championships.MeetingsDTO;
import nu.liga.open.rs.v2014.dto.championships.TeamGroupTablesDTO;

public class Start {

	final static String baseURL = "https://hbde-portal.liga.nu/rs";

	public static final String TOKEN_REQUEST_URL = baseURL + "/auth/token";

	/**
	 * Client ID of your client credential. Change this to match whatever credential
	 * you have created.
	 */
	public static final String CLIENT_ID = "e7ee38b2-1f4a-41c2-95fb-2ac98dfcc026";

	/**
	 * Client secret of your client credential. Change this to match whatever
	 * credential you have created.
	 */
	public static final String CLIENT_SECRET = "Cwa05031998mu";

	/**
	 * Account on which you want to request a resource. Change this to match the
	 * account you want to retrieve resources on.
	 */
	public static final String ACCOUNT_ID = "YOUR_ACCOUNT_ID";
	
	private static  String mA_ID;
	private static  String mB_ID;
	private static  String mC_ID;
	private static  String wA_ID;
	private static  String wB_ID;
	private static  String wC_ID;
	private static  String mA_Tabelle;
	private static  String wA_Tabelle;
	private static  String mB_Tabelle;
	private static  String wB_Tabelle;
	private static  String mC_Tabelle;
	private static  String wC_Tabelle;
	
	private static HashMap<String, JsonObject> mannschaftenHashMap = new HashMap<>();

	public static String refresh = null;

	
	public static void main(String[] args) {

		String token = null;
		FileReader in;
		Properties prop = null;
//		Properties mannschaftenProperties = null;
		try {
			prop = new Properties();
			String propFileName = "token.properties";
			String mannschaftenPropFile = "mannschaft.properties";
			
			
			in = new FileReader(propFileName);

			prop.load(in);

			token = prop.getProperty("token");
			refresh = prop.getProperty("refresh");

			in.close();
			ResourceBundle mannschaftenProperties = ResourceBundle.getBundle("mannschaft");
//			mannschaftenProperties = new Properties();
//			in = new FileReader(mannschaftenPropFile);
//			mannschaftenProperties.load(in);
			mA_ID = mannschaftenProperties.getString("mA_ID");
			mB_ID = mannschaftenProperties.getString("mB_ID");
			mC_ID = mannschaftenProperties.getString("mC_ID");
			wA_ID = mannschaftenProperties.getString("wA_ID");
			wB_ID = mannschaftenProperties.getString("wB_ID");
			wC_ID = mannschaftenProperties.getString("wC_ID");
			mA_Tabelle = mannschaftenProperties.getString("mA_Tabelle");
			wA_Tabelle = mannschaftenProperties.getString("wA_Tabelle");
			mB_Tabelle = mannschaftenProperties.getString("mB_Tabelle");
			wB_Tabelle = mannschaftenProperties.getString("wB_Tabelle");
			mC_Tabelle = mannschaftenProperties.getString("mC_Tabelle");
			wC_Tabelle = mannschaftenProperties.getString("wC_Tabelle");
			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {

		}

		ObjectMapper mapper = new ObjectMapper();

		try {

			OAuthClient client = new OAuthClient(new URLConnectionClient());

//			OAuthClientRequest request = getOAuthClientRequest();
			OAuthClientRequest request = getOAuthClientRequestWithRefreshToken();

			OAuthJSONAccessTokenResponse tokens = client.accessToken(request, OAuthJSONAccessTokenResponse.class);
			
			token = tokens.getAccessToken();
			refresh = tokens.getRefreshToken();
			prop.setProperty("token", token);
			prop.setProperty("refresh", refresh);
			prop.store(new FileOutputStream("token.properties"), null);

			// System.out.println("Token: " + token);
			// System.out.println("Refresh: " + refresh);

			// String resourceUrl
			// ="https://hbde-portal.liga.nu/rs/2014/federations/BHV/clubs/30461/teams";
			// String test
			// ="https://hbde-portal.liga.nu/rs/2014/federations/BHV/seasons/Q%2019%2F20/clubs/30461/downloads";

			String resourceUrl = "https://hbde-portal.liga.nu/rs/2014/federations/BHV/seasons/Q%2019%2F20/clubs/30461/meetings?fromDate=2019-06-02&toDate=2019-06-02";
			

			MeetingsDTO games = getNextGames(token, mapper, client, resourceUrl);

//			createGames(games);
			Tabelle updateTable = null;

//			updateTable = getTable(token, mapper, client, mA_ID, 1);
//			Writer.updateRankingTable(updateTable.getJSON(), mA_Tabelle);
//
//			updateTable = getTable(token, mapper, client, wA_ID, 1);
//			Writer.updateRankingTable(updateTable.getJSON(), wA_Tabelle);
//			
			updateTable = getTable(token, mapper, client, mB_ID, 1);
			Writer.updateRankingTable(updateTable.getJSON(), mB_Tabelle);
//
//			updateTable = getTable(token, mapper, client, wB_ID, 1);
//			Writer.updateRankingTable(updateTable.getJSON(), wB_Tabelle);
//
//			updateTable = getTable(token, mapper, client, mC_ID, 2);
//			Writer.updateRankingTable(updateTable.getJSON(), mC_Tabelle);
//			
//			updateTable = getTable(token, mapper, client, wC_ID, 0);
//			Writer.updateRankingTable(updateTable.getJSON(), wC_Tabelle);


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthProblemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void createGames(MeetingsDTO games) {

		Iterator<MeetingAbbrDTO> itr = games.getList().iterator();
		while (itr.hasNext()) {
			MeetingAbbrDTO meeting = itr.next();
//			System.out.println(meeting.getScheduled().toLocaleString() + ";" + meeting.getLeagueNickname() + ";"
//					+ meeting.getTeamHome() + ";" + meeting.getMatchesHome() + ":" + meeting.getMatchesGuest() + ";"
//					+ meeting.getTeamGuest() + ";" + meeting.getCourtHallName() + ";" + meeting.getMeetingId() + ";"
//					+ meeting.getTeamHomeId() + ";" + meeting.getTeamGuestId());
			JsonObject gamesHome=null;
			JsonObject gamesGuest=null;
			if (mannschaftenHashMap.containsKey(meeting.getTeamHomeId())) {
				
				System.out.println("Found Team: "+ meeting.getTeamHome());
				gamesHome = mannschaftenHashMap.get(meeting.getTeamHomeId());
			}
			else
			{
				gamesHome = Search.searchTeamByBHVNummer(meeting.getTeamHomeId());
				mannschaftenHashMap.put(meeting.getTeamHomeId().trim(), gamesHome);
			}
			
			
			if (mannschaftenHashMap.containsKey(meeting.getTeamGuestId()))
			{
				System.out.println("Found Team: "+ meeting.getTeamGuest());
				gamesGuest = mannschaftenHashMap.get(meeting.getTeamGuestId().trim());
			}
			else
			{
				gamesGuest = Search.searchTeamByBHVNummer(meeting.getTeamGuestId());
				mannschaftenHashMap.put(meeting.getTeamGuestId().trim(), gamesGuest);
			}
			
			
			BHVSpielRest spiel = new BHVSpielRest();
			spiel.setHeimmannschaft(meeting.getTeamHome());
			spiel.setGastmannschaft(meeting.getTeamGuest());
			spiel.wpHeimmannschaftID=gamesHome.get("id").getAsString();
			spiel.wpGastmannschaftID=gamesGuest.get("id").getAsString();
			spiel.setDatum(meeting.getScheduled());
			spiel.setSpielnummer(meeting.getMeetingId());
			spiel.toreHeim = meeting.getMatchesHome();
			spiel.toreGast = meeting.getMatchesGuest();
			
			String wpSpiel = Search.searchBHVSpieByNumberl(spiel.getSpielnummer());
			
			if (wpSpiel == null)
			{
				System.out.println("Erstelle Spiel: " +  spiel.getSpielnummer() + spiel.wpHeimmannschaftID + " : " + spiel.wpGastmannschaftID);
				Writer.createSpiel(spiel.getJSON());
			}
			else
			{
				System.out.println("Update Spiel: " +  spiel.getSpielnummer());
				Writer.update(wpSpiel, spiel.getJSON());
			}
		}

		
	}

	private static MeetingsDTO getNextGames(String token, ObjectMapper mapper, OAuthClient client,
			String resourceUrl)
			throws OAuthSystemException, OAuthProblemException, IOException, JsonParseException, JsonMappingException {

		System.out.println("--------------- Get NextGames -----------------------");
		OAuthResourceResponse resourceResponse = getResourceResponse(token, client, resourceUrl);
		System.out.println(resourceResponse.getBody());
		MeetingsDTO result = (MeetingsDTO) mapper.readValue(resourceResponse.getBody(), MeetingsDTO.class);
		ListIterator<MeetingAbbrDTO> itr = result.getList().listIterator();
		while (itr.hasNext()) {
			MeetingAbbrDTO meeting = itr.next();
			System.out.println(meeting.getScheduled().toLocaleString() + ";" + meeting.getLeagueNickname() + ";"
					+ meeting.getTeamHome() + ";" + meeting.getMatchesHome() + ":" + meeting.getMatchesGuest() + ";"
					+ meeting.getTeamGuest() + ";" + meeting.getCourtHallName() + ";" + meeting.getMeetingId() + ";"
					+ meeting.getTeamHomeId() + ";" + meeting.getTeamGuestId());
		}
		return result;
	}

	private static OAuthResourceResponse getResourceResponse(String token, OAuthClient client, String resourceUrl)
			throws OAuthSystemException, OAuthProblemException {
		OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(resourceUrl).setAccessToken(token)

				.buildHeaderMessage();

		bearerClientRequest.addHeader("Accept", "application/json");
		OAuthResourceResponse resourceResponse = client.resource(bearerClientRequest, OAuth.HttpMethod.GET,
				OAuthResourceResponse.class);

		return resourceResponse;
	}

	private static Tabelle getTable(String token, ObjectMapper mapper, OAuthClient client, String resourceUrl, int index)
			throws OAuthSystemException, OAuthProblemException, IOException, JsonParseException, JsonMappingException {

		System.out.println("--------------- Get Table -----------------------");

		String tableUrl = "https://hbde-portal.liga.nu/rs/2014/federations/BHV/clubs/30461/teams/" + resourceUrl + "/table";
		
		OAuthResourceResponse resourceResponse = getResourceResponse(token, client, tableUrl);
		System.out.println(resourceResponse.getBody());

		TeamGroupTablesDTO tabelle = (TeamGroupTablesDTO) mapper.readValue(resourceResponse.getBody(),
				TeamGroupTablesDTO.class);
		
		Tabelle tabelleUpdate = new Tabelle();
				
		ListIterator<GroupTableTeamDTO> tabellenItr = tabelle.getGroupTables().get(index).getList().listIterator();
		while (tabellenItr.hasNext()) {
			GroupTableTeamDTO tabellenPlatz = tabellenItr.next();
				System.out.println("Mannschaft: " + tabellenPlatz.getTeam() + "(" +tabellenPlatz.getTeamId() +")");
				
					
				TabellenPlatz platz = new TabellenPlatz();

				if (!mannschaftenHashMap.containsKey(tabellenPlatz.getTeamId()))
					platz.wpID = Search.searchTeamByBHVNummer(tabellenPlatz.getTeamId()).get("id").toString();
				else 
					platz.wpID = mannschaftenHashMap.get(tabellenPlatz.getTeamId()).get("id").toString();
				
				platz.position = tabellenPlatz.getTableRank();
				platz.spiele = tabellenPlatz.getMeetings();
				platz.punktePlus = tabellenPlatz.getOwnPoints().intValue();
				platz.punkteMinus = tabellenPlatz.getOtherPoints().toString();
				platz.gewonnen = tabellenPlatz.getOwnMeetings().toString();
				platz.unentschieden = tabellenPlatz.getTieMeetings().toString();
				platz.verloren = tabellenPlatz.getOtherMeetings().toString();
				
				platz.torePlus = tabellenPlatz.getOwnMatches().intValue();
				platz.toreMinus = tabellenPlatz.getOtherMatches().intValue();
				tabelleUpdate.plaetze.add(platz);
				
				
				
			System.out.println(tabellenPlatz.getTableRank() + "; (" + platz.wpID + ");" + tabellenPlatz.getTeam() + ";"
					+ tabellenPlatz.getMeetings() + ";" + tabellenPlatz.getOwnPoints().intValue() + ":"
					+ tabellenPlatz.getOtherPoints().intValue() + ";" + tabellenPlatz.getOwnMatches() + ":"
					+ tabellenPlatz.getOtherMatches());
			
		}
		
		return tabelleUpdate;
	}

	@SuppressWarnings("unused")
	private static OAuthClientRequest getOAuthClientRequest() throws OAuthSystemException {
		OAuthClientRequest request = OAuthClientRequest.tokenLocation(TOKEN_REQUEST_URL)
				.setGrantType(GrantType.CLIENT_CREDENTIALS).setClientId(CLIENT_ID).setClientSecret(CLIENT_SECRET)
				.setScope("nuPortalRS_club")

				.buildBodyMessage();

		return request;
	}

	private static OAuthClientRequest getOAuthClientRequestWithRefreshToken() throws OAuthSystemException {
		OAuthClientRequest request = OAuthClientRequest.tokenLocation(TOKEN_REQUEST_URL)
				.setGrantType(GrantType.REFRESH_TOKEN).setRefreshToken(refresh).setClientId(CLIENT_ID)
				.setClientSecret(CLIENT_SECRET).setScope("nuPortalRS_club")

				.buildBodyMessage();

		return request;
	}

}

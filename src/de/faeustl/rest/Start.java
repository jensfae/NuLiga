package de.faeustl.rest;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

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
import de.faeustl.threat.UpdateGames;
import de.faeustl.threat.UpdateTable; 
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

	private static String herren1_ID;
	private static String herren2_ID;
	private static String herren3_ID;
	private static String frauen2_ID;
	private static String frauen3_ID;
	private static String frauen4_ID;

	private static String frauen2_Tabelle;
	private static String frauen3_Tabelle;
	private static String frauen4_Tabelle;

	private static String herren1_Tabelle;
	private static String herren2_Tabelle;

	private static String mA_ID;
	private static String mB_ID;
	private static String mC_ID;
	private static String wA_ID;
	private static String wB_ID;
	private static String wC_ID;
	private static String mA_Tabelle;
	private static String wA_Tabelle;
	private static String mB_Tabelle;
	private static String wB_Tabelle;
	private static String mC_Tabelle;
	private static String wC_Tabelle;

	private static HashMap<String, JsonObject> mannschaftenHashMap = new HashMap<>();
	private static HashMap<String, JsonObject> venuesHashMap = new HashMap<>();

	public static String refresh = null;

	public static void main(String[] args) throws InterruptedException {

		String token = null;
		FileReader in;
		Properties prop = null;
		// Properties mannschaftenProperties = null;
		try {
			prop = new Properties();
			String propFileName = "token.properties";
//			String mannschaftenPropFile = "mannschaft.properties";

			in = new FileReader(propFileName);

			prop.load(in);

			token = prop.getProperty("token");
			refresh = prop.getProperty("refresh");

			in.close();
			ResourceBundle mannschaftenProperties = ResourceBundle.getBundle("mannschaft");
			// mannschaftenProperties = new Properties();
			// in = new FileReader(mannschaftenPropFile);
			// mannschaftenProperties.load(in);
			herren1_ID = mannschaftenProperties.getString("herren1_ID");
			herren1_Tabelle = mannschaftenProperties.getString("herren1_Tabelle");

			herren2_ID = mannschaftenProperties.getString("herren2_ID");
			herren2_Tabelle = mannschaftenProperties.getString("herren2_Tabelle");

			frauen2_ID = mannschaftenProperties.getString("frauen2_ID");
			frauen2_Tabelle = mannschaftenProperties.getString("frauen2_Tabelle");

			frauen3_ID = mannschaftenProperties.getString("frauen3_ID");
			frauen3_Tabelle = mannschaftenProperties.getString("frauen3_Tabelle");
			frauen4_ID = mannschaftenProperties.getString("frauen4_ID");
			frauen4_Tabelle = mannschaftenProperties.getString("frauen4_Tabelle");

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

			// OAuthClientRequest request = getOAuthClientRequest();
			OAuthClientRequest request = getOAuthClientRequestWithRefreshToken();

			OAuthJSONAccessTokenResponse tokens = client.accessToken(request, OAuthJSONAccessTokenResponse.class);

			token = tokens.getAccessToken();
			refresh = tokens.getRefreshToken();
			prop.setProperty("token", token);
			prop.setProperty("refresh", refresh);
			prop.store(new FileOutputStream("token.properties"), null);


			String resourceUrl = "https://hbde-portal.liga.nu/rs/2014/federations/BHV/seasons/19%2F20/clubs/30461/meetings?fromDate=2019-10-01&maxResults=300";

			MeetingsDTO games = getNextGames(token, mapper, client, resourceUrl);

			// createGames(games);
//			 Tabelle updateTable = null;

			
			ThreadGroup updateGamesThreatGroup = new ThreadGroup("updateGames");
//			createOrUpdateGames(games, updateGamesThreatGroup);
			
			

			Runnable run_Herren1 = new UpdateTable(token, mapper, client, herren1_ID, herren1_Tabelle);
//			Runnable run_Herren2 = new UpdateTable(token, mapper, client, herren2_ID, herren2_Tabelle);
//			Runnable run_Frauen2 = new UpdateTable(token, mapper, client, frauen2_ID, frauen2_Tabelle);
//			Runnable run_Frauen3 = new UpdateTable(token, mapper, client, frauen3_ID, frauen4_Tabelle);
//			Runnable run_Frauen4 = new UpdateTable(token, mapper, client, frauen4_ID, frauen4_Tabelle);
//
//			Runnable run_mA = new UpdateTable(token, mapper, client, mA_ID, mA_Tabelle);
//			Runnable run_wA = new UpdateTable(token, mapper, client, wA_ID, wA_Tabelle);
//			Runnable run_mB = new UpdateTable(token, mapper, client, mB_ID, mB_Tabelle);
//			Runnable run_wB = new UpdateTable(token, mapper, client, wB_ID, wB_Tabelle);
//			Runnable run_mC = new UpdateTable(token, mapper, client, mC_ID, mC_Tabelle);
//			Runnable run_wC = new UpdateTable(token, mapper, client, wC_ID, wC_Tabelle);
//
			ThreadGroup updateTableGroup = new ThreadGroup("updateTable");
//
			Thread thread_Herren1 = new Thread(updateTableGroup, run_Herren1);
//			Thread thread_Herren2 = new Thread(updateTableGroup, run_Herren2);
//			Thread thread_frauen2 = new Thread(updateTableGroup, run_Frauen2);
//			Thread thread_frauen3 = new Thread(updateTableGroup, run_Frauen3);
//			Thread thread_frauen4 = new Thread(updateTableGroup, run_Frauen4);
//			Thread thread_mA = new Thread(updateTableGroup, run_mA);
//			Thread thread_wA = new Thread(updateTableGroup, run_wA);
//			Thread thread_mB = new Thread(updateTableGroup, run_mB);
//			Thread thread_wB = new Thread(updateTableGroup, run_wB);
//			Thread thread_mC = new Thread(updateTableGroup, run_mC);
//			Thread thread_wC = new Thread(updateTableGroup, run_wC);
//
			thread_Herren1.setDaemon(true);
//			thread_Herren2.setDaemon(true);
//			thread_frauen2.setDaemon(true);
//			thread_frauen3.setDaemon(true);
//			thread_frauen4.setDaemon(true);
//			thread_mA.setDaemon(true);
//			thread_wA.setDaemon(true);
//			thread_mB.setDaemon(true);
//			thread_wB.setDaemon(true);
//			thread_mC.setDaemon(true);
//			thread_wC.setDaemon(true);
////
			System.out.println("**************************************************");
			System.out.println("Starte Threads");
			System.out.println("**************************************************");

			thread_Herren1.start();
//			thread_Herren2.start();
//			TimeUnit.SECONDS.sleep(2);
//			thread_frauen2.start();
//			thread_frauen3.start();
//			thread_frauen4.start();
//			TimeUnit.SECONDS.sleep(2);
//			thread_mA.start();
//			thread_wA.start();
//			thread_mB.start();
//			TimeUnit.SECONDS.sleep(2);
//			thread_wB.start();
//			thread_mC.start();
//			thread_wC.start();
//			TimeUnit.SECONDS.sleep(1);
//
			System.out.print("Working: .....");
			while (
//					updateGamesThreatGroup.activeCount()>0 
//					|| 
					thread_Herren1.isAlive() 
//					|| thread_Herren2.isAlive() || thread_frauen2.isAlive()
//					|| thread_frauen3.isAlive() || thread_frauen4.isAlive() || thread_mA.isAlive()
//					|| thread_wA.isAlive() || thread_mB.isAlive() || thread_wB.isAlive() || thread_mC.isAlive()
//					|| thread_wC.isAlive()
					)

			{
				System.out.print("...");
				// updateTableGroup.list();
				TimeUnit.SECONDS.sleep(1);

			}

			System.out.println("**************************************************");
			System.out.println("Ende Update");

			//
			// updateTable = getTable(token, mapper, client, herren1_ID, 0);
			// Writer.updateRankingTable(updateTable.getJSON(), herren1_Tabelle);
			//
			// updateTable = getTable(token, mapper, client, herren2_ID, 0);
			// Writer.updateRankingTable(updateTable.getJSON(), herren2_Tabelle);
			//
			// updateTable = getTable(token, mapper, client, frauen2_ID, 0);
			// Writer.updateRankingTable(updateTable.getJSON(), frauen2_Tabelle);
			//
			// updateTable = getTable(token, mapper, client, frauen3_ID, 0);
			// Writer.updateRankingTable(updateTable.getJSON(), frauen3_Tabelle);
			//
			// updateTable = getTable(token, mapper, client, frauen4_ID, 0);
			// Writer.updateRankingTable(updateTable.getJSON(), frauen4_Tabelle);
			//
			// updateTable = getTable(token, mapper, client, mA_ID, 0);
			// Writer.updateRankingTable(updateTable.getJSON(), mA_Tabelle);
			// updateTable = getTable(token, mapper, client, wA_ID, 0);
			// Writer.updateRankingTable(updateTable.getJSON(), wA_Tabelle);
			//
			// updateTable = getTable(token, mapper, client, mB_ID, 0);
			// Writer.updateRankingTable(updateTable.getJSON(), mB_Tabelle);
			//
			// updateTable = getTable(token, mapper, client, wB_ID, 0);
			// Writer.updateRankingTable(updateTable.getJSON(), wB_Tabelle);
			//
			// updateTable = getTable(token, mapper, client, mC_ID, 0);
			// Writer.updateRankingTable(updateTable.getJSON(), mC_Tabelle);
			//
			// updateTable = getTable(token, mapper, client, wC_ID, 0);
			// Writer.updateRankingTable(updateTable.getJSON(), wC_Tabelle);

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

	private static void createOrUpdateGames(MeetingsDTO games, ThreadGroup updateGamesThreatGroup) throws InterruptedException {
		Iterator<MeetingAbbrDTO> itr = games.getList().iterator();
		ThreadGroup tg = new ThreadGroup ("Group 1");
		int x = 0;
		int y = 1;
		while (itr.hasNext()) {
			List<MeetingAbbrDTO> myMeetingsList = new <MeetingAbbrDTO>ArrayList();
			MeetingsDTO myMeetings = new MeetingsDTO();
			
			for (x = 0; x < 20; x++) {
				if (itr.hasNext()) {
					MeetingAbbrDTO meeting = itr.next();

					myMeetingsList.add(meeting);

				}
			}
			myMeetings.setList(myMeetingsList);
			UpdateGames gamesUpdate = new UpdateGames(updateGamesThreatGroup, new Integer(y).toString(), myMeetings);
			//gamesUpdate.setName(new Integer(x).toString());
			gamesUpdate.setDaemon(true);
			gamesUpdate.start();
			
			
			
			
			y++;
			x = 0;

		}
		

	}

	private static void createGames(MeetingsDTO games) {

		Iterator<MeetingAbbrDTO> itr = games.getList().iterator();
		while (itr.hasNext()) {
			MeetingAbbrDTO meeting = itr.next();
			// System.out.println(meeting.getScheduled().toLocaleString() + ";" +
			// meeting.getLeagueNickname() + ";"
			// + meeting.getTeamHome() + ";" + meeting.getMatchesHome() + ":" +
			// meeting.getMatchesGuest() + ";"
			// + meeting.getTeamGuest() + ";" + meeting.getCourtHallName() + ";" +
			// meeting.getMeetingId() + ";"
			// + meeting.getTeamHomeId() + ";" + meeting.getTeamGuestId());
			JsonObject gamesHome = null;
			JsonObject gamesGuest = null;
			JsonObject venue = null;

			if ((meeting.getGroupId().equals("246305") || meeting.getGroupId().equals("246526")
					|| meeting.getGroupId().equals("246332") || meeting.getGroupId().equals("246555")
					|| meeting.getGroupId().equals("246443") || meeting.getGroupId().equals("246513")
					|| meeting.getGroupId().equals("246490") || meeting.getGroupId().equals("246532")
					|| meeting.getGroupId().equals("246557") || meeting.getGroupId().equals("246313")
					|| meeting.getGroupId().equals("246317")

			) && !(meeting.getTeamHome().equals("spielfrei*") || meeting.getTeamGuest().equals("spielfrei*"))) {
				if (mannschaftenHashMap.containsKey(meeting.getTeamHomeId())) {

					System.out.println("Found Team: " + meeting.getTeamHome());
					gamesHome = mannschaftenHashMap.get(meeting.getTeamHomeId());
				} else {
					gamesHome = Search.searchTeamByBHVNummer(meeting.getTeamHomeId());
					mannschaftenHashMap.put(meeting.getTeamHomeId().trim(), gamesHome);
				}

				if (mannschaftenHashMap.containsKey(meeting.getTeamGuestId())) {
					System.out.println("Found Team: " + meeting.getTeamGuest());
					gamesGuest = mannschaftenHashMap.get(meeting.getTeamGuestId().trim());
				} else {
					gamesGuest = Search.searchTeamByBHVNummer(meeting.getTeamGuestId());
					mannschaftenHashMap.put(meeting.getTeamGuestId().trim(), gamesGuest);
				}

				if (venuesHashMap.containsKey(meeting.getCourtHallNumbers())) {
					venue = venuesHashMap.get(meeting.getCourtHallNumbers());
					System.out.println("Found Venue: " + meeting.getCourtHallName());
				} else {
					System.out.println("Search Venue: " + meeting.getCourtHallName());
					venue = Search.findVenue("", meeting.getCourtHallNumbers(), "");
					venuesHashMap.put(meeting.getCourtHallNumbers(), venue);
				}
				BHVSpielRest spiel = new BHVSpielRest();
				spiel.setWpHallennummer(venue.get("id").getAsString());

				spiel.setHeimmannschaft(meeting.getTeamHome());
				spiel.setGastmannschaft(meeting.getTeamGuest());
				spiel.wpHeimmannschaftID = gamesHome.get("id").getAsString();
				spiel.wpGastmannschaftID = gamesGuest.get("id").getAsString();
				spiel.setDatum(meeting.getScheduled());
				spiel.setSpielnummer(meeting.getMeetingId());
				spiel.toreHeim = meeting.getMatchesHome();
				spiel.toreGast = meeting.getMatchesGuest();

				String wpSpiel = Search.searchBHVSpieByNumberl(spiel.getSpielnummer());

				if (wpSpiel == null) {
					System.out.println("Erstelle Spiel: " + spiel.getSpielnummer() + spiel.wpHeimmannschaftID + " : "
							+ spiel.wpGastmannschaftID);
					Writer.createSpiel(spiel.getJSON());
				} else {
					System.out.println("Update Spiel: " + spiel.getSpielnummer());
					Writer.update(wpSpiel, spiel.getJSON());
				}
			}
		}

	}

	private static MeetingsDTO getNextGames(String token, ObjectMapper mapper, OAuthClient client, String resourceUrl)
			throws OAuthSystemException, OAuthProblemException, IOException, JsonParseException, JsonMappingException {

		System.out.println("--------------- Get NextGames -----------------------");
		OAuthResourceResponse resourceResponse = getResourceResponse(token, client, resourceUrl);
		// System.out.println(resourceResponse.getBody());
		MeetingsDTO result = (MeetingsDTO) mapper.readValue(resourceResponse.getBody(), MeetingsDTO.class);
		ListIterator<MeetingAbbrDTO> itr = result.getList().listIterator();
		while (itr.hasNext()) {
			MeetingAbbrDTO meeting = itr.next();
			System.out.println(meeting.getScheduled().toLocaleString() + ";" + meeting.getGroupName() + ";"
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

	private static Tabelle getTable(String token, ObjectMapper mapper, OAuthClient client, String resourceUrl,
			int index)
			throws OAuthSystemException, OAuthProblemException, IOException, JsonParseException, JsonMappingException {

		System.out.println("--------------- Get Table -----------------------");

		String tableUrl = "https://hbde-portal.liga.nu/rs/2014/federations/BHV/clubs/30461/teams/" + resourceUrl
				+ "/table";

		OAuthResourceResponse resourceResponse = getResourceResponse(token, client, tableUrl);
		System.out.println(resourceResponse.getBody());

		TeamGroupTablesDTO tabelle = (TeamGroupTablesDTO) mapper.readValue(resourceResponse.getBody(),
				TeamGroupTablesDTO.class);

		Tabelle tabelleUpdate = new Tabelle();

		ListIterator<GroupTableTeamDTO> tabellenItr = tabelle.getGroupTables().get(index).getList().listIterator();
		while (tabellenItr.hasNext()) {
			GroupTableTeamDTO tabellenPlatz = tabellenItr.next();
			System.out.println("Mannschaft: " + tabellenPlatz.getTeam() + "(" + tabellenPlatz.getTeamId() + ")");

			TabellenPlatz platz = new TabellenPlatz();

			if (!mannschaftenHashMap.containsKey(tabellenPlatz.getTeamId()))
				try {
					platz.wpID = Search.searchTeamByBHVNummer(tabellenPlatz.getTeamId()).get("id").toString();
				} catch (NullPointerException nullExp) {
					platz.wpID = Writer.createTeam(tabellenPlatz.getTeam(), tabellenPlatz.getTeamId()).get("id")
							.toString();
				}
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

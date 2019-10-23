package de.faeustl.threat;

import java.io.IOException;
import java.util.ListIterator;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.faeustl.model.Tabelle;
import de.faeustl.model.TabellenPlatz;
import de.faeustl.wp.Search;
import de.faeustl.wp.Writer;
import nu.liga.open.rs.v2014.dto.championships.GroupTableTeamDTO;
import nu.liga.open.rs.v2014.dto.championships.TeamGroupTablesDTO;

public class UpdateTable implements Runnable
{
	String token;
	ObjectMapper mapper;
	OAuthClient client;
	String resourceUrl;
	String tabelle;
	
	public UpdateTable(String token, ObjectMapper mapper, OAuthClient client, String resourceUrl,
			String tabelle) {
		this.token=token;
		this.mapper=mapper;
		this.client = client;
		this.resourceUrl=resourceUrl;
		this.tabelle= tabelle;
	}

public void run() {

		// TODO Auto-generated method stub
		System.out.println("Starte Update Tabelle: " + this.resourceUrl);
		Tabelle updateTable = null;
		try {
			updateTable = getTable(token, mapper, client, resourceUrl, 0);
		} catch (OAuthSystemException | OAuthProblemException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Writer.updateRankingTable(updateTable.getJSON(), tabelle);
		System.out.println("Ende Update Tabelle: " + this.resourceUrl);
	}

	
	private  Tabelle getTable(String token, ObjectMapper mapper, OAuthClient client, String resourceUrl,
			int index)
			throws OAuthSystemException, OAuthProblemException, IOException, JsonParseException, JsonMappingException {

//		System.out.println("--------------- Get Table -----------------------");

		String tableUrl = "https://hbde-portal.liga.nu/rs/2014/federations/BHV/clubs/30461/teams/" + resourceUrl
				+ "/table";

		OAuthResourceResponse resourceResponse = getResourceResponse(token, client, tableUrl);
	
		TeamGroupTablesDTO tabelle = (TeamGroupTablesDTO) mapper.readValue(resourceResponse.getBody(),
				TeamGroupTablesDTO.class);

		Tabelle tabelleUpdate = new Tabelle();

		ListIterator<GroupTableTeamDTO> tabellenItr = tabelle.getGroupTables().get(index).getList().listIterator();
		while (tabellenItr.hasNext()) {
			GroupTableTeamDTO tabellenPlatz = tabellenItr.next();
//			System.out.println("Mannschaft: " + tabellenPlatz.getTeam() + "(" + tabellenPlatz.getTeamId() + ")");

			TabellenPlatz platz = new TabellenPlatz();

//			if (!mannschaftenHashMap.containsKey(tabellenPlatz.getTeamId()))
				try {
					platz.wpID = Search.searchTeamByBHVNummer(tabellenPlatz.getTeamId()).get("id").toString();
				} catch (NullPointerException nullExp) {
					
					System.out.println("Hier ist was schief gelaufen: " +tabellenPlatz.getTeam());
//					platz.wpID = Writer.createTeam(tabellenPlatz.getTeam(), tabellenPlatz.getTeamId()).get("id")
//							.toString();
				}
//			else
//				platz.wpID = mannschaftenHashMap.get(tabellenPlatz.getTeamId()).get("id").toString();

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
	private  OAuthResourceResponse getResourceResponse(String token, OAuthClient client, String resourceUrl)
			throws OAuthSystemException, OAuthProblemException {
		OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(resourceUrl).setAccessToken(token)

				.buildHeaderMessage();

		bearerClientRequest.addHeader("Accept", "application/json");
		OAuthResourceResponse resourceResponse = client.resource(bearerClientRequest, OAuth.HttpMethod.GET,
				OAuthResourceResponse.class);

		return resourceResponse;
	}
}

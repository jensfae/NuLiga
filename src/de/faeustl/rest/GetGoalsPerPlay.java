package de.faeustl.rest;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;

public class GetGoalsPerPlay {
	private static Writer writer;

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub

		JsonArray matchDays = DHB_Rest_Services.getSpielplan("1593", "579");
		try {
			GetGoalsPerPlay.getMatchDays(matchDays);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void getMatchDays(JsonArray pMatchDay) throws IOException

	{
		writer = Files.newBufferedWriter(Paths.get("toreProSpielUndSpieler.csv"));
		CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
		String[] headerRecord = { "Spieltag","SpielID","Mannschaft", "Name", "Nummer", "Tore", "7m", "7m Tore", "Gelb", "2min",
				"Disqualification", "Position", "Home_Away" };
		csvWriter.writeNext(headerRecord);

		for (int i = 0; i < pMatchDay.size(); i++) {
			String spieltag = pMatchDay.get(i).getAsJsonObject().get("title").getAsString();
			System.out.println(spieltag);
			JsonArray matches = pMatchDay.get(i).getAsJsonObject().get("matches").getAsJsonArray();
			for (int y = 0; y < matches.size(); y++) {
				JsonObject match = DHB_Rest_Services
						.getMatch_Info(matches.get(y).getAsJsonObject().get("_id").getAsString());
				String spiel_id = matches.get(y).getAsJsonObject().get("_id").getAsString();
				JsonObject home_team = match.get("home_team").getAsJsonObject();
				JsonArray home_teamPlayers = home_team.get("players").getAsJsonArray();
				JsonArray away_teamPlayers = match.get("away_team").getAsJsonObject().get("players").getAsJsonArray();
				writePlayer(spieltag, spiel_id, csvWriter, home_team, home_teamPlayers, "home");
				writePlayer(spieltag, spiel_id, csvWriter, match.get("away_team").getAsJsonObject(), away_teamPlayers, "away");
			}

		}
		writer.close();
		System.out.println("Fertig............!!!!");

	}

	private static void writePlayer(String pSpieltag, String spiel_id ,CSVWriter csvWriter, JsonObject pTeam, JsonArray pTeamPlayers, String pTeam_Art) {
		for (JsonElement player : pTeamPlayers) {

			csvWriter.writeNext(new String[] { pSpieltag,
					spiel_id,
					pTeam.get("name").getAsString().split(" 3.")[0],
					
					player.getAsJsonObject().get("name").getAsString(),
					player.getAsJsonObject().get("number").getAsString(),
					player.getAsJsonObject().get("goals").getAsString(),
					player.getAsJsonObject().get("seven_meter_goals").getAsString(),
					player.getAsJsonObject().get("seven_meter").getAsString(),
					
					player.getAsJsonObject().get("warning").getAsString(),
					player.getAsJsonObject().get("2_minute_penalty").getAsString(),
					player.getAsJsonObject().get("disqualification").getAsString(),
					player.getAsJsonObject().get("position").getAsString(),
					pTeam_Art

			});
		}
	}
}

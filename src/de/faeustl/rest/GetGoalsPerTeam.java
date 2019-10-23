package de.faeustl.rest;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.opencsv.CSVWriter;

public class GetGoalsPerTeam {

	private static ArrayList<String> teams = new ArrayList<String>();
	// private static String[] players = new String[];
	private static Writer writer;

	public static void main(String[] args) throws IOException {

		teams.add("768"); // ESV
		teams.add("1920"); // TSV Wolfschlugen
		teams.add("750"); // TSV Haunstetten
		teams.add("777"); // TuS Metzingen
		teams.add("909"); // ASV Dachau
		teams.add("765"); // Sportverein Allensbach
		teams.add("1923"); // SG Schozach-Bottwartal
		teams.add("762"); // SG Kappelwindeck/Steinbach
		teams.add("126"); // TV Nellingen
		teams.add("780"); // HCD Gröbenzell
		teams.add("753"); // TG 88 Pforzheim
		teams.add("771"); // TV Möglingen

		// TODO Auto-generated method stub

		writer = Files.newBufferedWriter(Paths.get("toreProSpieler.csv"));
		CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
		String[] headerRecord = { "Name", "Vorname", "Alter", "Tore", "Spiele", "Verein" };
		csvWriter.writeNext(headerRecord);
		for (String team : teams) {
			System.out.println(team);
			JsonArray squad = DHB_Rest_Services.getTeamInfo(team);

			for (JsonElement spieler : squad)
			// .get(0).getAsJsonObject();
			{
				
				String verein = spieler.getAsJsonObject().get("contracts").getAsJsonArray().get(0).getAsJsonObject().get("club_name").getAsString();
				
				csvWriter.writeNext(new String[]{spieler.getAsJsonObject().get("player_lastname").getAsString(),
						spieler.getAsJsonObject().get("player_firstname").getAsString(),
						spieler.getAsJsonObject().get("age").getAsString(),
						spieler.getAsJsonObject().get("goals").getAsString(),
						spieler.getAsJsonObject().get("matches").getAsString(),
						verein
				});
				
				System.out.print(".");

			}
			System.out.println();

		}
		writer.close();
		System.out.println();
		System.out.println("Fertig");
	}
}

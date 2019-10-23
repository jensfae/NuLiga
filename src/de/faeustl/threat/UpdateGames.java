package de.faeustl.threat;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;

import de.faeustl.model.BHVSpielRest;
import de.faeustl.wp.Search;
import de.faeustl.wp.Writer;
import nu.liga.open.rs.v2014.dto.championships.MeetingAbbrDTO;
import nu.liga.open.rs.v2014.dto.championships.MeetingsDTO;

public class UpdateGames extends Thread {

	MeetingsDTO meetings = null;
	
	public UpdateGames(MeetingsDTO  myMeetings ) {
		
		
		this.meetings = myMeetings;
		
	}
	public UpdateGames(ThreadGroup tg, String name, MeetingsDTO  myMeetings) {
		super(tg, name);
		this.meetings = myMeetings;
	}
	
	public void run()
	{
		Iterator itr = meetings.getList().iterator();
		
		while (itr.hasNext()) {
			
			MeetingAbbrDTO meeting = (MeetingAbbrDTO) itr.next();
			updateGame(meeting);
		}
	}
	
	private void updateGame(MeetingAbbrDTO meeting )
	{
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
//			if (mannschaftenHashMap.containsKey(meeting.getTeamHomeId())) {
//
//				System.out.println("Found Team: " + meeting.getTeamHome());
//				gamesHome = mannschaftenHashMap.get(meeting.getTeamHomeId());
//			} else {
				gamesHome = Search.searchTeamByBHVNummer(meeting.getTeamHomeId());
//				mannschaftenHashMap.put(meeting.getTeamHomeId().trim(), gamesHome);
//			}

//			if (mannschaftenHashMap.containsKey(meeting.getTeamGuestId())) {
//				System.out.println("Found Team: " + meeting.getTeamGuest());
//				gamesGuest = mannschaftenHashMap.get(meeting.getTeamGuestId().trim());
//			} else {
				gamesGuest = Search.searchTeamByBHVNummer(meeting.getTeamGuestId());
//				mannschaftenHashMap.put(meeting.getTeamGuestId().trim(), gamesGuest);
//			}

//			if (venuesHashMap.containsKey(meeting.getCourtHallNumbers())) {
//				venue = venuesHashMap.get(meeting.getCourtHallNumbers());
//				System.out.println("Found Venue: " + meeting.getCourtHallName());
//			} else {
//				System.out.println("Search Venue: " + meeting.getCourtHallName());
				venue = Search.findVenue("", meeting.getCourtHallNumbers(), "");
//				venuesHashMap.put(meeting.getCourtHallNumbers(), venue);
//			}
			BHVSpielRest spiel = new BHVSpielRest();
			if (venue != null)
			spiel.setWpHallennummer(venue.get("id").getAsString());
			else
				spiel.setWpHallennummer("");
				
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
//				Writer.createSpiel(spiel.getJSON());
			} else {
				System.out.println(this.getName() + "      Update Spiel: " + spiel.getSpielnummer() + " " + spiel.getHeimmannschaft() + " vs. " + spiel.getGastmannschaft());
				Writer.update(wpSpiel, spiel.getJSON());
			}
		}
	}
}

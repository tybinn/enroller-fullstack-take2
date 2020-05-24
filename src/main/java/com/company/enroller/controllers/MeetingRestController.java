package com.company.enroller.controllers;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.enroller.model.Meeting;
import com.company.enroller.model.Participant;
import com.company.enroller.persistence.MeetingService;

@RestController
@RequestMapping("/api/meetings")
public class MeetingRestController {

	@Autowired
	MeetingService meetingService;

	@RequestMapping(value = "", method = RequestMethod.GET)
	public ResponseEntity<?> getMeetings(@RequestParam(defaultValue = "id") String sort,
			@RequestParam(defaultValue = "asc") String order) {
		Collection<Meeting> meetings = meetingService.getAllSortedByTitle(sort, order);
		return new ResponseEntity<Collection<Meeting>>(meetings, HttpStatus.OK);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getMeeting(@PathVariable("id") long id) {
		Meeting meeting = meetingService.findById(id);
		if (meeting == null) {
			return new ResponseEntity<>("Meeting does not exist.", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	public ResponseEntity<?> createMeeting(@RequestBody Meeting meetring) {
		Meeting foundMeeting = meetingService.findById(meetring.getId());
		if (foundMeeting != null) {
			return new ResponseEntity<>("Unable to create. A meeting with Id " + meetring.getId() + " already exist.",
					HttpStatus.CONFLICT);
		}

		meetingService.add(meetring);
		return new ResponseEntity<Meeting>(meetring, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/{id}/participants", method = RequestMethod.GET)
	public ResponseEntity<?> getParticipants(@PathVariable("id") long id) {
		Meeting meeting = meetingService.findById(id);
		if (meeting == null) {
			return new ResponseEntity<>("Meeting does not exist.", HttpStatus.NOT_FOUND);
		}
		Collection<Participant> participants = meetingService.getAllParticipants(id);
		return new ResponseEntity<Collection<Participant>>(participants, HttpStatus.OK);
	}

	@RequestMapping(value = "/{id}/participants", method = RequestMethod.POST)
	public ResponseEntity<?> addParticipantToMeeting(@PathVariable("id") long id,
			@RequestBody Participant participant) {
		Meeting meeting = meetingService.findById(id);
		if (meeting == null) {
			return new ResponseEntity<>("Meeting does not exist.", HttpStatus.NOT_FOUND);
		}
		if (meetingService.isParticipantEgsist(participant.getLogin())) {
			return new ResponseEntity<>("Participant with login " + participant.getLogin() + " does not exist.",
					HttpStatus.NOT_FOUND);
		}

		if (meetingService.findByLoginInMeeting(id, participant.getLogin()) != null) {
			return new ResponseEntity("Unable to add. A participant with login " + participant.getLogin()
					+ " already added in to the Meeting.", HttpStatus.CONFLICT);
		}

		meeting.addParticipant(participant);
		meetingService.addParticipantToMeeting(meeting);

		return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteMeeting(@PathVariable("id") long id) {
		Meeting meeting = meetingService.findById(id);
		if (meeting == null) {
			return new ResponseEntity<>("Meeting does not exist.", HttpStatus.NOT_FOUND);
		}
		meetingService.deleteMeeting(meeting);

		return new ResponseEntity<Meeting>(meeting, HttpStatus.NO_CONTENT);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public ResponseEntity<?> updatenMeeting(@PathVariable("id") long id, @RequestBody Meeting incommingMeeting) {
		Meeting meeting = meetingService.findById(id);
		if (meeting == null) {
			return new ResponseEntity<>("Meeting does not exist.", HttpStatus.NOT_FOUND);
		}
		meeting.setTitle(incommingMeeting.getTitle());
		meeting.setDescription(incommingMeeting.getDescription());
		meeting.setDate(incommingMeeting.getDate());

		meetingService.update(meeting);

		return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
	}

	@RequestMapping(value = "/{id}/participants/{participantId}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteParticipantFromMeeting(@PathVariable("participantId") String login,
			@PathVariable("id") long id) {
		Meeting meeting = meetingService.findById(id);
		if (meeting == null) {
			return new ResponseEntity<>("Meeting does not exist.", HttpStatus.NOT_FOUND);
		}

		Participant participant = meetingService.findByLoginInMeeting(id, login);
		if (participant == null) {
			return new ResponseEntity<>("Participane with login " + login + " was not added to this meeting",
					HttpStatus.NOT_FOUND);
		}
		meeting.removeParticipant(participant);
		meetingService.deleteParticipantFromMeeting(meeting);
		return new ResponseEntity<Participant>(participant, HttpStatus.NO_CONTENT);
	}

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public ResponseEntity<?> searchMeetings(@RequestParam(defaultValue = "") String type,
			@RequestParam(defaultValue = "") String query) {
		if (query.equals("")) {
			return getMeetings("", "");
		}
		if (type.equals("participant")) {
			Collection<Meeting> meetings = meetingService.getMeetingsWithParticipant(query);
			if (meetings.size() > 0) {
				return new ResponseEntity<Collection<Meeting>>(meetings, HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Meeting with participant " + query + " does not exist.",
						HttpStatus.NOT_FOUND);
			}
		}

		Collection<Meeting> meetings = meetingService.getMeetingsWithSubstring(query);
		if (meetings.size() > 0) {
			return new ResponseEntity<Collection<Meeting>>(meetings, HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Meeting with given title or description does not exist.",
					HttpStatus.NOT_FOUND);
		}

	}
}

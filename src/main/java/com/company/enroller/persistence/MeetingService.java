package com.company.enroller.persistence;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import com.company.enroller.model.Meeting;
import com.company.enroller.model.Participant;

@Component("meetingService")
public class MeetingService {

	DatabaseConnector connector;

	public MeetingService() {
		connector = DatabaseConnector.getInstance();
	}

	public Collection<Meeting> getAllSortedByTitle(String sort, String order) {
		Criteria crit = connector.getSession().createCriteria(Meeting.class);
		if (order.equals("desc")) {
			crit.addOrder(Order.desc(sort));
		} else {
			crit.addOrder(Order.asc(sort));
		}

		return crit.list();
	}

	public Meeting findById(long id) {
		return (Meeting) connector.getSession().get(Meeting.class, id);
	}

	public Meeting add(Meeting meeting) {
		Transaction transaction = connector.getSession().beginTransaction();
		connector.getSession().save(meeting);
		transaction.commit();
		return meeting;
	}

	public Collection<Participant> getAllParticipants(long id) {
		return ((Meeting) connector.getSession().get(Meeting.class, id)).getParticipants();
	}

	public boolean isParticipantEgsist(String login) {
		return !(connector.getSession().get(Participant.class, login) != null);
	}

	public void addParticipantToMeeting(Meeting meeting) {
		Transaction transaction = connector.getSession().beginTransaction();
		connector.getSession().save(meeting);
		transaction.commit();
	}

	public void deleteMeeting(Meeting meeting) {
		Transaction transaction = connector.getSession().beginTransaction();
		connector.getSession().delete(meeting);
		transaction.commit();
	}

	public Participant findByLoginInMeeting(long id, String login) {
		Collection<Participant> participants = ((Meeting) connector.getSession().get(Meeting.class, id))
				.getParticipants();
		for (Participant participant : participants) {
			if (participant.getLogin().equals(login)) {
				return participant;
			}
		}

		connector.getSession().get(Meeting.class, id);

		return null;
	}

	public void deleteParticipantFromMeeting(Meeting meeting) {
		Transaction transaction = connector.getSession().beginTransaction();
		connector.getSession().save(meeting);
		transaction.commit();
	}

	public void update(Meeting meeting) {
		Transaction transaction = connector.getSession().beginTransaction();
		connector.getSession().merge(meeting);
		transaction.commit();
	}

	public Collection<Meeting> getMeetingsWithSubstring(String query) {
		Criteria crit = connector.getSession().createCriteria(Meeting.class);

		Criterion findInTitle = Restrictions.like("title", query, MatchMode.ANYWHERE);
		Criterion findInDescription = Restrictions.like("description", query, MatchMode.ANYWHERE);

		LogicalExpression orExp = Restrictions.or(findInTitle, findInDescription);
		crit.add(orExp);

		return crit.list();
	}

	public Collection getMeetingsWithParticipant(String query) {

		Criteria crit = connector.getSession().createCriteria(Meeting.class);

		crit.createAlias("participants", "participantsAlias");
		crit.add(Restrictions.eq("participantsAlias.login", query));

		return crit.list();
	}

}

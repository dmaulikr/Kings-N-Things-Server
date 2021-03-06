package com.kings.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kings.controllers.account.NotLoggedInException;
import com.kings.http.HttpResponseMessage;
import com.kings.model.SentMessage;
import com.kings.model.User;

/**
 * Used for the client asking the server for messages
 * @author devinlynch
 *
 */

@RequestMapping(value="messages")
public class MessageController extends AbstractLoggedInOnlyController {
	
	@RequestMapping(value="queuedMessages")
	public @ResponseBody String newMessages(
			HttpServletRequest req,
			HttpServletResponse res) throws NotLoggedInException{

		User user = getUser(req);
		List<SentMessage> messages = getDataAccess().getQueuedMessagesForUser(user.getUserId());
		
		HttpResponseMessage msg = new HttpResponseMessage();
		msg.setType("newMessages");
		
		Set<HashMap<String, Object>> messageMaps = new HashSet<HashMap<String,Object>>();
		
		for(SentMessage sentMessage: messages) {
			
			sentMessage.setQueued(false);
			messageMaps.add(sentMessage.toSerializedFormat());
		}
		
		msg.addToData("messages", messageMaps);

		return msg.toJson();
	}
	
	@RequestMapping(value="gotMessage")
	public @ResponseBody String gotMessage(
			@RequestParam String messageId,
			HttpServletRequest req,
			HttpServletResponse res) throws NotLoggedInException{

		User user = getUser(req);
		try{
			SentMessage message = getDataAccess().getQueuedMessageForUser(user.getUserId(), messageId);
			
			if(message == null) {
				return "{}";
			}
			
			message.setQueued(false);
			
			getDataAccess().save(message);

			return "{}";
		} catch (Exception e) {
			return genericError().toJson();
		}
	}
	
	
	/**
	 * Returns new messages after a given date that were sent to the user
	 * </br>
	 * <b>Given date must be in format: yyyy-MM-dd hh:mm:ss.SSS</b>
	 * @param lastMessageDate
	 * @param req
	 * @param res
	 * @return
	 * @throws NotLoggedInException
	 */
	@RequestMapping(value="newMessages")
	public @ResponseBody String newMessages(
			String lastMessageDate,
			HttpServletRequest req,
			HttpServletResponse res) throws NotLoggedInException{

		User user = getUser(req);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		
		try{
			Date date =  sdf.parse(lastMessageDate);
			Set<SentMessage> messages = user.getSentMessageAfterDate(date);
			
			HttpResponseMessage msg = new HttpResponseMessage();
			msg.setType("newMessages");
			
			Set<HashMap<String, Object>> messageMaps = new HashSet<HashMap<String,Object>>();
			
			for(SentMessage sentMessage: messages) {
				messageMaps.add(sentMessage.toSerializedFormat());
			}
			
			msg.addToData("messages", messageMaps);

			return msg.toJson();
		} catch (ParseException e) {
			return genericError().toJson();
		}
	}
	
}

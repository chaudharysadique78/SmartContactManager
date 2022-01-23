package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendEmail(String from,String to,String message, String subject) {
		boolean flag=false;
		
		String host="smtp.gmail.com";
		
		//setting properties
		
		Properties properties = System.getProperties();
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		//create Session
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				// TODO Auto-generated method stub
				return new PasswordAuthentication("javacodersakinaka@gmail.com", "Sadique@123");
			}					
		});	
		session.setDebug(true);
		
		//compose the message
		MimeMessage m=new MimeMessage(session);
		
		try {
			
			//set from
			m.setFrom(from);
			
			//set subject
			m.setSubject(subject);
			
			//set Recipient
			m.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			//set text message
			m.setContent(message,"text/html");
			
			//send message
			Transport.send(m);
			flag=true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return flag;
	}
	
}

package net.gvcc.goffice.email;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestPropertySources;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.web.client.RestTemplate;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

@SpringBootTest
@Import(EMailSendHelper.class)
@EnableAutoConfiguration
@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration(classes = { RestTemplate.class }, loader = AnnotationConfigContextLoader.class)
@TestPropertySources({ @TestPropertySource("classpath:application.properties") })
public class EMailSendHelperTest {
	private static Logger LOGGER = LoggerFactory.getLogger(EMailSendHelperTest.class);

	protected GreenMail smtpServer;

	// ========================================================================================//

	@Autowired
	private EMailSendHelper emailSender;

	// ========================================================================================//

	@BeforeEach
	void startServer() {
		smtpServer = new GreenMail(ServerSetupTest.SMTP); // default: porta 3025
		smtpServer.start();
	}

	@AfterEach
	void stopServer() {
		if (smtpServer != null) {
			smtpServer.stop();
		}
	}

	// ========================================================================================//

	final String from = "goffice20@gvcc.net";
	final String subject = "Ti arriva una mail test";
	final String body = "Scemo chi legge";

	String[] mailTo = new String[] { "goffice20@gvcc.net" };
	String[] mailToCC = new String[] { "goffice20@gvcc.net" };
	String[] mailToBCC = new String[] { "goffice20@gvcc.net" };

	@Test
	public void sendEMailTest() {
		LOGGER.info("sendEMailTest - START");

		try {

			LOGGER.info("sendEMailTest - mailTo:...........{}", StringUtils.join(mailTo, ","));
			LOGGER.info("sendEMailTest - mailToCC:.........{}", StringUtils.join(mailToCC, ","));
			LOGGER.info("sendEMailTest - mailToBCC:........{}", StringUtils.join(mailToBCC, ","));

			Map<String, InputStreamSource> allegati = new HashMap<>();

			URL file = EMailSendHelperTest.class.getClassLoader().getResource("plan5.PDF");

			allegati.put("Primo   file PDF - FileSystemResource", new FileSystemResource(new File(file.toURI())));
			allegati.put("Secondo file TXT - ByteArrayResource", new ByteArrayResource("ciao con accéntàte".getBytes(Charset.defaultCharset())));
			allegati.put("Secondo file JSON - ByteArrayResource", new ByteArrayResource("{ \"test\" : true, \"index\" : 1, \"value\" : \"test\" }".getBytes(Charset.defaultCharset())));

			LOGGER.info("sendEMailTest - attachmentCount:..{}", allegati.size());

			boolean ok = emailSender.sendMessage(mailTo, mailToCC, mailToBCC, from, subject, body, allegati);

			checkOutcome(ok, mailTo, mailToCC, mailToBCC, from, subject, body, allegati);
		} catch (Exception e) {
			LOGGER.error("sendEMailTest", e);
		}

		LOGGER.info("sendEMailTest - END");
	}

	@Test
	public void sendVerySimpleEMailTest() {
		LOGGER.info("sendVerySimpleEMailTest - START");

		try {

			LOGGER.info("sendVerySimpleEMailTest - mailTo:....{}", StringUtils.join(mailTo));
			LOGGER.info("sendVerySimpleEMailTest - mailToCC:..{}", StringUtils.join(mailToCC));

			boolean ok = emailSender.sendMessage(mailTo, mailToCC, from, subject, body);

			checkOutcome(ok, mailTo, mailToCC, null, from, subject, body, null);
		} catch (Exception e) {
			LOGGER.error("sendVerySimpleEMailTest", e);
		}

		LOGGER.info("sendVerySimpleEMailTest - END");
	}

	// ========================================================================================================================================= //

	private void checkOutcome(boolean outcome, String[] mailTo, String[] mailToCC, String[] mailToBCC, String from, String subject, String body, Map<String, InputStreamSource> allegati)
			throws MessagingException, IOException {
		LOGGER.info("checkOutcome - START");

		assertTrue(outcome);

		MimeMessage[] receivedEmails = smtpServer.getReceivedMessages();
		assertNotNull(receivedEmails);

		int receivedEmailCount = receivedEmails.length;
		int sentEmailCount = mailTo.length + mailToCC.length + (mailToBCC == null ? 0 : mailToBCC.length);
		LOGGER.info("checkOutcome - email count: sent/received={}/{}", sentEmailCount, receivedEmailCount);
		assertTrue(receivedEmailCount == sentEmailCount);

		for (MimeMessage message : receivedEmails) {
			Address[] receivedFrom = message.getFrom();
			assertNotNull(receivedFrom);
			assertTrue(receivedFrom.length == 1);
			assertTrue(from.equalsIgnoreCase(receivedFrom[0].toString()));

			String receivedSubject = message.getSubject();
			assertNotNull(receivedSubject);
			assertTrue(subject.equals(receivedSubject));

			checkAddresses(message.getRecipients(RecipientType.TO), mailTo);

			checkAddresses(message.getRecipients(RecipientType.CC), mailToCC);

			// checkAddresses(message.getRecipients(RecipientType.BCC), mailToBCC);

			Object receivedMimeBody = message.getContent();
			assertNotNull(receivedMimeBody);
			assertTrue(receivedMimeBody instanceof MimeMultipart);

			MimeMultipart multipartBody = (MimeMultipart) receivedMimeBody;
			for (int i = 0; i < multipartBody.getCount(); i++) {
				BodyPart part = multipartBody.getBodyPart(i);

				System.out.println();

				switch (StringUtils.defaultString(part.getDisposition())) {
					case Part.ATTACHMENT:
						assertNotNull(allegati.get(part.getFileName()));
						break;

					default:
						String receivedBody = getText(part);
						assertTrue(receivedBody.contains(body));
						break;
				}
			}
		}

		LOGGER.info("checkOutcome - END");
	}

	private void checkAddresses(Address[] recipients, String[] mails) {
		assertNotNull(recipients);
		assertTrue(recipients.length == mails.length);
		assertTrue(Arrays.asList(mails).toString().equals(Arrays.asList(recipients).toString()));
	}

	private static String getText(Part part) throws MessagingException, IOException {
		String body = null;

		if (part.isMimeType("text/*")) {
			body = (String) part.getContent();
		} else if (part.isMimeType("multipart/alternative")) {
			// prefer html text over plain text
			Multipart multipart = (Multipart) part.getContent();
			String text = null;
			for (int i = 0; i < multipart.getCount(); i++) {
				Part bodyPart = multipart.getBodyPart(i);
				if (bodyPart.isMimeType("text/plain")) {
					if (text == null) {
						text = getText(bodyPart);
					}
					continue;
				} else if (bodyPart.isMimeType("text/html")) {
					body = getText(bodyPart);
					if (body != null) {
						break;
					}
				} else {
					body = getText(bodyPart);
					break;
				}
			}
			return text;
		} else if (part.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) part.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				body = getText(mp.getBodyPart(i));
				if (body != null) {
					break;
				}
			}
		}

		return body;
	}
}

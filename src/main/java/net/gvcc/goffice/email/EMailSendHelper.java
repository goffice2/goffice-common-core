package net.gvcc.goffice.email;

import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

@Configuration
public class EMailSendHelper {

	/**
	 * Property name to enable/disable debug info
	 */
	public static final String MAIL_DEBUG = "mail.debug";
	/**
	 * Property name to enable/disable <code>startssl</code> option
	 */
	public static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
	/**
	 * Property name to configure SMTP auth
	 */
	public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
	/**
	 * Property name to configure mail protocol
	 */
	public static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";
	/**
	 * Mail protocol to use
	 */
	public static final String SMTP = "smtp";

	private static final Logger LOGGER = LogManager.getLogger(EMailSendHelper.class);

	@Autowired
	private JavaMailSender emailSender;

	/**
	 * get the java mail sender
	 * 
	 * @param mailUser
	 *            mail user
	 * @param mailPassword
	 *            mail password
	 * @param smtpHost
	 *            mail server host
	 * @param smtpPort
	 *            mail server port
	 * @param smtpAuth
	 *            auth for mail configuration messaging
	 * @param smtpStartTls
	 *            mail.smtp.starttls.enable value
	 * @param debug
	 *            debugging activation
	 * @return JavaMailSender
	 */
	@Bean
	public JavaMailSender getJavaMailSender( //
			@Value("${goffice.common.mail.username}") String mailUser, //
			@Value("${goffice.common.mail.password}") String mailPassword, //
			@Value("${goffice.common.mail.smtphost}") String smtpHost, //
			@Value("${goffice.common.mail.smtpport}") String smtpPort, //
			@Value("${goffice.common.mail.smtpauth}") String smtpAuth, //
			@Value("${goffice.common.mail.smtpstarttls}") String smtpStartTls, //
			@Value("${goffice.common.mail.debug}") String debug) {
		LOGGER.debug("getJavaMailSender - START");

		LOGGER.debug("getJavaMailSender - mailUser: {}", mailUser);
		LOGGER.debug("getJavaMailSender - mailPassword: {}", StringUtils.isBlank(mailPassword) ? "" : "*********");
		LOGGER.debug("getJavaMailSender - smtpHost: {}", smtpHost);
		LOGGER.debug("getJavaMailSender - smtpPort: {}", smtpPort);
		LOGGER.debug("getJavaMailSender - smtpauth: {}", smtpAuth);
		LOGGER.debug("getJavaMailSender - smtpstarttls: {}", smtpStartTls);
		LOGGER.debug("getJavaMailSender - debug: {}", debug);

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(smtpHost);
		mailSender.setPort(Integer.parseInt(smtpPort));

		mailSender.setUsername(mailUser);
		mailSender.setPassword(mailPassword);

		Properties props = mailSender.getJavaMailProperties();
		props.put(MAIL_TRANSPORT_PROTOCOL, SMTP);
		props.put(MAIL_SMTP_AUTH, smtpAuth);
		props.put(MAIL_SMTP_STARTTLS_ENABLE, smtpStartTls);
		props.put(MAIL_DEBUG, debug);

		LOGGER.debug("getJavaMailSender - END");

		return mailSender;
	}

	/**
	 * Using this method, you can sen an email message via SMTP protocol.
	 * 
	 * @param mailTo
	 *            The target addresses as TO recipient
	 * @param mailCc
	 *            The target addresses as CC recipient
	 * @param mailBcc
	 *            The target addresses as BCC recipient
	 * @param mailFrom
	 *            The sender email address
	 * @param mailSubject
	 *            The subject of the email message
	 * @param mailBody
	 *            The content of the email message
	 * @param attachmentList
	 *            The attachments of the email message
	 * @return true if the massage was sent correctly; false otherwise
	 * @throws MessagingException
	 * 
	 * @see boolean sendMessage(String[] mailTo, String[] mailCc, String mailFrom, String mailSubject, String mailBody)
	 */
	public boolean sendMessage(String[] mailTo, String[] mailCc, String[] mailBcc, String mailFrom, String mailSubject, String mailBody, Map<String, InputStreamSource> attachmentList)
			throws MessagingException {
		LOGGER.info("sendMessage - START");

		boolean emailSentOK = false;

		try {
			MimeMessage message = emailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setFrom(mailFrom);
			helper.setTo(mailTo);

			if (mailCc != null && mailCc.length != 0) {
				helper.setCc(mailCc);
			}
			if (mailBcc != null && mailBcc.length != 0) {
				helper.setBcc(mailBcc);
			}

			helper.setSubject(mailSubject);
			helper.setText(mailBody);

			if (attachmentList != null) {
				attachmentList.forEach((k, v) -> {
					try {
						helper.addAttachment(k, v);
					} catch (MessagingException e) {
						throw new RuntimeException("error while adding email attachment:", e);
					}
				});
			}

			emailSender.send(message);

			emailSentOK = true;
		} catch (MailException e) {
			LOGGER.error("sendMessage - error during sending simple mail", e);
		}

		LOGGER.info("sendMessage - END");

		return emailSentOK;
	}

	/**
	 * 
	 * @param mailTo
	 *            The target addresses as TO recipient
	 * @param mailCc
	 *            The target addresses as CC recipient
	 * @param mailFrom
	 *            The sender email address
	 * @param mailSubject
	 *            The subject of the email message
	 * @param mailBody
	 *            The content of the email message
	 * @return true if the massage was sent correctly; false otherwise
	 * @throws MessagingException
	 * @see boolean sendMessage(String[] mailTo, String[] mailCc, String[] mailBcc, String mailFrom, String mailSubject, String mailBody, Map&lt;String, InputStreamSource&gt; attachmentList)
	 */
	public boolean sendMessage(String[] mailTo, String[] mailCc, String mailFrom, String mailSubject, String mailBody) throws MessagingException {
		return sendMessage(mailTo, mailCc, null, mailFrom, mailSubject, mailBody, null);
	}
}

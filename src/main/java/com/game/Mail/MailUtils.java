package com.game.Mail;

import com.game.util.StrUtil;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class MailUtils {

	private MailUtils() {}

	final static String CHARSET = "UTF-8";

	private static Map<String, Session> sessionCache = new HashMap<String, Session>();

	private MimeMessage mimeMessage;
	
	public static MailUtils newInstance(MailAccount account) throws UnsupportedEncodingException, MessagingException {
		MailUtils instance = new MailUtils();
		instance.getMimeMessage(account, false);
		return instance;
	}
	
	public static MailUtils newInstance(MailAccount account, boolean isSSL) throws UnsupportedEncodingException, MessagingException {
		MailUtils instance = new MailUtils();
		instance.getMimeMessage(account, isSSL);
		return instance;
	}

	/**
	 * 发送邮件
	 * @param to
	 * @param mailBody
	 * @throws Exception
	 * @author YXD
	 * @throws MessagingException 
	 * @throws AddressException 
	 * @throws UnsupportedEncodingException 
	 */
	public void send(String to, MailBody mailBody) throws AddressException, MessagingException, UnsupportedEncodingException {
		// 收件人
		mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));

		// 设置邮件内容
		this.setContent(mailBody);
		
		// 设置发信时间
		mimeMessage.setSentDate(new Date());
		// 保存邮件
		mimeMessage.saveChanges();

		// 发送邮件
		Transport.send(mimeMessage);
	}
	
	/**
	 * 发送邮件
	 * @param tos
	 * @param mailBody
	 * @throws Exception
	 * @author YXD
	 */
	public void send(List<String> tos, MailBody mailBody) throws AddressException, MessagingException, UnsupportedEncodingException {
		Address[] address = new InternetAddress[tos.size()];
		for (int i = 0; i < tos.size(); i ++) {
			address[i] = new InternetAddress(tos.get(i));
		}
		
		// 收件人
		mimeMessage.setRecipients(Message.RecipientType.TO, address);

		// 设置邮件内容
		this.setContent(mailBody);
		
		// 设置发信时间
		mimeMessage.setSentDate(new Date());
		// 保存邮件
		mimeMessage.saveChanges();

		// 发送邮件
		Transport.send(mimeMessage);
	}
	
	/**
	 * 发送邮件
	 * @param to
	 * @param copyTos
	 * @param mailBody
	 * @throws Exception
	 * @author YXD
	 */
	public void send(String to, List<String> copyTos, MailBody mailBody) throws AddressException, MessagingException, UnsupportedEncodingException {
		Address[] copyTo = new InternetAddress[copyTos.size()];
		for (int i = 0; i < copyTos.size(); i ++) {
			copyTo[i] = new InternetAddress(copyTos.get(i));
		}
		
		// 收件人
		mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));

		// 设置抄送人
		mimeMessage.setRecipients(Message.RecipientType.CC, copyTo);

		// 设置邮件内容
		this.setContent(mailBody);
		
		// 设置发信时间
		mimeMessage.setSentDate(new Date());
		// 保存邮件
		mimeMessage.saveChanges();

		// 发送邮件
		Transport.send(mimeMessage);
	}
	
	/**
	 * 发送邮件
	 * @param tos
	 * @param copyTos
	 * @param mailBody
	 * @throws Exception
	 * @author YXD
	 */
	public void send(List<String> tos, List<String> copyTos, MailBody mailBody) throws AddressException, MessagingException, UnsupportedEncodingException {
		Address[] address = new InternetAddress[tos.size()];
		for (int i = 0; i < tos.size(); i ++) {
			address[i] = new InternetAddress(tos.get(i));
		}
		
		Address[] copyTo = new InternetAddress[copyTos.size()];
		for (int i = 0; i < copyTos.size(); i ++) {
			copyTo[i] = new InternetAddress(copyTos.get(i));
		}
		
		// 收件人
		mimeMessage.setRecipients(Message.RecipientType.TO, address);

		// 设置抄送人
		mimeMessage.setRecipients(Message.RecipientType.CC, copyTo);

		// 设置邮件内容
		this.setContent(mailBody);
		
		// 设置发信时间
		mimeMessage.setSentDate(new Date());
		// 保存邮件
		mimeMessage.saveChanges();

		// 发送邮件
		Transport.send(mimeMessage);
	}

	/**
	 * 设置邮件内容
	 * @param mailBody
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 * @author YXD
	 */
	private void setContent(MailBody mailBody) throws MessagingException, UnsupportedEncodingException {
		// 邮件主题
		String subject = mailBody.getSubject();
		mimeMessage.setSubject(subject, CHARSET);

		// 正文
		MimeMultipart multipart = new MimeMultipart();
		multipart.setSubType("related");

		// 正文内容
		MimeBodyPart content = new MimeBodyPart();
		content.setContent(mailBody.getContent(), mailBody.getContentType().value);
		multipart.addBodyPart(content);

		// 正文中的图片
		List<Map<String, Object>> images = mailBody.getImages();
		if (images != null && !images.isEmpty()) {
			for (Map<String, Object> mailImage : images) {
				MimeBodyPart image = new MimeBodyPart();
				DataHandler imageHander = new DataHandler(new FileDataSource((File) mailImage.get("image")));
				image.setDataHandler(imageHander);
				image.setContentID((String) mailImage.get("cid")); // src="cid:xxxx"
				multipart.addBodyPart(image);
			}

		}

		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setContent(multipart);

		// 邮件内容
		MimeMultipart mimeMultipart = new MimeMultipart();
		mimeMultipart.setSubType("mixed");
		mimeMultipart.addBodyPart(mimeBodyPart);

		// 添加附件
		List<Map<String, Object>> files = mailBody.getAttachments();
		if (files != null && !files.isEmpty()) { // 没有附件则抛出错误
			for (Map<String, Object> mailAttachment : files) {
				MimeBodyPart attachment = new MimeBodyPart();
				DataHandler attachmentHander = new DataHandler(new FileDataSource((File) mailAttachment.get("attachment")));
				attachment.setDataHandler(attachmentHander);
				attachment.setFileName(MimeUtility.encodeText((String) mailAttachment.get("alias")));
				mimeMultipart.addBodyPart(attachment);
			}
		}

		mimeMessage.setContent(mimeMultipart);
	}

	/**
	 * 获取MimeMessage实例
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 * @author YXD
	 */
	private void getMimeMessage(MailAccount account, boolean isSSL) throws UnsupportedEncodingException, MessagingException {
		Session session = getSession(account, isSSL);
		mimeMessage = new MimeMessage(session);

		// 设置发件人
		String accountName = account.getAccount();
		String nickname = account.getNickname();
		InternetAddress address = null;
		if (StrUtil.isNull(nickname)) {
			address = new InternetAddress(accountName);
		} else {
			address = new InternetAddress(accountName, nickname, CHARSET);
		}

		mimeMessage.setFrom(address);
	}

	/**
	 * 获取Session实例
	 * 
	 * @param account
	 * @return
	 * @throws Exception
	 * @author YXD
	 */
	private Session getSession(final MailAccount account, boolean isSSL) {
		// 构建授权信息,用于进行SMTP进行身份验证
		final String accountName = account.getAccount();
		// 先从缓存中获取
		Session session = sessionCache.get(accountName);
		if (session == null) {
			Authenticator authenticator = new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					// 用户名、密码
					return new PasswordAuthentication(accountName, account.getPassword());
				}
			};

			Properties prop = new Properties();
			prop.setProperty("mail.smtp.auth", "true");
			prop.setProperty("mail.smtp.timeout", "30000"); // 设置链接超时
			prop.setProperty("mail.transport.protocol", "smtp");
			prop.setProperty("mail.smtp.host", account.getSmtpHost());
			prop.setProperty("mail.smtp.port", account.getSmtpPort());
			
			if(isSSL){
				prop.setProperty("mail.smtp.socketFactory.port", account.getSmtpPort()); // 设置ssl端口
				prop.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				prop.setProperty("mail.smtp.socketFactory.fallback", "false");
			}
			
			session = Session.getInstance(prop, authenticator);
			session.setDebug(false); // 开启后有调试信息

			// 缓存Session
			sessionCache.put(accountName, session);
		}

		return session;
	}

	
	public static void main(String[] args) throws Exception {
		MailAccount account = new MailAccount();
		account.setAccount("fapiao@ytepark.com");
		account.setNickname("");
		account.setPassword("Fpytepark123");
		account.setSmtpHost("smtp.ytepark.com"); // smtp.exmail.qq.com
		account.setSmtpPort("25"); // 25 SSL 465
		
		MailBody mailBody = new MailBody();
		mailBody.setSubject("【测试】");
		
		mailBody.setContent("测试邮件.");
		
		/*FileReader fileReader = new FileReader("D:\\Test\\mail\\mail_temp.html");
		BufferedReader bufReader = new BufferedReader(fileReader);
		LineNumberReader reader = new LineNumberReader(bufReader);
		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append(System.getProperty("line.separator"));
		}
		reader.close();
		String content = sb.toString();
		mailBody.setContent(content);
		mailBody.setContentType(ContentType.HTML);
		
		mailBody.addImage(new File("D:\\Test\\mail\\logo.png"), "logo.png");
		
		mailBody.addAttachment(new File("D:\\Test\\mail\\电子发票.pdf"), "1111.pdf");*/
		
		String to = "ouli@ecaray.com";
		MailUtils.newInstance(account, false).send(to, mailBody);
		System.out.println("发送成功");
	}
}

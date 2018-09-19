package org.aksw.ocelot.common.request;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class TLSEmail {

  public static void main(final String[] args) {

    final String fromEmail = "email@informatik.uni-leipzig.de";
    final String password = "password";
    final String toEmail = "a@b.de";

    final Properties props = new Properties();
    props.put("mail.smtp.host", "smtp.informatik.uni-leipzig.de");
    props.put("mail.smtp.port", "587");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");

    final Authenticator auth = new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(fromEmail, password);
      }
    };

    final Session session = Session.getInstance(props, auth);

    final String subject = "TLSEmail Testing Subject";
    final String body = "TLSEmail Testing Body";

    sendEmail(session, toEmail, subject, body);
  }

  public static void sendEmail(final Session session, final String toEmail, final String subject,
      final String body) {

    try {
      final MimeMessage msg = new MimeMessage(session);
      msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
      msg.addHeader("format", "flowed");
      msg.addHeader("Content-Transfer-Encoding", "8bit");

      msg.setFrom(new InternetAddress("sadasd@informatik.uni-leipzig.de", "Axel"));
      msg.setReplyTo(InternetAddress.parse("sadasd@informatik.uni-leipzig.de", false));
      msg.setSubject(subject, "UTF-8");
      msg.setText(body, "UTF-8");
      msg.setSentDate(new Date());

      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));

      System.out.println("Message is ready");
      Transport.send(msg);
      System.out.println("EMail Sent Successfully!!");

    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}

package br.com.email;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

@Named
@ViewScoped
public class GeradorDeEmail implements Serializable {
	private static final long serialVersionUID = 1L;

	private Properties properties = System.getProperties();
	private String to = "";
	private String from = "deivison.matos@irede.net";

	public GeradorDeEmail() {
	}

	public Properties obtemCredenciaisDeEnvio() {
		try {
			InputStream stream = this.getClass().getResourceAsStream("/resources/config-email.properties");

			properties.load(stream);
			// properties.load(new FileInputStream(new File("config-email.properties")));
			// desktop

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return properties;
	}

	public Session efetuaAutenticacao() {
		Session session = Session.getDefaultInstance(properties, new Authenticator() {

			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(properties.getProperty("mail.user"),
						properties.getProperty("mail.password"));
			}
		});

		session.setDebug(true);

		return session;
	}

	public MimeMessage recuperaTemplateDoEmail(MimeMessage mensagem) {

		VelocityEngine velocityEngine = new VelocityEngine();
		velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		velocityEngine.init();

		Template template = velocityEngine.getTemplate("resources/template-email.vm");
		// Template template = velocityEngine.getTemplate("template-email.vm"); desktop

		VelocityContext context = new VelocityContext();
		context.put("paramNome", "Deivison");
		context.put("paramAutor", "Master");

		StringWriter writer = new StringWriter();
		template.merge(context, writer);

		BodyPart body = new MimeBodyPart();

		try {
			body.setContent(writer.toString(), "text/html");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(body);

			/*
			 * body = new MimeBodyPart(); String path =
			 * FacesContext.getCurrentInstance().getExternalContext().getRealPath(
			 * "/resources/anexo.txt"); InputStream is = new
			 * FileInputStream("resources/anexo.txt"); String arquivo = path;
			 * //"resources/anexo.txt"; DataSource dataSource = new FileDataSource(arquivo);
			 * body.setDataHandler(new DataHandler(dataSource));
			 * body.setFileName("arquivo anexo"); multipart.addBodyPart(body);
			 */

			mensagem.setContent(multipart, "text/html");

		} catch (MessagingException e) {
			e.printStackTrace();
		}

		return mensagem;
	}

	public MimeMessage configuraEnvioDoEmail(Session session, String email) {
		MimeMessage message = null;

		try {
			message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
			message.setSubject("Teste de e-mail com java API");

		} catch (MessagingException e) {
			e.printStackTrace();
		}

		return message;
	}

	public void enviaEmail(String[] emails) {
		obtemCredenciaisDeEnvio();
		Session session = efetuaAutenticacao();
		
		for (String email : emails) {
			MimeMessage message = configuraEnvioDoEmail(session, email);
			MimeMessage mensagem = recuperaTemplateDoEmail(message);

			try {
				Transport.send(mensagem);

			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}

	}
}

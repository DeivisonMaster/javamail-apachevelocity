package br.com.email;

public class EnviaEmail {
	public static void main(String[] args) {
		String[] emails = new String[] {"mail1", "mail2" };
		GeradorDeEmail geradorEmail = new GeradorDeEmail();
		
		geradorEmail.enviaEmail(emails);
	}
}

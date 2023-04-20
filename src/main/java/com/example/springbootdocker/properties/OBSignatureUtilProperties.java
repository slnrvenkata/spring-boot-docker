package com.example.springbootdocker.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Getter
@Setter
@Configuration
@Primary
@ConfigurationProperties(prefix = "directory")
public class OBSignatureUtilProperties {

	private Client client;
	private Server server;

	@Getter
	@Setter
	public static class Client {
		private boolean verificationEnabled;
		private String keyLocation;
	}

	@Getter
	@Setter
	public static class Server {
		private boolean verificationEnabled;
		private String truststorePassword;
		private String truststoreLocation;
	}
}
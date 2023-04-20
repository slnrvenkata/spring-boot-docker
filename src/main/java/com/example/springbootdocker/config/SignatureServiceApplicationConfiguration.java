package com.example.springbootdocker.config;

import com.example.springbootdocker.properties.OBSignatureUtilProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Configuration
@EnableConfigurationProperties({ OBSignatureUtilProperties.class})
public class SignatureServiceApplicationConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(SignatureServiceApplicationConfiguration.class);
	@Autowired
	private OBSignatureUtilProperties signatureUtilConfig;

	@Bean(name = "jwksRestTemplate")
	public RestTemplate jwksRestTemplate() throws IOException {
		//getJksFile(signatureUtilConfig.getServer().getTruststoreLocation());
		return new RestTemplate();
	}

/*	private File getJksFile(String fileDir) {

		Path cwd = Path.of("").toAbsolutePath();
		System.out.println("current path::"+cwd);

		Path path = Paths.get(fileDir);
		System.out.println(path.toString());

		try (Stream<Path> files = Files.list(Paths.get(fileDir))) {

			Path filePath = files.filter(path -> path.toString().contains(".jks")).findFirst().
					orElseThrow(() -> new FileNotFoundException("jks file is not found in specified path:" + fileDir));
			return filePath.toFile();

		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}*/
}
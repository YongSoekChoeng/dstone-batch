package net.dstone.batch.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.tuxdevelop.spring.batch.lightmin.client.classic.annotation.EnableLightminClientClassic;
import org.tuxdevelop.spring.batch.lightmin.repository.annotation.EnableLightminRemoteConfigurationRepository;

@SpringBootApplication
@EnableLightminClientClassic                // Enable classic client mode for Lightmin  
@EnableLightminRemoteConfigurationRepository // Use remote repository (Lightmin server) for job config  
@ComponentScan(basePackages={"net.dstone.batch"})
public class DstoneBatchApplication extends SpringBootServletInitializer {

	public static boolean IS_SYS_PROPERTIES_SET = false;
	@SuppressWarnings("rawtypes")
	public static void setSysProperties() {
		if(!IS_SYS_PROPERTIES_SET) {
			IS_SYS_PROPERTIES_SET = true;
			StringBuffer msg = new StringBuffer();
			try {
				msg.append("/******************************* env.properties System변수로 세팅 하기위한 조치 시작 *********************************/").append("\n");
				java.net.URL resource = DstoneBatchApplication.class.getClassLoader().getResource("env.properties");
				if (resource != null) {
			        try (InputStream input = resource.openStream()) {
			        	Properties props = new Properties();
			            if (input == null) {
			            	msg.append("Unable to find config.properties").append("\n");
			            }else {
				            props.load(input);
							String key = "";
							String val = "";
				            java.util.Iterator keys = props.keySet().iterator();
				            while( keys.hasNext() ) {
								key = (String)keys.next();
								val = props.getProperty(key, "");
								System.setProperty(key, val);
								msg.append("시스템프로퍼티 "+key+"["+val+"]").append("\n");
				            }
			            }

			        } catch (IOException ex) {
			            ex.printStackTrace();
			        }
				}
				msg.append("/******************************* env.properties System변수로 세팅 하기위한 조치 끝  *********************************/").append("\n");

				System.out.println(msg.toString());
				
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	public static void main(String[] args) {

		/*** env.properties의 항목들을 System변수로 세팅 ***/
		setSysProperties();
		
		SpringApplication app = new SpringApplication(DstoneBatchApplication.class);
		app.addListeners(new ApplicationPidFileWriter()); // ApplicationPidFileWriter 설정
	    app.run(args);
	}
	
}

/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dstone.batch.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication(exclude = {
	org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
})
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

		try {
			/*** env.properties의 항목들을 System변수로 세팅 ***/
			setSysProperties();
			
			SpringApplication app = new SpringApplication(DstoneBatchApplication.class);
			app.addListeners(new ApplicationPidFileWriter()); // ApplicationPidFileWriter 설정
		    app.run(args);
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
	
}

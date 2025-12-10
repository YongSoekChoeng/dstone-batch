
set JAVA_OPTS=-Xms512m -Xmx1024m
set JAR_FILE=../target/dstone-batch-0.0.1-SNAPSHOT.jar

set APP_CONF_FILE=--spring.config.location=file:../conf/application.yml
set LOG_CONF_FILE=--logging.config=file:../conf/log4j2.xml

java %JAVA_OPTS% -jar %JAR_FILE% net.dstone.batch.common.DstoneBatchApplication %APP_CONF_FILE% %LOG_CONF_FILE%

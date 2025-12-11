#! /bin/sh

JAVA_OPTS="-Xms512m -Xmx1024m"
JAR_FILE="../target/dstone-batch-0.0.1-SNAPSHOT.jar"

APP_CONF_FILE="--spring.config.location=file:../conf/application.yml"
LOG_CONF_FILE="--logging.config=file:../conf/log4j2.xml"

# nohup java ${JAVA_OPTS} -jar ${JAR_FILE} net.dstone.batch.common.DstoneBatchApplication ${APP_CONF_FILE} ${LOG_CONF_FILE} > /dev/null 2>&1 &
java ${JAVA_OPTS} -jar ${JAR_FILE} net.dstone.batch.common.DstoneBatchApplication ${APP_CONF_FILE} ${LOG_CONF_FILE}


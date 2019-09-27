FROM openjdk:8u212-jdk
ENV VERSION 1.0.0
ADD target/geoserver-testbed-adapter-${VERSION}.jar /opt/application/geoserver-testbed-adapter-${VERSION}.jar
ADD run.sh /opt/application/run.sh
ADD dockerconfig /opt/application/config
CMD ["/bin/sh","/opt/application/run.sh"]

FROM ubuntu:20.04

RUN groupadd -r mercury --gid=1000 && useradd -r -g mercury --uid=1000 mercury

ENV JAVA_RELEASE 11
ENV JAVA_VERSION 11.0.6
ENV JAVA_UPDATE 10
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8
ENV LANGUAGE C.UTF-8

RUN apt-get update \
    && apt-get install -y --no-install-recommends python-is-python3 python3.8-venv curl ca-certificates tar unzip locales gnupg \
    && curl "https://s3.amazonaws.com/aws-cli/awscli-bundle.zip" -o /tmp/awscli-bundle.zip \
    && unzip /tmp/awscli-bundle.zip -d /tmp/ \
    && /tmp/awscli-bundle/install -i /usr/local/aws -b /usr/local/bin/aws

# TO-DO: test with awscli v2
#    apt-get install -y --no-install-recommends python3.8-venv curl ca-certificates tar unzip locales gnupg && \
#    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
#    unzip awscliv2.zip && \
#    ./aws/install
#    rm -rf /var/lib/apt/lists/* /tmp/awscli*

# setup openssh
RUN apt-get install -y \
       openssh-server \
       pwgen \
    # configure sshd
    && mkdir -p /run/sshd \
    && chmod 0755 /run/sshd \
    && echo "UseDNS no" >> /etc/ssh/sshd_config \
    && echo "PasswordAuthentication no" >> /etc/ssh/sshd_config \
    && echo "PermitRootLogin yes" >> /etc/ssh/sshd_config \
    # Specifies that ~/.ssh/environment and environment= options in ~/.ssh/authorized_keys are processed by sshd(8).
    && echo "PermitUserEnvironment yes" >> /etc/ssh/sshd_config \
    # generate sshd host keys
    && ssh-keygen -A \
    && ssh-keygen -t rsa -b 4096 -f  /etc/ssh/ssh_host_key \
    && rm -rf /var/lib/apt/lists/* /tmp/awscli-bundle*

# setup openjdk11.0.6
RUN mkdir -p /opt/ && \
    curl -C - -L -R -s -S \
        https://github.com/AdoptOpenJDK/openjdk${JAVA_RELEASE}-binaries/releases/download/jdk-${JAVA_VERSION}+${JAVA_UPDATE}/OpenJDK${JAVA_RELEASE}U-jdk_x64_linux_hotspot_${JAVA_VERSION}_${JAVA_UPDATE}.tar.gz | tar -xzf - -C /opt &&\
    ln -s "/opt/jdk-${JAVA_VERSION}+${JAVA_UPDATE}" /opt/jdk &&\
    rm -rf /opt/jdk/lib/*src.zip \
           /opt/jdk/lib/missioncontrol \
           /opt/jdk/lib/visualvm \
           /opt/jdk/lib/*javafx* \
           /opt/jdk/lib/plugin.jar \
           /opt/jdk/lib/ext/jfxrt.jar \
           /opt/jdk/bin/javaws \
           /opt/jdk/lib/javaws.jar \
           /opt/jdk/lib/desktop \
           /opt/jdk/plugin \
           /opt/jdk/lib/deploy* \
           /opt/jdk/lib/*javafx* \
           /opt/jdk/lib/*jfx* \
           /opt/jdk/lib/amd64/libdecora_sse.so \
           /opt/jdk/lib/amd64/libprism_*.so \
           /opt/jdk/lib/amd64/libfxplugins.so \
           /opt/jdk/lib/amd64/libglass.so \
           /opt/jdk/lib/amd64/libgstreamer-lite.so \
           /opt/jdk/lib/amd64/libjavafx*.so \
           /opt/jdk/lib/amd64/libjfx*.so
ENV JAVA_HOME /opt/jdk
ENV PATH ${PATH}:${JAVA_HOME}/bin

ENV GOSU_VERSION 1.10
RUN gpg --batch --keyserver hkps://keys.openpgp.org --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4
RUN curl -o /usr/local/bin/gosu -SL "https://github.com/tianon/gosu/releases/download/${GOSU_VERSION}/gosu-$(dpkg --print-architecture)" \
    && curl -o /usr/local/bin/gosu.asc -SL "https://github.com/tianon/gosu/releases/download/${GOSU_VERSION}/gosu-$(dpkg --print-architecture).asc" \
    && gpg --verify /usr/local/bin/gosu.asc \
    && rm /usr/local/bin/gosu.asc \
    && chmod +x /usr/local/bin/gosu

ENV MERCURY_HOME /opt/mercury/

RUN  mkdir -p ${MERCURY_HOME}
COPY build/libs/mercury*.jar ${MERCURY_HOME}
RUN  chown -R mercury:mercury ${MERCURY_HOME}

COPY src/resources/*.sh /
RUN chmod +x /docker-entrypoint.sh /fetch_keystore.sh

# New relic java agent
ADD https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip /newrelic-java.zip
RUN unzip /newrelic-java.zip && \
    chown -Rv mercury:mercury /newrelic && \
    chmod -Rv 744 /newrelic && \
    rm /newrelic-java.zip

#
ENV TINI_VERSION v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini

#|nunununununununununununununununununununununununununununununu
#| Install goss
#|nunununununununununununununununununununununununununununununu
RUN curl -fsSL https://goss.rocks/install | sh
ENV GOSS_FILE=/etc/goss.yaml
COPY src/resources/goss.yaml /etc/goss.yaml

# 143 exit code is Java's graceful shutdown
ENTRYPOINT ["/tini", "-v", "-g", "-e", "143", "--", "/docker-entrypoint.sh"]

EXPOSE 8080 22

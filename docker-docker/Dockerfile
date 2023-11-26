# Use the official CentOS 7 base image
FROM centos:7

# Your additional instructions go here
# For example, install software using yum
RUN yum -y update && \
    yum -y install yum-utils && \
    yum clean all

RUN yum -y install sudo

RUN yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

RUN yum -y install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

RUN curl -L "https://github.com/docker/compose/releases/download/1.23.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

RUN chmod +x /usr/local/bin/docker-compose

RUN cp -rp /usr/local/bin/docker-compose /usr/bin/docker-compose

CMD ["/scripts/start.sh"]
FROM ubuntu:16.10

RUN apt-get update
RUN apt-get dist-upgrade -y
RUN apt-get install software-properties-common -y
RUN add-apt-repository ppa:webupd8team/java -y
RUN apt-get update
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-get install oracle-java8-installer -y

WORKDIR /

# Copy files
RUN mkdir ccg-curator-server
COPY bin/ccg-curator-server-1.0.0.jar /ccg-curator-server
RUN mkdir ccg-curator-server/lib
COPY bin/lib /ccg-curator-server/lib
COPY docker /ccg-curator-server

WORKDIR /ccg-curator-server

# Change to Unix line endings
RUN sed -i 's/\r//' docker-entrypoint.sh

# Make runnable
RUN chmod 777 /ccg-curator-server/docker-entrypoint.sh

EXPOSE 8080 50051

CMD ["./docker-entrypoint.sh"]

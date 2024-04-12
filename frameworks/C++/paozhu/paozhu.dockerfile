FROM ubuntu:22.04
RUN apt-get update -yqq && apt-get install -yqq apt-utils software-properties-common wget unzip cmake git
RUN apt-get install -yqq gcc g++ openssl libssl-dev zlib1g-dev build-essential locales

RUN apt-get -y install brotli libbrotli-dev 
RUN apt-get -y install libreadline-dev 
RUN apt-get -y install mysql-client
RUN apt-get -y install libmysqlclient-dev

RUN locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

COPY ./ ./
WORKDIR /

# RUN wget https://github.com/hggq/paozhu/releases/download/v1.5.8/benchmark.zip
RUN git clone https://github.com/hggq/paozhu
# RUN unzip benchmark.zip
RUN rm -Rf ./paozhu/controller
COPY ./paozhu_benchmark/controller ./paozhu/
COPY ./paozhu_benchmark/common ./paozhu/

COPY ./paozhu_benchmark/conf/server.conf ./paozhu/conf/server.conf
COPY ./paozhu_benchmark/conf/orm.conf ./paozhu/conf/orm.conf
COPY ./paozhu_benchmark/CMakeLists.txt ./paozhu/CMakeLists.txt

WORKDIR /paozhu
RUN unzip asio.zip

RUN cmake . -B build -DCMAKE_BUILD_TYPE=Release 
RUN cmake --build build


EXPOSE 8888

CMD ./bin/paozhu 

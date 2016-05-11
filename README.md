 
# Projeto de Sistemas Distribuídos 2015-2016 #

Grupo de SD 45 - Campus Alameda

João Neves      70171   joaoneves792@gmail.com

Paulo Gouveia   75657   paulojlgouveia@gmail.com

Daniel Figueira 75694   daniel.figueira@ist.utl.pt


Repositório:
[tecnico-distsys/A_45-project](https://github.com/tecnico-distsys/A_45-project/)

-------------------------------------------------------------------------------

## Instruções de instalação 


### Ambiente

[0] Iniciar sistema operativo

Linux

[1] Iniciar servidores de apoio

JUDDI:
```
juddi-3.3.2_tomcat-7.0.64_9090/bin/startup.sh
```

[2] Obter código fonte do projeto (versão entregue)

```
git clone -b SD_R2 https://github.com/tecnico-distsys/A_45-project/
```

[3] Instalar módulos de bibliotecas auxiliares

```
cd uddi-naming
mvn clean install
```

[4] Criar e instalar chaves

(Opcional: as chaves ja se encontram instaladas no repositorio)

```
cd CA-ws
./generateKeystores.sh
cp generated-keystores/CA.jks ..
cp generated-keystores/UpaBroker/UpaBroker.jks ../broker-ws
cp generated-keystores/cacert.pem ../broker-ws
cp generated-keystores/UpaBroker/UpaBroker.jks ../transporter-ws-cli
cp generated-keystores/cacert.pem ../transporter-ws-cli
cp generated-keystores/UpaTransporter1/UpaTransporter1.jks ../transporter-ws
cp generated-keystores/UpaTransporter2/UpaTransporter2.jks ../transporter-ws
cp generated-keystores/cacert.pem ../transporter-ws
```

-------------------------------------------------------------------------------
### Servico CA
[1] Construir e executar **servidor**
```
cd CA-ws
mvn clean compile test exec:java
```
[2] Construir e instalar **cliente**
```
cd CA-ws-cli
mvn clean compile install
```

-------------------------------------------------------------------------------
### Handlers
[1] Construir e installar handlers
```
cd ws-handlers
mvn clean compile install
```
-------------------------------------------------------------------------------

### Serviço TRANSPORTER

[1] Construir e executar **servidor 1**

```
cd transporter-ws
mvn clean compile test
mvn exec:java
```

[2] Construir e executar **servidor 2**

```
cd transporter-ws
mvn clean compile test
mvn exec:java -Dws.i=2
```

[3] Construir **cliente**

```
cd transporter-ws-cli
mvn clean compile install
```

...


-------------------------------------------------------------------------------

### Serviço BROKER
[1] Construir e installar cliente
```
cd broker-ws-cli
mvn clean compile -DskipTests=true install
```

[2] Construir e executar **servidor de backup**

```
cd broker-ws
mvn clean compile test
mvn exec:java -Dws.port=8078 -Dws.backupMode=true
```

[3] Executar **servidor principal**
```
cd broker-ws
mvn exec:java
```


[4] Executar testes de integração

```
cd broker-ws-cli
mvn verify
```

...

-------------------------------------------------------------------------------
**FIM**
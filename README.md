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


[3] Obter código fonte do projeto (versão entregue)

```
git clone -b SD_R1 https://github.com/tecnico-distsys/A_45-project/
```


[4] Instalar módulos de bibliotecas auxiliares

```
cd uddi-naming
mvn clean install
```

-------------------------------------------------------------------------------

### Serviço TRANSPORTER

[1] Construir e executar **servidor**

```
cd transporter-ws
mvn clean install
mvn exec:java
mvn exec:java -Dws.i=2
```

[2] Construir **cliente**

```
cd transporter-ws-cli
mvn clean install
```

...


-------------------------------------------------------------------------------

### Serviço BROKER

[1] Construir e executar **servidor**

```
cd broker-ws
mvn clean install
mvn exec:java
```

[2] Construir **cliente** e executar testes

```
cd broker-ws-cli
mvn clean install
mvn verify
```

...

-------------------------------------------------------------------------------
**FIM**

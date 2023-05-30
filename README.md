# [FCCPD] - Módulo 2

### Linguagens e Ferramentas

![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/rabbitmq-%23FF6600.svg?&style=for-the-badge&logo=rabbitmq&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)


## Pré-Requisitos

1. Ter Docker instalado em sua máquina
2. Ter uma versão LTS e atualizada de Java/OpenJDK 

# Preparar Ambiente
Com o Docker instalado, abra seu terminal e execute o seguinte comando, ele irá trazer a imagem do RabbitMQ para sua máquina:
```bash
$ docker pull rabbitmq:3-management
```
Em seguida, execute:
```bash
$ docker run -d --hostname rabbitmq --name some-rabbit -p 15672:15672 -p 5672:5672 rabbitmq:3-management
```
Após isso, abra o projeto na IDE de preferência e vá até o arquivo MusicSuggestion e clique em RUN. E vá seguindo as instruções do terminal.

### Importante
Projetos Maven, é primordial que se vá na aba "Maven" da IDE selecione "Package" e então rode para que se possa carregar todas as dependencias e atualizar os pacotes. 

Após isso, selecionar "clean" e "install", e fazer o mesmo processo. E em seguida, acessar arquivo MusicSuggestion para utilizar do sistema.
# ftc-online-course-report-serverless

Projeto Tech Challenge Online Course Report Serverless do Curso de Pós-Graduação Lato Sensu Arquitetura e Desenvolvimento em JAVA da Faculdade de Informática e Administração Paulista (FIAP). Aplicação Serverless responsável por receber mensagem identificando o feedback urgente para que possa ser enviado ao administrador.

## Diagrama de Arquitetura

O diagrama abaixo ilustra a arquitetura e o fluxo de dados de todos os serviços envolvidos.

<img width="1466" height="1091" alt="diagrama-arquitetural" src="https://github.com/user-attachments/assets/dafefab7-a3bc-4219-b373-a9a776ff689f" />

## Arquitetura do Sistema

Este sistema foi desenvolvido utilizando Java 21 e PostgreSQL 17.4.

### Tecnologias Utilizadas
- **Java 21** como linguagem de programação da aplicação
- **PostgreSQL** como banco de dados relacional
- **AWS Lambda** para definição da estrutura serverless
- **AWS KMS** para criptografia das variáveis de ambiente
- **Jackson** para conversão de estrutura de dados (serializar e desserializar)
- **Javax Mail** para envio de e-mail

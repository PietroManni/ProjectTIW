# Progetto di Tecnologie per l'Informazione e il Web 
# -AA 2022-2023
## Gruppo <br />
Professor: Prof. Piero Fraternali <br />

Studenti: <br />

  - Lonati Marco (10782822) <br />
  - Manni Pietro (10743117) <br />

## Descrizione
Questo progetto è stato sviluppato come parte del corso di Tecnologie per l'Informazione e il Web (TIW). L'obiettivo è la creazione di un'applicazione web per la gestione degli appelli universitari, dei voti degli studenti e delle operazioni correlate, come l'inserimento, la modifica, la pubblicazione e la verbalizzazione dei voti.

L'applicazione consente agli utenti (docenti e studenti) di interagire con gli appelli e i relativi dati, garantendo funzionalità avanzate come ordinamento, filtraggio, e gestione di stati come "Rifiutato" o "Pubblicato".

La gestione degli utenti, dei corsi, degli appelli e dei voti è stata implementata utilizzando un database relazionale progettato con SQL. Le tabelle e le relazioni del database sono ottimizzate per garantire efficienza e integrità dei dati.
## Funzionalità
-**Docenti:**

-Visualizzazione degli appelli associati ai corsi di competenza.
-Gestione degli esami degli studenti:
-Inserimento o modifica dei voti.
-Pubblicazione dei voti.
-Creazione del verbale per gli esami pubblicati o rifiutati.
-Ordinamento dei dati degli studenti in base a diversi attributi (numero di matricola, nome, voto, ecc.).

-**Studenti:**

Visualizzazione degli esiti degli esami.
Rifiuto dei voti pubblicati (se previsto dalle regole).
Consultazione degli appelli e delle informazioni correlate.

## Struttura del Progetto
Il progetto è stato organizzato seguendo il modello Model-View-Controller (MVC) per una migliore separazione delle responsabilità:

**Model (DAO e Beans):**

UserDAO: Gestione degli utenti (autenticazione e autorizzazione).
ExamDAO: Gestione degli esami e dei relativi voti.
AppealDAO: Gestione degli appelli.
VerbalDAO: Creazione e gestione dei verbali.
Beans come User, Exam, Appeal, Course per rappresentare le entità.

**View (HTML, Thymeleaf, CSS):**

Template per pagine come login, home, gestione appelli, risultati, ecc.
Utilizzo di Thymeleaf per il rendering dinamico delle pagine e la gestione dei dati lato client.

**Controller (Servlet):**

Servlet dedicate alla gestione delle azioni principali come GoToAppeal, RefuseMark, PublishMarks, CreateVerbal, ecc.
Comunicazione con il database attraverso i DAO.

## Tecnologie Utilizzate
**Linguaggi:** Java (Servlet, JDBC), HTML5, CSS3, SQL.

**Framework:** Thymeleaf per il rendering lato server.

**Database:** MySQL per la persistenza dei dati.

**Server:** Apache Tomcat 9.0.

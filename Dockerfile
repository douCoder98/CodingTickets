FROM tomcat:10.1-jdk17

#Supprimer les applications de démonstration
RUN rm -rf /usr/local/tomcat/webapps/*

#Ajouter le driver PostgreSQL dans le dossier lib de Tomcat
ADD https://jdbc.postgresql.org/download/postgresql-42.7.1.jar \
    /usr/local/tomcat/lib/postgresql-driver.jar

#Copier le WAR généré par Maven
COPY target/*.war /usr/local/tomcat/webapps/ROOT.war

#Exposer le port Tomcat
EXPOSE 8080

#Lancement de Tomcat
CMD ["catalina.sh", "run"]
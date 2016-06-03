# solr-document-copier
A Java Project using Spring and REST for copying Data From a DB to Solr.

#Command for running the standalone version with class files (not jar)

java -classpath .;./*;resources com.debashish.solr.Main report C:\0 SIT

where

1st argument -> report is the operation type and one of : report/sync
2nd argument -> C:\0 is the path to the configuration files
3rd argument -> The environment and one of (DEV/PRD/STR/SIT)



#Command for running the standalone version with the executable jar

java -jar 0.jar report C:\a SIT
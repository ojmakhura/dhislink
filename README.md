# dhislink

This application is built using maven. But it requires Andromda 4.0-SNAPSHOT (https://github.com/ojmakhura/andromda.git). The instructions for building AndroMDA can
be found in the repository itself.

##RedcapData
Currently, we are not using the redcap api to search so we have to use the full stack RedcapData objects to interact with the redca_data table directly. It doesn't have a primary key 
so we use composite keys. make sure you put @Id on the getters of the attributes similar to the ones in RedcapDataId class.

You should also run the two sql files in the scripts directory. 

# hospital-web-service
Web service created for hospital administrative CRUD tasks using JAX-RS with Jersey implementation.

To deploy service, I use Tomcat and execute the following ant script:
% ant -Dwar.name=hospital deploy

Before running, update build.xml file with local Tomcat directory at line 90.

Service initially loads data from two .db (drs.db and patients.db) files, included in the src folder.

The following CRUD operations are supported by the service:

- Get all doctors and their patients in xml via /xml
- Get one doctor and their patients in xml via /xml/ID where ID is the unique doctor ID
- Get all patients in XML via /xmlPatients
- Get all doctors and their patients in JSON via /json
- Get one doctor and their patients in JSON via /json/ID where ID is the unique doctor ID
- Get all doctors and their patients in plain text via /plain
- Get one doctor and their patients in plain text via /plain/ID where ID is the unique doctor ID
- Get one patient in plain text via /plainPatient/ID where ID is the unique patient ID
- Get doctors only without patient info via /plainDoctors
- Get one patient in xml via /xmlPatient/ID where ID is the unique patient ID
- Get one patient in JSON via /jsonPatient/ID where ID is the unique patient ID
- Get all patients in JSON via /jsonPatients
- Post new patient via /createPatient
- Post new doctor via /create
- Post new doctor with no patients via /createDoctor
- Delete doctor without deleting corresponding patients via /delete/ID where ID is the unique doctor ID
- Delete doctor and their patients via /deleteAll/ID where ID is the unique doctor ID
- Delete patient via /deletePatient/ID where ID is unique patient ID

package drpatients;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.servlet.ServletContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import jdk.nashorn.internal.objects.annotations.Getter;

@Path("/")
public class DoctorsRS {
    @Context 
    private ServletContext sctx;          // dependency injection
    private static DoctorsList drslist;	  // set in populate()
    private static PatientList pslist;	  // set in populate()

    public DoctorsRS() { }

	//get all doctors/patients in xml
    @GET
    @Path("/xml")
    @Produces({MediaType.APPLICATION_XML}) 
    public Response getXml() {
	checkContext();
	return Response.ok(drslist, "application/xml").build();
    }

	//get one doctor and their patients in xml 
    @GET
    @Path("/xml/{id: \\d+}")
    @Produces({MediaType.APPLICATION_XML}) // could use "application/xml" instead
    public Response getXml(@PathParam("id") int id) {
	checkContext();
	return toRequestedType(id, "application/xml");
	}
	
	//get all patients in xml
	@GET
	@Path("/xmlPatients")
	@Produces({MediaType.APPLICATION_XML}) // could use "application/xml" instead
	public Response getPatients() {
	checkContext();
	return Response.ok(pslist, "application/xml").build();
	}

	//get all doctors/patients in json
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/json")
    public Response getJson() {
	checkContext();
	return Response.ok(toJson(drslist), "application/json").build();
	}

	//get one doctor and their patients in json
    @GET    
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/json/{id: \\d+}")
    public Response getJson(@PathParam("id") int id) {
	checkContext();
	return toRequestedType(id, "application/json");
    }

	//get all doctors/patients in plain text
    @GET
    @Path("/plain")
    @Produces({MediaType.TEXT_PLAIN}) 
    public String getPlain() {
	checkContext();
	return drslist.toString();
	}

	//get one doctor and their patients in plain text
	@GET
	@Path("/plain/{id: \\d+}")
	@Produces({MediaType.TEXT_PLAIN}) 
	public String getPlainSingleDoctor(@PathParam("id") int id) {
	checkContext();
	
	Doctor search = new Doctor();
	for (Doctor d : drslist.getDoctors()){
		if (d.getId() == id){
			search = d;
		}
	}
	return search.toString();
	}

	//get one patient in plain text
	@GET
	@Path("/plainPatient/{id: \\d+}")
	@Produces({MediaType.TEXT_PLAIN}) 
	public String getPlainSinglePatient(@PathParam("id") int id) {
	checkContext();
		
	Patient search = new Patient();

		for (Patient p : pslist.getPatients()){
			if (p.getId() == id){
				search = p;
			}
		}
	return search.toString();
	}

	//get doctors only
	@GET
    @Path("/plainDoctors")
    @Produces({MediaType.TEXT_PLAIN}) 
    public String getPlainDoctors() {
	checkContext();
	return drslist.doctorString();
	}

	//get all patients in plain text
	@GET
	@Path("/plainPatients")
	@Produces({MediaType.TEXT_PLAIN})
	public String getPlainPatients(){
		checkContext();
		return pslist.toString();
	}

	//get one patient in xml
	@GET
    @Path("/xmlPatient/{id: \\d+}")
    @Produces({MediaType.APPLICATION_XML}) // could use "application/xml" instead
    public Response getXmlPatient(@PathParam("id") int id) {
	checkContext();
	return toRequestedTypePatients(id, "application/xml");
    }

	//get all patients in json
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/jsonPatients")
    public Response getJsonPatients() {
	checkContext();
	return Response.ok(toJsonPatients(pslist), "application/json").build();
    }

	//get one patient in json
    @GET    
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/jsonPatient/{id: \\d+}")
    public Response getJsonPatient(@PathParam("id") int id) {
	checkContext();
	return toRequestedTypePatients(id, "application/json");
    }

	//post new patient
    @POST
    @Produces({MediaType.TEXT_PLAIN})
    @Path("/createPatient")
    public Response createPatient(@FormParam("who") String who, 
               @FormParam("insurance") String insurance,
               @FormParam("docId") String docId) {
	checkContext();
	String msg = null;
		// Require both properties to create.
		if (who == null || insurance == null || docId == null) {
	    	msg = "Property 'who' or 'patients' or 'insurance' is missing.\n";
	    	return Response.status(Response.Status.BAD_REQUEST).
		                                   entity(msg).
		                                   type(MediaType.TEXT_PLAIN).
		                                   build();
		}	    

		//check to make sure docId corresponds to actual doctor
		boolean exists = false;

		for (Doctor d : drslist.getDoctors()){
			if (Integer.parseInt(docId) == d.getId())
				exists = true;
		}

		if (exists == false) {
	    	msg = "Doctor ID " + docId + " does not correspond to existing doctor.\n";
	    	return Response.status(Response.Status.BAD_REQUEST).
		                                   entity(msg).
		                                   type(MediaType.TEXT_PLAIN).
		                                   build();
		}	  

		// Otherwise, create the Patient and add it to the collection.
		int id = addPatient(who, insurance, docId);

		Patient p = new Patient();

		//get patient from patients list
		for (Patient patient : pslist.getPatients()){
			if (patient.getId() == id){
				p = patient;
			}
		}

		//reset patients of corresponding doctor
		for (Doctor d : drslist.getDoctors()){
			if (d.getId() == p.getDocId()){
				d.setPatients(pslist);
				}
			}

	msg = "Patient " + id + " created: (who = " + who + " insurance = " + insurance + ").\n";
	return Response.ok(msg, "text/plain").build();
	}
	
    //post new doctor
    @POST
    @Produces({MediaType.TEXT_PLAIN})
    @Path("/create")
    public Response create(@FormParam("who") String who, 
               @FormParam("patients") String patients,
               @FormParam("insurance") String insurance) {
    checkContext();
	String msg = null;
	//creating doctor with one or multiple patients
    // Require doctor name, patient name(s), and insurance number(s)
    if (who == null || patients == null || insurance == null) {
        msg = "Property 'who' or 'patients' or 'insurance' is missing.\n";
        return Response.status(Response.Status.BAD_REQUEST).
                                           entity(msg).
                                           type(MediaType.TEXT_PLAIN).
                                           build();
    }       

	int id = 0;

    // check to see if more than one patient is being added
    // if so, patients and insurance numbers must be separated by .
    if (patients.contains(".") || insurance.contains(".")){

        //now, split string into parts
        String[] patientParts = patients.split("\\.");
		String[] insuranceParts = insurance.split("\\.");
		
		if (patientParts.length != insuranceParts.length){
			msg = "Number of patients must match number of insurance card numbers.\n";
            return Response.status(Response.Status.BAD_REQUEST).
                                               entity(msg).
                                               type(MediaType.TEXT_PLAIN).
                                               build();
		}

        //add doctor to list to create docId
        //note: patients not added at this point
        id = addDoctor(who, "", "", 0);

        //create patients using info in arrays 
        for(int k = 0; k < patientParts.length; k++){

            String patientName = patientParts[k];
            String insuranceNum = insuranceParts[k];

            //add new patient from arrays to patients collection 
            int id2 = addPatient(patientName, insuranceNum, Integer.toString(id));
            
		}
		
		//reset patients list for corresponding doctor
		for(Doctor d : drslist.getDoctors()){
			if (d.getId() == id){
				d.setPatients(pslist);
			}
		}
	}
	
    else{
		// Otherwise, create the Doctor and add it to the collection.
    	//case of one patient being added
    	id = addDoctor(who, patients, insurance, 1);
	}

	msg = "Doctor " + id + " created: (who = " + who + " patients = " + patients + ").\n";
    	return Response.ok(msg, "text/plain").build();

}


//post new doctor with no patients
@POST
@Produces({MediaType.TEXT_PLAIN})
@Path("/createDoctor")
public Response createDoctorOnly(@FormParam("who") String who) {
    checkContext();
    String msg = null;
    // Require both properties to create.
    if (who == null) {
        msg = "Property 'who' is missing.\n";
        return Response.status(Response.Status.BAD_REQUEST).
                                            entity(msg).
                                            type(MediaType.TEXT_PLAIN).
                                            build();
    }       

    // Otherwise, create the Doctor and add it to the collection.
    //case of one patient being added
    int id = addDoctor(who, "", "", 0);
    
    msg = "Doctor " + id + " created: (who = " + who + ").\n";
    return Response.ok(msg, "text/plain").build();
    }

	//update doctor name given id
    @PUT
    @Produces({MediaType.TEXT_PLAIN})
    @Path("/update")
    public Response update(@FormParam("id") int id,
			   @FormParam("who") String who) {
	checkContext();

	// Check that sufficient data are present to do an edit.
	String msg = null;
	if (who == null) 
	    msg = "Who is not given: nothing to edit.\n";

	Doctor d = drslist.find(id);
	if (d == null)
	    msg = "There is no doctor with ID " + id + "\n";

	if (msg != null)
	    return Response.status(Response.Status.BAD_REQUEST).
		                                   entity(msg).
		                                   type(MediaType.TEXT_PLAIN).
		                                   build();
	// Update.
	if (who != null) d.setWho(who);
	msg = "Doctor " + id + " has been updated.\n";
	return Response.ok(msg, "text/plain").build();
	}
	
	//update patient name given id
	@PUT
	@Produces({MediaType.TEXT_PLAIN})
	@Path("/updatePatient")
	public Response updatePatient(@FormParam("id") int id,
			   @FormParam("who") String who,
			   @FormParam("docId") Integer docId) {
	checkContext();
	
	// Check that sufficient data are present to do an edit.
	String msg = null;
	
	Patient p = pslist.find(id);
	if (p == null)
		msg = "There is no patient with ID " + id + "\n";
	
	if (msg != null)
		return Response.status(Response.Status.BAD_REQUEST).
										   entity(msg).
										   type(MediaType.TEXT_PLAIN).
										   build();

	//check to see if doc id was provided
	//if so, update doc id to new doc id 

	int currentDocId = p.getDocId();

	//check to see id new docId was entered
	if(docId != null){
		//if so, update docId
		p.setDocId(docId);

		//remove patient from old doctor's list and add patient to new doctor's list
		for(Doctor d: drslist.getDoctors()){
			if (d.getId() == p.getDocId() || d.getId() == currentDocId){
				d.setPatients(pslist);
			}
		}
	}


	//check to see if new name was entered
	if(who != null){

		//if so, update patient name and list of corresponding doctor
		p.setWho(who);

		for(Doctor d: drslist.getDoctors()){
			if (d.getId() == p.getDocId()){
				d.setPatients(pslist);
			}
		}
	}

	//display success message
	msg = "Patient " + id + " has been updated.\n";
	return Response.ok(msg, "text/plain").build();
	}


	//delete doctor without deleting corresponding patients
    @DELETE
    @Produces({MediaType.TEXT_PLAIN})
    @Path("/delete/{id: \\d+}")
    public Response delete(@PathParam("id") int id) {
	checkContext();
	String msg = null;
	Doctor d = drslist.find(id);
	if (d == null) {
	    msg = "There is no doctor with ID " + id + ". Cannot delete.\n";
	    return Response.status(Response.Status.BAD_REQUEST).
		                                   entity(msg).
		                                   type(MediaType.TEXT_PLAIN).
		                                   build();
	}
	drslist.getDoctors().remove(d);
	
	//update patients assigned to doctor to the first doctor in the list
	Doctor firstDoctor = drslist.getDoctors().get(0);

	for(Patient p : pslist.getPatients()){
		if (p.getDocId() == id){
			p.setDocId(firstDoctor.getId());
		}
	}

	firstDoctor.setPatients(pslist);

	msg = "Doctor " + id + " deleted. Patients temporarily reassigned to first available doctor.\n";

	return Response.ok(msg, "text/plain").build();
	}

	//delete doctor and corresponding patients
	@DELETE
    @Produces({MediaType.TEXT_PLAIN})
    @Path("/deleteAll/{id: \\d+}")
    public Response deleteAll(@PathParam("id") int id) {
	checkContext();
	String msg = null;
	Doctor d = drslist.find(id);
	if (d == null) {
	    msg = "There is no doctor with ID " + id + ". Cannot delete.\n";
	    return Response.status(Response.Status.BAD_REQUEST).
		                                   entity(msg).
		                                   type(MediaType.TEXT_PLAIN).
		                                   build();
	}
	drslist.getDoctors().remove(d);
	
	//remove corresponding patients from patients list

	for(Patient p : pslist.getPatients()){
		if (p.getDocId() == id){
			pslist.getPatients().remove(p);
		}
	}

	msg = "Doctor " + id + " deleted. Patients also deleted from system.\n";

	return Response.ok(msg, "text/plain").build();
	}
	
	//delete patient
	@DELETE
	@Produces({MediaType.TEXT_PLAIN})
	@Path("/deletePatient/{id: \\d+}")
	public Response deletePatient(@PathParam("id") int id) {
		checkContext();
		String msg = null;
		Patient p = pslist.find(id);
		if (p == null) {
			msg = "There is no patient with ID " + id + ". Cannot delete.\n";
			return Response.status(Response.Status.BAD_REQUEST).
											   entity(msg).
											   type(MediaType.TEXT_PLAIN).
											   build();
		}
		pslist.getPatients().remove(p);

		//remove patient from doctors list of patients
		for (Doctor d : drslist.getDoctors()){
			d.setPatients(pslist);
		}
		msg = "Patient " + id + " deleted.\n";
	
		return Response.ok(msg, "text/plain").build();
		}

    //** utilities
    private void checkContext() {
	if (drslist == null) populate();
	}
	
	private void populate(){

		drslist = new DoctorsList();
		pslist = new PatientList();

		String filename = "/WEB-INF/data/patients.db";
		InputStream in = sctx.getResourceAsStream(filename);
		
		// Read the data into the array of Patients
		if (in != null) {
			try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			int i = 0;
			String record = null;
			while ((record = reader.readLine()) != null) {
				String[] parts = record.split("!");
				addPatient(parts[0], parts[1], parts[2]);
			}

			}
			catch (Exception e) { 
			throw new RuntimeException("I/O failed for patients!"); 
			}
		}


		String dFilename = "/WEB-INF/data/drs.db";
		InputStream dIn = sctx.getResourceAsStream(dFilename);

		String doctorPlaceholder = "";
		
		// Read the data into the array of Doctor
		if (dIn != null) {
			try {
			BufferedReader reader2 = new BufferedReader(new InputStreamReader(dIn));
			int j = 0;
			String record2 = null;
			while ((record2 = reader2.readLine()) != null) {
				String[] parts2 = record2.split("!");
				addDoctor(parts2[0], doctorPlaceholder, "", 0);
			}

			}
			catch (Exception e) { 
			throw new RuntimeException("I/O failed for doctors!"); 
			}
		}

	}

    
    // Add a new doctor to the list.
    private int addDoctor(String who, String postPatients, String postInsurance, int fromPost) {
	int id = -1;
	
		if (fromPost == 0){
			
			//add doctor without adding patient
			id = drslist.add(who, pslist);
		}

		//else if post, add doctor with patients from string not file
		else {

			id = drslist.add(who, pslist);

			//add new patient from post body to patients collection 
			int id2 = addPatient(postPatients, postInsurance, Integer.toString(id));

			//reset patients list for corresponding doctor
			for(Doctor d : drslist.getDoctors()){
				if (d.getId() == id){
					d.setPatients(pslist);
				}
			}
		}
    
		return id;
    }

    // Add a new patient to the list.
    private int addPatient(String who, String insurance, String docId) {
        int id = pslist.add(who, insurance, docId, 0);
        return id;
        }

    // Doctor --> JSON document
    private String toJson(Doctor doctor) {
	String json = "If you see this, there's a problem.";
	try {
	    json = new ObjectMapper().writeValueAsString(doctor);
	}
	catch(Exception e) { }
	return json;
	}
	
	// Patient --> JSON document
    private String toJsonPatient(Patient patient) {
		String json = "If you see this, there's a problem.";
		try {
			json = new ObjectMapper().writeValueAsString(patient);
		}
		catch(Exception e) { }
		return json;
		}

    // DoctorList --> JSON document
    private String toJson(DoctorsList drslist) {
	String json = "If you see this, there's a problem.";
	try {
	    json = new ObjectMapper().writeValueAsString(drslist);
	}
	catch(Exception e) { }
	return json;
	}
	
	// PatientList --> JSON document
    private String toJsonPatients(PatientList plist) {
		String json = "If you see this, there's a problem.";
		try {
			json = new ObjectMapper().writeValueAsString(plist);
		}
		catch(Exception e) { }
		return json;
	}

    // Generate an HTTP error response or typed OK response.
    private Response toRequestedType(int id, String type) {
	Doctor dr = drslist.find(id);
	if (dr == null) {
	    String msg = id + " is a bad ID.\n";
	    return Response.status(Response.Status.BAD_REQUEST).
		                                   entity(msg).
		                                   type(MediaType.TEXT_PLAIN).
		                                   build();
	}
	else if (type.contains("json"))
	    return Response.ok(toJson(dr), type).build();
	else
	    return Response.ok(dr, type).build(); // toXml is automatic
	}
	
	// Generate an HTTP error response or typed OK response.
    private Response toRequestedTypePatients(int id, String type) {
		Patient p = pslist.find(id);
		if (p == null) {
			String msg = id + " is a bad patient ID.\n";
			return Response.status(Response.Status.BAD_REQUEST).
											   entity(msg).
											   type(MediaType.TEXT_PLAIN).
											   build();
		}
		else if (type.contains("json"))
			return Response.ok(toJsonPatient(p), type).build();
		else
			return Response.ok(p, type).build(); // toXml is automatic
		}
}




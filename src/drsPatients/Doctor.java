package drpatients;

import javax.xml.bind.annotation.XmlRootElement; 
import javax.xml.bind.annotation.XmlElement;

@XmlRootElement(name = "doctor")
public class Doctor implements Comparable<Doctor> {
    private String who;   // doctor
    private String patients;  // his/her patients
    private static PatientList pslist; //list of all patients
    private PatientList drpslist; //list of patients for this doctor
    private int    id;    // identifier used as lookup-key

    public Doctor() { }

    @Override
    public String toString() {

	    return String.format("%2d: ", id) + who + " -- " + drpslist.toString() + "\n";
    }

    public String doctorsOnlyList(){
        return String.format("%2d ", id) + who + "\n";
    }
    
    //** properties
    public void setWho(String who) {
	this.who = who;
    }
    @XmlElement
    public String getWho() {
	return this.who;
    }

    public void setPatients(PatientList list) {

        pslist = list;

        this.drpslist = new PatientList();

	    for (Patient p : list.getPatients()){

            //add patient to list if docId matches current doctor
            if (p.getDocId() == this.id){
                    addPatientToDoctor(p);
                
            }
        }
    }

    public void addPatientToDoctor(Patient patient){

        int trueId = 0;

        for (Patient p : pslist.getPatients()){
            if (p.getWho().equals(patient.getWho())){
                trueId = p.getId();
            }
        }
        this.drpslist.add(patient.getWho(), patient.getInsurance(), Integer.toString(patient.getDocId()), trueId);
    }

    public void removePatientFromDoctor(Patient patient){
        for (Patient p : drpslist.getPatients()){
            if(p.equals(patient)){
                this.drpslist.getPatients().remove(p);
            }
        }
    }

    @XmlElement
    public PatientList getPatients() {
	return this.drpslist;
    }

    public void setId(int id) {
	this.id = id;
    }
    @XmlElement
    public int getId() {
	return this.id;
    }

    // implementation of Comparable interface
    public int compareTo(Doctor other) {
	return this.id - other.id;
    }	
}
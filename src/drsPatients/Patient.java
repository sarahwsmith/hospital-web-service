package drpatients;

import javax.xml.bind.annotation.XmlRootElement; 
import javax.xml.bind.annotation.XmlElement;

@XmlRootElement(name = "patient")
public class Patient implements Comparable<Patient> {
    private String who;   // patient
    private String insurance;  // his/her insurance number
    private int    id;    // identifier used as lookup-key
    private int docId;

    public Patient() { }

    @Override
    public String toString() {

	return String.format("%2d: ", id) + who + " ==> " + insurance + "\n";
    }
    
    //** properties
    public void setWho(String who) {
	this.who = who;
    }
    @XmlElement
    public String getWho() {
	return this.who;
    }

    public void setDocId(int did){
        this.docId = did;
    }
    @XmlElement
    public int getDocId(){
        return this.docId;
    }

    public void setInsurance(String insurance) {
	this.insurance = insurance;
    }
    @XmlElement
    public String getInsurance() {
	return this.insurance;
    }

    public void setId(int id) {
	this.id = id;
    }
    @XmlElement
    public int getId() {
	return this.id;
    }

    // implementation of Comparable interface
    public int compareTo(Patient other) {
	return this.id - other.id;
    }	
}
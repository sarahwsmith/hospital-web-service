package drpatients;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.bind.annotation.XmlElement; 
import javax.xml.bind.annotation.XmlElementWrapper; 
import javax.xml.bind.annotation.XmlRootElement;
import static java.lang.Integer.parseInt;

@XmlRootElement(name = "patientList")
public class PatientList {
    private List<Patient> ps; 
	private AtomicInteger pId;
	public int size;

    public PatientList() { 
	ps = new CopyOnWriteArrayList<Patient>(); 
	pId = new AtomicInteger();
    }

    @XmlElement 
    @XmlElementWrapper(name = "patients") 
    public List<Patient> getPatients() { 
	return this.ps;
    } 
    public void setPatients(List<Patient> ps) { 
	this.ps = ps;
    }

    @Override
    public String toString() {
	String s = "";
	for (Patient p : ps) s += p.toString();
	return s;
	}
	
	public int getSize(){
		return this.size;
	}

    public Patient find(int id) {
	Patient patient = null;
	// Search the list -- for now, the list is short enough that
	// a linear search is ok but binary search would be better if the
	// list got to be an order-of-magnitude larger in size.
	for (Patient p : ps) {
	    if (p.getId() == id) {
		patient = p;
		break;
	    }
	}	
	return patient;
    }
    
    public int add(String who, String insurance, String docId, int optionalId){
	int id = pId.incrementAndGet();
	Patient p = new Patient();
	p.setWho(who);
	p.setInsurance(insurance);
	if (optionalId == 0){
		p.setId(id);
	}
	else p.setId(optionalId);
	int i = Integer.parseInt(docId);
    p.setDocId(i);
	ps.add(p);
	size++;
	return id;
    }
}
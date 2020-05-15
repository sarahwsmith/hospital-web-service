package drpatients;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.bind.annotation.XmlElement; 
import javax.xml.bind.annotation.XmlElementWrapper; 
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "doctorsList")
public class DoctorsList {
    private List<Doctor> drs; 
	private AtomicInteger drId;
	public int size;

    public DoctorsList() { 
	drs = new CopyOnWriteArrayList<Doctor>(); 
	drId = new AtomicInteger();
    }

    @XmlElement 
    @XmlElementWrapper(name = "doctors") 
    public List<Doctor> getDoctors() { 
	return this.drs;
    } 
    public void setDoctors(List<Doctor> drs) { 
	this.drs = drs;
    }

    @Override
    public String toString() {
	String s = "";
	for (Doctor d : drs) s += d.toString();
	return s;
	}

	public String doctorString(){
	String s = "";
	for (Doctor d : drs) s += d.doctorsOnlyList();
	return s;
	}
	
	public int getSize(){
		return this.size;
	}

    public Doctor find(int id) {
	Doctor dr = null;
	// Search the list -- for now, the list is short enough that
	// a linear search is ok but binary search would be better if the
	// list got to be an order-of-magnitude larger in size.
	for (Doctor d : drs) {
	    if (d.getId() == id) {
		dr = d;
		break;
	    }
	}	
	return dr;
    }
    public int add(String who, PatientList pslist) {
	int id = drId.incrementAndGet();
	Doctor d = new Doctor();
	d.setWho(who);
	d.setId(id);
	d.setPatients(pslist);
	drs.add(d);
	size++;
	return id;
    }
}
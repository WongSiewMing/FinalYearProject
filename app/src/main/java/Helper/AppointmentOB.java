package Helper;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import java.io.Serializable;

public class AppointmentOB implements Serializable {
    private String appointmentID;
    private Student studentID;
    private AvailableTimeOB availableID;
    private Stuff stuffID;
    private String opponentID;
    private String appointmentStatus;
    private String appointmentDate;
    private Student stuffOwnerID;

    public AppointmentOB(String appointmentID, Student studentID, AvailableTimeOB availableID, Stuff stuffID, String opponentID, String appointmentStatus, String appointmentDate) {
        this.appointmentID = appointmentID;
        this.studentID = studentID;
        this.availableID = availableID;
        this.stuffID = stuffID;
        this.opponentID = opponentID;
        this.appointmentStatus = appointmentStatus;
        this.appointmentDate = appointmentDate;
    }

    public AppointmentOB(String appointmentID, AvailableTimeOB availableID, Stuff stuffID, String opponentID, String appointmentStatus, String appointmentDate, Student stuffOwnerID) {
        this.appointmentID = appointmentID;
        this.availableID = availableID;
        this.stuffID = stuffID;
        this.opponentID = opponentID;
        this.appointmentStatus = appointmentStatus;
        this.appointmentDate = appointmentDate;
        this.stuffOwnerID = stuffOwnerID;
    }

    public String getAppointmentID() {
        return appointmentID;
    }

    public Student getStudentID() {
        return studentID;
    }

    public AvailableTimeOB getAvailableID() {
        return availableID;
    }

    public Stuff getStuffID() {
        return stuffID;
    }

    public String getOpponentID() {
        return opponentID;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public Student getStuffOwnerID() {
        return stuffOwnerID;
    }
}

package Helper;

import java.io.Serializable;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class Student implements Serializable {
    private String studentID;
    private String clientID;
    private String photo;
    private String studentName;
    private String icNo;
    private String studentProgramme;
    private String studentFaculty;
    private int yearOfStudy;

    public Student(String studentID, String photo, String studentName, String icNo,
                   String studentProgramme, String studentFaculty, int yearOfStudy) {
        this.studentID = studentID;
        this.photo = photo;
        this.studentName = studentName;
        this.icNo = icNo;
        this.studentProgramme = studentProgramme;
        this.studentFaculty = studentFaculty;
        this.yearOfStudy = yearOfStudy;
    }

    public Student(String studentID, String clientID, String photo, String studentName, String icNo,
                   String studentProgramme, String studentFaculty, int yearOfStudy){
        this.studentID = studentID;
        this.clientID = clientID;
        this.photo = photo;
        this.studentName = studentName;
        this.icNo = icNo;
        this.studentProgramme = studentProgramme;
        this.studentFaculty = studentFaculty;
        this.yearOfStudy = yearOfStudy;
    }

    public Student(String studentID, String photo, String studentName, String studentProgramme) {
        this.studentID = studentID;
        this.photo = photo;
        this.studentName = studentName;
        this.studentProgramme = studentProgramme;
    }

    public Student(String studentID, String photo, String studentName, String studentProgramme, String studentFaculty, int yearOfStudy) {
        this.studentID = studentID;
        this.photo = photo;
        this.studentName = studentName;
        this.studentProgramme = studentProgramme;
        this.studentFaculty = studentFaculty;
        this.yearOfStudy = yearOfStudy;
    }

    public Student(String studentID) {
        this.studentID = studentID;
    }

    public String getStudentID() {
        return studentID;
    }

    public String getPhoto() {
        return photo;
    }

    public String getClientID(){
        return clientID;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getIcNo() {
        return icNo;
    }

    public String getStudentProgramme() {
        return studentProgramme;
    }

    public String getStudentFaculty() {
        return studentFaculty;
    }

    public int getYearOfStudy() {
        return yearOfStudy;
    }
}

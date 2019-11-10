package Helper;

import java.io.Serializable;

/*Author : Wong Siew Ming
Programme : RSD3
Year : 2019*/

public class SearchHistoryOB implements Serializable {
    private String SearchHistoryID;
    private Student StudentID;
    private String date;
    private String time;
    private String SearchKeyword;
    private String status;

    public SearchHistoryOB(String searchHistoryID, Student studentID, String date, String time, String searchKeyword, String status) {
        this.SearchHistoryID = searchHistoryID;
        this.StudentID = studentID;
        this.date = date;
        this.time = time;
        this.SearchKeyword = searchKeyword;
        this.status = status;
    }

    public String getSearchHistoryID() {
        return SearchHistoryID;
    }

    public Student getStudentID() {
        return StudentID;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getSearchKeyword() {
        return SearchKeyword;
    }

    public String getStatus() {
        return status;
    }
}

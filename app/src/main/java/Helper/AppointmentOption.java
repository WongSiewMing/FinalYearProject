package Helper;

import java.io.Serializable;

public class AppointmentOption implements Serializable {

    private int appointmentOption;
    private String appointmentOptionName;

    public AppointmentOption (int appointmentOption, String appointmentOptionName) {
        this.appointmentOption = appointmentOption;
        this.appointmentOptionName = appointmentOptionName;
    }

    public int getAppointmentOption() {
        return appointmentOption;
    }

    public String getAppointmentOptionName() {
        return appointmentOptionName;
    }

}

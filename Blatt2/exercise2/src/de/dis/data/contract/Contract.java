package de.dis.data.contract;

import java.sql.Connection;
import java.sql.Date;

public abstract class Contract {
    protected int contractNo = -1;
    protected Date date;
    protected String place;
    protected int personId;
    protected int estateId;

    public int getContractNo() {
        return contractNo;
    }

    public void setContractNo(int contractNo) {
        this.contractNo = contractNo;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public int getEstateId() {
        return estateId;
    }

    public void setEstateId(int estateId) {
        this.estateId = estateId;
    }

    public abstract void save();

}

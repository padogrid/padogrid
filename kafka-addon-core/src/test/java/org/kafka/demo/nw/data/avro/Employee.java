package org.kafka.demo.nw.data.avro;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.kafka.demo.nw.data.avro.generated.__Employee;

/**
 * A {@linkplain __Employee} wrapper class.
 * 
 * @author dpark
 *
 */
@Table(name = "employee")
public class Employee {
	private org.kafka.demo.nw.data.avro.generated.__Employee avro;

	@CreationTimestamp
	private LocalDateTime createdOn = LocalDateTime.now();

	@UpdateTimestamp
	private LocalDateTime updatedOn = LocalDateTime.now();

	public Employee() {
		avro = new org.kafka.demo.nw.data.avro.generated.__Employee();
		setCreatedOn(createdOn);
		setUpdatedOn(updatedOn);
	}

	public Employee(org.kafka.demo.nw.data.avro.generated.__Employee avro) {
		setAvro(avro);
	}

	public void setAvro(org.kafka.demo.nw.data.avro.generated.__Employee avro) {
		this.avro = avro;
	}

	@Transient
	public org.kafka.demo.nw.data.avro.generated.__Employee getAvro() {
		return avro;
	}

	@CreationTimestamp
	public void setCreatedOn(LocalDateTime date) {
		if (date == null) {
			avro.setCreatedOnMillis(0L);
		} else {
			avro.setCreatedOnMillis(date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		}
	}

	@CreationTimestamp
	public LocalDateTime getCreatedOn() {
		long epoch = avro.getCreatedOnMillis();
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault());
	}

	@CreationTimestamp
	public void setUpdatedOn(LocalDateTime date) {
		if (date == null) {
			avro.setUpdatedOnMillis(0L);
		} else {
			avro.setUpdatedOnMillis(date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		}
	}

	@CreationTimestamp
	public LocalDateTime getUpdatedOn() {
		long epoch = avro.getUpdatedOnMillis();
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault());
	}

	@Id
	@Column(length = 20)
	public String getEmployeeId() {
		return avro.getEmployeeId().toString();
	}

	public void setEmployeeId(String employeeId) {
		avro.setEmployeeId(employeeId);
	}

	@Column(length = 50)
	public String getLastName() {
		return avro.getLastName().toString();
	}

	public void setLastName(String lastName) {
		avro.setLastName(lastName);
	}

	@Column(length = 50)
	public String getFirstName() {
		return avro.getFirstName().toString();
	}

	public void setFirstName(String firstName) {
		avro.setFirstName(firstName);
	}

	@Column(length = 50)
	public String getTitle() {
		return avro.getTitle().toString();
	}

	public void setTitle(String title) {
		avro.setTitle(title);
	}

	@Column(length = 50)
	public String getTitleOfCourtesy() {
		return avro.getTitleOfCourtesy().toString();
	}

	public void setTitleOfCourtesy(String titleOfCourtesy) {
		avro.setTitleOfCourtesy(titleOfCourtesy);
	}

	public Date getBirthDate() {
		return new Date(avro.getBirthDateMillis());
	}

	public void setBirthDate(Date date) {
		if (date == null) {
			avro.setBirthDateMillis(0L);
		} else {
			avro.setBirthDateMillis(date.getTime());
		}
	}

	public Date getHireDate() {
		return new Date(avro.getHireDateMillis());
	}

	public void setHireDate(Date date) {
		if (date == null) {
			avro.setHireDateMillis(0L);
		} else {
			avro.setHireDateMillis(date.getTime());
		}
	}

	@Column(length = 100)
	public String getAddress() {
		return avro.getAddress().toString();
	}

	public void setAddress(String address) {
		avro.setAddress(address);
	}

	@Column(length = 50)
	public String getCity() {
		return avro.getCity().toString();
	}

	public void setCity(String city) {
		avro.setCity(city);
	}

	@Column(length = 10)
	public String getRegion() {
		return avro.getRegion().toString();
	}

	public void setRegion(String region) {
		avro.setRegion(region);
	}

	@Column(length = 10)
	public String getPostalCode() {
		return avro.getPostalCode().toString();
	}

	public void setPostalCode(String postalCode) {
		avro.setPostalCode(postalCode);
	}

	@Column(length = 100)
	public String getCountry() {
		return avro.getCountry().toString();
	}

	public void setCountry(String country) {
		avro.setCountry(country);
	}

	@Column(length = 30)
	public String getHomePhone() {
		return avro.getHomePhone().toString();
	}

	public void setHomePhone(String homePhone) {
		avro.setHomePhone(homePhone);
	}

	@Column(length = 10)
	public String getExtension() {
		return avro.getExtension().toString();
	}

	public void setExtension(String extension) {
		avro.setExtension(extension);
	}

	@Column(length = 500)
	public String getPhoto() {
		return avro.getPhoto().toString();
	}

	public void setPhoto(String photo) {
		avro.setPhoto(photo);
	}

	@Column(length = 500)
	public String getNotes() {
		return avro.getNotes().toString();
	}

	public void setNotes(String notes) {
		avro.setNotes(notes);
	}

	@Column(length = 50)
	public String getReportsTo() {
		return avro.getReportsTo().toString();
	}

	public void setReportsTo(String reportsTo) {
		avro.setReportsTo(reportsTo);
	}

	@Column(length = 200)
	public String getPhotoPath() {
		return avro.getPhotoPath().toString();
	}

	public void setPhotoPath(String photoPath) {
		avro.setPhotoPath(photoPath);
	}

	@Override
	public String toString() {
		return "Employee [getCreatedOn()=" + getCreatedOn() + ", getUpdatedOn()=" + getUpdatedOn()
				+ ", getEmployeeId()=" + getEmployeeId() + ", getLastName()=" + getLastName() + ", getFirstName()="
				+ getFirstName() + ", getTitle()=" + getTitle() + ", getTitleOfCourtesy()=" + getTitleOfCourtesy()
				+ ", getBirthDate()=" + getBirthDate() + ", getHireDateMillis()=" + getHireDate()
				+ ", getAddress()=" + getAddress() + ", getCity()=" + getCity() + ", getRegion()=" + getRegion()
				+ ", getPostalCode()=" + getPostalCode() + ", getCountry()=" + getCountry() + ", getHomePhone()="
				+ getHomePhone() + ", getExtension()=" + getExtension() + ", getPhoto()=" + getPhoto() + ", getNotes()="
				+ getNotes() + ", getReportsTo()=" + getReportsTo() + ", getPhotoPath()=" + getPhotoPath() + "]";
	}
}

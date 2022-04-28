package org.apache.geode.addon.demo.nw.data;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

@Entity
@Table(name = "employees")
public class Employee extends BaseEntity implements PdxSerializable
{
	@Id
	private String employeeId;
	@Column(length = 100)
	private String lastName;
	@Column(length = 100)
	private String firstName;
	@Column(length = 100)
	private String title;
	@Column(length = 100)
	private String titleOfCourtesy;
	@Column
	private Date birthDate;
	@Column
	private Date hireDate;
	@Column(length = 100)
	private String address;
	@Column(length = 100)
	private String city;
	@Column(length = 100)
	private String region;
	@Column(length = 30)
	private String postalCode;
	@Column(length = 100)
	private String country;
	@Column(length = 30)
	private String homePhone;
	@Column(length = 30)
	private String extension;
	@Column(length = 1000)
	private String photo;
	@Column(length = 100)
	private String notes;
	@Column(length = 100)
	private String reportsTo;
	@Column(length = 100)
	private String photoPath;

	public Employee()
	{
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId=employeeId;
	}

	public String getEmployeeId() {
		return this.employeeId;
	}

	public void setLastName(String lastName) {
		this.lastName=lastName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setFirstName(String firstName) {
		this.firstName=firstName;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setTitle(String title) {
		this.title=title;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitleOfCourtesy(String titleOfCourtesy) {
		this.titleOfCourtesy=titleOfCourtesy;
	}

	public String getTitleOfCourtesy() {
		return this.titleOfCourtesy;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate=birthDate;
	}

	public Date getBirthDate() {
		return this.birthDate;
	}

	public void setHireDate(Date hireDate) {
		this.hireDate=hireDate;
	}

	public Date getHireDate() {
		return this.hireDate;
	}

	public void setAddress(String address) {
		this.address=address;
	}

	public String getAddress() {
		return this.address;
	}

	public void setCity(String city) {
		this.city=city;
	}

	public String getCity() {
		return this.city;
	}

	public void setRegion(String region) {
		this.region=region;
	}

	public String getRegion() {
		return this.region;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode=postalCode;
	}

	public String getPostalCode() {
		return this.postalCode;
	}

	public void setCountry(String country) {
		this.country=country;
	}

	public String getCountry() {
		return this.country;
	}

	public void setHomePhone(String homePhone) {
		this.homePhone=homePhone;
	}

	public String getHomePhone() {
		return this.homePhone;
	}

	public void setExtension(String extension) {
		this.extension=extension;
	}

	public String getExtension() {
		return this.extension;
	}

	public void setPhoto(String photo) {
		this.photo=photo;
	}

	public String getPhoto() {
		return this.photo;
	}

	public void setNotes(String notes) {
		this.notes=notes;
	}

	public String getNotes() {
		return this.notes;
	}

	public void setReportsTo(String reportsTo) {
		this.reportsTo=reportsTo;
	}

	public String getReportsTo() {
		return this.reportsTo;
	}

	public void setPhotoPath(String photoPath) {
		this.photoPath=photoPath;
	}

	public String getPhotoPath() {
		return this.photoPath;
	}
	
	@Override
	public String toString()
	{
		return "[address=" + this.address
			 + ", birthDate=" + this.birthDate
			 + ", city=" + this.city
			 + ", country=" + this.country
			 + ", employeeId=" + this.employeeId
			 + ", extension=" + this.extension
			 + ", firstName=" + this.firstName
			 + ", hireDate=" + this.hireDate
			 + ", homePhone=" + this.homePhone
			 + ", lastName=" + this.lastName
			 + ", notes=" + this.notes
			 + ", photo=" + this.photo
			 + ", photoPath=" + this.photoPath
			 + ", postalCode=" + this.postalCode
			 + ", region=" + this.region
			 + ", reportsTo=" + this.reportsTo
			 + ", title=" + this.title
			 + ", titleOfCourtesy=" + this.titleOfCourtesy + "]";
	}

	@Override
	public void toData(PdxWriter writer) {
		super.toData(writer);
		writer.writeString("employeeId", employeeId);
		writer.writeString("lastName", lastName);
		writer.writeString("firstName", firstName);
		writer.writeString("title", title);
		writer.writeString("titleOfCourtesy", titleOfCourtesy);
		writer.writeDate("birthDate", birthDate);
		writer.writeDate("hireDate", hireDate);
		writer.writeString("address", address);
		writer.writeString("city", city);
		writer.writeString("region", region);
		writer.writeString("postalCode", postalCode);
		writer.writeString("country", country);
		writer.writeString("homePhone", homePhone);
		writer.writeString("extension", extension);
		writer.writeString("photo", photo);
		writer.writeString("notes", notes);
		writer.writeString("reportsTo", reportsTo);
		writer.writeString("photoPath", photoPath);
	}

	@Override
	public void fromData(PdxReader reader) {
		super.fromData(reader);
		this.employeeId = reader.readString("employeeId");
		this.lastName = reader.readString("lastName");
		this.firstName = reader.readString("firstName");
		this.title = reader.readString("title");
		this.titleOfCourtesy = reader.readString("titleOfCourtesy");
		this.birthDate = reader.readDate("birthDate");
		this.hireDate = reader.readDate("hireDate");
		this.address = reader.readString("address");
		this.city = reader.readString("city");
		this.region = reader.readString("region");
		this.postalCode = reader.readString("postalCode");
		this.country = reader.readString("country");
		this.homePhone = reader.readString("homePhone");
		this.extension = reader.readString("extension");
		this.photo = reader.readString("photo");
		this.notes = reader.readString("notes");
		this.reportsTo = reader.readString("reportsTo");
		this.photoPath = reader.readString("photoPath");
	}
}

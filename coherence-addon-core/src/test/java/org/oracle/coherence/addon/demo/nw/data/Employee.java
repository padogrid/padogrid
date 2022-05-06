package org.oracle.coherence.addon.demo.nw.data;

import java.io.IOException;
import java.util.Date;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class Employee extends BaseEntity implements PortableObject
{
	private String employeeId;
	private String lastName;
	private String firstName;
	private String title;
	private String titleOfCourtesy;
	private Date birthDate;
	private Date hireDate;
	private String address;
	private String city;
	private String region;
	private String postalCode;
	private String country;
	private String homePhone;
	private String extension;
	private String photo;
	private String notes;
	private String reportsTo;
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
	public void readExternal(PofReader reader) throws IOException {
		int i = super.readExternal(0, reader);
		this.employeeId = reader.readString(i++);
		this.lastName = reader.readString(i++);
		this.firstName = reader.readString(i++);
		this.title = reader.readString(i++);
		this.titleOfCourtesy = reader.readString(i++);
		this.birthDate = reader.readDate(i++);
		this.hireDate = reader.readDate(i++);
		this.address = reader.readString(i++);
		this.city = reader.readString(i++);
		this.region = reader.readString(i++);
		this.postalCode = reader.readString(i++);
		this.country = reader.readString(i++);
		this.homePhone = reader.readString(i++);
		this.extension = reader.readString(i++);
		this.photo = reader.readString(i++);
		this.notes = reader.readString(i++);
		this.reportsTo = reader.readString(i++);
		this.photoPath = reader.readString(i++);
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		int i = super.writeExternal(0, writer);
		writer.writeString(i++, employeeId);
		writer.writeString(i++, lastName);
		writer.writeString(i++, firstName);
		writer.writeString(i++, title);
		writer.writeString(i++, titleOfCourtesy);
		writer.writeDate(i++, birthDate);
		writer.writeDate(i++, hireDate);
		writer.writeString(i++, address);
		writer.writeString(i++, city);
		writer.writeString(i++, region);
		writer.writeString(i++, postalCode);
		writer.writeString(i++, country);
		writer.writeString(i++, homePhone);
		writer.writeString(i++, extension);
		writer.writeString(i++, photo);
		writer.writeString(i++, notes);
		writer.writeString(i++, reportsTo);
		writer.writeString(i++, photoPath);
	}
}

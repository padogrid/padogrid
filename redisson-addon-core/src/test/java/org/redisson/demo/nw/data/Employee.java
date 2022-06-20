package org.redisson.demo.nw.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

/**
  * Employee is generated code. To modify this class, you must follow the
  * guidelines below.
  * <ul>
  * <li>Always add new fields and do NOT delete old fields.</li>
  * <li>If new fields have been added, then make sure to increment the version number.</li>
  * </ul>
  *
  * @generator com.netcrest.pado.tools.hazelcast.VersionedPortableClassGenerator
  * @schema employees.schema
  * @date Fri May 17 20:50:06 EDT 2019
**/
public class Employee extends BaseEntity implements Externalizable
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
	
	/**
	 * Writes the state of this object to the given <code>ObjectOutput</code>.
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {

		super.writeExternal(out);
		out.writeUTF(employeeId);
		out.writeUTF(lastName);
		out.writeUTF(firstName);
		out.writeUTF(title);
		out.writeUTF(titleOfCourtesy);
		if (this.birthDate == null) {
			out.writeLong(-1L);
		} else {
			out.writeLong(this.birthDate.getTime());
		}
		if (this.hireDate == null) {
			out.writeLong(-1L);
		} else {
			out.writeLong(this.hireDate.getTime());
		}
		out.writeUTF(address);
		out.writeUTF(city);
		out.writeUTF(region);
		out.writeUTF(postalCode);
		out.writeUTF(country);
		out.writeUTF(homePhone);
		out.writeUTF(extension);
		out.writeUTF(photo);
		out.writeUTF(notes);
		out.writeUTF(reportsTo);
		out.writeUTF(photoPath);
	
	}

	/**
	 * Reads the state of this object from the given <code>ObjectInput</code>.
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

		super.readExternal(in);
		this.employeeId = in.readUTF();
		this.lastName = in.readUTF();
		this.firstName = in.readUTF();
		this.title = in.readUTF();
		this.titleOfCourtesy = in.readUTF();
		long l = in.readLong();
		if (l != -1L) {
			this.birthDate = new Date(l);
		}
		l = in.readLong();
		if (l != -1L) {
			this.hireDate = new Date(l);
		}
		this.address = in.readUTF();
		this.city = in.readUTF();
		this.region = in.readUTF();
		this.postalCode = in.readUTF();
		this.country = in.readUTF();
		this.homePhone = in.readUTF();
		this.extension = in.readUTF();
		this.photo = in.readUTF();
		this.notes = in.readUTF();
		this.reportsTo = in.readUTF();
		this.photoPath = in.readUTF();
	
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
}

package org.hazelcast.demo.nw.data;

import java.io.IOException;
import java.util.Date;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;

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
public class Employee extends BaseEntity implements VersionedPortable
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
	public int getClassId() 
	{
		return PortableFactoryImpl.Employee_CLASS_ID;
	}

	@Override
	public int getFactoryId() {
		return PortableFactoryImpl.FACTORY_ID;
	}
	
	@Override
	public int getClassVersion() {
		return 1;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		super.writePortable(writer);
		writer.writeUTF("employeeId", employeeId);
		writer.writeUTF("lastName", lastName);
		writer.writeUTF("firstName", firstName);
		writer.writeUTF("title", title);
		writer.writeUTF("titleOfCourtesy", titleOfCourtesy);
		if (this.birthDate == null) {
			writer.writeLong("birthDate", -1L);
		} else {
			writer.writeLong("birthDate", this.birthDate.getTime());
		}
		if (this.hireDate == null) {
			writer.writeLong("hireDate", -1L);
		} else {
			writer.writeLong("hireDate", this.hireDate.getTime());
		}
		writer.writeUTF("address", address);
		writer.writeUTF("city", city);
		writer.writeUTF("region", region);
		writer.writeUTF("postalCode", postalCode);
		writer.writeUTF("country", country);
		writer.writeUTF("homePhone", homePhone);
		writer.writeUTF("extension", extension);
		writer.writeUTF("photo", photo);
		writer.writeUTF("notes", notes);
		writer.writeUTF("reportsTo", reportsTo);
		writer.writeUTF("photoPath", photoPath);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		super.readPortable(reader);
		this.employeeId = reader.readUTF("employeeId");
		this.lastName = reader.readUTF("lastName");
		this.firstName = reader.readUTF("firstName");
		this.title = reader.readUTF("title");
		this.titleOfCourtesy = reader.readUTF("titleOfCourtesy");
		long l = reader.readLong("birthDate");
		if (l != -1L) {
			this.birthDate = new Date(l);
		}
		l = reader.readLong("hireDate");
		if (l != -1L) {
			this.hireDate = new Date(l);
		}
		this.address = reader.readUTF("address");
		this.city = reader.readUTF("city");
		this.region = reader.readUTF("region");
		this.postalCode = reader.readUTF("postalCode");
		this.country = reader.readUTF("country");
		this.homePhone = reader.readUTF("homePhone");
		this.extension = reader.readUTF("extension");
		this.photo = reader.readUTF("photo");
		this.notes = reader.readUTF("notes");
		this.reportsTo = reader.readUTF("reportsTo");
		this.photoPath = reader.readUTF("photoPath");
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

package org.hazelcast.demo.nw.data;

import java.io.IOException;


import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;

/**
  * Supplier is generated code. To modify this class, you must follow the
  * guidelines below.
  * <ul>
  * <li>Always add new fields and do NOT delete old fields.</li>
  * <li>If new fields have been added, then make sure to increment the version number.</li>
  * </ul>
  *
  * @generator com.netcrest.pado.tools.hazelcast.VersionedPortableClassGenerator
  * @schema suppliers.schema
  * @date Fri May 17 20:50:06 EDT 2019
**/
public class Supplier implements VersionedPortable
{
	private String supplierId;
	private String companyName;
	private String contactName;
	private String contactTitle;
	private String address;
	private String city;
	private String region;
	private String postalCode;
	private String country;
	private String phone;
	private String fax;
	private String homePage;

	public Supplier()
	{
	}

	public void setSupplierId(String supplierId) {
		this.supplierId=supplierId;
	}

	public String getSupplierId() {
		return this.supplierId;
	}

	public void setCompanyName(String companyName) {
		this.companyName=companyName;
	}

	public String getCompanyName() {
		return this.companyName;
	}

	public void setContactName(String contactName) {
		this.contactName=contactName;
	}

	public String getContactName() {
		return this.contactName;
	}

	public void setContactTitle(String contactTitle) {
		this.contactTitle=contactTitle;
	}

	public String getContactTitle() {
		return this.contactTitle;
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

	public void setPhone(String phone) {
		this.phone=phone;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setFax(String fax) {
		this.fax=fax;
	}

	public String getFax() {
		return this.fax;
	}

	public void setHomePage(String homePage) {
		this.homePage=homePage;
	}

	public String getHomePage() {
		return this.homePage;
	}


	@Override
	public int getClassId() 
	{
		return PortableFactoryImpl.Supplier_CLASS_ID;
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
		writer.writeUTF("supplierId", supplierId);
		writer.writeUTF("companyName", companyName);
		writer.writeUTF("contactName", contactName);
		writer.writeUTF("contactTitle", contactTitle);
		writer.writeUTF("address", address);
		writer.writeUTF("city", city);
		writer.writeUTF("region", region);
		writer.writeUTF("postalCode", postalCode);
		writer.writeUTF("country", country);
		writer.writeUTF("phone", phone);
		writer.writeUTF("fax", fax);
		writer.writeUTF("homePage", homePage);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.supplierId = reader.readUTF("supplierId");
		this.companyName = reader.readUTF("companyName");
		this.contactName = reader.readUTF("contactName");
		this.contactTitle = reader.readUTF("contactTitle");
		this.address = reader.readUTF("address");
		this.city = reader.readUTF("city");
		this.region = reader.readUTF("region");
		this.postalCode = reader.readUTF("postalCode");
		this.country = reader.readUTF("country");
		this.phone = reader.readUTF("phone");
		this.fax = reader.readUTF("fax");
		this.homePage = reader.readUTF("homePage");
	}
    
	@Override
	public String toString()
	{
		return "[address=" + this.address
			 + ", city=" + this.city
			 + ", companyName=" + this.companyName
			 + ", contactName=" + this.contactName
			 + ", contactTitle=" + this.contactTitle
			 + ", country=" + this.country
			 + ", fax=" + this.fax
			 + ", homePage=" + this.homePage
			 + ", phone=" + this.phone
			 + ", postalCode=" + this.postalCode
			 + ", region=" + this.region
			 + ", supplierId=" + this.supplierId + "]";
	}
}

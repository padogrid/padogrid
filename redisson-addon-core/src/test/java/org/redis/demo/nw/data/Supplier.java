package org.redis.demo.nw.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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
public class Supplier extends BaseEntity implements Externalizable
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
    
	/**
	 * Writes the state of this object to the given <code>ObjectOutput</code>.
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(supplierId);
		out.writeUTF(companyName);
		out.writeUTF(contactName);
		out.writeUTF(contactTitle);
		out.writeUTF(address);
		out.writeUTF(city);
		out.writeUTF(region);
		out.writeUTF(postalCode);
		out.writeUTF(country);
		out.writeUTF(phone);
		out.writeUTF(fax);
		out.writeUTF(homePage);
	}

	/**
	 * Reads the state of this object from the given <code>ObjectInput</code>.
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		this.supplierId = in.readUTF();
		this.companyName = in.readUTF();
		this.contactName = in.readUTF();
		this.contactTitle = in.readUTF();
		this.address = in.readUTF();
		this.city = in.readUTF();
		this.region = in.readUTF();
		this.postalCode = in.readUTF();
		this.country = in.readUTF();
		this.phone = in.readUTF();
		this.fax = in.readUTF();
		this.homePage = in.readUTF();
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

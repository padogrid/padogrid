package org.redis.demo.nw.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
  * Customer is generated code. To modify this class, you must follow the
  * guidelines below.
  * <ul>
  * <li>Always add new fields and do NOT delete old fields.</li>
  * <li>If new fields have been added, then make sure to increment the version number.</li>
  * </ul>
  *
  * @generator com.netcrest.pado.tools.hazelcast.VersionedPortableClassGenerator
  * @schema customers.schema
  * @date Fri May 17 20:50:06 EDT 2019
**/
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity implements Externalizable
{
	@Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(length = 20)
	private String customerId;
	@Column(length = 50)
	private String companyName;
	@Column(length = 50)
	private String contactName;
	@Column(length = 50)
	private String contactTitle;
	@Column(length = 100)
	private String address;
	@Column(length = 50)
	private String city;
	@Column(length = 10)
	private String region;
	@Column(length = 10)
	private String postalCode;
	@Column(length = 100)
	private String country;
	@Column(length = 30)
	private String phone;
	@Column(length = 30)
	private String fax;

	public Customer()
	{
	}

	public void setCustomerId(String customerId) {
		this.customerId=customerId;
	}

	public String getCustomerId() {
		return this.customerId;
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

	/**
	 * Writes the state of this object to the given <code>ObjectOutput</code>.
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(customerId);
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
	}

	/**
	 * Reads the state of this object from the given <code>ObjectInput</code>.
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		this.customerId = in.readUTF();
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
			 + ", customerId=" + this.customerId
			 + ", fax=" + this.fax
			 + ", phone=" + this.phone
			 + ", postalCode=" + this.postalCode
			 + ", region=" + this.region + "]";
	}
}

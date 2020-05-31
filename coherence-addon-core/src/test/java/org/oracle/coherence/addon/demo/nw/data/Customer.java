package org.oracle.coherence.addon.demo.nw.data;

import java.io.IOException;

import javax.persistence.Column;
import javax.persistence.Table;

import com.tangosol.internal.sleepycat.persist.model.Entity;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import nonapi.io.github.classgraph.json.Id;

@Entity
@Table(name = "customers")
public class Customer implements PortableObject
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
	
	@Override
	public void readExternal(PofReader reader) throws IOException {
		int i = 0;
		this.customerId = reader.readString(i++);
		this.companyName = reader.readString(i++);
		this.contactName = reader.readString(i++);
		this.contactTitle = reader.readString(i++);
		this.address = reader.readString(i++);
		this.city = reader.readString(i++);
		this.region = reader.readString(i++);
		this.postalCode = reader.readString(i++);
		this.country = reader.readString(i++);
		this.phone = reader.readString(i++);
		this.fax = reader.readString(i++);
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		int i = 0;
		writer.writeString(i++, customerId);
		writer.writeString(i++, companyName);
		writer.writeString(i++, contactName);
		writer.writeString(i++, contactTitle);
		writer.writeString(i++, address);
		writer.writeString(i++, city);
		writer.writeString(i++, region);
		writer.writeString(i++, postalCode);
		writer.writeString(i++, country);
		writer.writeString(i++, phone);
		writer.writeString(i++, fax);
	}
}

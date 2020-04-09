package org.apache.geode.addon.demo.nw.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

@Entity
@Table(name = "customers")
public class Customer implements PdxSerializable
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
	public void toData(PdxWriter writer) {
		writer.writeString("customerId", customerId);
		writer.writeString("companyName", companyName);
		writer.writeString("contactName", contactName);
		writer.writeString("contactTitle", contactTitle);
		writer.writeString("address", address);
		writer.writeString("city", city);
		writer.writeString("region", region);
		writer.writeString("postalCode", postalCode);
		writer.writeString("country", country);
		writer.writeString("phone", phone);
		writer.writeString("fax", fax);
	}

	@Override
	public void fromData(PdxReader reader) {
		this.customerId = reader.readString("customerId");
		this.companyName = reader.readString("companyName");
		this.contactName = reader.readString("contactName");
		this.contactTitle = reader.readString("contactTitle");
		this.address = reader.readString("address");
		this.city = reader.readString("city");
		this.region = reader.readString("region");
		this.postalCode = reader.readString("postalCode");
		this.country = reader.readString("country");
		this.phone = reader.readString("phone");
		this.fax = reader.readString("fax");
	}
}

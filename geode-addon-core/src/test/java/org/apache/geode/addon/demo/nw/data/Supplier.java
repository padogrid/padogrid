package org.apache.geode.addon.demo.nw.data;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

public class Supplier implements PdxSerializable
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

	@Override
	public void toData(PdxWriter writer) {
		writer.writeString("supplierId", supplierId);
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
		writer.writeString("homePage", homePage);
	}

	@Override
	public void fromData(PdxReader reader) {
		this.supplierId = reader.readString("supplierId");
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
		this.homePage = reader.readString("homePage");
	}
}

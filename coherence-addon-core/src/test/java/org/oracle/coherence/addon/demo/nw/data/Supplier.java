package org.oracle.coherence.addon.demo.nw.data;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class Supplier extends BaseEntity implements PortableObject {
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

	public Supplier() {
	}

	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}

	public String getSupplierId() {
		return this.supplierId;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCompanyName() {
		return this.companyName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactName() {
		return this.contactName;
	}

	public void setContactTitle(String contactTitle) {
		this.contactTitle = contactTitle;
	}

	public String getContactTitle() {
		return this.contactTitle;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return this.address;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCity() {
		return this.city;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getRegion() {
		return this.region;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getPostalCode() {
		return this.postalCode;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountry() {
		return this.country;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getFax() {
		return this.fax;
	}

	public void setHomePage(String homePage) {
		this.homePage = homePage;
	}

	public String getHomePage() {
		return this.homePage;
	}

	@Override
	public String toString() {
		return "[address=" + this.address + ", city=" + this.city + ", companyName=" + this.companyName
				+ ", contactName=" + this.contactName + ", contactTitle=" + this.contactTitle + ", country="
				+ this.country + ", fax=" + this.fax + ", homePage=" + this.homePage + ", phone=" + this.phone
				+ ", postalCode=" + this.postalCode + ", region=" + this.region + ", supplierId=" + this.supplierId
				+ "]";
	}

	@Override
	public void readExternal(PofReader reader) throws IOException {
		int i = super.readExternal(0, reader);
		this.supplierId = reader.readString(i++);
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
		this.homePage = reader.readString(i++);
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		int i = super.writeExternal(0, writer);
		writer.writeString(i++, supplierId);
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
		writer.writeString(i++, homePage);
	}
}

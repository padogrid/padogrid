package org.kafka.demo.nw.data.avro;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.kafka.demo.nw.data.avro.generated.__Supplier;

/**
 * A {@linkplain __Supplier} wrapper class.
 * 
 * @author dpark
 *
 */
@Entity
@Table(name = "suppliers")
public class Supplier extends org.kafka.demo.nw.data.avro.generated.__Supplier {
	private org.kafka.demo.nw.data.avro.generated.__Supplier avro;

	@CreationTimestamp
	private LocalDateTime createdOn = LocalDateTime.now();

	@UpdateTimestamp
	private LocalDateTime updatedOn = LocalDateTime.now();

	public Supplier() {
		avro = new org.kafka.demo.nw.data.avro.generated.__Supplier();
		setCreatedOn(createdOn);
		setUpdatedOn(updatedOn);
	}

	public Supplier(org.kafka.demo.nw.data.avro.generated.__Supplier avro) {
		setAvro(avro);
	}

	public void setAvro(org.kafka.demo.nw.data.avro.generated.__Supplier avro) {
		this.avro = avro;
	}

	@Transient
	public org.kafka.demo.nw.data.avro.generated.__Supplier getAvro() {
		return avro;
	}

	@CreationTimestamp
	public void setCreatedOn(LocalDateTime date) {
		if (date == null) {
			avro.setCreatedOnMillis(0L);
		} else {
			avro.setCreatedOnMillis(date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		}
	}

	@CreationTimestamp
	public LocalDateTime getCreatedOn() {
		long epoch = avro.getCreatedOnMillis();
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault());
	}

	@CreationTimestamp
	public void setUpdatedOn(LocalDateTime date) {
		if (date == null) {
			avro.setUpdatedOnMillis(0L);
		} else {
			avro.setUpdatedOnMillis(date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		}
	}

	@CreationTimestamp
	public LocalDateTime getUpdatedOn() {
		long epoch = avro.getUpdatedOnMillis();
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault());
	}
	
	@Id
	@Column(length = 20)
	public String getSupplierId() {
		return avro.getSupplierId().toString();
	}

	public void setSupplierId(String supplierId) {
		avro.setSupplierId(supplierId);
	}

	@Column(length = 50)
	public String getCompanyName() {
		return avro.getCompanyName().toString();
	}

	public void setCompanyName(String companyName) {
		avro.setCompanyName(companyName);
	}

	@Column(length = 50)
	public String getContactName() {
		return avro.getContactName().toString();
	}

	public void setContactName(String contactName) {
		avro.setContactName(contactName);
	}

	@Column(length = 50)
	public String getContactTitle() {
		return avro.getContactTitle().toString();
	}

	public void setContactTitle(String contactTitle) {
		avro.setContactTitle(contactTitle);
	}

	@Column(length = 100)
	public String getAddress() {
		return avro.getAddress().toString();
	}

	public void setAddress(String address) {
		avro.setAddress(address);
	}

	@Column(length = 50)
	public String getCity() {
		return avro.getCity().toString();
	}

	public void setCity(String city) {
		avro.setCity(city);
	}

	@Column(length = 10)
	public String getRegion() {
		return avro.getRegion().toString();
	}

	public void setRegion(String region) {
		avro.setRegion(region);
	}

	@Column(length = 10)
	public String getPostalCode() {
		return avro.getPostalCode().toString();
	}

	public void setPostalCode(String postalCode) {
		avro.setPostalCode(postalCode);
	}

	@Column(length = 100)
	public String getCountry() {
		return avro.getCountry().toString();
	}

	public void setCountry(String country) {
		avro.setCountry(country);
	}

	@Column(length = 30)
	public String getPhone() {
		return avro.getPhone().toString();
	}

	public void setPhone(String phone) {
		avro.setPhone(phone);
	}

	@Column(length = 30)
	public String getFax() {
		return avro.getFax().toString();
	}

	public void setFax(String fax) {
		avro.setFax(fax);
	}

	@Column(length = 200)
	public String getHomePage() {
		return avro.getHomePage().toString();
	}

	public void setHomePage(String homePage) {
		avro.setHomePage(homePage);
	}

	@Override
	public String toString() {
		return "Supplier [getCreatedOn()=" + getCreatedOn() + ", getUpdatedOn()=" + getUpdatedOn()
				+ ", getSupplierId()=" + getSupplierId() + ", getCompanyName()=" + getCompanyName()
				+ ", getContactName()=" + getContactName() + ", getContactTitle()=" + getContactTitle()
				+ ", getAddress()=" + getAddress() + ", getCity()=" + getCity() + ", getRegion()=" + getRegion()
				+ ", getPostalCode()=" + getPostalCode() + ", getCountry()=" + getCountry() + ", getPhone()="
				+ getPhone() + ", getFax()=" + getFax() + ", getHomePage()=" + getHomePage() + "]";
	} 
}

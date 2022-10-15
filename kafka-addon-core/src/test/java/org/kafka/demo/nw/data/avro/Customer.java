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
import org.kafka.demo.nw.data.avro.generated.__Customer;


/**
 * A {@linkplain __Customer} wrapper class.
 * @author dpark
 *
 */
@Entity
@Table(name = "customers")
public class Customer {

	private org.kafka.demo.nw.data.avro.generated.__Customer avro;
	
	@CreationTimestamp
	private LocalDateTime createdOn = LocalDateTime.now();

	@UpdateTimestamp
	private LocalDateTime updatedOn = LocalDateTime.now();

	public Customer() {
		avro = new org.kafka.demo.nw.data.avro.generated.__Customer();
		setCreatedOn(createdOn);
		setUpdatedOn(updatedOn);
	}

	public Customer(org.kafka.demo.nw.data.avro.generated.__Customer avro) {
		setAvro(avro);
	}

	public void setAvro(org.kafka.demo.nw.data.avro.generated.__Customer avro) {
		this.avro = avro;
	}
	
	@Transient
	public org.kafka.demo.nw.data.avro.generated.__Customer getAvro() {
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
	
	public void setCustomerId(String customerId) {
		avro.setCustomerId(customerId);
	}

	public void setCompanyName(String companyName) {
		avro.setCompanyName(companyName);
	}

	public void setContactName(String contactName) {
		avro.setContactName(contactName);
	}

	public void setContactTitle(String contactTitle) {
		avro.setContactTitle(contactTitle);
	}

	public void setAddress(String address) {
		avro.setAddress(address);
	}

	public void setCity(String city) {
		avro.setCity(city);
	}

	public void setRegion(String region) {
		avro.setRegion(region);
	}

	public void setPostalCode(String postalCode) {
		avro.setPostalCode(postalCode);
	}

	public void setCountry(String country) {
		avro.setCountry(country);
	}

	public void setPhone(String phone) {
		avro.setPhone(phone);
	}

	public void setFax(String fax) {
		avro.setFax(fax);
	}

	@Id
	@Column(length = 20)
	public String getCustomerId() {
		return avro.getCustomerId().toString();
	}

	@Column(length = 50)
	public String getCompanyName() {
		return avro.getCompanyName().toString();
	}

	@Column(length = 50)
	public String getContactName() {
		return avro.getContactName().toString();
	}

	@Column(length = 50)
	public String getContactTitle() {
		return avro.getContactTitle().toString();
	}

	@Column(length = 100)
	public String getAddress() {
		return avro.getAddress().toString();
	}

	@Column(length = 50)
	public String getCity() {
		return avro.getCity().toString();
	}

	@Column(length = 10)
	public String getRegion() {
		return avro.getRegion().toString();
	}

	@Column(length = 10)
	public String getPostalCode() {
		return avro.getPostalCode().toString();
	}

	@Column(length = 100)
	public String getCountry() {
		return avro.getCountry().toString();
	}

	@Column(length = 30)
	public String getPhone() {
		return avro.getPhone().toString();
	}

	@Column(length = 30)
	public String getFax() {
		return avro.getFax().toString();
	}

	@Override
	public String toString() {
		return "Customer [getCreatedOn()=" + getCreatedOn() + ", getUpdatedOn()=" + getUpdatedOn()
				+ ", getCustomerId()=" + getCustomerId() + ", getCompanyName()=" + getCompanyName()
				+ ", getContactName()=" + getContactName() + ", getContactTitle()=" + getContactTitle()
				+ ", getAddress()=" + getAddress() + ", getCity()=" + getCity() + ", getRegion()=" + getRegion()
				+ ", getPostalCode()=" + getPostalCode() + ", getCountry()=" + getCountry() + ", getPhone()="
				+ getPhone() + ", getFax()=" + getFax() + "]";
	}
}

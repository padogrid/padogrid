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
import org.kafka.demo.nw.data.avro.generated.__Shipper;

/**
 * A {@linkplain __Shipper} wrapper class.
 * 
 * @author dpark
 *
 */
@Entity
@Table(name = "shippers")
public class Shipper {
	private org.kafka.demo.nw.data.avro.generated.__Shipper avro;

	@CreationTimestamp
	private LocalDateTime createdOn = LocalDateTime.now();

	@UpdateTimestamp
	private LocalDateTime updatedOn = LocalDateTime.now();

	public Shipper() {
		avro = new org.kafka.demo.nw.data.avro.generated.__Shipper();
		setCreatedOn(createdOn);
		setUpdatedOn(updatedOn);
	}

	public Shipper(org.kafka.demo.nw.data.avro.generated.__Shipper avro) {
		setAvro(avro);
	}

	public void setAvro(org.kafka.demo.nw.data.avro.generated.__Shipper avro) {
		this.avro = avro;
	}

	@Transient
	public org.kafka.demo.nw.data.avro.generated.__Shipper getAvro() {
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
	public String getShipperId() {
		return avro.getShipperId().toString();
	}

	@Column(length = 20)
	public void setShipperId(String shipperId) {
		avro.setShipperId(shipperId);
	}

	@Column(length = 50)
	public String getCompanyName() {
		return avro.getCompanyName().toString();
	}

	public void setCompanyName(String companyName) {
		avro.setCompanyName(companyName);
	}

	@Column(length = 30)
	public String getPhone() {
		return avro.getPhone().toString();
	}

	public void setPhone(String phone) {
		avro.setPhone(phone);
	}

	@Override
	public String toString() {
		return "Shipper [getCreatedOn()=" + getCreatedOn() + ", getUpdatedOn()=" + getUpdatedOn() + ", getShipperId()="
				+ getShipperId() + ", getCompanyName()=" + getCompanyName() + ", getPhone()=" + getPhone() + "]";
	}
}

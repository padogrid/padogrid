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
import org.kafka.demo.nw.data.avro.generated.__Region;

/**
 * A {@linkplain __Region} wrapper class.
 * 
 * @author dpark
 *
 */
@Entity
@Table(name = "regions")
public class Region {
	private org.kafka.demo.nw.data.avro.generated.__Region avro;

	@CreationTimestamp
	private LocalDateTime createdOn = LocalDateTime.now();

	@UpdateTimestamp
	private LocalDateTime updatedOn = LocalDateTime.now();

	public Region() {
		avro = new org.kafka.demo.nw.data.avro.generated.__Region();
		setCreatedOn(createdOn);
		setUpdatedOn(updatedOn);
	}

	public Region(org.kafka.demo.nw.data.avro.generated.__Region avro) {
		setAvro(avro);
	}

	public void setAvro(org.kafka.demo.nw.data.avro.generated.__Region avro) {
		this.avro = avro;
	}

	@Transient
	public org.kafka.demo.nw.data.avro.generated.__Region getAvro() {
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
	public String getRegionId() {
		return avro.getRegionId().toString();
	}

	public void setRegionId(String regionId) {
		avro.setRegionId(regionId);
	}

	@Column(length = 200)
	public String getRegionDescription() {
		return avro.getRegionDescription().toString();
	}

	public void setRegionDescription(String regionDescription) {
		avro.setRegionDescription(regionDescription);
	}

	@Override
	public String toString() {
		return "Region [getCreatedOn()=" + getCreatedOn() + ", getUpdatedOn()=" + getUpdatedOn() + ", getRegionId()="
				+ getRegionId() + ", getRegionDescription()=" + getRegionDescription() + "]";
	}
}

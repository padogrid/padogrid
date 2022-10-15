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
import org.kafka.demo.nw.data.avro.generated.__Territory;

/**
 * A {@linkplain __Territory} wrapper class.
 * 
 * @author dpark
 *
 */
@Entity
@Table(name = "territories")
public class Territory {
	private org.kafka.demo.nw.data.avro.generated.__Territory avro;

	@CreationTimestamp
	private LocalDateTime createdOn = LocalDateTime.now();

	@UpdateTimestamp
	private LocalDateTime updatedOn = LocalDateTime.now();

	public Territory() {
		avro = new org.kafka.demo.nw.data.avro.generated.__Territory();
		setCreatedOn(createdOn);
		setUpdatedOn(updatedOn);
	}

	public Territory(org.kafka.demo.nw.data.avro.generated.__Territory avro) {
		setAvro(avro);
	}

	public void setAvro(org.kafka.demo.nw.data.avro.generated.__Territory avro) {
		this.avro = avro;
	}

	@Transient
	public org.kafka.demo.nw.data.avro.generated.__Territory getAvro() {
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
	public String getTerritoryId() {
		return avro.getTerritoryId().toString();
	}

	public void setTerritoryId(String territoryId) {
		avro.setTerritoryId(territoryId);
	}

	@Column(length = 200)
	public String getTerritoryDescription() {
		return avro.getTerritoryDescription().toString();
	}

	public void setTerritoryDescription(String territoryDescription) {
		avro.setTerritoryDescription(territoryDescription);
	}

	@Column(length = 20)
	public String getRegionId() {
		return avro.getRegionId().toString();
	}

	public void setRegionId(String regionId) {
		avro.setRegionId(regionId);
	}

	@Override
	public String toString() {
		return "Territory [getCreatedOn()=" + getCreatedOn() + ", getUpdatedOn()=" + getUpdatedOn()
				+ ", getTerritoryId()=" + getTerritoryId() + ", getTerritoryDescription()=" + getTerritoryDescription()
				+ ", getRegionId()=" + getRegionId() + "]";
	}
}

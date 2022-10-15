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
import org.kafka.demo.nw.data.avro.generated.__EmployeeTerritory;

/**
 * A {@linkplain __EmployeeTerritory} wrapper class.
 * @author dpark
 *
 */
@Entity
@Table(name = "employee_territories")
public class EmployeeTerritory {
private org.kafka.demo.nw.data.avro.generated.__EmployeeTerritory avro;
	
	@CreationTimestamp
	private LocalDateTime createdOn = LocalDateTime.now();

	@UpdateTimestamp
	private LocalDateTime updatedOn = LocalDateTime.now();

	public EmployeeTerritory() {
		avro = new org.kafka.demo.nw.data.avro.generated.__EmployeeTerritory();
		setCreatedOn(createdOn);
		setUpdatedOn(updatedOn);
	}

	public EmployeeTerritory(org.kafka.demo.nw.data.avro.generated.__EmployeeTerritory avro) {
		setAvro(avro);
	}

	public void setAvro(org.kafka.demo.nw.data.avro.generated.__EmployeeTerritory avro) {
		this.avro = avro;
	}
	
	@Transient
	public org.kafka.demo.nw.data.avro.generated.__EmployeeTerritory getAvro() {
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
	public String getEmployeeId() {
		return avro.getEmployeeId().toString();
	}

	public void setEmployeeId(String employeeId) {
		avro.setEmployeeId(employeeId);
	}

	@Column(length = 20)
	public String getTerritoryId() {
		return avro.getTerritoryId().toString();
	}

	public void setTerritoryId(String territoryId) {
		avro.setTerritoryId(territoryId);
	}

	@Override
	public String toString() {
		return "EmployeeTerritory [getCreatedOn()=" + getCreatedOn() + ", getUpdatedOn()=" + getUpdatedOn()
				+ ", getEmployeeId()=" + getEmployeeId() + ", getTerritoryId()=" + getTerritoryId() + "]";
	}
}

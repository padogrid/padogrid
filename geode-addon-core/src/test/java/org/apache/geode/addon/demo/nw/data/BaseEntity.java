package org.apache.geode.addon.demo.nw.data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@MappedSuperclass
public class BaseEntity implements PdxSerializable {
	@Column(name = "created_on", columnDefinition = "DATETIME(3)")
	@CreationTimestamp
	private LocalDateTime createdOn = LocalDateTime.now();

	@Column(name = "updated_on", columnDefinition = "DATETIME(3)")
	@UpdateTimestamp
	private LocalDateTime updatedOn = LocalDateTime.now();

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public LocalDateTime getUpdatedOn() {
		return updatedOn;
	}

	@Override
	public void toData(PdxWriter writer) {
		writer.writeLong("createdOn", createdOn.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		writer.writeLong("updatedOn", updatedOn.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		
	}

	@Override
	public void fromData(PdxReader reader) {
		long epoch = reader.readLong("createdOn");
		createdOn = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault());
		epoch = reader.readLong("updatedOn");
		updatedOn = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault());
	}
}

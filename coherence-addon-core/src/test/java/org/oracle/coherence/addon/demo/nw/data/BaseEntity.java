package org.oracle.coherence.addon.demo.nw.data;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;

@MappedSuperclass
public class BaseEntity {
	@Column(name = "createdOn")
	@CreationTimestamp
	private LocalDateTime createdOn = LocalDateTime.now();

	@Column(name = "updatedOn")
	@UpdateTimestamp
	private LocalDateTime updatedOn = LocalDateTime.now();

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public LocalDateTime getUpdatedOn() {
		return updatedOn;
	}
	
	protected int readExternal(int index, PofReader reader) throws IOException {
		long epoch = reader.readLong(index++);
		createdOn = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault());
		epoch = reader.readLong(index++);	
		updatedOn = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault());
		return index;
	}

	protected int writeExternal(int index, PofWriter writer) throws IOException {
		writer.writeLong(index++, createdOn.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		writer.writeLong(index++, updatedOn.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		return index;
	}
}

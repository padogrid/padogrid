package org.hazelcast.demo.nw.data;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;

@MappedSuperclass
public abstract class BaseEntity implements VersionedPortable {
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

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeLong("createdOn", createdOn.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		writer.writeLong("updatedOn", updatedOn.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		long epoch = reader.readLong("createdOn");
		createdOn = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault());
		epoch = reader.readLong("updatedOn");
		updatedOn = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault());
	}
}

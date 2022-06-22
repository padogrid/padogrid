package org.redis.demo.nw.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@MappedSuperclass
public abstract class BaseEntity implements Externalizable {
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
	
	/**
	 * Writes the state of this object to the given <code>ObjectOutput</code>.
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(createdOn.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		out.writeLong(updatedOn.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
	}

	/**
	 * Reads the state of this object from the given <code>ObjectInput</code>.
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		long epoch = in.readLong();
		createdOn = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault());
		epoch = in.readLong();
		updatedOn = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault());
	}
}

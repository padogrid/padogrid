package org.apache.geode.addon.demo.nw.data;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@MappedSuperclass
public class BaseEntity implements PdxSerializable {
	@Column(name = "created_on", columnDefinition = "DATETIME(3)")
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	private Date createdOn = new Date();

	@Column(name = "updated_on", columnDefinition = "DATETIME(3)")
	@Temporal(TemporalType.TIMESTAMP)
	@UpdateTimestamp
	private Date updatedOn = new Date();

	public Date getCreatedOn() {
		return createdOn;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	@Override
	public void toData(PdxWriter writer) {
		writer.writeLong("createdOn", createdOn.getTime());
		writer.writeLong("updatedOn", updatedOn.getTime());
		
	}

	@Override
	public void fromData(PdxReader reader) {
		long epoch = reader.readLong("createdOn");
		createdOn = new Date(epoch);
		epoch = reader.readLong("updatedOn");
		updatedOn = new Date(epoch);
	}
}

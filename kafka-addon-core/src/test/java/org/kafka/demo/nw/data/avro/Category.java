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
import org.kafka.demo.nw.data.avro.generated.__Category;

/**
 * A {@linkplain __Category} wrapper class.
 * 
 * @author dpark
 *
 */
@Entity
@Table(name = "category")
public class Category {
	private org.kafka.demo.nw.data.avro.generated.__Category avro;

	@CreationTimestamp
	private LocalDateTime createdOn = LocalDateTime.now();

	@UpdateTimestamp
	private LocalDateTime updatedOn = LocalDateTime.now();

	public Category() {
		avro = new org.kafka.demo.nw.data.avro.generated.__Category();
		setCreatedOn(createdOn);
		setUpdatedOn(updatedOn);
	}

	public Category(org.kafka.demo.nw.data.avro.generated.__Category avro) {
		setAvro(avro);
	}

	public void setAvro(org.kafka.demo.nw.data.avro.generated.__Category avro) {
		this.avro = avro;
	}

	@Transient
	public org.kafka.demo.nw.data.avro.generated.__Category getAvro() {
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
	public String getCategoryId() {
		return avro.getCategoryId().toString();
	}

	public void setCategoryId(String categoryId) {
		avro.setCategoryId(categoryId);
	}

	@Column(length = 50)
	public String getCategoryName() {
		return avro.getCategoryName().toString();
	}

	public void setCategoryName(String categoryName) {
		avro.setCategoryName(categoryName);
	}

	@Column(length = 200)
	public String getDescription() {
		return avro.getDescription().toString();
	}

	public void setDescription(String description) {
		avro.setDescription(description);
	}

	@Column(length = 50)
	public String getTag() {
		return avro.getTag().toString();
	}

	public void setTag(String tag) {
		avro.setTag(tag);
	}

	@Column(length = 500)
	public String getPicture() {
		return avro.getPicture().toString();
	}

	public void setPicture(String picture) {
		avro.setPicture(picture);
	}

	@Override
	public String toString() {
		return "Category [getCreatedOn()=" + getCreatedOn() + ", getUpdatedOn()=" + getUpdatedOn()
				+ ", getCategoryId()=" + getCategoryId() + ", getCategoryName()=" + getCategoryName()
				+ ", getDescription()=" + getDescription() + ", getTag()=" + getTag() + ", getPicture()=" + getPicture()
				+ "]";
	}
}

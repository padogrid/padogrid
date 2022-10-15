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
import org.kafka.demo.nw.data.avro.generated.__Product;

/**
 * A {@linkplain __Product} wrapper class.
 * 
 * @author dpark
 *
 */
@Entity
@Table(name = "products")
public class Product {
	private org.kafka.demo.nw.data.avro.generated.__Product avro;

	@CreationTimestamp
	private LocalDateTime createdOn = LocalDateTime.now();

	@UpdateTimestamp
	private LocalDateTime updatedOn = LocalDateTime.now();

	public Product() {
		avro = new org.kafka.demo.nw.data.avro.generated.__Product();
		setCreatedOn(createdOn);
		setUpdatedOn(updatedOn);
	}

	public Product(org.kafka.demo.nw.data.avro.generated.__Product avro) {
		setAvro(avro);
	}

	public void setAvro(org.kafka.demo.nw.data.avro.generated.__Product avro) {
		this.avro = avro;
	}

	@Transient
	public org.kafka.demo.nw.data.avro.generated.__Product getAvro() {
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
	public String getProductId() {
		return avro.getProductId().toString();
	}

	@Column(length = 50)
	public String getProductName() {
		return avro.getProductName().toString();
	}

	@Column(length = 20)
	public String getSupplierId() {
		return avro.getSupplierId().toString();
	}

	public String getCategoryId() {
		return avro.getCategoryId().toString();
	}

	@Column(length = 20)
	public String getQuantityPerUnit() {
		return avro.getQuantityPerUnit().toString();
	}

	public double getUnitPrice() {
		return avro.getUnitPrice();
	}

	public int getUnitsInStock() {
		return avro.getUnitsInStock();
	}

	public int getUnitsOnOrder() {
		return avro.getUnitsOnOrder();
	}

	public int getReorderLevel() {
		return avro.getReorderLevel();
	}

	public int getDiscontinued() {
		return avro.getDiscontinued();
	}

	public void setProductId(String productId) {
		avro.setProductId(productId);
	}

	public void setProductName(String productName) {
		avro.setProductName(productName);
	}

	public void setSupplierId(String supplierId) {
		avro.setSupplierId(supplierId);
	}

	public void setCategoryId(String categoryId) {
		avro.setCategoryId(categoryId);
	}

	public void setQuantityPerUnit(String quantityPerUnit) {
		avro.setQuantityPerUnit(quantityPerUnit);
	}

	public void setUnitPrice(double unitPrice) {
		avro.setUnitPrice(unitPrice);
	}

	public void setUnitsInStock(int unitsInStock) {
		avro.setUnitsInStock(unitsInStock);
	}

	public void setUnitsOnOrder(int unitsOnOrder) {
		avro.setUnitsOnOrder(unitsOnOrder);
	}

	public void setReorderLevel(int reorderLevel) {
		avro.setReorderLevel(reorderLevel);
	}

	public void setDiscontinued(int discontinued) {
		avro.setDiscontinued(discontinued);
	}

	@Override
	public String toString() {
		return "Product [getCreatedOn()=" + getCreatedOn() + ", getUpdatedOn()=" + getUpdatedOn() + ", getProductId()="
				+ getProductId() + ", getProductName()=" + getProductName() + ", getSupplierId()=" + getSupplierId()
				+ ", getCategoryId()=" + getCategoryId() + ", getQuantityPerUnit()=" + getQuantityPerUnit()
				+ ", getUnitPrice()=" + getUnitPrice() + ", getUnitsInStock()=" + getUnitsInStock()
				+ ", getUnitsOnOrder()=" + getUnitsOnOrder() + ", getReorderLevel()=" + getReorderLevel()
				+ ", getDiscontinued()=" + getDiscontinued() + "]";
	}
}

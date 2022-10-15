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
import org.kafka.demo.nw.data.avro.generated.__OrderDetail;

/**
 * A {@linkplain __OrderDetail} wrapper class.
 * 
 * @author dpark
 *
 */
@Entity
@Table(name = "order_details")
public class OrderDetail {
	private org.kafka.demo.nw.data.avro.generated.__OrderDetail avro;

	@CreationTimestamp
	private LocalDateTime createdOn = LocalDateTime.now();

	@UpdateTimestamp
	private LocalDateTime updatedOn = LocalDateTime.now();

	public OrderDetail() {
		avro = new org.kafka.demo.nw.data.avro.generated.__OrderDetail();
		setCreatedOn(createdOn);
		setUpdatedOn(updatedOn);
	}

	public OrderDetail(org.kafka.demo.nw.data.avro.generated.__OrderDetail avro) {
		setAvro(avro);
	}

	public void setAvro(org.kafka.demo.nw.data.avro.generated.__OrderDetail avro) {
		this.avro = avro;
	}

	@Transient
	public org.kafka.demo.nw.data.avro.generated.__OrderDetail getAvro() {
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
	public String getOrderId() {
		return avro.getOrderId().toString();
	}

	@Column(length = 20)
	public String getProductId() {
		return avro.getProductId().toString();
	}

	public double getUnitPrice() {
		return avro.getUnitPrice();
	}

	public int getQuantity() {
		return avro.getQuantity();
	}

	public double getDiscount() {
		return avro.getDiscount();
	}

	public void setOrderId(String orderId) {
		avro.setOrderId(orderId);
	}

	public void setProductId(String productId) {
		avro.setProductId(productId);
	}

	public void setUnitPrice(double unitPrice) {
		avro.setUnitPrice(unitPrice);
	}

	public void setQuantity(int quantity) {
		avro.setQuantity(quantity);
	}

	public void setDiscount(double discount) {
		avro.setDiscount(discount);
	}

	@Override
	public String toString() {
		return "OrderDetail [getCreatedOn()=" + getCreatedOn() + ", getUpdatedOn()=" + getUpdatedOn()
				+ ", getOrderId()=" + getOrderId() + ", getProductId()=" + getProductId() + ", getUnitPrice()="
				+ getUnitPrice() + ", getQuantity()=" + getQuantity() + ", getDiscount()=" + getDiscount() + "]";
	}
}

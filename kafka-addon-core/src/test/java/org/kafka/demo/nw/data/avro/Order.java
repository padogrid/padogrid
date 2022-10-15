package org.kafka.demo.nw.data.avro;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.kafka.demo.nw.data.avro.generated.__Order;

/**
 * A {@linkplain __Order} wrapper class.
 * @author dpark
 *
 */
@Entity
@Table(name = "orders")
public class Order {
	
	private org.kafka.demo.nw.data.avro.generated.__Order avro;
	
	@CreationTimestamp
	private LocalDateTime createdOn = LocalDateTime.now();

	@UpdateTimestamp
	private LocalDateTime updatedOn = LocalDateTime.now();
	
	public Order() {
		avro = new org.kafka.demo.nw.data.avro.generated.__Order();
		setCreatedOn(createdOn);
		setUpdatedOn(updatedOn);
	}
	
	public Order(org.kafka.demo.nw.data.avro.generated.__Order avro) {
		setAvro(avro);
	}

	public void setAvro(org.kafka.demo.nw.data.avro.generated.__Order avro) {
		this.avro = avro;
	}
	
	@Transient
	public org.kafka.demo.nw.data.avro.generated.__Order getAvro() {
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
	
	public void setOrderId(String orderId) {
		avro.setOrderId(orderId);
	}

	public void setCustomerId(String customerId) {
		avro.setCustomerId(customerId);
	}

	public void setEmployeeId(String employeeId) {
		avro.setEmployeeId(employeeId);
	}

	public void setShipVia(String shipVia) {
		avro.setShipVia(shipVia);
	}

	public void setFreight(double freight) {
		avro.setFreight(freight);
	}

	public void setShipName(String shipName) {
		avro.setShipName(shipName);
	}

	public void setShipAddress(String shipAddress) {
		avro.setShipAddress(shipAddress);
	}

	public void setShipCity(String shipCity) {
		avro.setShipCity(shipCity);
	}

	public void setShipRegion(String shipRegion) {
		avro.setShipRegion(shipRegion);
	}

	public void setShipPostalCode(String shipPostalCode) {
		avro.setShipPostalCode(shipPostalCode);
	}

	public void setShipCountry(String shipCountry) {
		avro.setShipCountry(shipCountry);
	}

	public void setOrderDate(Date date) {
		if (date == null) {
			avro.setOrderDateMillis(0L);
		} else {
			avro.setOrderDateMillis(date.getTime());
		}
	}

	@Id
	@Column(length = 20)
	public String getOrderId() {
		return avro.getOrderId().toString();
	}

	@Column(length = 20)
	public String getCustomerId() {
		return avro.getCustomerId().toString();
	}

	@Column(length = 20)
	public String getEmployeeId() {
		return avro.getEmployeeId().toString();
	}

	@Column(length = 50)
	public String getShipVia() {
		return avro.getShipVia().toString();
	}

	public double getFreight() {
		return avro.getFreight();
	}

	@Column(length = 100)
	public String getShipName() {
		return avro.getShipName().toString();
	}

	@Column(length = 100)
	public String getShipAddress() {
		return avro.getShipAddress().toString();
	}

	@Column(length = 50)
	public String getShipCity() {
		return avro.getShipCity().toString();
	}

	@Column(length = 10)
	public String getShipRegion() {
		return avro.getShipRegion().toString();
	}

	@Column(length = 10)
	public String getShipPostalCode() {
		return avro.getShipPostalCode().toString();
	}

	@Column(length = 100)
	public String getShipCountry() {
		return avro.getShipCountry().toString().toString();
	}

	public Date getOrderDate() {
		return new Date(avro.getOrderDateMillis());
	}

	public void setShippedDate(Date date) {
		if (date == null) {
			avro.setShippedDateMillis(0L);
		} else {
			avro.setShippedDateMillis(date.getTime());
		}
	}

	public Date getShippedDate() {
		return new Date(avro.getShippedDateMillis());
	}

	public void setRequiredDate(Date date) {
		if (date == null) {
			avro.setRequiredDateMillis(0L);
		} else {
			avro.setRequiredDateMillis(date.getTime());
		}
	}

	public Date getRequiredDate() {
		return new Date(avro.getRequiredDateMillis());
	}

	@Override
	public String toString() {
		return "Order [getCreatedOn()=" + getCreatedOn() + ", getUpdatedOn()=" + getUpdatedOn() + ", getOrderId()="
				+ getOrderId() + ", getCustomerId()=" + getCustomerId() + ", getEmployeeId()=" + getEmployeeId()
				+ ", getShipVia()=" + getShipVia() + ", getFreight()=" + getFreight() + ", getShipName()="
				+ getShipName() + ", getShipAddress()=" + getShipAddress() + ", getShipCity()=" + getShipCity()
				+ ", getShipRegion()=" + getShipRegion() + ", getShipPostalCode()=" + getShipPostalCode()
				+ ", getShipCountry()=" + getShipCountry() + ", getOrderDate()=" + getOrderDate()
				+ ", getShippedDate()=" + getShippedDate() + ", getRequiredDate()=" + getRequiredDate() + "]";
	}
}

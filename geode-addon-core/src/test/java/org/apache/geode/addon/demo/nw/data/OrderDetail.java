package org.apache.geode.addon.demo.nw.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

@Entity
@Table(name = "order_details")
public class OrderDetail extends BaseEntity implements PdxSerializable
{
	@Id
	private String orderId;
	@Column(length = 30)
	private String productId;
	@Column
	private double unitPrice;
	@Column
	private int quantity;
	@Column
	private double discount;

	public OrderDetail()
	{
	}

	public void setOrderId(String orderId) {
		this.orderId=orderId;
	}

	public String getOrderId() {
		return this.orderId;
	}

	public void setProductId(String productId) {
		this.productId=productId;
	}

	public String getProductId() {
		return this.productId;
	}

	public void setUnitPrice(double unitPrice) {
		this.unitPrice=unitPrice;
	}

	public double getUnitPrice() {
		return this.unitPrice;
	}

	public void setQuantity(int quantity) {
		this.quantity=quantity;
	}

	public int getQuantity() {
		return this.quantity;
	}

	public void setDiscount(double discount) {
		this.discount=discount;
	}

	public double getDiscount() {
		return this.discount;
	}

	@Override
	public String toString()
	{
		return "[discount=" + this.discount
			 + ", orderId=" + this.orderId
			 + ", productId=" + this.productId
			 + ", quantity=" + this.quantity
			 + ", unitPrice=" + this.unitPrice + "]";
	}

	@Override
	public void toData(PdxWriter writer) {
		super.toData(writer);
		writer.writeString("orderId", orderId).markIdentityField("orderId");
		writer.writeString("productId", productId);
		writer.writeDouble("unitPrice", unitPrice);
		writer.writeInt("quantity", quantity);
		writer.writeDouble("discount", discount);
	}

	@Override
	public void fromData(PdxReader reader) {
		super.fromData(reader);
		this.orderId = reader.readString("orderId");
		this.productId = reader.readString("productId");
		this.unitPrice = reader.readDouble("unitPrice");
		this.quantity = reader.readInt("quantity");
		this.discount = reader.readDouble("discount");
	}
}

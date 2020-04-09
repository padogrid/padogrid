package org.apache.geode.addon.demo.nw.data;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

public class OrderDetail implements PdxSerializable
{
	private String orderId;
	private String productId;
	private double unitPrice;
	private int quantity;
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
		writer.writeString("orderId", orderId);
		writer.writeString("productId", productId);
		writer.writeDouble("unitPrice", unitPrice);
		writer.writeInt("quantity", quantity);
		writer.writeDouble("discount", discount);
	}

	@Override
	public void fromData(PdxReader reader) {
		this.orderId = reader.readString("orderId");
		this.productId = reader.readString("productId");
		this.unitPrice = reader.readDouble("unitPrice");
		this.quantity = reader.readInt("quantity");
		this.discount = reader.readDouble("discount");
	}
}

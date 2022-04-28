package org.oracle.coherence.addon.demo.nw.data;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class OrderDetail extends BaseEntity implements PortableObject
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
	public void readExternal(PofReader reader) throws IOException {
		int i = super.readExternal(0, reader);
		this.orderId = reader.readString(i++);
		this.productId = reader.readString(i++);
		this.unitPrice = reader.readDouble(i++);
		this.quantity = reader.readInt(i++);
		this.discount = reader.readDouble(i++);
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		int i = super.writeExternal(0, writer);
		writer.writeString(i++, orderId);
		writer.writeString(i++, productId);
		writer.writeDouble(i++, unitPrice);
		writer.writeInt(i++, quantity);
		writer.writeDouble(i++, discount);
	}
}

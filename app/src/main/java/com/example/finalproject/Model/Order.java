package com.example.finalproject.Model;


import android.os.Parcel;
import android.os.Parcelable;

public class Order implements Parcelable {

    private String quantity;
    private String price;
    private Long date;
    private Varian size;
    private Varian mattboard;
    private Varian linen;
    private Varian glass;
    private String id;
    private double weight;
    private int width;
    private int height;
    private int poTime;

    private Product product;

    public Order() {
    }

    public Order(String id,Product product, String quantity, String price, Long date, Varian size, Varian mattboard, Varian linen, Varian glass, double weight, int width, int height,int poTime) {
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.size = size;
        this.mattboard = mattboard;
        this.linen = linen;
        this.glass = glass;
        this.date = date;
        this.id = id;
        this.weight = weight;
        this.height = height;
        this.width = width;
        this.poTime = poTime;

    }

    protected Order(Parcel in) {
        quantity = in.readString();
        price = in.readString();
        if (in.readByte() == 0) {
            date = null;
        } else {
            date = in.readLong();
        }
        size = in.readParcelable(Varian.class.getClassLoader());
        mattboard = in.readParcelable(Varian.class.getClassLoader());
        linen = in.readParcelable(Varian.class.getClassLoader());
        glass = in.readParcelable(Varian.class.getClassLoader());
        id = in.readString();
        weight = in.readDouble();
        width = in.readInt();
        height = in.readInt();
        poTime = in.readInt();
        product = in.readParcelable(Product.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(quantity);
        dest.writeString(price);
        if (date == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(date);
        }
        dest.writeParcelable(size, flags);
        dest.writeParcelable(mattboard, flags);
        dest.writeParcelable(linen, flags);
        dest.writeParcelable(glass, flags);
        dest.writeString(id);
        dest.writeDouble(weight);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeInt(poTime);
        dest.writeParcelable(product, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }


    public String getPrice() {
        return price;
    }

    public String getQuantity() {
        return quantity;
    }


    public void setPrice(String price) {
        this.price = price;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public Varian getGlass() {
        return glass;
    }

    public void setGlass(Varian glass) {
        this.glass = glass;
    }

    public Varian getLinen() {
        return linen;
    }

    public void setLinen(Varian linen) {
        this.linen = linen;
    }

    public Varian getSize() {
        return size;
    }

    public void setSize(Varian size) {
        this.size = size;
    }

    public Varian getMattboard() {
        return mattboard;
    }

    public void setMattboard(Varian mattboard) {
        this.mattboard = mattboard;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}

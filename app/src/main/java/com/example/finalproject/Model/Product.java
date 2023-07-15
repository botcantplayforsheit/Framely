package com.example.finalproject.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.PropertyName;

public class Product implements Parcelable {
    private String name, image, price, stock,productId, desc;
    private int poTime;
    public Product(){

    }

    public Product(String id,String name, String image, String price, int poTime, String desc) {
        productId=id;
        this.name = name;
        this.image = image;
        this.price = price;
        this.poTime = poTime;
        this.desc = desc;

    }


    protected Product(Parcel in) {
        name = in.readString();
        image = in.readString();
        price = in.readString();
        stock = in.readString();
        productId = in.readString();
        desc = in.readString();
        poTime = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(image);
        dest.writeString(price);
        dest.writeString(stock);
        dest.writeString(productId);
        dest.writeString(desc);
        dest.writeInt(poTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public String getProductId() {
        return productId;
    }


    public String getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }


    public String getName() {
        return name;
    }

    public String getStock() {
        return stock;
    }

    public int getPoTime() {
        return poTime;
    }

    public void setPoTime(int poTime) {
        this.poTime = poTime;
    }

    public String getDesc() {
        if(desc == null){
            return "";
        }
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}



package com.testapp.android.Model;

import java.io.Serializable;

/**
 * Created by Administrator on 18/07/2017.
 */

public class Product implements Serializable {
    private String id;
    private String name;
    private String price;

    public Product() {
    }

    public Product(String id, String name, String price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", price=" + price +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Product))
            return false;

        Product p = (Product) obj;
        return p.getId().equals(((Product) obj).getId()) && p.getName().equals(((Product) obj).getName());
    }
}

package com.hulylabs.updater.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "products")
public class Products {
    @JacksonXmlProperty(localName = "product")
    public List<Product> products;

    public static Products defaultProducts() {
        Products products = new Products();
        Product product = new Product();
        product.channel = new ProductChannel();
        product.channel.builds = new ArrayList<>();
        products.products = List.of(product);
        return products;
    }
}
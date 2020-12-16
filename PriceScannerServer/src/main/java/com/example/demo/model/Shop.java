package com.example.demo.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;

// TODO jak nadpiszę lombokiem toString() to przy metodzie findEnableProductCodes() wywala sie program
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "shop")
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    private String name;

    private String urlToSearchProduct;

    @OneToMany(mappedBy = "shop")
    private List<Code> productCodes;

    @OneToMany(mappedBy = "shop")
    private List<ErrorCode> errorCodes;

    @OneToMany(mappedBy = "shop")
    private List<AvailableCode> availableCodes;

    @OneToMany(mappedBy = "shop")
    private List<Product> products;

}

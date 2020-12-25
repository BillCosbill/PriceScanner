package com.example.demo.model;

import com.example.demo.model.enums.ECodeStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "code")
public class Code {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long codeValue;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private CodeStatus codeStatus;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;
}

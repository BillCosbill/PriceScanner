package com.example.demo.model;

import com.example.demo.model.enums.CodeStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "x_kom_code")
public class Code {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long code;

    private CodeStatus codeStatus;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;
}

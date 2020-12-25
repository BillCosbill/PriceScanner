package com.example.demo.model;

import com.example.demo.model.enums.ECodeStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
public class CodeStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ECodeStatus status;

    @JsonIgnore
    @OneToMany(mappedBy = "codeStatus")
    List<Code> codes;

    public CodeStatus(ECodeStatus status) {
        this.status = status;
    }
}
